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

    public OpenAIRequest(String model, List<Message> messages, double temperature, boolean stream) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.stream = stream;
    }

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
} 