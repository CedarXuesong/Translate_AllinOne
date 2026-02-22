package com.cedarxuesong.translate_allinone.utils.llmapi.openai;

import java.util.List;

/**
 * 代表从OpenAI Chat Completions API接收到的响应。
 * 它可以用于完整的非流式响应，也可以代表流式响应中的单个块。
 */
public class OpenAIChatCompletion {

    public List<Choice> choices;
    public String model;
    public String object;

    public static class Choice {
        public Message delta;       // 用于流式响应
        public Message message;     // 用于非流式响应
    }

    public static class Message {
        public String content;
    }
} 