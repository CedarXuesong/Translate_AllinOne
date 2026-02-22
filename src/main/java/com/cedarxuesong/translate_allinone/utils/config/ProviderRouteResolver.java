package com.cedarxuesong.translate_allinone.utils.config;

import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.CustomParameterEntry;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ProviderManagerConfig;

import java.util.ArrayList;
import java.util.List;

public final class ProviderRouteResolver {
    private ProviderRouteResolver() {
    }

    public enum Route {
        ITEM,
        SCOREBOARD,
        CHAT_INPUT,
        CHAT_OUTPUT
    }

    public static ApiProviderProfile resolve(ModConfig config, Route route) {
        if (config == null || config.providerManager == null) {
            return null;
        }

        ProviderManagerConfig manager = config.providerManager;
        manager.ensureDefaults();

        String routeKey = switch (route) {
            case ITEM -> manager.routes.item;
            case SCOREBOARD -> manager.routes.scoreboard;
            case CHAT_INPUT -> manager.routes.chat_input;
            case CHAT_OUTPUT -> manager.routes.chat_output;
        };

        if (routeKey == null || routeKey.isBlank()) {
            return null;
        }

        String providerId = ProviderManagerConfig.extractProviderId(routeKey);
        String modelId = ProviderManagerConfig.extractModelId(routeKey);
        if (providerId.isBlank() || modelId.isBlank()) {
            return null;
        }

        ApiProviderProfile provider = manager.findById(providerId);
        if (provider == null || !provider.enabled) {
            return null;
        }

        provider.ensureModelSettings();
        ApiProviderProfile.ModelSettings modelSettings = provider.getModelSettings(modelId);
        if (modelSettings == null) {
            return null;
        }

        return snapshotForModel(provider, modelSettings);
    }

    private static ApiProviderProfile snapshotForModel(ApiProviderProfile source, ApiProviderProfile.ModelSettings modelSettings) {
        ApiProviderProfile snapshot = new ApiProviderProfile();

        snapshot.id = source.id;
        snapshot.name = source.name;
        snapshot.enabled = source.enabled;
        snapshot.type = source.type;
        snapshot.base_url = source.base_url;
        snapshot.api_key = source.api_key;

        snapshot.model_id = modelSettings.model_id;
        snapshot.model_ids = new ArrayList<>(1);
        snapshot.model_ids.add(modelSettings.model_id);

        List<CustomParameterEntry> customParametersCopy = cloneCustomParameters(modelSettings.custom_parameters);

        ApiProviderProfile.ModelSettings modelCopy = new ApiProviderProfile.ModelSettings();
        modelCopy.model_id = modelSettings.model_id;
        modelCopy.temperature = modelSettings.temperature;
        modelCopy.keep_alive_time = modelSettings.keep_alive_time;
        modelCopy.enable_structured_output_if_available = modelSettings.enable_structured_output_if_available;
        modelCopy.supports_system_message = modelSettings.supports_system_message;
        modelCopy.inject_system_prompt_into_user_message = modelSettings.inject_system_prompt_into_user_message;
        modelCopy.system_prompt_suffix = modelSettings.system_prompt_suffix;
        modelCopy.custom_parameters = customParametersCopy;
        snapshot.model_settings = new ArrayList<>(1);
        snapshot.model_settings.add(modelCopy);

        snapshot.temperature = modelCopy.temperature;
        snapshot.keep_alive_time = modelCopy.keep_alive_time;
        snapshot.enable_structured_output_if_available = modelCopy.enable_structured_output_if_available;
        snapshot.supports_system_message = modelCopy.supports_system_message;
        snapshot.inject_system_prompt_into_user_message = modelCopy.inject_system_prompt_into_user_message;
        snapshot.system_prompt_suffix = modelCopy.system_prompt_suffix;
        snapshot.custom_parameters = customParametersCopy;
        return snapshot;
    }

    private static List<CustomParameterEntry> cloneCustomParameters(List<CustomParameterEntry> source) {
        return CustomParameterEntry.deepCopyList(source);
    }
}
