package com.cedarxuesong.translate_allinone.utils.llmapi;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.llmapi.ollama.OllamaChatRequest;
import com.cedarxuesong.translate_allinone.utils.llmapi.ollama.OllamaClient;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIChatCompletion;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIClient;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
            boolean structuredOutputEnabled = settings.openAISettings().enableStructuredOutputIfAvailable();
            CompletionSupplier primary = () -> openAIClient.getChatCompletion(
                    buildOpenAIRequest(messages, false, structuredOutputEnabled)
            ).thenApply(response -> response.choices.get(0).message.content);
            CompletionSupplier fallback = () -> openAIClient.getChatCompletion(
                    buildOpenAIRequest(messages, false, false)
            ).thenApply(response -> response.choices.get(0).message.content);
            return withStructuredOutputFallback(structuredOutputEnabled, primary, fallback, "OpenAI");
        } else { // ollamaClient != null
            boolean structuredOutputEnabled = settings.ollamaSettings().enableStructuredOutputIfAvailable();
            CompletionSupplier primary = () -> ollamaClient.getChatCompletion(
                    buildOllamaRequest(messages, false, structuredOutputEnabled)
            ).thenApply(response -> response.message.content);
            CompletionSupplier fallback = () -> ollamaClient.getChatCompletion(
                    buildOllamaRequest(messages, false, false)
            ).thenApply(response -> response.message.content);
            return withStructuredOutputFallback(structuredOutputEnabled, primary, fallback, "Ollama");
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
            boolean structuredOutputEnabled = settings.openAISettings().enableStructuredOutputIfAvailable();
            try {
                return openAIClient.getStreamingChatCompletion(
                                buildOpenAIRequest(messages, true, structuredOutputEnabled)
                        )
                        .map(chunk -> chunk.choices.get(0).delta.content);
            } catch (RuntimeException e) {
                Throwable rootCause = unwrapCompletionThrowable(e);
                if (structuredOutputEnabled && isStructuredOutputUnsupported(rootCause)) {
                    Translate_AllinOne.LOGGER.warn("OpenAI structured output unsupported in streaming mode, retrying without it: {}", rootCause.getMessage());
                    return openAIClient.getStreamingChatCompletion(
                                    buildOpenAIRequest(messages, true, false)
                            )
                            .map(chunk -> chunk.choices.get(0).delta.content);
                }
                throw e;
            }
        } else { // ollamaClient != null
            boolean structuredOutputEnabled = settings.ollamaSettings().enableStructuredOutputIfAvailable();
            try {
                return ollamaClient.getStreamingChatCompletion(
                                buildOllamaRequest(messages, true, structuredOutputEnabled)
                        )
                        .map(chunk -> chunk.message.content);
            } catch (RuntimeException e) {
                Throwable rootCause = unwrapCompletionThrowable(e);
                if (structuredOutputEnabled && isStructuredOutputUnsupported(rootCause)) {
                    Translate_AllinOne.LOGGER.warn("Ollama structured output unsupported in streaming mode, retrying without it: {}", rootCause.getMessage());
                    return ollamaClient.getStreamingChatCompletion(
                                    buildOllamaRequest(messages, true, false)
                            )
                            .map(chunk -> chunk.message.content);
                }
                throw e;
            }
        }
    }

    private OpenAIRequest buildOpenAIRequest(List<OpenAIRequest.Message> messages, boolean stream, boolean structuredOutputEnabled) {
        OpenAIRequest.ResponseFormat responseFormat = structuredOutputEnabled
                ? new OpenAIRequest.ResponseFormat("json_object")
                : null;
        return new OpenAIRequest(
                settings.openAISettings().modelId(),
                messages,
                settings.openAISettings().temperature(),
                stream,
                responseFormat
        );
    }

    private OllamaChatRequest buildOllamaRequest(List<OpenAIRequest.Message> messages, boolean stream, boolean structuredOutputEnabled) {
        String format = structuredOutputEnabled ? "json" : null;
        return new OllamaChatRequest(
                settings.ollamaSettings().modelId(),
                messages,
                stream,
                settings.ollamaSettings().keepAlive(),
                settings.ollamaSettings().options(),
                format
        );
    }

    private CompletableFuture<String> withStructuredOutputFallback(
            boolean structuredOutputEnabled,
            CompletionSupplier primary,
            CompletionSupplier fallback,
            String providerName
    ) {
        CompletableFuture<String> primaryFuture = primary.get();
        if (!structuredOutputEnabled) {
            return primaryFuture;
        }

        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        primaryFuture.whenComplete((result, throwable) -> {
            if (throwable == null) {
                resultFuture.complete(result);
                return;
            }

            Throwable rootCause = unwrapCompletionThrowable(throwable);
            if (!isStructuredOutputUnsupported(rootCause)) {
                resultFuture.completeExceptionally(rootCause);
                return;
            }

            Translate_AllinOne.LOGGER.warn("{} structured output unsupported, retrying without it: {}", providerName, rootCause.getMessage());
            try {
                fallback.get().whenComplete((fallbackResult, fallbackThrowable) -> {
                    if (fallbackThrowable == null) {
                        resultFuture.complete(fallbackResult);
                    } else {
                        resultFuture.completeExceptionally(unwrapCompletionThrowable(fallbackThrowable));
                    }
                });
            } catch (Throwable fallbackStartError) {
                resultFuture.completeExceptionally(unwrapCompletionThrowable(fallbackStartError));
            }
        });
        return resultFuture;
    }

    private Throwable unwrapCompletionThrowable(Throwable throwable) {
        if (throwable instanceof CompletionException && throwable.getCause() != null) {
            return throwable.getCause();
        }
        return throwable;
    }

    private boolean isStructuredOutputUnsupported(Throwable throwable) {
        if (!(throwable instanceof LLMApiException) || throwable.getMessage() == null) {
            return false;
        }

        String message = throwable.getMessage().toLowerCase(Locale.ROOT);
        if (message.contains("response_format") || message.contains("json_schema") || message.contains("json_object")) {
            return true;
        }

        if (message.contains("unknown field") && message.contains("format")) {
            return true;
        }

        return (message.contains("format") || message.contains("structured"))
                && (message.contains("unsupported") || message.contains("not support") || message.contains("invalid"));
    }

    @FunctionalInterface
    private interface CompletionSupplier {
        CompletableFuture<String> get();
    }
}
