package com.cedarxuesong.translate_allinone.utils.llmapi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cedarxuesong.translate_allinone.utils.config.pojos.ChatTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.CustomParameterEntry;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ItemTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.Provider;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ScoreboardConfig;

/**
 * 一个用于封装不同翻译提供商API设置的记录。
 * 这是一个不可变的数据结构。
 */
public record ProviderSettings(OpenAISettings openAISettings, OllamaSettings ollamaSettings) {

    public static ProviderSettings fromOpenAI(OpenAISettings settings) {
        return new ProviderSettings(settings, null);
    }

    public static ProviderSettings fromOllama(OllamaSettings settings) {
        return new ProviderSettings(null, settings);
    }

    public static ProviderSettings fromItemConfig(ItemTranslateConfig config) {
        if (config.llm_provider == Provider.OPENAI) {
            Map<String, Object> customParams = toParameterMap(config.openapi.custom_parameters);
            OpenAISettings settings = new OpenAISettings(
                    config.openapi.baseUrl,
                    config.openapi.apiKey,
                    config.openapi.modelId,
                    config.openapi.temperature,
                    config.openapi.enable_structured_output_if_available,
                    customParams
            );
            return new ProviderSettings(settings, null);
        } else if (config.llm_provider == Provider.OLLAMA) {
            Map<String, Object> options = new java.util.HashMap<>();
            options.put("temperature", config.ollama.temperature);
            options.putAll(toParameterMap(config.ollama.custom_parameters));
            
            OllamaSettings settings = new OllamaSettings(
                    config.ollama.ollamaUrl,
                    config.ollama.modelId,
                    config.ollama.keep_alive_time,
                    config.ollama.enable_structured_output_if_available,
                    options
            );
            return new ProviderSettings(null, settings);
        }
        return new ProviderSettings(null, null);
    }

    public static ProviderSettings fromScoreboardConfig(ScoreboardConfig config) {
        if (config.llm_provider == Provider.OPENAI) {
            Map<String, Object> customParams = toParameterMap(config.openapi.custom_parameters);
            OpenAISettings settings = new OpenAISettings(
                    config.openapi.baseUrl,
                    config.openapi.apiKey,
                    config.openapi.modelId,
                    config.openapi.temperature,
                    config.openapi.enable_structured_output_if_available,
                    customParams
            );
            return new ProviderSettings(settings, null);
        } else if (config.llm_provider == Provider.OLLAMA) {
            Map<String, Object> options = new java.util.HashMap<>();
            options.put("temperature", config.ollama.temperature);
            options.putAll(toParameterMap(config.ollama.custom_parameters));
            
            OllamaSettings settings = new OllamaSettings(
                    config.ollama.ollamaUrl,
                    config.ollama.modelId,
                    config.ollama.keep_alive_time,
                    config.ollama.enable_structured_output_if_available,
                    options
            );
            return new ProviderSettings(null, settings);
        }
        return new ProviderSettings(null, null);
    }

    public static ProviderSettings fromChatInputConfig(ChatTranslateConfig.ChatInputTranslateConfig config) {
        if (config.llm_provider == Provider.OLLAMA) {
            ChatTranslateConfig.ChatInputTranslateConfig.OllamaApi ollama = config.ollama;
            Map<String, Object> options = new java.util.HashMap<>();
            options.put("temperature", ollama.temperature);
            options.putAll(toParameterMap(ollama.custom_parameters));
            ProviderSettings.OllamaSettings settings = new ProviderSettings.OllamaSettings(
                    ollama.ollamaUrl,
                    ollama.modelId,
                    ollama.keep_alive_time,
                    ollama.enable_structured_output_if_available,
                    options
            );
            return ProviderSettings.fromOllama(settings);
        } else {
            ChatTranslateConfig.ChatInputTranslateConfig.OpenaiApi openai = config.openapi;
            Map<String, Object> customParams = toParameterMap(openai.custom_parameters);
            ProviderSettings.OpenAISettings settings = new ProviderSettings.OpenAISettings(
                    openai.baseUrl,
                    openai.apiKey,
                    openai.modelId,
                    openai.temperature,
                    openai.enable_structured_output_if_available,
                    customParams
            );
            return ProviderSettings.fromOpenAI(settings);
        }
    }

    public static ProviderSettings fromChatOutputConfig(ChatTranslateConfig.ChatOutputTranslateConfig config) {
        if (config.llm_provider == Provider.OLLAMA) {
            ChatTranslateConfig.ChatOutputTranslateConfig.OllamaApi ollama = config.ollama;
            Map<String, Object> options = new java.util.HashMap<>();
            options.put("temperature", ollama.temperature);
            options.putAll(toParameterMap(ollama.custom_parameters));
            ProviderSettings.OllamaSettings settings = new ProviderSettings.OllamaSettings(
                    ollama.ollamaUrl,
                    ollama.modelId,
                    ollama.keep_alive_time,
                    ollama.enable_structured_output_if_available,
                    options
            );
            return ProviderSettings.fromOllama(settings);
        } else {
            ChatTranslateConfig.ChatOutputTranslateConfig.OpenaiApi openai = config.openapi;
            Map<String, Object> customParams = toParameterMap(openai.custom_parameters);
            ProviderSettings.OpenAISettings settings = new ProviderSettings.OpenAISettings(
                    openai.baseUrl,
                    openai.apiKey,
                    openai.modelId,
                    openai.temperature,
                    openai.enable_structured_output_if_available,
                    customParams
            );
            return ProviderSettings.fromOpenAI(settings);
        }
    }

    private static Map<String, Object> toParameterMap(List<CustomParameterEntry> customParameters) {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        if (customParameters == null) {
            return parameterMap;
        }

        for (CustomParameterEntry parameter : customParameters) {
            if (parameter == null || parameter.key == null) {
                continue;
            }

            String key = parameter.key.trim();
            if (key.isEmpty()) {
                continue;
            }

            parameterMap.put(key, convertParameterValue(parameter.value));
        }
        return parameterMap;
    }

    private static Object convertParameterValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        if (trimmedValue.equalsIgnoreCase("true")) return true;
        if (trimmedValue.equalsIgnoreCase("false")) return false;
        try {
            return Integer.parseInt(trimmedValue);
        } catch (NumberFormatException e1) {
            try {
                return Double.parseDouble(trimmedValue);
            } catch (NumberFormatException e2) {
                return trimmedValue;
            }
        }
    }

    /**
     * OpenAI API 的相关设置
     * @param baseUrl API的基地址
     * @param apiKey API密钥
     * @param modelId 使用的模型ID
     * @param temperature 模型温度
     * @param enableStructuredOutputIfAvailable 是否启用结构化输出（如果可用）
     * @param customParameters 可选的自定义参数，会被添加到请求体中
     */
    public static record OpenAISettings(String baseUrl, String apiKey, String modelId, double temperature, boolean enableStructuredOutputIfAvailable, Map<String, Object> customParameters) {

        /**
         * 一个不含结构化输出开关的便捷构造函数（默认关闭）。
         */
        public OpenAISettings(String baseUrl, String apiKey, String modelId, double temperature, Map<String, Object> customParameters) {
            this(baseUrl, apiKey, modelId, temperature, false, customParameters);
        }

        /**
         * 一个不含自定义参数的便捷构造函数。
         */
        public OpenAISettings(String baseUrl, String apiKey, String modelId, double temperature) {
            this(baseUrl, apiKey, modelId, temperature, false, null);
        }
    }

    /**
     * Ollama API 的相关设置
     * @param baseUrl API的基地址, 例如 "http://localhost:11434"
     * @param modelId 使用的模型ID
     * @param keepAlive 模型在内存中保持加载的时间 (例如 "5m")
     * @param enableStructuredOutputIfAvailable 是否启用结构化输出（如果可用）
     * @param options 额外的模型参数 (例如 temperature, top_p)
     */
    public static record OllamaSettings(String baseUrl, String modelId, String keepAlive, boolean enableStructuredOutputIfAvailable, Map<String, Object> options) {
        /**
         * 一个不含结构化输出开关的便捷构造函数（默认关闭）。
         */
        public OllamaSettings(String baseUrl, String modelId, String keepAlive, Map<String, Object> options) {
            this(baseUrl, modelId, keepAlive, false, options);
        }

        /**
         * 一个使用默认keepAlive且不含自定义参数的便捷构造函数。
         */
        public OllamaSettings(String baseUrl, String modelId) {
            this(baseUrl, modelId, "5m", false, null);
        }
    }
}
