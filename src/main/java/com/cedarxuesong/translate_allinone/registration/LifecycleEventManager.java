package com.cedarxuesong.translate_allinone.registration;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.cache.ScoreboardTextCache;
import com.cedarxuesong.translate_allinone.utils.cache.ItemTemplateCache;
import com.cedarxuesong.translate_allinone.utils.translate.ItemTranslateManager;
import com.cedarxuesong.translate_allinone.utils.translate.ScoreboardTranslateManager;
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
        // Add a shutdown hook to save the cache on game exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Game is shutting down, performing final cache save...");
            ItemTemplateCache.getInstance().save();
            ScoreboardTextCache.getInstance().save();
        }));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            isReadyForTranslation = false;
            awaitingReadinessCheck = true;
            readinessGracePeriodTicks = -1;
            LOGGER.info("Player joining world, awaiting client readiness for translation...");
            ItemTemplateCache.getInstance().load();
            ItemTranslateManager.getInstance().start();
            ScoreboardTextCache.getInstance().load();
            ScoreboardTranslateManager.getInstance().start();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (awaitingReadinessCheck) {
                if (client.player != null && client.world != null && client.currentScreen == null) {
                    awaitingReadinessCheck = false;
                    readinessGracePeriodTicks = GRACE_PERIOD_DURATION_TICKS; // Start grace period
                    LOGGER.info("Client is ready. Starting grace period for {} ticks before enabling translations.", readinessGracePeriodTicks);
                }
            }

            if (readinessGracePeriodTicks > 0) {
                readinessGracePeriodTicks--;
                if (readinessGracePeriodTicks == 0) {
                    isReadyForTranslation = true;
                    LOGGER.info("Grace period over. Translations are now active.");
                }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            isReadyForTranslation = false;
            awaitingReadinessCheck = false;
            readinessGracePeriodTicks = -1;
            LOGGER.info("Player has disconnected. Translation readiness reset.");
            ItemTranslateManager.getInstance().stop();
            ItemTemplateCache.getInstance().save();
            ScoreboardTranslateManager.getInstance().stop();
            ScoreboardTextCache.getInstance().save();
        });
    }
} 