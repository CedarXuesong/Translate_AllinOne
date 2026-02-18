package com.cedarxuesong.translate_allinone.utils.llmapi;

import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderType;
import com.cedarxuesong.translate_allinone.utils.config.pojos.CustomParameterEntry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static ProviderSettings fromProviderProfile(ApiProviderProfile profile) {
        if (profile == null) {
            return new ProviderSettings(null, null);
        }

        ApiProviderProfile.ModelSettings activeModelSettings = profile.getActiveModelSettings();
        ApiProviderType providerType = profile.type == null ? ApiProviderType.OPENAI_COMPAT : profile.type;
        String modelId = activeModelSettings == null ? profile.model_id : activeModelSettings.model_id;
        double temperature = activeModelSettings == null ? profile.temperature : activeModelSettings.temperature;
        String keepAlive = activeModelSettings == null ? profile.keep_alive_time : activeModelSettings.keep_alive_time;
        boolean structuredOutput = activeModelSettings == null
                ? profile.enable_structured_output_if_available
                : activeModelSettings.enable_structured_output_if_available;
        Map<String, Object> parameters = toParameterMap(activeModelSettings == null ? profile.custom_parameters : activeModelSettings.custom_parameters);

        if (modelId == null || modelId.isBlank()) {
            modelId = providerType == ApiProviderType.OLLAMA ? "qwen3:0.6b" : "gpt-4o";
        }
        if (keepAlive == null || keepAlive.isBlank()) {
            keepAlive = "1m";
        }

        if (providerType == ApiProviderType.OLLAMA) {
            Map<String, Object> options = new java.util.HashMap<>();
            options.put("temperature", temperature);
            options.putAll(parameters);
            OllamaSettings ollamaSettings = new OllamaSettings(
                    profile.base_url,
                    modelId,
                    keepAlive,
                    structuredOutput,
                    options
            );
            return fromOllama(ollamaSettings);
        }

        OpenAISettings openAISettings = new OpenAISettings(
                profile.base_url,
                profile.api_key,
                modelId,
                temperature,
                structuredOutput,
                parameters,
                providerType
        );
        return fromOpenAI(openAISettings);
    }

    public static Map<String, Object> toParameterMap(List<CustomParameterEntry> customParameters) {
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

            if (parameter.is_object || (parameter.children != null && !parameter.children.isEmpty())) {
                parameterMap.put(key, toParameterMap(parameter.children));
            } else {
                parameterMap.put(key, convertParameterValue(parameter.value));
            }
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
     * @param providerType OpenAI 接口类型（兼容 Chat Completions 或 Responses）
     */
    public static record OpenAISettings(
            String baseUrl,
            String apiKey,
            String modelId,
            double temperature,
            boolean enableStructuredOutputIfAvailable,
            Map<String, Object> customParameters,
            ApiProviderType providerType
    ) {

        public OpenAISettings {
            if (providerType == null || providerType == ApiProviderType.OLLAMA) {
                providerType = ApiProviderType.OPENAI_COMPAT;
            }
        }

        /**
         * 一个不含结构化输出开关的便捷构造函数（默认关闭）。
         */
        public OpenAISettings(String baseUrl, String apiKey, String modelId, double temperature, Map<String, Object> customParameters) {
            this(baseUrl, apiKey, modelId, temperature, false, customParameters, ApiProviderType.OPENAI_COMPAT);
        }

        /**
         * 一个不含自定义参数的便捷构造函数。
         */
        public OpenAISettings(String baseUrl, String apiKey, String modelId, double temperature) {
            this(baseUrl, apiKey, modelId, temperature, false, null, ApiProviderType.OPENAI_COMPAT);
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
