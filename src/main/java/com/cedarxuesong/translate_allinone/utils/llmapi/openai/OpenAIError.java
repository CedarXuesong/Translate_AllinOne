package com.cedarxuesong.translate_allinone.utils.llmapi.openai;

/**
 * 用于解析OpenAI兼容API返回的错误信息的POJO。
 */
public class OpenAIError {
    public ErrorDetails error;

    public static class ErrorDetails {
        public String message;
        public String type;
        public String param;
        public String code;
    }
} 