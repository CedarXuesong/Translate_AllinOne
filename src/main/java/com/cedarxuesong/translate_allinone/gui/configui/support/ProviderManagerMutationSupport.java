package com.cedarxuesong.translate_allinone.gui.configui.support;

import com.cedarxuesong.translate_allinone.gui.configui.sections.RouteModelSectionSupport;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderType;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ProviderManagerConfig;

public final class ProviderManagerMutationSupport {
    private ProviderManagerMutationSupport() {
    }

    public static AddProviderResult addProviderFromDraft(
            ProviderManagerConfig providerManager,
            String providerNameDraft,
            ApiProviderType providerType
    ) {
        String providerName = ProviderProfileSupport.sanitizeText(providerNameDraft).trim();
        if (providerName.isEmpty()) {
            providerName = ProviderEditorSupport.defaultProviderName(providerType);
        }

        ApiProviderProfile profile = ProviderEditorSupport.createProfileByType(providerType);
        profile.name = providerName;
        profile.id = ProviderEditorSupport.generateProviderId(providerManager, providerName, providerType);
        profile.ensureModelSettings();

        providerManager.providers.add(profile);
        int selectedProviderIndex = providerManager.providers.size() - 1;
        return new AddProviderResult(profile, profile.id, selectedProviderIndex);
    }

    public static DeleteProviderResult deleteProvider(
            ProviderManagerConfig providerManager,
            String selectedProviderId,
            ApiProviderProfile profile
    ) {
        String removedId = profile.id;
        providerManager.providers.removeIf(candidate -> candidate != null && removedId.equals(candidate.id));
        RouteModelSectionSupport.clearRouteIfMatched(providerManager, removedId);

        String nextSelectedProviderId = removedId.equals(selectedProviderId) ? "" : selectedProviderId;
        return new DeleteProviderResult(removedId, nextSelectedProviderId);
    }

    public record AddProviderResult(ApiProviderProfile profile, String selectedProviderId, int selectedProviderIndex) {
    }

    public record DeleteProviderResult(String removedProviderId, String selectedProviderId) {
    }
}
