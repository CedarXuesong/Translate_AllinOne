package com.cedarxuesong.translate_allinone.utils.llmapi;

/**
 * 一个自定义运行时异常，用于表示在与LLM API交互时发生的错误。
 */
public class LLMApiException extends RuntimeException {
    public LLMApiException(String message) {
        super(message);
    }

    public LLMApiException(String message, Throwable cause) {
        super(message, cause);
    }
} 