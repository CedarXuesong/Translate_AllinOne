package com.cedarxuesong.translate_allinone.gui.configui.support;

import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.CustomParameterEntry;

import java.util.List;

public final class ModelSettingsMutationSupport {
    private ModelSettingsMutationSupport() {
    }

    public static boolean hasModelIdConflict(ApiProviderProfile profile, String nextModelId, String originalModelId) {
        profile.ensureModelSettings();
        boolean creating = originalModelId == null || originalModelId.isBlank();
        ApiProviderProfile.ModelSettings conflict = profile.getModelSettings(nextModelId);
        return conflict != null && (creating || !nextModelId.equals(originalModelId));
    }

    public static void upsertModelSettings(
            ApiProviderProfile profile,
            String originalModelId,
            String nextModelId,
            double temperature,
            String keepAliveTime,
            boolean supportsSystemMessage,
            boolean injectPromptIntoUser,
            boolean structuredOutput,
            String systemPromptSuffix,
            List<CustomParameterEntry> customParameters,
            boolean setAsDefault
    ) {
        profile.ensureModelSettings();
        boolean creating = originalModelId == null || originalModelId.isBlank();

        ApiProviderProfile.ModelSettings settings = creating ? null : profile.getModelSettings(originalModelId);
        if (settings == null) {
            settings = new ApiProviderProfile.ModelSettings();
            profile.model_settings.add(settings);
        }

        settings.model_id = nextModelId;
        settings.temperature = temperature;
        settings.keep_alive_time = keepAliveTime;
        settings.supports_system_message = supportsSystemMessage;
        settings.inject_system_prompt_into_user_message = injectPromptIntoUser;
        settings.enable_structured_output_if_available = structuredOutput;
        settings.system_prompt_suffix = systemPromptSuffix;
        settings.custom_parameters = CustomParameterEntry.deepCopyList(customParameters);

        if (creating || setAsDefault || profile.model_id == null || profile.model_id.isBlank() || profile.model_id.equals(originalModelId)) {
            profile.model_id = nextModelId;
        }

        profile.ensureModelSettings();
    }

    public static boolean removeModel(ApiProviderProfile profile, String modelId) {
        profile.ensureModelSettings();
        boolean removed = profile.model_settings.removeIf(settings -> settings != null && modelId.equals(settings.model_id));
        if (!removed) {
            return false;
        }

        profile.model_ids.removeIf(existing -> modelId.equals(existing));
        if (modelId.equals(profile.model_id)) {
            profile.model_id = "";
        }
        profile.ensureModelSettings();
        return true;
    }
}
