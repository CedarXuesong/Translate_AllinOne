package com.cedarxuesong.translate_allinone.utils.config.pojos;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class ChatTranslateConfig {
    @Comment("Enable chat translation feature")
    public boolean enabled = false;

    @Comment("Automatically translate incoming chat messages without clicking the button")
    public boolean auto_translate = false;

    @Comment("The target language for translation")
    public String target_language = "Chinese";


    @Comment("Receive the translated response in a streaming fashion")
    public boolean streaming_response = false;

    @Comment("Large Language Model Suppliers (Click to Switch)")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public Provider llm_provider = Provider.OPENAI;

    @Comment("Maximum number of concurrent translation tasks. Be careful with high values, as it may trigger API rate limits.")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public int max_concurrent_requests = 1;

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