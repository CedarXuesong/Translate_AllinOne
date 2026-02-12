package com.cedarxuesong.translate_allinone.utils.llmapi.openai;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.llmapi.LLMApiException;
import com.cedarxuesong.translate_allinone.utils.llmapi.ProviderSettings;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpenAIClient {

    private static final Gson GSON = new Gson();
    private final HttpClient httpClient;
    private final ProviderSettings.OpenAISettings settings;

    public OpenAIClient(ProviderSettings.OpenAISettings settings) {
        this.httpClient = HttpClient.newHttpClient();
        this.settings = settings;
    }

    private String buildRequestBody(OpenAIRequest request) {
        // 使用GSON将基础请求对象转换为JsonObject
        com.google.gson.JsonObject jsonObject = GSON.toJsonTree(request).getAsJsonObject();

        // 如果存在自定义参数，则添加到JsonObject中
        if (settings.customParameters() != null && !settings.customParameters().isEmpty()) {
            settings.customParameters().forEach((key, value) ->
                    jsonObject.add(key, GSON.toJsonTree(value))
            );
        }
        return GSON.toJson(jsonObject);
    }

    /**
     * 发送非流式请求到OpenAI
     * @param request 请求对象
     * @return 包含完整响应的Future
     */
    public CompletableFuture<OpenAIChatCompletion> getChatCompletion(OpenAIRequest request) {
        request.stream = false; // 确保为非流式
        String requestBody = buildRequestBody(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(settings.baseUrl() + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + settings.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        OpenAIError error = GSON.fromJson(response.body(), OpenAIError.class);
                        String message = "Unknown API error";
                        if (error != null && error.error != null && error.error.message != null) {
                            message = error.error.message;
                        }
                        throw new LLMApiException("API returned error: " + response.statusCode() + " - " + message);
                    }
                    return GSON.fromJson(response.body(), OpenAIChatCompletion.class);
                });
    }

    /**
     * 发送流式请求到OpenAI
     * @param request 请求对象
     * @return 响应块的Stream
     */
    public Stream<OpenAIChatCompletion> getStreamingChatCompletion(OpenAIRequest request) {
        request.stream = true; // 确保为流式
        String requestBody = buildRequestBody(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(settings.baseUrl() + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + settings.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<Stream<String>> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() != 200) {
                String errorBody = response.body().collect(Collectors.joining("\n"));
                OpenAIError error = GSON.fromJson(errorBody, OpenAIError.class);
                String message = "Unknown API error";
                if (error != null && error.error != null && error.error.message != null) {
                    message = error.error.message;
                }
                throw new LLMApiException("API returned error: " + response.statusCode() + " - " + message);
            }

            return response.body()
                    .filter(line -> line.startsWith("data: "))
                    .map(line -> line.substring("data: ".length()))
                    .filter(data -> !data.equals("[DONE]"))
                    .map(data -> GSON.fromJson(data, OpenAIChatCompletion.class))
                    .filter(chunk -> chunk != null && chunk.choices != null && !chunk.choices.isEmpty() && chunk.choices.get(0).delta != null && chunk.choices.get(0).delta.content != null);

        } catch (Exception e) {
            if (e instanceof LLMApiException llmApiException) {
                throw llmApiException;
            }
            Translate_AllinOne.LOGGER.error("请求OpenAI流式API时出错", e);
            throw new LLMApiException("请求OpenAI流式API时出错", e);
        }
    }
}
