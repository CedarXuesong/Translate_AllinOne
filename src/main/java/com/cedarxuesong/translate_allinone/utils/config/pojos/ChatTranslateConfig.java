package com.cedarxuesong.translate_allinone.utils.config.pojos;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class ChatTranslateConfig {

    @ConfigEntry.Gui.CollapsibleObject
    public ChatOutputTranslateConfig output = new ChatOutputTranslateConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public ChatInputTranslateConfig input = new ChatInputTranslateConfig();

    public static class ChatOutputTranslateConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean enabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean auto_translate = false;

        @ConfigEntry.Gui.Tooltip
        public String target_language = "Chinese";


        @ConfigEntry.Gui.Tooltip
        public boolean streaming_response = false;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public Provider llm_provider = Provider.OPENAI;

        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int max_concurrent_requests = 1;

        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public OpenaiApi openapi = new OpenaiApi();

        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public OllamaApi ollama = new OllamaApi();

        public static class OpenaiApi {
            @ConfigEntry.Gui.PrefixText
            public String baseUrl = "https://api.openai.com/v1";
            public String apiKey = "sk-xxxxxx";
            public String modelId = "gpt-4o";
            public double temperature = 0.7;
            public String system_prompt_suffix = "\\no_think";
            @ConfigEntry.Gui.Tooltip
            public List<CustomParameterEntry> custom_parameters = new ArrayList<>();
        }

        public static class OllamaApi {
            @ConfigEntry.Gui.PrefixText
            public String ollamaUrl = "http://localhost:11434";
            public String modelId = "qwen3:0.6b";
            public String keep_alive_time = "1m";
            public double temperature = 0.7;
            public String system_prompt_suffix = "\\no_think";
            @ConfigEntry.Gui.Tooltip
            public List<CustomParameterEntry> custom_parameters = new ArrayList<>();
        }
    }

    public static class ChatInputTranslateConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean enabled = false;

        @ConfigEntry.Gui.Tooltip
        public String target_language = "English";

        @ConfigEntry.Gui.Tooltip
        public boolean streaming_response = false;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public Provider llm_provider = Provider.OPENAI;

        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public OpenaiApi openapi = new OpenaiApi();

        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public OllamaApi ollama = new OllamaApi();

        public static class OpenaiApi {
            @ConfigEntry.Gui.PrefixText
            public String baseUrl = "https://api.openai.com/v1";
            public String apiKey = "sk-xxxxxx";
            public String modelId = "gpt-4o";
            public double temperature = 0.7;
            public String system_prompt_suffix = "\\no_think";
            @ConfigEntry.Gui.Tooltip
            public List<CustomParameterEntry> custom_parameters = new ArrayList<>();
        }

        public static class OllamaApi {
            @ConfigEntry.Gui.PrefixText
            public String ollamaUrl = "http://localhost:11434";
            public String modelId = "qwen3:0.6b";
            public String keep_alive_time = "1m";
            public double temperature = 0.7;
            public String system_prompt_suffix = "\\no_think";
            @ConfigEntry.Gui.Tooltip
            public List<CustomParameterEntry> custom_parameters = new ArrayList<>();
        }
    }
} 