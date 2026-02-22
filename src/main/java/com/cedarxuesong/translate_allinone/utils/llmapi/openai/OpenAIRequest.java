package com.cedarxuesong.translate_allinone.utils.llmapi.openai;

import java.util.List;

/**
 * 代表发送到OpenAI Chat Completions API的请求体。
 */
public class OpenAIRequest {
    public String model;
    public List<Message> messages;
    public double temperature;
    public boolean stream;
    public ResponseFormat response_format;

    public OpenAIRequest(String model, List<Message> messages, double temperature, boolean stream, ResponseFormat responseFormat) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.stream = stream;
        this.response_format = responseFormat;
    }

    public OpenAIRequest(String model, List<Message> messages, double temperature, boolean stream) {
        this(model, messages, temperature, stream, null);
    }

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class ResponseFormat {
        public String type;

        public ResponseFormat(String type) {
            this.type = type;
        }
    }
} 
