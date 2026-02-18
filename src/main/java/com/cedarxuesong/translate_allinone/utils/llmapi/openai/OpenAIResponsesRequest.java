package com.cedarxuesong.translate_allinone.utils.llmapi.openai;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 代表发送到 OpenAI Responses API 的请求体。
 */
public class OpenAIResponsesRequest {
    public String model;
    public List<InputMessage> input;
    public double temperature;
    public boolean stream;
    public TextConfig text;

    public OpenAIResponsesRequest(
            String model,
            List<InputMessage> input,
            double temperature,
            boolean stream,
            TextConfig text
    ) {
        this.model = model;
        this.input = input;
        this.temperature = temperature;
        this.stream = stream;
        this.text = text;
    }

    public static OpenAIResponsesRequest fromChatMessages(
            String model,
            List<OpenAIRequest.Message> messages,
            double temperature,
            boolean stream,
            TextConfig text
    ) {
        return new OpenAIResponsesRequest(model, toInput(messages), temperature, stream, text);
    }

    private static List<InputMessage> toInput(List<OpenAIRequest.Message> messages) {
        List<InputMessage> inputMessages = new ArrayList<>();
        if (messages == null) {
            return inputMessages;
        }

        for (OpenAIRequest.Message message : messages) {
            if (message == null) {
                continue;
            }
            String role = normalizeRole(message.role);
            String text = message.content == null ? "" : message.content;
            inputMessages.add(new InputMessage(role, List.of(new InputContent("input_text", text))));
        }
        return inputMessages;
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "user";
        }

        String normalized = role.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "system", "user", "assistant", "developer" -> normalized;
            default -> "user";
        };
    }

    public static class InputMessage {
        public String role;
        public List<InputContent> content;

        public InputMessage(String role, List<InputContent> content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class InputContent {
        public String type;
        public String text;

        public InputContent(String type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    public static class TextConfig {
        public Format format;

        public TextConfig(Format format) {
            this.format = format;
        }
    }

    public static class Format {
        public String type;

        public Format(String type) {
            this.type = type;
        }
    }
}
