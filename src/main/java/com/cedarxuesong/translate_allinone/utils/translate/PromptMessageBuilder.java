package com.cedarxuesong.translate_allinone.utils.translate;

import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;

import java.util.List;

public final class PromptMessageBuilder {
    private PromptMessageBuilder() {
    }

    public static List<OpenAIRequest.Message> buildMessages(String systemPrompt, String userPrompt, boolean supportsSystemMessage) {
        return buildMessages(systemPrompt, userPrompt, supportsSystemMessage, null, true);
    }

    public static List<OpenAIRequest.Message> buildMessages(
            String systemPrompt,
            String userPrompt,
            boolean supportsSystemMessage,
            String modelId
    ) {
        return buildMessages(systemPrompt, userPrompt, supportsSystemMessage, modelId, true);
    }

    public static List<OpenAIRequest.Message> buildMessages(
            String systemPrompt,
            String userPrompt,
            boolean supportsSystemMessage,
            String modelId,
            boolean injectSystemPromptIntoUserMessage
    ) {
        String safeSystem = systemPrompt == null ? "" : systemPrompt;
        String safeUser = userPrompt == null ? "" : userPrompt;

        if (!supportsSystemMessage) {
            if (injectSystemPromptIntoUserMessage) {
                String mergedPrompt = mergeSystemIntoUserPrompt(safeSystem, safeUser);
                return List.of(new OpenAIRequest.Message("user", mergedPrompt));
            }
            return List.of(new OpenAIRequest.Message("user", safeUser));
        }

        if (supportsSystemMessage) {
            return List.of(
                    new OpenAIRequest.Message("system", safeSystem),
                    new OpenAIRequest.Message("user", safeUser)
            );
        }

        return List.of(new OpenAIRequest.Message("user", safeUser));
    }

    public static String appendSystemPromptSuffix(String basePrompt, String suffix) {
        String safeBase = basePrompt == null ? "" : basePrompt;
        if (suffix == null || suffix.isBlank()) {
            return safeBase;
        }
        if (suffix.startsWith("\n")) {
            return safeBase + suffix;
        }
        return safeBase + "\n" + suffix;
    }

    private static String mergeSystemIntoUserPrompt(String systemPrompt, String userPrompt) {
        if (systemPrompt == null || systemPrompt.isBlank()) {
            return userPrompt == null ? "" : userPrompt;
        }
        if (userPrompt == null || userPrompt.isBlank()) {
            return systemPrompt;
        }
        return systemPrompt + "\n\nInput:\n" + userPrompt;
    }
}
