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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ItemTemplateCache {

    public enum TranslationStatus {
        TRANSLATED,
        IN_PROGRESS,
        PENDING,
        ERROR,
        NOT_CACHED
    }

    public record CacheStats(int translated, int total) {}

    public record LookupResult(TranslationStatus status, String translation, String errorMessage) {}

    private static final ItemTemplateCache INSTANCE = new ItemTemplateCache();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CACHE_FILE_NAME = "item_translate_cache.json";
    private static final long SAVE_DEBOUNCE_MILLIS = 1500L;
    private final Path cacheFilePath;
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();
    private final Set<String> inProgress = ConcurrentHashMap.newKeySet();
    private final LinkedBlockingDeque<String> pendingQueue = new LinkedBlockingDeque<>();
    private final LinkedBlockingQueue<List<String>> batchWorkQueue = new LinkedBlockingQueue<>();
    private final Set<String> allQueuedOrInProgressKeys = ConcurrentHashMap.newKeySet();
    private final Map<String, String> errorCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService saveExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "translate_allinone-item-cache-save");
        thread.setDaemon(true);
        return thread;
    });
    private volatile boolean isDirty = false;
    private volatile long lastSaveAtMillis = 0;
    private volatile boolean saveScheduled = false;

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

    public synchronized void load() {
        pendingQueue.clear();
        inProgress.clear();
        batchWorkQueue.clear();
        allQueuedOrInProgressKeys.clear();
        errorCache.clear();

        if (Files.exists(cacheFilePath)) {
            try (FileReader reader = new FileReader(cacheFilePath.toFile())) {
                Type type = new TypeToken<ConcurrentHashMap<String, String>>() {}.getType();
                Map<String, String> loadedCache = GSON.fromJson(reader, type);
                if (loadedCache != null) {
                    templateCache.putAll(loadedCache);
                    Translate_AllinOne.LOGGER.info(
                            "Successfully loaded {} item translation cache entries (in-memory total: {}).",
                            loadedCache.size(),
                            templateCache.size()
                    );
                }
            } catch (IOException | RuntimeException e) {
                Translate_AllinOne.LOGGER.error("Failed to load item translation cache. Keeping in-memory entries untouched.", e);
            }
        } else {
            Translate_AllinOne.LOGGER.info("Item translation cache file not found, a new one will be created upon saving.");
        }
        isDirty = false;
    }

    public synchronized void save() {
        saveScheduled = false;
        if (!isDirty) {
            return;
        }

        try {
            Files.createDirectories(cacheFilePath.getParent());
            Path tempPath = cacheFilePath.resolveSibling(cacheFilePath.getFileName() + ".tmp");
            try (FileWriter writer = new FileWriter(tempPath.toFile())) {
                GSON.toJson(templateCache, writer);
            }

            try {
                Files.move(tempPath, cacheFilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempPath, cacheFilePath, StandardCopyOption.REPLACE_EXISTING);
            }

                Translate_AllinOne.LOGGER.info("Successfully saved {} item translation cache entries.", templateCache.size());
                isDirty = false;
                lastSaveAtMillis = System.currentTimeMillis();
        } catch (IOException e) {
            Translate_AllinOne.LOGGER.error("Failed to save item translation cache", e);
        }
    }

    public String getOrQueue(String originalTemplate) {
        return lookupOrQueue(originalTemplate).translation();
    }

    public LookupResult lookupOrQueue(String originalTemplate) {
        String translation = templateCache.get(originalTemplate);
        if (translation != null && !translation.isEmpty()) {
            return new LookupResult(TranslationStatus.TRANSLATED, translation, null);
        }

        String errorMessage = errorCache.get(originalTemplate);
        if (errorMessage != null) {
            return new LookupResult(TranslationStatus.ERROR, "", errorMessage);
        }

        if (inProgress.contains(originalTemplate)) {
            return new LookupResult(TranslationStatus.IN_PROGRESS, "", null);
        }

        if (allQueuedOrInProgressKeys.add(originalTemplate)) {
            pendingQueue.offerLast(originalTemplate);
        }

        return new LookupResult(TranslationStatus.PENDING, "", null);
    }

    public TranslationStatus getTemplateStatus(String templateKey) {
        if (errorCache.containsKey(templateKey)) {
            return TranslationStatus.ERROR;
        }
        if (inProgress.contains(templateKey)) {
            return TranslationStatus.IN_PROGRESS;
        }
        if (allQueuedOrInProgressKeys.contains(templateKey)) {
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
            pendingQueue.offerFirst(key);
        }
    }

    public synchronized void releaseInProgress(Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        inProgress.removeAll(keys);
        allQueuedOrInProgressKeys.removeAll(keys);
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

        scheduleSave();
    }

    private synchronized void scheduleSave() {
        long elapsed = System.currentTimeMillis() - lastSaveAtMillis;
        if (elapsed >= SAVE_DEBOUNCE_MILLIS) {
            save();
            return;
        }

        if (saveScheduled) {
            return;
        }

        long delayMillis = Math.max(0, SAVE_DEBOUNCE_MILLIS - elapsed);
        saveScheduled = true;
        saveExecutor.schedule(this::save, delayMillis, TimeUnit.MILLISECONDS);
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
