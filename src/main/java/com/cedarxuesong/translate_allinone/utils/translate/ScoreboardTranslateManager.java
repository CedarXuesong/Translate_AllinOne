package com.cedarxuesong.translate_allinone.utils.translate;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.cache.ScoreboardTextCache;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ScoreboardConfig;
import com.cedarxuesong.translate_allinone.utils.llmapi.LLM;
import com.cedarxuesong.translate_allinone.utils.llmapi.ProviderSettings;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutorService;

public class ScoreboardTranslateManager {
    private static final ScoreboardTranslateManager INSTANCE = new ScoreboardTranslateManager();
    private static final Gson GSON = new Gson();
    private static final Pattern JSON_EXTRACT_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);

    private ExecutorService workerExecutor;
    private ScheduledExecutorService collectorExecutor;
    private ScheduledExecutorService rateLimiterExecutor;
    private final ScoreboardTextCache cache = ScoreboardTextCache.getInstance();
    private Semaphore rateLimiter;
    private int currentRpm = -1;
    private int currentConcurrentRequests = -1;
    private volatile long nextPermitReleaseTime = 0;

    public record RateLimitStatus(boolean isRateLimited, long estimatedWaitSeconds) {}

    private ScoreboardTranslateManager() {}

    public static ScoreboardTranslateManager getInstance() {
        return INSTANCE;
    }

    public RateLimitStatus getRateLimitStatus() {
        if (rateLimiter == null || rateLimiter.availablePermits() > 0 || currentRpm <= 0) {
            return new RateLimitStatus(false, 0);
        }
        long waitMillis = nextPermitReleaseTime - System.currentTimeMillis();
        long waitSeconds = Math.max(0, (long) Math.ceil(waitMillis / 1000.0));
        return new RateLimitStatus(true, waitSeconds);
    }

    public void start() {
        if (workerExecutor == null || workerExecutor.isShutdown()) {
            ScoreboardConfig config = Translate_AllinOne.getConfig().scoreboardTranslate;
            currentConcurrentRequests = Math.max(1, config.max_concurrent_requests);
            workerExecutor = Executors.newFixedThreadPool(currentConcurrentRequests);
            for (int i = 0; i < currentConcurrentRequests; i++) {
                workerExecutor.submit(this::processingLoop);
            }
            Translate_AllinOne.LOGGER.info("ScoreboardTranslateManager started with {} worker threads.", currentConcurrentRequests);
        }

        if (collectorExecutor == null || collectorExecutor.isShutdown()) {
            collectorExecutor = Executors.newSingleThreadScheduledExecutor();
            collectorExecutor.scheduleAtFixedRate(this::collectAndBatchItems, 0, 1, TimeUnit.SECONDS);
            Translate_AllinOne.LOGGER.info("Scoreboard translation collector started.");
        }

        updateRateLimiter();
    }

    public void stop() {
        if (workerExecutor != null && !workerExecutor.isShutdown()) {
            workerExecutor.shutdownNow();
            try {
                if (!workerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    Translate_AllinOne.LOGGER.error("Processing executor did not terminate in time.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Translate_AllinOne.LOGGER.info("ScoreboardTranslateManager's processing threads stopped.");
        }
        if (collectorExecutor != null && !collectorExecutor.isShutdown()) {
            collectorExecutor.shutdownNow();
            Translate_AllinOne.LOGGER.info("Scoreboard translation collector stopped.");
        }
        if (rateLimiterExecutor != null && !rateLimiterExecutor.isShutdown()) {
            rateLimiterExecutor.shutdownNow();
            Translate_AllinOne.LOGGER.info("ScoreboardTranslateManager's rate limiter thread stopped.");
        }
    }

    private void updateRateLimiter() {
        ScoreboardConfig config = Translate_AllinOne.getConfig().scoreboardTranslate;
        int newRpm = config.requests_per_minute;
        int newConcurrentRequests = Math.max(1, config.max_concurrent_requests);

        if (newRpm != currentRpm || newConcurrentRequests != currentConcurrentRequests) {
            currentRpm = newRpm;
            currentConcurrentRequests = newConcurrentRequests;

            if (rateLimiterExecutor != null && !rateLimiterExecutor.isShutdown()) {
                rateLimiterExecutor.shutdownNow();
            }

            if (currentRpm > 0) {
                final int burstSize = Math.max(1, currentRpm / 30);
                rateLimiter = new Semaphore(burstSize, true);

                long delayBetweenPermits = 60 * 1000L / currentRpm;
                nextPermitReleaseTime = System.currentTimeMillis() + delayBetweenPermits;

                rateLimiterExecutor = Executors.newSingleThreadScheduledExecutor();
                rateLimiterExecutor.scheduleAtFixedRate(this::requeueErroredItems, 15, 15, TimeUnit.SECONDS);
                
                rateLimiterExecutor.scheduleAtFixedRate(() -> {
                    if (rateLimiter.availablePermits() < burstSize) {
                        rateLimiter.release();
                    }
                    nextPermitReleaseTime = System.currentTimeMillis() + delayBetweenPermits;
                }, delayBetweenPermits, delayBetweenPermits, TimeUnit.MILLISECONDS);

                Translate_AllinOne.LOGGER.info("Scoreboard rate limiter updated to {} RPM (1 permit every {}ms), initial permits: {}.", currentRpm, delayBetweenPermits, burstSize);
            } else {
                rateLimiter = null; // No limit
                nextPermitReleaseTime = 0;
                Translate_AllinOne.LOGGER.info("Scoreboard rate limiter disabled.");
            }
        }
    }

    private void processingLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ScoreboardConfig config = Translate_AllinOne.getConfig().scoreboardTranslate;
                if (!config.enabled) {
                    TimeUnit.SECONDS.sleep(5);
                    continue;
                }
                
                List<String> batch = cache.takeBatchForTranslation();
                cache.markAsInProgress(batch);

                if (rateLimiter != null) {
                    rateLimiter.acquire();
                }

                translateBatch(batch, config);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Translate_AllinOne.LOGGER.info("Processing thread interrupted, shutting down.");
                break;
            } catch (Exception e) {
                Translate_AllinOne.LOGGER.error("An unexpected error occurred in the processing loop, continuing.", e);
            }
        }
    }

    private void collectAndBatchItems() {
        try {
            ScoreboardConfig config = Translate_AllinOne.getConfig().scoreboardTranslate;
            if (!config.enabled) {
                return;
            }
            List<String> items = cache.drainAllPendingItems();
            if (items.isEmpty()) {
                return;
            }

            int batchSize = config.max_batch_size;
            for (int i = 0; i < items.size(); i += batchSize) {
                int end = Math.min(items.size(), i + batchSize);
                List<String> batch = new java.util.ArrayList<>(items.subList(i, end));
                cache.submitBatchForTranslation(batch);
            }
            Translate_AllinOne.LOGGER.info("Collected and submitted {} batch(es) for scoreboard translation.", (int) Math.ceil((double) items.size() / batchSize));
        } catch (Exception e) {
            Translate_AllinOne.LOGGER.error("Error in scoreboard collector thread", e);
        }
    }

    private void requeueErroredItems() {
        try {
            java.util.Set<String> erroredKeys = cache.getErroredKeys();
            if (!erroredKeys.isEmpty()) {
                Translate_AllinOne.LOGGER.info("Re-queueing {} errored scoreboard items for another attempt.", erroredKeys.size());
                erroredKeys.forEach(cache::requeueFromError);
            }
        } catch (Exception e) {
            Translate_AllinOne.LOGGER.error("Error during scheduled re-queue of errored scoreboard items", e);
        }
    }

    private void translateBatch(List<String> originalTexts, ScoreboardConfig config) {
        if (originalTexts.isEmpty()) {
            return;
        }
        
        Map<String, String> batchForAI = new java.util.LinkedHashMap<>();
        for (int i = 0; i < originalTexts.size(); i++) {
            batchForAI.put(String.valueOf(i + 1), originalTexts.get(i));
        }

        ProviderSettings settings = ProviderSettings.fromScoreboardConfig(config);
        LLM llm = new LLM(settings);

        String systemPrompt = "You are a translation assistant. You will receive a JSON object, where the keys are numeric IDs and the values are the text to be translated. Translate these values into " + config.target_language + ". Use the same numeric IDs as keys and the translated text as values, and in the response, only provide the JSON object, retaining all formatting characters, retaining words or nouns that are uncertain to translate.";

        String suffix = "";
        if (config.llm_provider == com.cedarxuesong.translate_allinone.utils.config.pojos.Provider.OPENAI) {
            suffix = config.openapi.system_prompt_suffix;
        } else if (config.llm_provider == com.cedarxuesong.translate_allinone.utils.config.pojos.Provider.OLLAMA) {
            suffix = config.ollama.system_prompt_suffix;
        }

        if (suffix != null && !suffix.trim().isEmpty()) {
            systemPrompt += " " + suffix.replace("\\n", "\n");
        }
        
        String userPrompt = "JSON:" + GSON.toJson(batchForAI);

        List<OpenAIRequest.Message> messages = List.of(
                new OpenAIRequest.Message("system", systemPrompt),
                new OpenAIRequest.Message("user", userPrompt)
        );

        llm.getCompletion(messages).whenComplete((response, error) -> {
            if (error != null) {
                Translate_AllinOne.LOGGER.error("Failed to get scoreboard translation from LLM", error);
                cache.requeueFailed(new java.util.HashSet<>(originalTexts), error.getMessage());
                return;
            }

            try {
                Matcher matcher = JSON_EXTRACT_PATTERN.matcher(response);
                if (matcher.find()) {
                    String jsonResponse = matcher.group();
                    Type type = new TypeToken<Map<String, String>>() {}.getType();
                    Map<String, String> translatedMapFromAI = GSON.fromJson(jsonResponse, type);

                    Map<String, String> finalTranslatedMap = new java.util.HashMap<>();
                    java.util.Set<String> itemsToRequeueForColor = new java.util.HashSet<>();

                    for (Map.Entry<String, String> entry : translatedMapFromAI.entrySet()) {
                        try {
                            int index = Integer.parseInt(entry.getKey()) - 1;
                            if (index >= 0 && index < originalTexts.size()) {
                                String originalTemplate = originalTexts.get(index);
                                String translatedTemplate = entry.getValue();

                                if (originalTemplate.contains("§") && !translatedTemplate.contains("§")) {
                                    itemsToRequeueForColor.add(originalTemplate);
                                } else {
                                    finalTranslatedMap.put(originalTemplate, translatedTemplate);
                                }
                            } else {
                                Translate_AllinOne.LOGGER.warn("Received out-of-bounds index {} from LLM for scoreboard, skipping.", entry.getKey());
                            }
                        } catch (NumberFormatException e) {
                            Translate_AllinOne.LOGGER.warn("Received non-numeric key '{}' from LLM for scoreboard, skipping.", entry.getKey());
                        }
                    }

                    if (!finalTranslatedMap.isEmpty()) {
                        cache.updateTranslations(finalTranslatedMap);
                    }

                    if (!itemsToRequeueForColor.isEmpty()) {
                        Translate_AllinOne.LOGGER.warn("Re-queueing {} scoreboard translations that failed color code validation.", itemsToRequeueForColor.size());
                        cache.requeueFailed(itemsToRequeueForColor, "Missing color codes in translation");
                    }

                    java.util.Set<String> allOriginalTexts = new java.util.HashSet<>(originalTexts);
                    allOriginalTexts.removeAll(finalTranslatedMap.keySet());
                    allOriginalTexts.removeAll(itemsToRequeueForColor); // remove items already handled
                    if (!allOriginalTexts.isEmpty()) {
                        Translate_AllinOne.LOGGER.warn("Scoreboard LLM response did not contain all original keys. Re-queueing {} missing translations.", allOriginalTexts.size());
                        cache.requeueFailed(allOriginalTexts, "LLM response missing keys");
                    }
                } else {
                    throw new JsonSyntaxException("No JSON object found in the scoreboard response.");
                }
            } catch (JsonSyntaxException e) {
                Translate_AllinOne.LOGGER.error("Failed to parse JSON response from LLM for scoreboard. Response: {}", response, e);
                cache.requeueFailed(new java.util.HashSet<>(originalTexts), "Invalid JSON response");
            }
        });
    }
} 