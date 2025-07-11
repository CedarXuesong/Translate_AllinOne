package com.cedarxuesong.translate_allinone.utils.cache;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;

public class ItemTemplateCache {

    public enum TranslationStatus {
        TRANSLATED,
        IN_PROGRESS,
        PENDING,
        ERROR,
        NOT_CACHED
    }

    public record CacheStats(int translated, int total) {}

    private static final ItemTemplateCache INSTANCE = new ItemTemplateCache();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CACHE_FILE_NAME = "item_translate_cache.json";
    private final Path cacheFilePath;
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();
    private final Set<String> inProgress = ConcurrentHashMap.newKeySet();
    private final LinkedBlockingQueue<String> pendingQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<List<String>> batchWorkQueue = new LinkedBlockingQueue<>();
    private final Set<String> allQueuedOrInProgressKeys = ConcurrentHashMap.newKeySet();
    private final Map<String, String> errorCache = new ConcurrentHashMap<>();
    private volatile boolean isDirty = false;

    private ItemTemplateCache() {
        this.cacheFilePath = FabricLoader.getInstance().getConfigDir()
                .resolve(Translate_AllinOne.MOD_ID)
                .resolve(CACHE_FILE_NAME);
    }

    public void clearPendingAndInProgress() {
        if (!pendingQueue.isEmpty()) {
            pendingQueue.clear();
        }
        if (!inProgress.isEmpty()) {
            inProgress.clear();
        }
        allQueuedOrInProgressKeys.clear();
        batchWorkQueue.clear();
    }

    public synchronized boolean isPendingQueueEmpty() {
        return pendingQueue.isEmpty();
    }

    public static ItemTemplateCache getInstance() {
        return INSTANCE;
    }

    public void load() {
        templateCache.clear();
        pendingQueue.clear();
        inProgress.clear();

        if (Files.exists(cacheFilePath)) {
            try (FileReader reader = new FileReader(cacheFilePath.toFile())) {
                Type type = new TypeToken<ConcurrentHashMap<String, String>>() {}.getType();
                Map<String, String> loadedCache = GSON.fromJson(reader, type);
                if (loadedCache != null) {
                    templateCache.putAll(loadedCache);
                    Translate_AllinOne.LOGGER.info("Successfully loaded {} item translation cache entries.", templateCache.size());
                }
            } catch (IOException e) {
                Translate_AllinOne.LOGGER.error("Failed to load item translation cache", e);
            }
        } else {
            Translate_AllinOne.LOGGER.info("Item translation cache file not found, a new one will be created upon saving.");
        }
        isDirty = false;
    }

    public synchronized void save() {
        if (!isDirty) {
            return;
        }

        try {
            Files.createDirectories(cacheFilePath.getParent());
            try (FileWriter writer = new FileWriter(cacheFilePath.toFile())) {
                GSON.toJson(templateCache, writer);
                Translate_AllinOne.LOGGER.info("Successfully saved {} item translation cache entries.", templateCache.size());
                isDirty = false;
            }
        } catch (IOException e) {
            Translate_AllinOne.LOGGER.error("Failed to save item translation cache", e);
        }
    }

    public String getOrQueue(String originalTemplate) {
        if (templateCache.containsKey(originalTemplate)) {
            String translation = templateCache.get(originalTemplate);
            if (translation != null && !translation.isEmpty()) {
                return translation;
            }
        }

        // Atomically check and add.
        // If the key was not already in the set, add it to the pending queue.
        if (allQueuedOrInProgressKeys.add(originalTemplate)) {
        pendingQueue.offer(originalTemplate);
        }
        return "";
    }

    public TranslationStatus getTemplateStatus(String templateKey) {
        if (errorCache.containsKey(templateKey)) {
            return TranslationStatus.ERROR;
        }
        if (inProgress.contains(templateKey)) {
            return TranslationStatus.IN_PROGRESS;
        }
        if (pendingQueue.contains(templateKey)) {
            return TranslationStatus.PENDING;
        }

        if (templateCache.containsKey(templateKey)) {
            String translation = templateCache.get(templateKey);
            if (translation != null && !translation.isEmpty()) {
                return TranslationStatus.TRANSLATED;
            } else {
                return TranslationStatus.PENDING;
            }
        }
        
        return TranslationStatus.NOT_CACHED;
    }

    public synchronized CacheStats getCacheStats() {
        long translatedCount = templateCache.values().stream()
                .filter(v -> v != null && !v.isEmpty())
                .count();

        Set<String> allKeys = ConcurrentHashMap.newKeySet();
        allKeys.addAll(templateCache.keySet());
        allKeys.addAll(pendingQueue);
        allKeys.addAll(inProgress);

        int totalCount = allKeys.size();

        return new CacheStats((int) translatedCount, totalCount);
    }

    public Set<String> getErroredKeys() {
        return new java.util.HashSet<>(errorCache.keySet());
    }

    public String getError(String templateKey) {
        return errorCache.get(templateKey);
    }

    public List<String> drainAllPendingItems() {
        List<String> items = new ArrayList<>();
        pendingQueue.drainTo(items);
        return items;
    }

    public void submitBatchForTranslation(List<String> batch) {
        if (batch != null && !batch.isEmpty()) {
            batchWorkQueue.offer(batch);
        }
    }

    public List<String> takeBatchForTranslation() throws InterruptedException {
        return batchWorkQueue.take();
    }
    
    public void markAsInProgress(List<String> batch) {
        inProgress.addAll(batch);
    }

    public synchronized void requeueFromError(String key) {
        if (errorCache.remove(key) != null) {
            pendingQueue.offer(key);
        }
    }

    public synchronized void updateTranslations(Map<String, String> translations) {
        if (translations == null || translations.isEmpty()) {
            return;
        }
        templateCache.putAll(translations);
        
        Set<String> finishedKeys = translations.keySet();
        inProgress.removeAll(finishedKeys);
        allQueuedOrInProgressKeys.removeAll(finishedKeys);
        finishedKeys.forEach(errorCache::remove);
        
        isDirty = true;
        Translate_AllinOne.LOGGER.info("Updated {} translations in the cache.", translations.size());
         
        save();
    }

    public synchronized void requeueFailed(Set<String> failedKeys, String errorMessage) {
        if (failedKeys == null || failedKeys.isEmpty()) {
            return;
        }
        inProgress.removeAll(failedKeys);
        failedKeys.forEach(key -> errorCache.put(key, errorMessage));
        Translate_AllinOne.LOGGER.warn("Marked {} keys as errored. They will be retried later.", failedKeys.size());
    }
}
