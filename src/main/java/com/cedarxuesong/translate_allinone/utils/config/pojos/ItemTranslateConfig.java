package com.cedarxuesong.translate_allinone.utils.config.pojos;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class ItemTranslateConfig {
    @ConfigEntry.Gui.Tooltip
    public boolean enabled = false;

    @ConfigEntry.Gui.Tooltip
    public boolean enabled_translate_item_custom_name = false;

    @ConfigEntry.Gui.Tooltip
    public boolean enabled_translate_item_lore = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public int max_concurrent_requests = 2;

    @ConfigEntry.Gui.Tooltip
    public int requests_per_minute = 60;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int max_batch_size = 10;

    @ConfigEntry.Gui.Tooltip
    public String target_language = "Chinese";

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public Provider llm_provider = Provider.OPENAI;


    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public KeybindingConfig keybinding = new KeybindingConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public OpenaiApi openapi = new OpenaiApi();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public OllamaApi ollama = new OllamaApi();


    public enum KeybindingMode {
        HOLD_TO_TRANSLATE,
        HOLD_TO_SEE_ORIGINAL,
        DISABLED
    }

    public static class KeybindingConfig {
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip(count = 4)
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public KeybindingMode mode = KeybindingMode.DISABLED;
    }

    public static class OpenaiApi{
        @ConfigEntry.Gui.PrefixText
        public String baseUrl = "https://api.openai.com/v1";
        public String apiKey = "sk-xxxxxx";
        public String modelId = "gpt-4o";
        public double temperature = 0.7;
        @ConfigEntry.Gui.Tooltip
        public boolean enable_structured_output_if_available = false;
        public String system_prompt_suffix = "\\no_think";
        @ConfigEntry.Gui.Tooltip
        public List<CustomParameterEntry> custom_parameters = new ArrayList<>();
    }
    public static class OllamaApi{
        @ConfigEntry.Gui.PrefixText
        public String ollamaUrl = "http://localhost:11434";
        public String modelId = "qwen3:0.6b";
        public String keep_alive_time = "1m";
        public double temperature = 0.7;
        @ConfigEntry.Gui.Tooltip
        public boolean enable_structured_output_if_available = false;
        public String system_prompt_suffix = "\\no_think";
        @ConfigEntry.Gui.Tooltip
        public List<CustomParameterEntry> custom_parameters = new ArrayList<>();
    }
}
