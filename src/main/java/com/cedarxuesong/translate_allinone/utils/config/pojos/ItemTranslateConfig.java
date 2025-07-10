package com.cedarxuesong.translate_allinone.utils.config.pojos;

import com.google.gson.annotations.SerializedName;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.List;

public class ItemTranslateConfig {
    @Comment("Enable item translation features")
    public boolean enabled = false;

    @Comment("Translate custom names of items (e.g., from an anvil)")
    public boolean enabled_translate_item_custom_name = false;

    @Comment("Translate the lore of items")
    public boolean enabled_translate_item_lore = false;

    @Comment("The number of parallel translation threads")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public int max_concurrent_requests = 2;

    @Comment("API requests per minute limit (0 to disable)")
    public int requests_per_minute = 60;

    @Comment("The maximum number of item lore lines to translate in a single API request")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int max_batch_size = 10;

    @Comment("The target language for translation")
    public String target_language = "Chinese";

    @Comment("Large Language Model Suppliers (Click to Switch)")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public Provider llm_provider = Provider.OPENAI;


    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public OpenaiApi openapi = new OpenaiApi();

    @ConfigEntry.Gui.CollapsibleObject
    public OllamaApi ollama = new OllamaApi();

    public static class OpenaiApi{
        @ConfigEntry.Gui.PrefixText
        public String baseUrl = "https://api.openai.com/v1";
        public String apiKey = "sk-xxxxxx";
        public String modelId = "gpt-4o";
        public double temperature = 0.7;
        public String system_prompt_suffix = "\\no_think";
        @Comment("Custom parameters to be sent with the API request")
        public List<CustomParameterEntry> custom_parameters = new ArrayList<>();
    }
    public static class OllamaApi{
        @ConfigEntry.Gui.PrefixText
        public String ollamaUrl = "http://localhost:11434";
        public String modelId = "qwen3:0.6b";
        public String keep_alive_time = "1m";
        public double temperature = 0.7;
        public String system_prompt_suffix = "\\no_think";
        @Comment("Custom parameters to be sent with the API request")
        public List<CustomParameterEntry> custom_parameters = new ArrayList<>();
    }
}
