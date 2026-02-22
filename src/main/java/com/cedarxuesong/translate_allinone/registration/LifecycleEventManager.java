package com.cedarxuesong.translate_allinone.registration;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.cache.ScoreboardTextCache;
import com.cedarxuesong.translate_allinone.utils.cache.ItemTemplateCache;
import com.cedarxuesong.translate_allinone.utils.translate.ItemTranslateManager;
import com.cedarxuesong.translate_allinone.utils.translate.ScoreboardTranslateManager;
import com.cedarxuesong.translate_allinone.utils.update.UpdateCheckManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifecycleEventManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(Translate_AllinOne.MOD_ID);

    public static volatile boolean isReadyForTranslation = false;
    private static boolean awaitingReadinessCheck = false;
    private static int readinessGracePeriodTicks = -1;
    private static final int GRACE_PERIOD_DURATION_TICKS = 20; // 1 second (20 ticks/sec)

    public static void register() {
        registerShutdownHook();
        registerJoinHandler();
        registerReadinessTickHandler();
        registerDisconnectHandler();
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Game is shutting down, performing final cache save...");
            saveCaches();
        }));
    }

    private static void registerJoinHandler() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            resetReadinessState();
            awaitingReadinessCheck = true;
            LOGGER.info("Player joining world, awaiting client readiness for translation...");

            stopTranslationManagers();
            loadCachesAndStartTranslationManagers();
        });
    }

    private static void registerReadinessTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (awaitingReadinessCheck && client.player != null && client.world != null && client.currentScreen == null) {
                awaitingReadinessCheck = false;
                readinessGracePeriodTicks = GRACE_PERIOD_DURATION_TICKS;
                LOGGER.info("Client is ready. Starting grace period for {} ticks before enabling translations.", readinessGracePeriodTicks);
            }

            if (readinessGracePeriodTicks > 0) {
                readinessGracePeriodTicks--;
                if (readinessGracePeriodTicks == 0) {
                    isReadyForTranslation = true;
                    LOGGER.info("Grace period over. Translations are now active.");
                }
            }

            if (isReadyForTranslation) {
                UpdateCheckManager.tryNotifyInChat(client);
            }
        });
    }

    private static void registerDisconnectHandler() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            resetReadinessState();
            LOGGER.info("Player has disconnected. Translation readiness reset.");
            stopTranslationManagers();
            saveCaches();
        });
    }

    private static void stopTranslationManagers() {
        ItemTranslateManager.getInstance().stop();
        ScoreboardTranslateManager.getInstance().stop();
    }

    private static void loadCachesAndStartTranslationManagers() {
        ItemTemplateCache.getInstance().load();
        ItemTranslateManager.getInstance().start();
        ScoreboardTextCache.getInstance().load();
        ScoreboardTranslateManager.getInstance().start();
    }

    private static void saveCaches() {
        ItemTemplateCache.getInstance().save();
        ScoreboardTextCache.getInstance().save();
    }

    private static void resetReadinessState() {
        isReadyForTranslation = false;
        awaitingReadinessCheck = false;
        readinessGracePeriodTicks = -1;
    }
}
