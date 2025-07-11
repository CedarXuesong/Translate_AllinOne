package com.cedarxuesong.translate_allinone;

import com.cedarxuesong.translate_allinone.registration.CommandManager;
import com.cedarxuesong.translate_allinone.registration.ConfigManager;
import com.cedarxuesong.translate_allinone.registration.LifecycleEventManager;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.input.KeybindingManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Translate_AllinOne implements ModInitializer {

	public static final String MOD_ID = "translate_allinone";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Translate All in One is initializing...");
		ConfigManager.register();
		CommandManager.register();
		KeybindingManager.register();
		LifecycleEventManager.register();
	}

	public static ModConfig getConfig() {
		return ConfigManager.getConfig();
	}
}