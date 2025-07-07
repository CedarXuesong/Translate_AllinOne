package com.cedarxuesong.translate_allinone.utils.translate;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.cache.TextTemplateCache;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ItemTranslateConfig;
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

public class ItemTranslateManager {
    private static final ItemTranslateManager INSTANCE = new ItemTranslateManager();
    private static final Gson GSON = new Gson();
    private static final Pattern JSON_EXTRACT_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);

    private ExecutorService workerExecutor;
    private ScheduledExecutorService collectorExecutor;
    private ScheduledExecutorService rateLimiterExecutor;
    private final TextTemplateCache cache = TextTemplateCache.getInstance();
    private Semaphore rateLimiter;
    private int currentRpm = -1;
    private int currentConcurrentRequests = -1;
    private volatile long nextPermitReleaseTime = 0;

    public record RateLimitStatus(boolean isRateLimited, long estimatedWaitSeconds) {}

    private ItemTranslateManager() {}

    public static ItemTranslateManager getInstance() {
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
            ItemTranslateConfig config = Translate_AllinOne.getConfig().itemTranslate;
            currentConcurrentRequests = Math.max(1, config.max_concurrent_requests);
            workerExecutor = Executors.newFixedThreadPool(currentConcurrentRequests);
            for (int i = 0; i < currentConcurrentRequests; i++) {
                workerExecutor.submit(this::processingLoop);
            }
            Translate_AllinOne.LOGGER.info("ItemTranslateManager started with {} worker threads.", currentConcurrentRequests);
        }

        if (collectorExecutor == null || collectorExecutor.isShutdown()) {
            collectorExecutor = Executors.newSingleThreadScheduledExecutor();
            collectorExecutor.scheduleAtFixedRate(this::collectAndBatchItems, 0, 1, TimeUnit.SECONDS);
            Translate_AllinOne.LOGGER.info("Item translation collector started.");
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
            Translate_AllinOne.LOGGER.info("ItemTranslateManager's processing threads stopped.");
        }
        if (collectorExecutor != null && !collectorExecutor.isShutdown()) {
            collectorExecutor.shutdownNow();
            Translate_AllinOne.LOGGER.info("Item translation collector stopped.");
        }
        if (rateLimiterExecutor != null && !rateLimiterExecutor.isShutdown()) {
            rateLimiterExecutor.shutdownNow();
            Translate_AllinOne.LOGGER.info("ItemTranslateManager's rate limiter thread stopped.");
        }
    }

    private void updateRateLimiter() {
        ItemTranslateConfig config = Translate_AllinOne.getConfig().itemTranslate;
        int newRpm = config.requests_per_minute;
        int newConcurrentRequests = Math.max(1, config.max_concurrent_requests);

        if (newRpm != currentRpm || newConcurrentRequests != currentConcurrentRequests) {
            currentRpm = newRpm;
            currentConcurrentRequests = newConcurrentRequests;

            if (rateLimiterExecutor != null && !rateLimiterExecutor.isShutdown()) {
                rateLimiterExecutor.shutdownNow();
            }

            if (currentRpm > 0) {
                // The burst size is calculated as the number of requests allowed in 2 seconds.
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

                Translate_AllinOne.LOGGER.info("Rate limiter updated to {} RPM (1 permit every {}ms), initial permits: {}.", currentRpm, delayBetweenPermits, burstSize);
            } else {
                rateLimiter = null; // No limit
                nextPermitReleaseTime = 0;
                Translate_AllinOne.LOGGER.info("Rate limiter disabled.");
            }
        }
    }

    private void processingLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ItemTranslateConfig config = Translate_AllinOne.getConfig().itemTranslate;
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
            ItemTranslateConfig config = Translate_AllinOne.getConfig().itemTranslate;
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
            Translate_AllinOne.LOGGER.info("Collected and submitted {} batch(es) for translation.", (int) Math.ceil((double) items.size() / batchSize));
        } catch (Exception e) {
            Translate_AllinOne.LOGGER.error("Error in collector thread", e);
        }
    }

    private void requeueErroredItems() {
        try {
            java.util.Set<String> erroredKeys = cache.getErroredKeys();
            if (!erroredKeys.isEmpty()) {
                Translate_AllinOne.LOGGER.info("Re-queueing {} errored items for another attempt.", erroredKeys.size());
                erroredKeys.forEach(cache::requeueFromError);
            }
        } catch (Exception e) {
            Translate_AllinOne.LOGGER.error("Error during scheduled re-queue of errored items", e);
        }
    }

    private void translateBatch(List<String> originalTexts, ItemTranslateConfig config) {
        if (originalTexts.isEmpty()) {
            return;
        }
        
        Map<String, String> batchForAI = new java.util.LinkedHashMap<>();
        for (int i = 0; i < originalTexts.size(); i++) {
            batchForAI.put(String.valueOf(i + 1), originalTexts.get(i));
        }

        ProviderSettings settings = ProviderSettings.fromItemConfig(config);
        LLM llm = new LLM(settings);

        String systemPrompt = "You are a translation assistant. You will be provided with a JSON object where keys are numeric IDs and values are the texts to be translated. Translate the values into " + config.target_language + ". Respond with a valid JSON object using the same numeric IDs as keys and the translated texts as values. Provide only the JSON object in your response, without any additional explanations or markdown formatting.";
        String userPrompt = "JSON:" + GSON.toJson(batchForAI);

        List<OpenAIRequest.Message> messages = List.of(
                new OpenAIRequest.Message("system", systemPrompt),
                new OpenAIRequest.Message("user", userPrompt)
        );

        llm.getCompletion(messages).whenComplete((response, error) -> {
            if (error != null) {
                Translate_AllinOne.LOGGER.error("Failed to get translation from LLM", error);
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
                    for (Map.Entry<String, String> entry : translatedMapFromAI.entrySet()) {
                        try {
                            int index = Integer.parseInt(entry.getKey()) - 1;
                            if (index >= 0 && index < originalTexts.size()) {
                                finalTranslatedMap.put(originalTexts.get(index), entry.getValue());
                            } else {
                                Translate_AllinOne.LOGGER.warn("Received out-of-bounds index {} from LLM, skipping.", entry.getKey());
                            }
                        } catch (NumberFormatException e) {
                            Translate_AllinOne.LOGGER.warn("Received non-numeric key '{}' from LLM, skipping.", entry.getKey());
                        }
                    }

                    if (!finalTranslatedMap.isEmpty()) {
                        cache.updateTranslations(finalTranslatedMap);
                    }

                    // Re-queue texts that were not successfully translated
                    java.util.Set<String> allOriginalTexts = new java.util.HashSet<>(originalTexts);
                    allOriginalTexts.removeAll(finalTranslatedMap.keySet());
                    if (!allOriginalTexts.isEmpty()) {
                        Translate_AllinOne.LOGGER.warn("LLM response did not contain all original keys. Re-queueing {} missing translations.", allOriginalTexts.size());
                        cache.requeueFailed(allOriginalTexts, "LLM response missing keys");
                    }
                } else {
                    throw new JsonSyntaxException("No JSON object found in the response.");
                }
            } catch (JsonSyntaxException e) {
                Translate_AllinOne.LOGGER.error("Failed to parse JSON response from LLM. Response: {}", response, e);
                cache.requeueFailed(new java.util.HashSet<>(originalTexts), "Invalid JSON response");
            }
        });
    }
} 