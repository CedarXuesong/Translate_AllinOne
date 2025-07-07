package com.cedarxuesong.translate_allinone.utils.llmapi.ollama;

import com.cedarxuesong.translate_allinone.utils.llmapi.LLMApiException;
import com.cedarxuesong.translate_allinone.utils.llmapi.ProviderSettings;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIError;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OllamaClient {

    private static final Gson GSON = new Gson();
    private final HttpClient httpClient;
    private final ProviderSettings.OllamaSettings settings;

    public OllamaClient(ProviderSettings.OllamaSettings settings) {
        this.httpClient = HttpClient.newHttpClient();
        this.settings = settings;
    }

    public CompletableFuture<OllamaChatResponse> getChatCompletion(OllamaChatRequest request) {
        request.stream = false;
        String requestBody = GSON.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(settings.baseUrl() + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        // Ollama的错误格式可能不同，但我们尝试用OpenAI的格式解析
                        OpenAIError error = GSON.fromJson(response.body(), OpenAIError.class);
                        String message = error != null && error.error != null ? error.error.message : response.body();
                        throw new LLMApiException("API returned error: " + response.statusCode() + " - " + message);
                    }
                    return GSON.fromJson(response.body(), OllamaChatResponse.class);
                });
    }

    public Stream<OllamaChatResponse> getStreamingChatCompletion(OllamaChatRequest request) {
        request.stream = true;
        String requestBody = GSON.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(settings.baseUrl() + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<Stream<String>> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() != 200) {
                String errorBody = response.body().collect(Collectors.joining("\n"));
                OpenAIError error = GSON.fromJson(errorBody, OpenAIError.class);
                String message = error != null && error.error != null ? error.error.message : errorBody;
                throw new LLMApiException("API returned error: " + response.statusCode() + " - " + message);
            }

            return response.body()
                    .map(line -> GSON.fromJson(line, OllamaChatResponse.class))
                    .filter(chunk -> chunk != null && chunk.message != null && chunk.message.content != null);
        } catch (Exception e) {
            if (e instanceof LLMApiException) {
                throw (LLMApiException) e;
            }
            throw new LLMApiException("请求Ollama流式API时出错", e);
        }
    }
} 