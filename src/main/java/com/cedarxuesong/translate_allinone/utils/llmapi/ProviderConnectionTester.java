package com.cedarxuesong.translate_allinone.utils.llmapi;

import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProviderConnectionTester {
    private static final Pattern HTTP_STATUS_PATTERN_A = Pattern.compile("API returned error:\\s*(\\d{3})", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTTP_STATUS_PATTERN_B = Pattern.compile("request failed\\s*\\((\\d{3})\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTTP_STATUS_PATTERN_C = Pattern.compile("\\bHTTP\\s*(\\d{3})\\b", Pattern.CASE_INSENSITIVE);

    private ProviderConnectionTester() {
    }

    public static CompletableFuture<TestResult> test(ApiProviderProfile profile) {
        if (profile == null) {
            return CompletableFuture.completedFuture(TestResult.failure(FailureCategory.CONFIG, null, "Profile is null"));
        }

        try {
            ProviderSettings settings = ProviderSettings.fromProviderProfile(profile);
            LLM llm = new LLM(settings);

            if (!llm.supportsChatCompletion()) {
                return CompletableFuture.completedFuture(TestResult.failure(FailureCategory.UNSUPPORTED, null, "Provider does not support chat completion"));
            }

            List<OpenAIRequest.Message> messages = buildProbeMessages(profile.activeSupportsSystemMessage());
            return llm.getCompletion(messages)
                    .orTimeout(20, TimeUnit.SECONDS)
                    .thenApply(response -> {
                        if (response == null || response.trim().isEmpty()) {
                            return TestResult.failure(FailureCategory.UNKNOWN, null, "Empty response");
                        }
                        return TestResult.success(shortText(response));
                    })
                    .exceptionally(throwable -> toFailureResult(throwable));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(toFailureResult(e));
        }
    }

    private static List<OpenAIRequest.Message> buildProbeMessages(boolean supportsSystemMessage) {
        String instruction = "Reply with exactly OK.";
        if (supportsSystemMessage) {
            return List.of(
                    new OpenAIRequest.Message("system", instruction),
                    new OpenAIRequest.Message("user", "Connection probe")
            );
        }
        return List.of(new OpenAIRequest.Message("user", instruction + "\n\nConnection probe"));
    }

    private static String shortText(String text) {
        String trimmed = text == null ? "" : text.replace('\n', ' ').replace('\r', ' ').trim();
        if (trimmed.length() <= 90) {
            return trimmed;
        }
        return trimmed.substring(0, 87) + "...";
    }

    private static TestResult toFailureResult(Throwable throwable) {
        FailureDetail detail = classifyFailure(throwable);
        return TestResult.failure(detail.category, detail.httpStatus, detail.message);
    }

    private static FailureDetail classifyFailure(Throwable throwable) {
        Throwable root = unwrapThrowable(throwable);
        String message = root.getMessage() == null ? "request failed" : root.getMessage();
        String normalized = message.toLowerCase(Locale.ROOT);

        if (root instanceof TimeoutException || root instanceof HttpTimeoutException) {
            return new FailureDetail(FailureCategory.TIMEOUT, null, "Request timed out");
        }

        Integer status = extractHttpStatus(root, message);
        if (status != null) {
            if (status == 401 || status == 403) {
                return new FailureDetail(FailureCategory.AUTH, status, authHint(message));
            }

            if (status == 429) {
                return new FailureDetail(FailureCategory.HTTP, status, "Rate limited");
            }

            return new FailureDetail(FailureCategory.HTTP, status, shortText(message));
        }

        if (isAuthKeyword(normalized)) {
            return new FailureDetail(FailureCategory.AUTH, null, authHint(message));
        }

        if (root instanceof ConnectException || root instanceof UnknownHostException || root instanceof SocketException) {
            return new FailureDetail(FailureCategory.NETWORK, null, shortText(message));
        }

        if (normalized.contains("does not support") || normalized.contains("not support") || normalized.contains("unsupported")) {
            return new FailureDetail(FailureCategory.UNSUPPORTED, null, shortText(message));
        }

        if (root instanceof IllegalArgumentException || root instanceof IllegalStateException) {
            return new FailureDetail(FailureCategory.CONFIG, null, shortText(message));
        }

        if (root instanceof LLMApiException) {
            return new FailureDetail(FailureCategory.HTTP, null, shortText(message));
        }

        return new FailureDetail(FailureCategory.UNKNOWN, null, shortText(message));
    }

    private static Throwable unwrapThrowable(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private static Integer extractHttpStatus(Throwable root, String message) {
        Integer status = extractHttpStatusFromMessage(message);
        if (status != null) {
            return status;
        }

        Throwable cursor = root;
        while (cursor != null) {
            String cursorMessage = cursor.getMessage();
            status = extractHttpStatusFromMessage(cursorMessage);
            if (status != null) {
                return status;
            }
            cursor = cursor.getCause();
        }
        return null;
    }

    private static Integer extractHttpStatusFromMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }

        Matcher matcher = HTTP_STATUS_PATTERN_A.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        matcher = HTTP_STATUS_PATTERN_B.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        matcher = HTTP_STATUS_PATTERN_C.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }

    private static boolean isAuthKeyword(String normalizedMessage) {
        return normalizedMessage.contains("unauthorized")
                || normalizedMessage.contains("forbidden")
                || normalizedMessage.contains("api key")
                || normalizedMessage.contains("invalid key")
                || normalizedMessage.contains("authentication")
                || normalizedMessage.contains("auth");
    }

    private static String authHint(String message) {
        if (message == null || message.isBlank()) {
            return "Authentication failed";
        }
        return shortText(message);
    }

    private record FailureDetail(FailureCategory category, Integer httpStatus, String message) {
    }

    public enum FailureCategory {
        AUTH,
        TIMEOUT,
        HTTP,
        NETWORK,
        CONFIG,
        UNSUPPORTED,
        UNKNOWN
    }

    public record TestResult(boolean success, String detail) {
        public static TestResult success(String detail) {
            return new TestResult(true, "OK | " + detail);
        }

        public static TestResult failure(FailureCategory category, Integer statusCode, String detail) {
            StringBuilder builder = new StringBuilder(category.name());
            if (statusCode != null) {
                builder.append(" HTTP ").append(statusCode);
            }
            if (detail != null && !detail.isBlank()) {
                builder.append(" | ").append(detail);
            }
            return new TestResult(false, builder.toString());
        }
    }
}
