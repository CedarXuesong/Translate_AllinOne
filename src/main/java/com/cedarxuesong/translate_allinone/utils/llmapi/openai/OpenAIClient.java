package com.cedarxuesong.translate_allinone.utils.llmapi.openai;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.llmapi.LLMApiException;
import com.cedarxuesong.translate_allinone.utils.llmapi.ProviderSettings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private String buildRequestBody(Object request) {
        // 使用GSON将基础请求对象转换为JsonObject
        JsonObject jsonObject = GSON.toJsonTree(request).getAsJsonObject();

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
        String requestSummary = summarizeRequest(request, requestBody);
        String endpoint = settings.baseUrl() + "/chat/completions";

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + settings.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        CompletableFuture<OpenAIChatCompletion> future = httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        String message = resolveApiErrorMessage(response.body());
                        logApiError(response, endpoint, requestSummary, response.body());
                        throw new LLMApiException("API returned error: " + response.statusCode() + " - " + message);
                    }
                    return GSON.fromJson(response.body(), OpenAIChatCompletion.class);
                });

        return future.whenComplete((ignored, throwable) -> {
            if (throwable == null) {
                return;
            }
            Throwable root = unwrapThrowable(throwable);
            if (root instanceof LLMApiException) {
                return;
            }
            Translate_AllinOne.LOGGER.error("OpenAI request failed before receiving valid API response. endpoint={} summary={}",
                    endpoint, requestSummary, root);
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
        String requestSummary = summarizeRequest(request, requestBody);
        String endpoint = settings.baseUrl() + "/chat/completions";

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + settings.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<Stream<String>> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() != 200) {
                String errorBody = response.body().collect(Collectors.joining("\n"));
                String message = resolveApiErrorMessage(errorBody);
                logApiError(response, endpoint, requestSummary, errorBody);
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
            Translate_AllinOne.LOGGER.error("OpenAI streaming request failed. endpoint={} summary={}", endpoint, requestSummary, e);
            throw new LLMApiException("请求OpenAI流式API时出错", e);
        }
    }

    /**
     * 发送非流式请求到 OpenAI Responses API。
     */
    public CompletableFuture<String> getResponsesCompletion(OpenAIResponsesRequest request) {
        request.stream = false;
        String requestBody = buildRequestBody(request);
        String requestSummary = summarizeResponsesRequest(request, requestBody);
        String endpoint = settings.baseUrl() + "/responses";

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + settings.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        CompletableFuture<String> future = httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        String message = resolveApiErrorMessage(response.body());
                        logApiError(response, endpoint, requestSummary, response.body());
                        throw new LLMApiException("API returned error: " + response.statusCode() + " - " + message);
                    }

                    JsonObject responseJson = parseJsonObject(response.body(), "无法解析 Responses API 响应");
                    String content = extractResponseText(responseJson);
                    if (content.isBlank()) {
                        throw new LLMApiException("Responses API returned empty output text");
                    }
                    return content;
                });

        return future.whenComplete((ignored, throwable) -> {
            if (throwable == null) {
                return;
            }
            Throwable root = unwrapThrowable(throwable);
            if (root instanceof LLMApiException) {
                return;
            }
            Translate_AllinOne.LOGGER.error("OpenAI Responses request failed before receiving valid API response. endpoint={} summary={}",
                    endpoint, requestSummary, root);
        });
    }

    /**
     * 发送流式请求到 OpenAI Responses API。
     */
    public Stream<String> getStreamingResponsesCompletion(OpenAIResponsesRequest request) {
        request.stream = true;
        String requestBody = buildRequestBody(request);
        String requestSummary = summarizeResponsesRequest(request, requestBody);
        String endpoint = settings.baseUrl() + "/responses";

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + settings.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<Stream<String>> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines());
            if (response.statusCode() != 200) {
                String errorBody = response.body().collect(Collectors.joining("\n"));
                String message = resolveApiErrorMessage(errorBody);
                logApiError(response, endpoint, requestSummary, errorBody);
                throw new LLMApiException("API returned error: " + response.statusCode() + " - " + message);
            }

            AtomicBoolean sawDelta = new AtomicBoolean(false);
            AtomicBoolean emittedFallback = new AtomicBoolean(false);

            return response.body()
                    .filter(line -> line.startsWith("data: "))
                    .map(line -> line.substring("data: ".length()))
                    .map(String::trim)
                    .filter(data -> !data.isEmpty() && !"[DONE]".equals(data))
                    .map(data -> parseJsonObject(data, "无法解析 Responses API 流式事件"))
                    .map(event -> extractResponsesStreamText(event, sawDelta, emittedFallback))
                    .filter(chunk -> chunk != null && !chunk.isEmpty());

        } catch (Exception e) {
            if (e instanceof LLMApiException llmApiException) {
                throw llmApiException;
            }
            Translate_AllinOne.LOGGER.error("OpenAI Responses streaming request failed. endpoint={} summary={}", endpoint, requestSummary, e);
            throw new LLMApiException("请求 OpenAI Responses 流式 API 时出错", e);
        }
    }

    private String summarizeRequest(OpenAIRequest request, String requestBody) {
        int bodyLength = requestBody == null ? 0 : requestBody.length();
        String bodyHash = requestBody == null ? "0" : Integer.toHexString(requestBody.hashCode());
        String responseFormat = request.response_format == null ? "null" : request.response_format.type;
        String customKeys = settings.customParameters() == null ? "[]" : settings.customParameters().keySet().toString();
        String customShape = summarizeCustomParameters(settings.customParameters());

        return "model=" + safeText(request.model)
                + ", stream=" + request.stream
                + ", temperature=" + request.temperature
                + ", response_format=" + responseFormat
                + ", messages=" + messageCount(request.messages)
                + ", roles=" + summarizeRoles(request.messages)
                + ", content_lengths=" + summarizeMessageLengths(request.messages)
                + ", custom_keys=" + customKeys
                + ", custom_shape=" + customShape
                + ", preview=\"" + summarizePreview(request.messages) + "\""
                + ", body_len=" + bodyLength
                + ", body_hash=" + bodyHash;
    }

    private String summarizeResponsesRequest(OpenAIResponsesRequest request, String requestBody) {
        int bodyLength = requestBody == null ? 0 : requestBody.length();
        String bodyHash = requestBody == null ? "0" : Integer.toHexString(requestBody.hashCode());
        String textFormat = request.text == null || request.text.format == null ? "null" : safeText(request.text.format.type);
        String customKeys = settings.customParameters() == null ? "[]" : settings.customParameters().keySet().toString();
        String customShape = summarizeCustomParameters(settings.customParameters());

        return "model=" + safeText(request.model)
                + ", stream=" + request.stream
                + ", temperature=" + request.temperature
                + ", text.format=" + textFormat
                + ", input=" + inputCount(request.input)
                + ", roles=" + summarizeInputRoles(request.input)
                + ", content_lengths=" + summarizeInputLengths(request.input)
                + ", custom_keys=" + customKeys
                + ", custom_shape=" + customShape
                + ", preview=\"" + summarizeInputPreview(request.input) + "\""
                + ", body_len=" + bodyLength
                + ", body_hash=" + bodyHash;
    }

    private String summarizeCustomParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "{}";
        }
        return truncate(renderValue(parameters), 600);
    }

    private String renderValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .map(entry -> safeText(String.valueOf(entry.getKey())) + ":" + renderValue(entry.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(this::renderValue)
                    .collect(Collectors.joining(",", "[", "]"));
        }
        if (value instanceof String stringValue) {
            return '"' + truncate(normalizeWhitespace(stringValue), 80) + '"';
        }
        return String.valueOf(value);
    }

    private int messageCount(List<OpenAIRequest.Message> messages) {
        return messages == null ? 0 : messages.size();
    }

    private String summarizeRoles(List<OpenAIRequest.Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "[]";
        }
        return messages.stream()
                .map(message -> message == null ? "null" : safeText(message.role))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String summarizeMessageLengths(List<OpenAIRequest.Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "[]";
        }
        return messages.stream()
                .map(message -> Integer.toString(message == null || message.content == null ? 0 : message.content.length()))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String summarizePreview(List<OpenAIRequest.Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        for (OpenAIRequest.Message message : messages) {
            if (message != null && "user".equalsIgnoreCase(safeText(message.role))) {
                return truncate(normalizeWhitespace(message.content), 240);
            }
        }

        OpenAIRequest.Message first = messages.get(0);
        return truncate(normalizeWhitespace(first == null ? "" : first.content), 240);
    }

    private int inputCount(List<OpenAIResponsesRequest.InputMessage> input) {
        return input == null ? 0 : input.size();
    }

    private String summarizeInputRoles(List<OpenAIResponsesRequest.InputMessage> input) {
        if (input == null || input.isEmpty()) {
            return "[]";
        }
        return input.stream()
                .map(message -> message == null ? "null" : safeText(message.role))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String summarizeInputLengths(List<OpenAIResponsesRequest.InputMessage> input) {
        if (input == null || input.isEmpty()) {
            return "[]";
        }
        return input.stream()
                .map(message -> Integer.toString(extractInputMessageText(message).length()))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String summarizeInputPreview(List<OpenAIResponsesRequest.InputMessage> input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        for (OpenAIResponsesRequest.InputMessage message : input) {
            if (message != null && "user".equalsIgnoreCase(safeText(message.role))) {
                return truncate(normalizeWhitespace(extractInputMessageText(message)), 240);
            }
        }

        return truncate(normalizeWhitespace(extractInputMessageText(input.get(0))), 240);
    }

    private String extractInputMessageText(OpenAIResponsesRequest.InputMessage message) {
        if (message == null || message.content == null || message.content.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (OpenAIResponsesRequest.InputContent content : message.content) {
            if (content == null || content.text == null || content.text.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(content.text);
        }
        return builder.toString();
    }

    private JsonObject parseJsonObject(String rawBody, String errorMessage) {
        try {
            return GSON.fromJson(rawBody, JsonObject.class);
        } catch (Exception e) {
            throw new LLMApiException(errorMessage, e);
        }
    }

    private String extractResponsesStreamText(JsonObject event, AtomicBoolean sawDelta, AtomicBoolean emittedFallback) {
        if (event == null) {
            return null;
        }

        String eventType = getStringMember(event, "type");
        if (eventType == null || eventType.isEmpty()) {
            return null;
        }

        if (eventType.contains("output_text.delta")) {
            String delta = extractTextFromElement(event.get("delta"));
            if (!delta.isEmpty()) {
                sawDelta.set(true);
                return delta;
            }
            return null;
        }

        if (eventType.contains("output_text.done")) {
            String doneText = extractTextFromElement(event.get("text"));
            if (!sawDelta.get() && !doneText.isEmpty() && emittedFallback.compareAndSet(false, true)) {
                return doneText;
            }
            return null;
        }

        if (eventType.equals("response.completed") || eventType.endsWith(".completed")) {
            if (sawDelta.get() || emittedFallback.get()) {
                return null;
            }

            String fullText = extractResponseText(event);
            if (fullText.isEmpty() && event.has("response") && event.get("response").isJsonObject()) {
                fullText = extractResponseText(event.getAsJsonObject("response"));
            }

            if (!fullText.isEmpty() && emittedFallback.compareAndSet(false, true)) {
                return fullText;
            }
            return null;
        }

        if (eventType.contains("error")) {
            String message = getStringMember(event, "message");
            if ((message == null || message.isEmpty()) && event.has("error")) {
                message = extractTextFromElement(event.get("error"));
            }
            if (message != null && !message.isEmpty()) {
                throw new LLMApiException("Responses stream error: " + message);
            }
        }

        return null;
    }

    private String extractResponseText(JsonObject responseJson) {
        if (responseJson == null) {
            return "";
        }

        String outputText = extractTextFromElement(responseJson.get("output_text"));
        if (!outputText.isEmpty()) {
            return outputText;
        }

        if (responseJson.has("output") && responseJson.get("output").isJsonArray()) {
            String fromOutput = extractResponseTextFromOutput(responseJson.getAsJsonArray("output"));
            if (!fromOutput.isEmpty()) {
                return fromOutput;
            }
        }

        if (responseJson.has("choices") && responseJson.get("choices").isJsonArray()) {
            String fromChoices = extractResponseTextFromChoices(responseJson.getAsJsonArray("choices"));
            if (!fromChoices.isEmpty()) {
                return fromChoices;
            }
        }

        if (responseJson.has("response") && responseJson.get("response").isJsonObject()) {
            return extractResponseText(responseJson.getAsJsonObject("response"));
        }

        return "";
    }

    private String extractResponseTextFromOutput(JsonArray outputArray) {
        if (outputArray == null || outputArray.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (JsonElement itemElement : outputArray) {
            if (itemElement == null || !itemElement.isJsonObject()) {
                continue;
            }

            JsonObject item = itemElement.getAsJsonObject();
            String contentText = extractTextFromElement(item.get("content"));
            if (!contentText.isEmpty()) {
                builder.append(contentText);
                continue;
            }

            String itemText = extractDirectTextField(item);
            if (!itemText.isEmpty()) {
                builder.append(itemText);
            }
        }

        return builder.toString();
    }

    private String extractResponseTextFromChoices(JsonArray choices) {
        if (choices == null || choices.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (JsonElement choiceElement : choices) {
            if (choiceElement == null || !choiceElement.isJsonObject()) {
                continue;
            }

            JsonObject choice = choiceElement.getAsJsonObject();
            String messageText = extractTextFromElement(choice.get("message"));
            if (!messageText.isEmpty()) {
                builder.append(messageText);
                continue;
            }

            String deltaText = extractTextFromElement(choice.get("delta"));
            if (!deltaText.isEmpty()) {
                builder.append(deltaText);
            }
        }

        return builder.toString();
    }

    private String extractTextFromElement(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "";
        }

        if (element.isJsonPrimitive()) {
            return element.getAsJsonPrimitive().isString() ? safeText(element.getAsString()) : "";
        }

        if (element.isJsonArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonElement child : element.getAsJsonArray()) {
                String text = extractTextFromElement(child);
                if (!text.isEmpty()) {
                    builder.append(text);
                }
            }
            return builder.toString();
        }

        if (!element.isJsonObject()) {
            return "";
        }

        JsonObject object = element.getAsJsonObject();
        String directText = extractDirectTextField(object);
        if (!directText.isEmpty()) {
            return directText;
        }

        if (object.has("message")) {
            String message = extractTextFromElement(object.get("message"));
            if (!message.isEmpty()) {
                return message;
            }
        }

        if (object.has("content")) {
            String content = extractTextFromElement(object.get("content"));
            if (!content.isEmpty()) {
                return content;
            }
        }

        if (object.has("delta")) {
            String delta = extractTextFromElement(object.get("delta"));
            if (!delta.isEmpty()) {
                return delta;
            }
        }

        if (object.has("output_text")) {
            String outputText = extractTextFromElement(object.get("output_text"));
            if (!outputText.isEmpty()) {
                return outputText;
            }
        }

        return "";
    }

    private String extractDirectTextField(JsonObject object) {
        String text = getStringMember(object, "text");
        if (text != null && !text.isEmpty()) {
            return text;
        }
        String content = getStringMember(object, "content");
        if (content != null && !content.isEmpty()) {
            return content;
        }
        return "";
    }

    private String getStringMember(JsonObject object, String memberName) {
        if (object == null || memberName == null || !object.has(memberName)) {
            return null;
        }
        JsonElement element = object.get(memberName);
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return null;
        }
        if (!element.getAsJsonPrimitive().isString()) {
            return null;
        }
        return element.getAsString();
    }

    private String resolveApiErrorMessage(String responseBody) {
        OpenAIError error;
        try {
            error = GSON.fromJson(responseBody, OpenAIError.class);
        } catch (Exception ignored) {
            error = null;
        }
        if (error != null && error.error != null && error.error.message != null && !error.error.message.isBlank()) {
            return error.error.message;
        }

        JsonObject errorJson;
        try {
            errorJson = GSON.fromJson(responseBody, JsonObject.class);
        } catch (Exception ignored) {
            return "Unknown API error";
        }
        if (errorJson == null) {
            return "Unknown API error";
        }
        String errorMessage = getStringMember(errorJson, "message");
        if (errorMessage != null && !errorMessage.isBlank()) {
            return errorMessage;
        }
        if (errorJson.has("error") && errorJson.get("error").isJsonObject()) {
            String nested = getStringMember(errorJson.getAsJsonObject("error"), "message");
            if (nested != null && !nested.isBlank()) {
                return nested;
            }
        }

        return "Unknown API error";
    }

    private String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value, int maxLength) {
        String safeValue = safeText(value);
        if (safeValue.length() <= maxLength) {
            return safeValue;
        }
        return safeValue.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private void logApiError(HttpResponse<?> response, String endpoint, String requestSummary, String responseBody) {
        String requestId = response.headers().firstValue("x-request-id").orElse("-");
        Translate_AllinOne.LOGGER.error(
                "OpenAI API returned non-200. status={} requestId={} endpoint={} summary={} response_body={}"
                , response.statusCode()
                , requestId
                , endpoint
                , requestSummary
                , truncate(normalizeWhitespace(responseBody), 1200)
        );
    }

    private Throwable unwrapThrowable(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof java.util.concurrent.CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
