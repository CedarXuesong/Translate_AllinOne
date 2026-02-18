package com.cedarxuesong.translate_allinone.gui.configui.support;

import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderType;
import com.cedarxuesong.translate_allinone.utils.config.pojos.CustomParameterEntry;

import java.util.ArrayList;
import java.util.List;

public final class ModelSettingsDraftSupport {
    private ModelSettingsDraftSupport() {
    }

    public static Draft fromProfile(ApiProviderProfile profile, String originalModelId) {
        profile.ensureModelSettings();
        String resolvedOriginalId = ProviderProfileSupport.sanitizeText(originalModelId).trim();

        ApiProviderProfile.ModelSettings sourceSettings = profile.getModelSettings(resolvedOriginalId);
        if (sourceSettings == null) {
            sourceSettings = profile.getActiveModelSettings();
        }
        if (sourceSettings == null) {
            sourceSettings = profile.type == ApiProviderType.OLLAMA
                    ? ApiProviderProfile.ModelSettings.ollamaDefault("qwen3:0.6b")
                    : ApiProviderProfile.ModelSettings.openAiDefault("gpt-4o");
        }

        List<CustomParameterEntry> customParameters = CustomParameterEntry.deepCopyList(sourceSettings.custom_parameters);
        return new Draft(
                profile.id,
                resolvedOriginalId,
                resolvedOriginalId.isBlank() ? "" : ProviderProfileSupport.sanitizeText(sourceSettings.model_id),
                ModelSettingsValueSupport.formatTemperature(sourceSettings.temperature),
                ProviderProfileSupport.sanitizeText(sourceSettings.keep_alive_time),
                sourceSettings.supports_system_message,
                sourceSettings.inject_system_prompt_into_user_message,
                sourceSettings.enable_structured_output_if_available,
                ProviderProfileSupport.sanitizeText(sourceSettings.system_prompt_suffix),
                customParameters,
                CustomParameterEntry.deepCopyList(customParameters),
                resolvedOriginalId.isBlank() || resolvedOriginalId.equals(profile.model_id)
        );
    }

    public static Draft empty() {
        return new Draft(
                "",
                "",
                "",
                "",
                "",
                false,
                true,
                false,
                "",
                new ArrayList<>(),
                new ArrayList<>(),
                false
        );
    }

    public record Draft(
            String providerId,
            String originalModelId,
            String modelIdDraft,
            String temperatureDraft,
            String keepAliveDraft,
            boolean supportsSystem,
            boolean injectPromptIntoUser,
            boolean structuredOutput,
            String systemPromptSuffixDraft,
            List<CustomParameterEntry> customParametersDraft,
            List<CustomParameterEntry> customParametersBackup,
            boolean setDefault
    ) {
    }
}
