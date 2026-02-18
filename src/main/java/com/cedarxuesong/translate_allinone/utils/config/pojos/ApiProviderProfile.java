package com.cedarxuesong.translate_allinone.utils.config.pojos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiProviderProfile {
    public String id = "provider";
    public String name = "Provider";
    public boolean enabled = true;

    public ApiProviderType type = ApiProviderType.OPENAI_COMPAT;
    public String base_url = "https://api.openai.com/v1";
    public String api_key = "";
    public String model_id = "gpt-4o";
    public List<String> model_ids = new ArrayList<>(List.of("gpt-4o"));
    public List<ModelSettings> model_settings = new ArrayList<>(List.of(ModelSettings.openAiDefault("gpt-4o")));

    // Legacy fields kept for backward compatibility.
    public double temperature = 0.7;
    public String keep_alive_time = "1m";
    public boolean enable_structured_output_if_available = false;
    public boolean supports_system_message = true;
    public boolean inject_system_prompt_into_user_message = true;
    public String system_prompt_suffix = "\\no_think";
    public List<CustomParameterEntry> custom_parameters = new ArrayList<>();

    public static ApiProviderProfile createOpenAiDefault() {
        ApiProviderProfile profile = new ApiProviderProfile();
        profile.id = "openai_default";
        profile.name = "OpenAI Default";
        profile.type = ApiProviderType.OPENAI_COMPAT;
        profile.base_url = "https://api.openai.com/v1";
        profile.model_id = "gpt-4o";
        profile.model_ids = new ArrayList<>(List.of("gpt-4o"));
        profile.model_settings = new ArrayList<>(List.of(ModelSettings.openAiDefault("gpt-4o")));
        return profile;
    }

    public static ApiProviderProfile createOllamaDefault() {
        ApiProviderProfile profile = new ApiProviderProfile();
        profile.id = "ollama_default";
        profile.name = "Ollama Default";
        profile.type = ApiProviderType.OLLAMA;
        profile.base_url = "http://localhost:11434";
        profile.model_id = "qwen3:0.6b";
        profile.model_ids = new ArrayList<>(List.of("qwen3:0.6b"));
        profile.model_settings = new ArrayList<>(List.of(ModelSettings.ollamaDefault("qwen3:0.6b")));
        return profile;
    }

    public List<ModelSettings> ensureModelSettings() {
        Map<String, ModelSettings> normalized = new LinkedHashMap<>();

        if (model_settings != null) {
            for (ModelSettings settings : model_settings) {
                if (settings == null) {
                    continue;
                }
                String key = normalizeModelId(settings.model_id);
                if (key.isEmpty()) {
                    continue;
                }
                normalized.putIfAbsent(key, normalizeModelSettings(settings, key));
            }
        }

        if (model_ids != null) {
            for (String model : model_ids) {
                String key = normalizeModelId(model);
                if (key.isEmpty()) {
                    continue;
                }
                normalized.putIfAbsent(key, createDefaultModelSettings(key));
            }
        }

        String activeModelId = normalizeModelId(model_id);
        if (!activeModelId.isEmpty()) {
            normalized.putIfAbsent(activeModelId, createDefaultModelSettings(activeModelId));
        }

        if (normalized.isEmpty()) {
            model_settings = new ArrayList<>();
            model_ids = new ArrayList<>();
            model_id = "";
            return model_settings;
        }

        if (activeModelId.isEmpty() || !normalized.containsKey(activeModelId)) {
            activeModelId = normalized.keySet().iterator().next();
        }

        model_settings = new ArrayList<>(normalized.values());
        model_ids = new ArrayList<>(normalized.keySet());
        model_id = activeModelId;

        ModelSettings active = normalized.get(activeModelId);
        syncLegacyFieldsFrom(active);
        return model_settings;
    }

    public ModelSettings getModelSettings(String id) {
        String normalizedId = normalizeModelId(id);
        if (normalizedId.isEmpty()) {
            return null;
        }

        for (ModelSettings settings : ensureModelSettings()) {
            if (normalizedId.equals(settings.model_id)) {
                return settings;
            }
        }
        return null;
    }

    public ModelSettings getActiveModelSettings() {
        String activeModelId = normalizeModelId(model_id);
        if (activeModelId.isEmpty()) {
            return null;
        }
        return getModelSettings(activeModelId);
    }

    public boolean activeSupportsSystemMessage() {
        ModelSettings settings = getActiveModelSettings();
        return settings == null ? supports_system_message : settings.supports_system_message;
    }

    public boolean activeInjectSystemPromptIntoUserMessage() {
        ModelSettings settings = getActiveModelSettings();
        return settings == null ? inject_system_prompt_into_user_message : settings.inject_system_prompt_into_user_message;
    }

    public boolean activeStructuredOutputEnabled() {
        ModelSettings settings = getActiveModelSettings();
        return settings == null ? enable_structured_output_if_available : settings.enable_structured_output_if_available;
    }

    public String activeSystemPromptSuffix() {
        ModelSettings settings = getActiveModelSettings();
        return settings == null ? system_prompt_suffix : settings.system_prompt_suffix;
    }

    public double activeTemperature() {
        ModelSettings settings = getActiveModelSettings();
        return settings == null ? temperature : settings.temperature;
    }

    public String activeKeepAliveTime() {
        ModelSettings settings = getActiveModelSettings();
        return settings == null ? keep_alive_time : settings.keep_alive_time;
    }

    public List<CustomParameterEntry> activeCustomParameters() {
        ModelSettings settings = getActiveModelSettings();
        return settings == null ? copyCustomParameters(custom_parameters) : copyCustomParameters(settings.custom_parameters);
    }

    private void syncLegacyFieldsFrom(ModelSettings active) {
        if (active == null) {
            return;
        }
        temperature = active.temperature;
        keep_alive_time = normalizeKeepAlive(active.keep_alive_time);
        enable_structured_output_if_available = active.enable_structured_output_if_available;
        supports_system_message = active.supports_system_message;
        inject_system_prompt_into_user_message = active.inject_system_prompt_into_user_message;
        system_prompt_suffix = normalizeSuffix(active.system_prompt_suffix);
        custom_parameters = copyCustomParameters(active.custom_parameters);
    }

    private ModelSettings normalizeModelSettings(ModelSettings source, String normalizedId) {
        ModelSettings settings = new ModelSettings();
        settings.model_id = normalizedId;
        settings.temperature = source.temperature;
        settings.keep_alive_time = normalizeKeepAlive(source.keep_alive_time);
        settings.enable_structured_output_if_available = source.enable_structured_output_if_available;
        settings.supports_system_message = source.supports_system_message;
        settings.inject_system_prompt_into_user_message = source.inject_system_prompt_into_user_message;
        settings.system_prompt_suffix = normalizeSuffix(source.system_prompt_suffix);
        settings.custom_parameters = copyCustomParameters(source.custom_parameters);
        return settings;
    }

    private ModelSettings createDefaultModelSettings(String modelId) {
        ModelSettings settings = new ModelSettings();
        settings.model_id = modelId;
        settings.temperature = temperature;
        settings.keep_alive_time = normalizeKeepAlive(keep_alive_time);
        settings.enable_structured_output_if_available = enable_structured_output_if_available;
        settings.supports_system_message = supports_system_message;
        settings.inject_system_prompt_into_user_message = inject_system_prompt_into_user_message;
        settings.system_prompt_suffix = normalizeSuffix(system_prompt_suffix);
        settings.custom_parameters = copyCustomParameters(custom_parameters);
        return settings;
    }

    private String normalizeModelId(String modelIdRaw) {
        return modelIdRaw == null ? "" : modelIdRaw.trim();
    }

    private String normalizeKeepAlive(String keepAliveRaw) {
        String value = keepAliveRaw == null ? "" : keepAliveRaw.trim();
        return value.isEmpty() ? "1m" : value;
    }

    private String normalizeSuffix(String suffixRaw) {
        return suffixRaw == null ? "" : suffixRaw;
    }

    private List<CustomParameterEntry> copyCustomParameters(List<CustomParameterEntry> source) {
        return CustomParameterEntry.deepCopyList(source);
    }

    public static class ModelSettings {
        public String model_id = "gpt-4o";
        public double temperature = 0.7;
        public String keep_alive_time = "1m";
        public boolean enable_structured_output_if_available = false;
        public boolean supports_system_message = true;
        public boolean inject_system_prompt_into_user_message = true;
        public String system_prompt_suffix = "\\no_think";
        public List<CustomParameterEntry> custom_parameters = new ArrayList<>();

        public static ModelSettings openAiDefault(String modelId) {
            ModelSettings settings = new ModelSettings();
            settings.model_id = modelId;
            settings.keep_alive_time = "1m";
            return settings;
        }

        public static ModelSettings ollamaDefault(String modelId) {
            ModelSettings settings = new ModelSettings();
            settings.model_id = modelId;
            settings.keep_alive_time = "1m";
            return settings;
        }
    }
}
