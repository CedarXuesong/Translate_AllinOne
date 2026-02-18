package com.cedarxuesong.translate_allinone.gui.configui.support;

import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ProviderManagerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProviderProfileSupport {
    private ProviderProfileSupport() {
    }

    public static String safeProviderName(ApiProviderProfile profile) {
        if (profile == null) {
            return "";
        }
        String name = sanitizeText(profile.name).trim();
        return name.isEmpty() ? profile.id : name;
    }

    public static List<String> normalizeModelIds(ApiProviderProfile profile) {
        profile.ensureModelSettings();
        return profile.model_ids == null ? new ArrayList<>() : profile.model_ids;
    }

    public static List<ApiProviderProfile> filterProviders(List<ApiProviderProfile> providers, String query) {
        List<ApiProviderProfile> result = new ArrayList<>();
        if (providers == null) {
            return result;
        }

        String q = sanitizeText(query).trim().toLowerCase(Locale.ROOT);
        for (ApiProviderProfile profile : providers) {
            if (profile == null) {
                continue;
            }
            if (q.isEmpty()) {
                result.add(profile);
                continue;
            }

            String name = sanitizeText(profile.name).toLowerCase(Locale.ROOT);
            String id = sanitizeText(profile.id).toLowerCase(Locale.ROOT);
            if (name.contains(q) || id.contains(q)) {
                result.add(profile);
            }
        }
        return result;
    }

    public static SelectedProvider resolveSelectedProvider(
            ProviderManagerConfig providerManager,
            String selectedProviderId,
            int selectedProviderIndex
    ) {
        if (providerManager.providers == null || providerManager.providers.isEmpty()) {
            return new SelectedProvider(null, "");
        }

        String resolvedId = selectedProviderId;
        if (resolvedId == null || resolvedId.isBlank() || providerManager.findById(resolvedId) == null) {
            int boundedIndex = Math.max(0, Math.min(selectedProviderIndex, providerManager.providers.size() - 1));
            resolvedId = providerManager.providers.get(boundedIndex).id;
        }

        ApiProviderProfile selected = providerManager.findById(resolvedId);
        if (selected == null) {
            selected = providerManager.providers.get(0);
            resolvedId = selected.id;
        }
        return new SelectedProvider(selected, resolvedId);
    }

    public static String sanitizeText(String value) {
        return value == null ? "" : value;
    }

    public record SelectedProvider(ApiProviderProfile profile, String selectedProviderId) {
    }
}
