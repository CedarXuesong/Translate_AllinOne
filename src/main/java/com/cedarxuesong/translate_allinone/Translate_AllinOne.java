package com.cedarxuesong.translate_allinone;

import com.cedarxuesong.translate_allinone.command.ChatHudTranslateCommand;
import com.cedarxuesong.translate_allinone.command.MainCommand;
import com.cedarxuesong.translate_allinone.utils.cache.TextTemplateCache;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.translate.ItemTranslateManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cedarxuesong.translate_allinone.utils.cache.ScoreboardTextCache;
import com.cedarxuesong.translate_allinone.utils.translate.ScoreboardTranslateManager;

public class Translate_AllinOne implements ModInitializer {

	public static final String MOD_ID = "translate_allinone";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static ConfigHolder<ModConfig> configHolder;

	public static volatile boolean isReadyForTranslation = false;
	private static boolean awaitingReadinessCheck = false;
	private static int readinessGracePeriodTicks = -1;
	private static final int GRACE_PERIOD_DURATION_TICKS = 20; // 1 second (20 ticks/sec)

	@Override
	public void onInitialize() {
		configHolder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> MainCommand.register(dispatcher));
		LOGGER.info("Translate All in One is initializing...");

		// Add a shutdown hook to save the cache on game exit
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			LOGGER.info("Game is shutting down, performing final cache save...");
			TextTemplateCache.getInstance().save();
		}));

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			isReadyForTranslation = false;
			awaitingReadinessCheck = true;
			readinessGracePeriodTicks = -1;
			LOGGER.info("Player joining world, awaiting client readiness for translation...");
			TextTemplateCache.getInstance().load();
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
			TextTemplateCache.getInstance().save();
			ScoreboardTranslateManager.getInstance().stop();
			ScoreboardTextCache.getInstance().save();
		});
	}

	public static ModConfig getConfig() {
		return configHolder.getConfig();
	}
}