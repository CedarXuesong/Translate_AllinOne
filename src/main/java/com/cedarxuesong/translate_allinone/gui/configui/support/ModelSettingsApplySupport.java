package com.cedarxuesong.translate_allinone.gui.configui.support;

import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.CustomParameterEntry;

import java.util.List;

public final class ModelSettingsApplySupport {
    private ModelSettingsApplySupport() {
    }

    public static ApplyResult apply(
            ApiProviderProfile profile,
            String originalModelId,
            String modelIdDraft,
            String temperatureDraft,
            String keepAliveDraft,
            boolean supportsSystem,
            boolean injectPromptIntoUser,
            boolean structuredOutput,
            String systemPromptSuffixDraft,
            List<CustomParameterEntry> customParametersDraft,
            boolean setDefault
    ) {
        String nextModelId = ProviderProfileSupport.sanitizeText(modelIdDraft).trim();
        if (nextModelId.isEmpty()) {
            return ApplyResult.error("error.model_id_required", null);
        }

        Double parsedTemperature = ModelSettingsValueSupport.parseTemperatureInput(temperatureDraft);
        if (parsedTemperature == null) {
            return ApplyResult.error("error.temperature_invalid", null);
        }

        if (ModelSettingsMutationSupport.hasModelIdConflict(profile, nextModelId, originalModelId)) {
            return ApplyResult.error("error.model_id_exists", nextModelId);
        }

        boolean creating = originalModelId == null || originalModelId.isBlank();
        ModelSettingsMutationSupport.upsertModelSettings(
                profile,
                originalModelId,
                nextModelId,
                parsedTemperature,
                ModelSettingsValueSupport.normalizeKeepAliveInput(keepAliveDraft),
                supportsSystem,
                injectPromptIntoUser,
                structuredOutput,
                ProviderProfileSupport.sanitizeText(systemPromptSuffixDraft),
                customParametersDraft,
                setDefault
        );
        return ApplyResult.success(creating, nextModelId);
    }

    public record ApplyResult(boolean success, boolean creating, String modelId, String errorKey, String errorArg) {
        public static ApplyResult success(boolean creating, String modelId) {
            return new ApplyResult(true, creating, modelId, null, null);
        }

        public static ApplyResult error(String errorKey, String errorArg) {
            return new ApplyResult(false, false, "", errorKey, errorArg);
        }
    }
}
