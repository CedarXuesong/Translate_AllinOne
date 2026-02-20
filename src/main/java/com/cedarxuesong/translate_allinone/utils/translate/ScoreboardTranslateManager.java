package com.cedarxuesong.translate_allinone.utils.translate;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.cache.ScoreboardTextCache;
import com.cedarxuesong.translate_allinone.utils.config.ProviderRouteResolver;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ScoreboardConfig;
import com.cedarxuesong.translate_allinone.utils.llmapi.LLM;
import com.cedarxuesong.translate_allinone.utils.llmapi.ProviderSettings;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
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
    private static final int MAX_KEY_MISMATCH_BATCH_RETRIES = 1;

    private ExecutorService workerExecutor;
    private ScheduledExecutorService collectorExecutor;
    private ScheduledExecutorService retryExecutor;
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

        if (retryExecutor == null || retryExecutor.isShutdown()) {
            retryExecutor = Executors.newSingleThreadScheduledExecutor();
            retryExecutor.scheduleAtFixedRate(this::requeueErroredItems, 15, 15, TimeUnit.SECONDS);
            Translate_AllinOne.LOGGER.info("Scoreboard translation retry scheduler started.");
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
        if (retryExecutor != null && !retryExecutor.isShutdown()) {
            retryExecutor.shutdownNow();
            Translate_AllinOne.LOGGER.info("Scoreboard translation retry scheduler stopped.");
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
            List<String> batch = null;
            try {
                ScoreboardConfig config = Translate_AllinOne.getConfig().scoreboardTranslate;
                if (!config.enabled) {
                    TimeUnit.SECONDS.sleep(5);
                    continue;
                }
                
                batch = cache.takeBatchForTranslation();
                cache.markAsInProgress(batch);

                if (rateLimiter != null) {
                    rateLimiter.acquire();
                }

                translateBatch(batch, config);

            } catch (InterruptedException e) {
                if (batch != null && !batch.isEmpty()) {
                    cache.requeueFailed(new java.util.HashSet<>(batch), "Processing thread interrupted");
                }
                Thread.currentThread().interrupt();
                Translate_AllinOne.LOGGER.info("Processing thread interrupted, shutting down.");
                break;
            } catch (Exception e) {
                if (batch != null && !batch.isEmpty()) {
                    cache.requeueFailed(new java.util.HashSet<>(batch), "Processing loop failure: " + e.getMessage());
                }
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
        translateBatch(originalTexts, config, 0);
    }

    private void translateBatch(List<String> originalTexts, ScoreboardConfig config, int keyMismatchRetryCount) {
        if (originalTexts.isEmpty()) {
            return;
        }
        
        Map<String, String> batchForAI = new java.util.LinkedHashMap<>();
        for (int i = 0; i < originalTexts.size(); i++) {
            batchForAI.put(String.valueOf(i + 1), originalTexts.get(i));
        }

        ApiProviderProfile providerProfile = ProviderRouteResolver.resolve(
                Translate_AllinOne.getConfig(),
                ProviderRouteResolver.Route.SCOREBOARD
        );
        if (providerProfile == null) {
            Translate_AllinOne.LOGGER.warn("No routed provider/model configured for scoreboard translation; re-queueing {} entries.", originalTexts.size());
            cache.requeueFailed(new java.util.HashSet<>(originalTexts), "No routed model selected");
            return;
        }

        ProviderSettings settings = ProviderSettings.fromProviderProfile(providerProfile);
        LLM llm = new LLM(settings);

        String systemPrompt = buildSystemPrompt(config.target_language, providerProfile.activeSystemPromptSuffix());
        String userPrompt = GSON.toJson(batchForAI);

        List<OpenAIRequest.Message> messages = PromptMessageBuilder.buildMessages(
                systemPrompt,
                userPrompt,
                providerProfile.activeSupportsSystemMessage(),
                providerProfile.model_id,
                providerProfile.activeInjectSystemPromptIntoUserMessage()
        );
        String requestContext = buildRequestContext(providerProfile, config.target_language, originalTexts, messages);

        llm.getCompletion(messages).whenComplete((response, error) -> {
            if (error != null) {
                if (isInternalPostprocessError(error) && originalTexts.size() > 1) {
                    Translate_AllinOne.LOGGER.warn(
                            "Scoreboard batch translation hit internal post-process error, retrying as single-item batches. context={} batchSize={}",
                            requestContext,
                            originalTexts.size()
                    );
                    for (String text : originalTexts) {
                        translateBatch(List.of(text), config);
                    }
                    return;
                }

                Translate_AllinOne.LOGGER.error("Failed to get scoreboard translation from LLM. context={}", requestContext, error);
                cache.requeueFailed(new java.util.HashSet<>(originalTexts), error.getMessage());
                return;
            }

            try {
                Matcher matcher = JSON_EXTRACT_PATTERN.matcher(response);
                if (matcher.find()) {
                    String jsonResponse = matcher.group();
                    Type type = new TypeToken<Map<String, String>>() {}.getType();
                    Map<String, String> translatedMapFromAI = GSON.fromJson(jsonResponse, type);
                    if (translatedMapFromAI == null) {
                        throw new JsonSyntaxException("Parsed scoreboard translation result is null");
                    }

                    if (hasKeyMismatch(translatedMapFromAI, originalTexts.size())) {
                        if (keyMismatchRetryCount < MAX_KEY_MISMATCH_BATCH_RETRIES) {
                            int nextAttempt = keyMismatchRetryCount + 1;
                            Translate_AllinOne.LOGGER.warn(
                                    "Scoreboard translation keys mismatched, retrying full batch. attempt={}/{} context={}",
                                    nextAttempt,
                                    MAX_KEY_MISMATCH_BATCH_RETRIES,
                                    requestContext
                            );
                            translateBatch(new java.util.ArrayList<>(originalTexts), config, nextAttempt);
                        } else {
                            Translate_AllinOne.LOGGER.warn(
                                    "Scoreboard translation keys mismatched after retries, re-queueing full batch. context={}",
                                    requestContext
                            );
                            cache.requeueFailed(new java.util.HashSet<>(originalTexts), "LLM response key mismatch");
                        }
                        return;
                    }

                    Map<String, String> finalTranslatedMap = new java.util.HashMap<>();
                    java.util.Set<String> itemsToRequeueForColor = new java.util.HashSet<>();
                    java.util.Set<String> itemsToRequeueForEmpty = new java.util.HashSet<>();

                    for (Map.Entry<String, String> entry : translatedMapFromAI.entrySet()) {
                        try {
                            int index = Integer.parseInt(entry.getKey()) - 1;
                            if (index >= 0 && index < originalTexts.size()) {
                                String originalTemplate = originalTexts.get(index);
                                String translatedTemplate = entry.getValue();

                                if (translatedTemplate == null || translatedTemplate.trim().isEmpty()) {
                                    itemsToRequeueForEmpty.add(originalTemplate);
                                    continue;
                                }

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

                    if (!itemsToRequeueForEmpty.isEmpty()) {
                        Translate_AllinOne.LOGGER.warn("Re-queueing {} scoreboard translations that returned empty values.", itemsToRequeueForEmpty.size());
                        cache.requeueFailed(itemsToRequeueForEmpty, "Empty translation response");
                    }

                    java.util.Set<String> allOriginalTexts = new java.util.HashSet<>(originalTexts);
                    allOriginalTexts.removeAll(finalTranslatedMap.keySet());
                    allOriginalTexts.removeAll(itemsToRequeueForColor); // remove items already handled
                    allOriginalTexts.removeAll(itemsToRequeueForEmpty);
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

    private boolean hasKeyMismatch(Map<String, String> translatedMapFromAI, int expectedSize) {
        java.util.Set<String> expectedKeys = new java.util.LinkedHashSet<>();
        for (int i = 1; i <= expectedSize; i++) {
            expectedKeys.add(String.valueOf(i));
        }

        java.util.Set<String> actualKeys = new java.util.LinkedHashSet<>(translatedMapFromAI.keySet());
        java.util.Set<String> missingKeys = new java.util.LinkedHashSet<>(expectedKeys);
        missingKeys.removeAll(actualKeys);

        java.util.Set<String> extraKeys = new java.util.LinkedHashSet<>(actualKeys);
        extraKeys.removeAll(expectedKeys);

        if (missingKeys.isEmpty() && extraKeys.isEmpty()) {
            return false;
        }

        Translate_AllinOne.LOGGER.warn(
                "Scoreboard translation key mismatch. expectedCount={}, actualCount={}, missing={}, extra={}",
                expectedKeys.size(),
                actualKeys.size(),
                summarizeKeys(missingKeys),
                summarizeKeys(extraKeys)
        );
        return true;
    }

    private String summarizeKeys(java.util.Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return "[]";
        }
        final int limit = 8;
        java.util.List<String> sample = new java.util.ArrayList<>(limit);
        int count = 0;
        for (String key : keys) {
            if (count++ >= limit) {
                break;
            }
            sample.add(key);
        }
        if (keys.size() <= limit) {
            return sample.toString();
        }
        return sample + "...(+" + (keys.size() - limit) + ")";
    }

    private String buildSystemPrompt(String targetLanguage, String suffix) {
        String basePrompt = "You are a deterministic JSON value translator.\n"
                + "Target language: " + targetLanguage + ".\n"
                + "\n"
                + "Input is a JSON object with string keys and string values.\n"
                + "Output must be one valid JSON object only.\n"
                + "\n"
                + "Rules:\n"
                + "1) Keep all keys unchanged.\n"
                + "2) Keep key count unchanged.\n"
                + "3) Translate values only.\n"
                + "4) Preserve tokens exactly: §a §l §r %s %d %f {d1} URLs numbers <...> {...} \\n \\t.\n"
                + "5) If unsure for a value, keep that value unchanged.\n"
                + "6) No extra text outside JSON.";
        return PromptMessageBuilder.appendSystemPromptSuffix(basePrompt, suffix);
    }

    private boolean isInternalPostprocessError(Throwable throwable) {
        Throwable root = unwrapThrowable(throwable);
        if (root == null || root.getMessage() == null) {
            return false;
        }
        String message = root.getMessage().toLowerCase(Locale.ROOT);
        return message.contains("internalpostprocesserror")
                || message.contains("internal error during model post-process")
                || message.contains("translation failed due to internal error");
    }

    private Throwable unwrapThrowable(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof java.util.concurrent.CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private String buildRequestContext(
            ApiProviderProfile profile,
            String targetLanguage,
            List<String> originalTexts,
            List<OpenAIRequest.Message> messages
    ) {
        String providerId = profile == null ? "" : profile.id;
        String modelId = profile == null ? "" : profile.model_id;
        int messageCount = messages == null ? 0 : messages.size();
        String roles = messages == null
                ? "[]"
                : messages.stream().map(message -> message == null ? "null" : String.valueOf(message.role)).collect(java.util.stream.Collectors.joining(",", "[", "]"));
        int customParamCount = profile == null ? 0 : profile.activeCustomParameters().size();
        String sample = originalTexts == null || originalTexts.isEmpty() ? "" : truncate(normalizeWhitespace(originalTexts.get(0)), 160);
        return "route=scoreboard"
                + ", provider=" + providerId
                + ", model=" + modelId
                + ", target=" + (targetLanguage == null ? "" : targetLanguage)
                + ", batch=" + (originalTexts == null ? 0 : originalTexts.size())
                + ", messages=" + messageCount
                + ", roles=" + roles
                + ", custom_params=" + customParamCount
                + ", sample=\"" + sample + "\"";
    }

    private String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }
}
