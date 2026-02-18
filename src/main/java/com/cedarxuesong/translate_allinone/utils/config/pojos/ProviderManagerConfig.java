package com.cedarxuesong.translate_allinone.utils.config.pojos;

import java.util.ArrayList;
import java.util.List;

public class ProviderManagerConfig {
    public static final String ROUTE_SEPARATOR = "::";

    public List<ApiProviderProfile> providers = new ArrayList<>();
    public Routes routes = new Routes();

    public ProviderManagerConfig() {
        ensureDefaults();
    }

    public void ensureDefaults() {
        if (providers == null) {
            providers = new ArrayList<>();
        }

        providers.removeIf(provider -> provider == null || provider.type == null || provider.id == null || provider.id.isBlank());
        for (ApiProviderProfile provider : providers) {
            provider.ensureModelSettings();
        }

        if (routes == null) {
            routes = new Routes();
        }

        routes.item = normalizeRouteValue(routes.item);
        routes.scoreboard = normalizeRouteValue(routes.scoreboard);
        routes.chat_input = normalizeRouteValue(routes.chat_input);
        routes.chat_output = normalizeRouteValue(routes.chat_output);
    }

    public ApiProviderProfile findById(String id) {
        if (id == null || id.isBlank() || providers == null) {
            return null;
        }

        for (ApiProviderProfile provider : providers) {
            if (provider != null && id.equals(provider.id)) {
                return provider;
            }
        }
        return null;
    }

    public static String composeRouteKey(String providerId, String modelId) {
        String p = providerId == null ? "" : providerId.trim();
        String m = modelId == null ? "" : modelId.trim();
        if (p.isEmpty() || m.isEmpty()) {
            return "";
        }
        return p + ROUTE_SEPARATOR + m;
    }

    public static String extractProviderId(String routeKey) {
        if (routeKey == null) {
            return "";
        }
        int separator = routeKey.indexOf(ROUTE_SEPARATOR);
        if (separator < 0) {
            return routeKey.trim();
        }
        return routeKey.substring(0, separator).trim();
    }

    public static String extractModelId(String routeKey) {
        if (routeKey == null) {
            return "";
        }
        int separator = routeKey.indexOf(ROUTE_SEPARATOR);
        if (separator < 0) {
            return "";
        }
        return routeKey.substring(separator + ROUTE_SEPARATOR.length()).trim();
    }

    private String normalizeRouteValue(String routeValue) {
        if (routeValue == null || routeValue.isBlank()) {
            return "";
        }

        String trimmed = routeValue.trim();
        String providerId = extractProviderId(trimmed);
        String modelId = extractModelId(trimmed);

        ApiProviderProfile provider = findById(providerId);
        if (provider == null) {
            return "";
        }

        provider.ensureModelSettings();
        if (modelId.isBlank()) {
            return composeRouteKey(provider.id, provider.model_id);
        }

        ApiProviderProfile.ModelSettings modelSettings = provider.getModelSettings(modelId);
        if (modelSettings == null) {
            return "";
        }

        return composeRouteKey(provider.id, modelSettings.model_id);
    }

    public static class Routes {
        public String item = "";
        public String scoreboard = "";
        public String chat_input = "";
        public String chat_output = "";
    }
}
