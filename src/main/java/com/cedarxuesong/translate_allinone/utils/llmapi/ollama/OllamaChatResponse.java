package com.cedarxuesong.translate_allinone.utils.llmapi.ollama;

/**
 * 代表从Ollama Chat API接收到的响应。
 */
public class OllamaChatResponse {
    public String model;
    public Message message;

    public static class Message {
        public String content;
    }
} 