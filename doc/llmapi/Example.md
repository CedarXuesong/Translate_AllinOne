# 大语言模型Api使用示例
支持自定义请求参数，流式和非流式，多种模型供应商支持
```java
import com.cedarxuesong.translate_allinone.utils.llmapi.LLM;
import com.cedarxuesong.translate_allinone.utils.llmapi.ProviderSettings;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;

import java.util.List;
import java.util.Map;
```
```java
    private void runLLMTests() {
    // --- OpenAI 测试 ---
    LOGGER.info("--- 开始 OpenAI API 测试 ---");
    try {
        ProviderSettings.OpenAISettings testOpenAISettings = new ProviderSettings.OpenAISettings(
                "https://dashscope.aliyuncs.com/compatible-mode/v1",
                "sk-446a27a4889d498e8c6352bac0899f34", // 必须替换为有效密钥
                "qwen3-30b-a3b",
                0.7,
                Map.of("enable_thinking", false) // 添加自定义参数
        );
        ProviderSettings testOpenAISettingsObj = ProviderSettings.fromOpenAI(testOpenAISettings);
        LLM openAillm = new LLM(testOpenAISettingsObj);

        List<OpenAIRequest.Message> messages = List.of(
                new OpenAIRequest.Message("system", "You are a helpful assistant."),
                new OpenAIRequest.Message("user", "你好，简单介绍一下你自己")
        );

        // OpenAI 非流式测试
        LOGGER.info("[OpenAI 非流式测试] 正在发送请求...");
        openAillm.getCompletion(messages)
                .thenAccept(result -> LOGGER.info("[OpenAI 非流式测试] 结果: " + result))
                .exceptionally(ex -> {
                    LOGGER.error("[OpenAI 非流式测试] 发生错误: ", ex);
                    return null;
                });

        // OpenAI 流式测试
        LOGGER.info("[OpenAI 流式测试] 正在发送请求...");
        new Thread(() -> {
            try {
                LOGGER.info("[OpenAI 流式测试] 线程已启动，等待数据流...");
                StringBuilder fullResponse = new StringBuilder();
                openAillm.getStreamingCompletion(messages)
                        .forEach(chunk -> {
                            LOGGER.info("[OpenAI 流式测试] 收到数据块: " + chunk);
                            fullResponse.append(chunk);
                        });
                LOGGER.info("[OpenAI 流式测试] 数据流结束。完整响应: " + fullResponse);
            } catch (Exception e) {
                LOGGER.error("[OpenAI 流式测试] 线程中发生错误: ", e);
            }
        }, "LLM-OpenAI-Streaming-Test-Thread").start();

    } catch (Exception e) {
        LOGGER.error("[OpenAI 测试] 初始化或执行时发生错误: ", e);
    }


    // --- Ollama 测试 ---
    LOGGER.info("--- 开始 Ollama API 测试 ---");
    try {
        ProviderSettings.OllamaSettings testOllamaSettings = new ProviderSettings.OllamaSettings(
                "http://localhost:11434", // 默认Ollama地址
                "qwen3:0.6b", // 示例模型，请确保您已在本地拉取此模型
                "1m",
                Map.of("temperature", 0.8)
        );
        ProviderSettings testOllamaSettingsObj = ProviderSettings.fromOllama(testOllamaSettings);
        LLM ollamaLlm = new LLM(testOllamaSettingsObj);

        List<OpenAIRequest.Message> messages = List.of(
                new OpenAIRequest.Message("system", "You are a helpful bot."),
                new OpenAIRequest.Message("user", "你好，简单介绍一下你自己")
        );

        // Ollama 非流式测试
        LOGGER.info("[Ollama 非流式测试] 正在发送请求...");
        ollamaLlm.getCompletion(messages)
                .thenAccept(result -> LOGGER.info("[Ollama 非流式测试] 结果: " + result))
                .exceptionally(ex -> {
                    LOGGER.error("[Ollama 非流式测试] 发生错误: ", ex);
                    return null;
                });

        // Ollama 流式测试
        LOGGER.info("[Ollama 流式测试] 正在发送请求...");
        new Thread(() -> {
            try {
                LOGGER.info("[Ollama 流式测试] 线程已启动，等待数据流...");
                StringBuilder fullResponse = new StringBuilder();
                ollamaLlm.getStreamingCompletion(messages)
                        .forEach(chunk -> {
                            LOGGER.info("[Ollama 流式测试] 收到数据块: " + chunk);
                            fullResponse.append(chunk);
                        });
                LOGGER.info("[Ollama 流式测试] 数据流结束。完整响应: " + fullResponse);
            } catch (Exception e) {
                LOGGER.error("[Ollama 流式测试] 线程中发生错误: ", e);
            }
        }, "LLM-Ollama-Streaming-Test-Thread").start();

    } catch (Exception e) {
        LOGGER.error("[Ollama 测试] 初始化或执行时发生错误: ", e);
    }
}
```