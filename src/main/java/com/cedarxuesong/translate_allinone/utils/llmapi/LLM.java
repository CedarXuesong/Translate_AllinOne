package com.cedarxuesong.translate_allinone.utils.llmapi;

import com.cedarxuesong.translate_allinone.utils.llmapi.ollama.OllamaChatRequest;
import com.cedarxuesong.translate_allinone.utils.llmapi.ollama.OllamaClient;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIClient;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIChatCompletion;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class LLM {

    private final OpenAIClient openAIClient;
    private final OllamaClient ollamaClient;
    private final ProviderSettings settings;

    public LLM(ProviderSettings settings) {
        this.settings = settings;
        if (settings.openAISettings() != null) {
            this.openAIClient = new OpenAIClient(settings.openAISettings());
            this.ollamaClient = null;
        } else if (settings.ollamaSettings() != null) {
            this.ollamaClient = new OllamaClient(settings.ollamaSettings());
            this.openAIClient = null;
        } else {
            this.openAIClient = null;
            this.ollamaClient = null;
            throw new IllegalStateException("LLM服务提供商未配置或配置不正确。");
        }
    }

    /**
     * 发送非流式请求，并异步返回完整结果。
     * @param messages 消息列表 (使用OpenAI的Message结构，因为它们是兼容的)
     * @return 包含完整响应字符串的 CompletableFuture
     */
    public CompletableFuture<String> getCompletion(List<OpenAIRequest.Message> messages) {
        if (openAIClient != null) {
            OpenAIRequest request = new OpenAIRequest(
                    settings.openAISettings().modelId(),
                    messages,
                    settings.openAISettings().temperature(),
                    false
            );
            return openAIClient.getChatCompletion(request)
                    .thenApply(response -> response.choices.get(0).message.content);
        } else { // ollamaClient != null
            OllamaChatRequest request = new OllamaChatRequest(
                    settings.ollamaSettings().modelId(),
                    messages,
                    false,
                    settings.ollamaSettings().keepAlive(),
                    settings.ollamaSettings().options()
            );
            return ollamaClient.getChatCompletion(request)
                    .thenApply(response -> response.message.content);
        }
    }

    /**
     * 发送流式请求，并返回一个包含文本块的流。
     * <p>
     * <b>重要:</b> 对返回的流进行操作是一个阻塞操作。
     * 调用者必须负责在单独的线程中消费此流，以避免阻塞主线程。
     *
     * @param messages 消息列表
     * @return 包含响应文本块的 Stream
     */
    public Stream<String> getStreamingCompletion(List<OpenAIRequest.Message> messages) {
        if (openAIClient != null) {
            OpenAIRequest request = new OpenAIRequest(
                    settings.openAISettings().modelId(),
                    messages,
                    settings.openAISettings().temperature(),
                    true
            );
            return openAIClient.getStreamingChatCompletion(request)
                    .map(chunk -> chunk.choices.get(0).delta.content);
        } else { // ollamaClient != null
            OllamaChatRequest request = new OllamaChatRequest(
                    settings.ollamaSettings().modelId(),
                    messages,
                    true,
                    settings.ollamaSettings().keepAlive(),
                    settings.ollamaSettings().options()
            );
            return ollamaClient.getStreamingChatCompletion(request)
                    .map(chunk -> chunk.message.content);
        }
    }
}
