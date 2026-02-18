package com.cedarxuesong.translate_allinone.registration;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ChatTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.InputBindingConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ItemTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ProviderManagerConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ScoreboardConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(Translate_AllinOne.MOD_ID)
            .resolve(Translate_AllinOne.MOD_ID + ".json");

    private static ModConfig config;
    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }

        config = loadConfig();
        registered = true;
    }

    public static synchronized ModConfig getConfig() {
        ensureRegistered();
        return config;
    }

    public static synchronized void save() {
        ensureRegistered();
        writeConfig(config);
    }

    public static synchronized ModConfig copyCurrentConfig() {
        ensureRegistered();
        return normalizeConfig(deepCopy(config));
    }

    public static synchronized void replaceConfig(ModConfig replacement) {
        ensureRegistered();
        config = normalizeConfig(deepCopy(replacement));
    }

    public static synchronized void resetToDefaults() {
        ensureRegistered();
        config = normalizeConfig(new ModConfig());
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }

    private static ModConfig loadConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            ModConfig defaultConfig = normalizeConfig(new ModConfig());
            writeConfig(defaultConfig);
            return defaultConfig;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ModConfig loadedConfig = normalizeConfig(GSON.fromJson(reader, ModConfig.class));
            if (loadedConfig == null) {
                Translate_AllinOne.LOGGER.warn("Config file is empty or invalid, using defaults: {}", CONFIG_PATH);
                loadedConfig = normalizeConfig(new ModConfig());
                writeConfig(loadedConfig);
            }
            return loadedConfig;
        } catch (Exception e) {
            Translate_AllinOne.LOGGER.error("Failed to load config file, using defaults: {}", CONFIG_PATH, e);
            ModConfig fallback = normalizeConfig(new ModConfig());
            writeConfig(fallback);
            return fallback;
        }
    }

    private static void ensureRegistered() {
        if (!registered) {
            throw new IllegalStateException("Config not registered yet!");
        }
    }

    private static ModConfig deepCopy(ModConfig source) {
        if (source == null) {
            return new ModConfig();
        }
        ModConfig copied = GSON.fromJson(GSON.toJson(source), ModConfig.class);
        return copied == null ? new ModConfig() : copied;
    }

    private static ModConfig normalizeConfig(ModConfig loadedConfig) {
        ModConfig configToUse = loadedConfig;
        if (configToUse == null) {
            configToUse = new ModConfig();
        }

        if (configToUse.chatTranslate == null) {
            configToUse.chatTranslate = new ChatTranslateConfig();
        }
        if (configToUse.itemTranslate == null) {
            configToUse.itemTranslate = new ItemTranslateConfig();
        }
        if (configToUse.scoreboardTranslate == null) {
            configToUse.scoreboardTranslate = new ScoreboardConfig();
        }
        if (configToUse.providerManager == null) {
            configToUse.providerManager = new ProviderManagerConfig();
        }

        if (configToUse.chatTranslate.input == null) {
            configToUse.chatTranslate.input = new ChatTranslateConfig.ChatInputTranslateConfig();
        }
        if (configToUse.chatTranslate.output == null) {
            configToUse.chatTranslate.output = new ChatTranslateConfig.ChatOutputTranslateConfig();
        }
        if (configToUse.chatTranslate.input.keybinding == null) {
            configToUse.chatTranslate.input.keybinding = new InputBindingConfig();
        }

        if (configToUse.itemTranslate.keybinding == null) {
            configToUse.itemTranslate.keybinding = new ItemTranslateConfig.KeybindingConfig();
        }
        if (configToUse.itemTranslate.keybinding.binding == null) {
            configToUse.itemTranslate.keybinding.binding = new InputBindingConfig();
        }

        if (configToUse.scoreboardTranslate.keybinding == null) {
            configToUse.scoreboardTranslate.keybinding = new ScoreboardConfig.KeybindingConfig();
        }
        if (configToUse.scoreboardTranslate.keybinding.binding == null) {
            configToUse.scoreboardTranslate.keybinding.binding = new InputBindingConfig();
        }

        configToUse.providerManager.ensureDefaults();
        return configToUse;
    }

    private static void writeConfig(ModConfig targetConfig) {
        Path parent = CONFIG_PATH.getParent();
        if (parent == null) {
            throw new IllegalStateException("Config path has no parent: " + CONFIG_PATH);
        }

        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create config directory: " + parent, e);
        }

        Path tempPath = parent.resolve(CONFIG_PATH.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(tempPath)) {
            GSON.toJson(targetConfig, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write temp config file: " + tempPath, e);
        }

        try {
            Files.move(tempPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            try {
                Files.move(tempPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveException) {
                throw new RuntimeException("Failed to replace config file: " + CONFIG_PATH, moveException);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to replace config file: " + CONFIG_PATH, e);
        }
    }
}
