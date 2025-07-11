package com.cedarxuesong.translate_allinone.registration;

import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class ConfigManager {
    private static ConfigHolder<ModConfig> configHolder;

    public static void register() {
        configHolder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }

    public static ModConfig getConfig() {
        if (configHolder == null) {
            throw new IllegalStateException("Config not registered yet!");
        }
        return configHolder.getConfig();
    }
} 