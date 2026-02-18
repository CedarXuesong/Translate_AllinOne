package com.cedarxuesong.translate_allinone.gui.configui.support;

import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderType;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ProviderManagerConfig;

import java.util.Locale;

public final class ProviderEditorSupport {
    private ProviderEditorSupport() {
    }

    public static String defaultProviderName(ApiProviderType type) {
        return switch (type) {
            case OPENAI_COMPAT -> "OpenAI";
            case OPENAI_RESPONSE -> "OpenAI-Response";
            case OLLAMA -> "Ollama";
        };
    }

    public static ApiProviderProfile createProfileByType(ApiProviderType type) {
        ApiProviderProfile profile = type == ApiProviderType.OLLAMA
                ? ApiProviderProfile.createOllamaDefault()
                : ApiProviderProfile.createOpenAiDefault();
        profile.type = type;
        applyProviderTypeDefaults(profile);
        return profile;
    }

    public static String generateProviderId(ProviderManagerConfig providerManager, String providerName, ApiProviderType type) {
        String base = toSlug(providerName);
        if (base.isEmpty()) {
            base = type.name().toLowerCase(Locale.ROOT);
        }

        String candidate = base;
        int index = 2;
        while (providerManager.findById(candidate) != null) {
            candidate = base + "_" + index;
            index++;
        }
        return candidate;
    }

    public static String previewRequestAddress(ApiProviderProfile profile) {
        String baseUrl = sanitize(profile.base_url).trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        String endpoint = switch (profile.type) {
            case OLLAMA -> "/api/chat";
            case OPENAI_RESPONSE -> "/responses";
            case OPENAI_COMPAT -> "/chat/completions";
        };
        return baseUrl.isEmpty() ? endpoint : baseUrl + endpoint;
    }

    public static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "";
        }
        if (apiKey.length() <= 6) {
            return "******";
        }
        return apiKey.substring(0, 3) + "********" + apiKey.substring(apiKey.length() - 3);
    }

    private static void applyProviderTypeDefaults(ApiProviderProfile profile) {
        if (profile.type == ApiProviderType.OLLAMA) {
            if (profile.base_url == null || profile.base_url.isBlank()) {
                profile.base_url = "http://localhost:11434";
            }
            if (profile.model_id == null || profile.model_id.isBlank()) {
                profile.model_id = "qwen3:0.6b";
            }
        } else {
            if (profile.base_url == null || profile.base_url.isBlank()) {
                profile.base_url = "https://api.openai.com/v1";
            }
            if (profile.model_id == null || profile.model_id.isBlank()) {
                profile.model_id = "gpt-4o";
            }
        }
        ProviderProfileSupport.normalizeModelIds(profile);
    }

    private static String toSlug(String value) {
        String source = sanitize(value).trim().toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder();
        boolean previousUnderscore = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
                result.append(c);
                previousUnderscore = false;
            } else if (!previousUnderscore && result.length() > 0) {
                result.append('_');
                previousUnderscore = true;
            }
        }
        while (result.length() > 0 && result.charAt(result.length() - 1) == '_') {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    private static String sanitize(String value) {
        return value == null ? "" : value;
    }
}
