package com.cedarxuesong.translate_allinone.utils.llmapi.ollama;

import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;

import java.util.List;
import java.util.Map;

/**
 * 代表发送到Ollama Chat API的请求体。
 */
public class OllamaChatRequest {
    public String model;
    public List<OpenAIRequest.Message> messages;
    public boolean stream;
    public Map<String, Object> options;
    public String keep_alive;

    public OllamaChatRequest(String model, List<OpenAIRequest.Message> messages, boolean stream, String keep_alive, Map<String, Object> options) {
        this.model = model;
        this.messages = messages;
        this.stream = stream;
        this.keep_alive = keep_alive;
        this.options = options;
    }
} 