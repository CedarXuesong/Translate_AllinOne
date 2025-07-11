package com.cedarxuesong.translate_allinone.utils.llmapi;

import java.util.Map;
import java.util.stream.Collectors;

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
            Map<String, Object> customParams = config.openapi.custom_parameters.stream()
                    .collect(Collectors.toMap(p -> p.key, p -> convertParameterValue(p.value)));
            OpenAISettings settings = new OpenAISettings(
                    config.openapi.baseUrl,
                    config.openapi.apiKey,
                    config.openapi.modelId,
                    config.openapi.temperature,
                    customParams
            );
            return new ProviderSettings(settings, null);
        } else if (config.llm_provider == Provider.OLLAMA) {
            Map<String, Object> options = new java.util.HashMap<>();
            options.put("temperature", config.ollama.temperature);
            if (config.ollama.custom_parameters != null) {
                config.ollama.custom_parameters.forEach(p -> options.put(p.key, convertParameterValue(p.value)));
            }
            
            OllamaSettings settings = new OllamaSettings(
                    config.ollama.ollamaUrl,
                    config.ollama.modelId,
                    config.ollama.keep_alive_time,
                    options
            );
            return new ProviderSettings(null, settings);
        }
        return new ProviderSettings(null, null);
    }

    public static ProviderSettings fromScoreboardConfig(ScoreboardConfig config) {
        if (config.llm_provider == Provider.OPENAI) {
            Map<String, Object> customParams = config.openapi.custom_parameters.stream()
                    .collect(Collectors.toMap(p -> p.key, p -> convertParameterValue(p.value)));
            OpenAISettings settings = new OpenAISettings(
                    config.openapi.baseUrl,
                    config.openapi.apiKey,
                    config.openapi.modelId,
                    config.openapi.temperature,
                    customParams
            );
            return new ProviderSettings(settings, null);
        } else if (config.llm_provider == Provider.OLLAMA) {
            Map<String, Object> options = new java.util.HashMap<>();
            options.put("temperature", config.ollama.temperature);
            if (config.ollama.custom_parameters != null) {
                config.ollama.custom_parameters.forEach(p -> options.put(p.key, convertParameterValue(p.value)));
            }
            
            OllamaSettings settings = new OllamaSettings(
                    config.ollama.ollamaUrl,
                    config.ollama.modelId,
                    config.ollama.keep_alive_time,
                    options
            );
            return new ProviderSettings(null, settings);
        }
        return new ProviderSettings(null, null);
    }

    public static ProviderSettings fromChatConfig(ChatTranslateConfig config) {
        if (config.llm_provider == Provider.OLLAMA) {
            ChatTranslateConfig.OllamaApi ollama = config.ollama;
            Map<String, Object> options = new java.util.HashMap<>();
            options.put("temperature", ollama.temperature);
            if (ollama.custom_parameters != null) {
                ollama.custom_parameters.forEach(p -> options.put(p.key, convertParameterValue(p.value)));
            }
            ProviderSettings.OllamaSettings settings = new ProviderSettings.OllamaSettings(ollama.ollamaUrl, ollama.modelId, ollama.keep_alive_time, options);
            return ProviderSettings.fromOllama(settings);
        } else {
            ChatTranslateConfig.OpenaiApi openai = config.openapi;
            Map<String, Object> customParams = openai.custom_parameters.stream()
                    .collect(Collectors.toMap(entry -> entry.key, entry -> convertParameterValue(entry.value)));
            ProviderSettings.OpenAISettings settings = new ProviderSettings.OpenAISettings(openai.baseUrl, openai.apiKey, openai.modelId, openai.temperature, customParams);
            return ProviderSettings.fromOpenAI(settings);
        }
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
     * @param customParameters 可选的自定义参数，会被添加到请求体中
     */
    public static record OpenAISettings(String baseUrl, String apiKey, String modelId, double temperature, Map<String, Object> customParameters) {

        /**
         * 一个不含自定义参数的便捷构造函数。
         */
        public OpenAISettings(String baseUrl, String apiKey, String modelId, double temperature) {
            this(baseUrl, apiKey, modelId, temperature, null);
        }
    }

    /**
     * Ollama API 的相关设置
     * @param baseUrl API的基地址, 例如 "http://localhost:11434"
     * @param modelId 使用的模型ID
     * @param keepAlive 模型在内存中保持加载的时间 (例如 "5m")
     * @param options 额外的模型参数 (例如 temperature, top_p)
     */
    public static record OllamaSettings(String baseUrl, String modelId, String keepAlive, Map<String, Object> options) {
        /**
         * 一个使用默认keepAlive且不含自定义参数的便捷构造函数。
         */
        public OllamaSettings(String baseUrl, String modelId) {
            this(baseUrl, modelId, "5m", null);
        }
    }
} 