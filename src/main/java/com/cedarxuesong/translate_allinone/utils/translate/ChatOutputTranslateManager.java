package com.cedarxuesong.translate_allinone.utils.translate;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.mixin.mixinChatHud.ChatHudAccessor;
import com.cedarxuesong.translate_allinone.utils.AnimationManager;
import com.cedarxuesong.translate_allinone.utils.MessageUtils;
import com.cedarxuesong.translate_allinone.utils.config.ProviderRouteResolver;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ChatTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.llmapi.LLM;
import com.cedarxuesong.translate_allinone.utils.llmapi.ProviderSettings;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;
import com.cedarxuesong.translate_allinone.utils.text.StylePreserver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatOutputTranslateManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatOutputTranslateManager.class);
    private static final Map<UUID, ChatHudLine> activeTranslationLines = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> lineLocateRetryCounts = new ConcurrentHashMap<>();
    private static ExecutorService translationExecutor;
    private static int currentConcurrentRequests = -1;
    private static final int MAX_LINE_LOCATE_RETRIES = 4;
    private static final long LINE_LOCATE_RETRY_DELAY_MS = 40L;
    private static final long ROUTE_ERROR_DISPLAY_MS = 3_000L;

    private static synchronized void updateExecutorServiceIfNeeded() {
        int configuredConcurrentRequests = Translate_AllinOne.getConfig().chatTranslate.output.max_concurrent_requests;
        if (translationExecutor == null || configuredConcurrentRequests != currentConcurrentRequests) {
            if (translationExecutor != null) {
                translationExecutor.shutdown();
                LOGGER.info("Shutting down old translation executor service...");
            }
            translationExecutor = Executors.newFixedThreadPool(Math.max(1, configuredConcurrentRequests), r -> {
                Thread t = new Thread(r, "Translate-Queue-Processor");
                t.setDaemon(true);
                return t;
            });
            currentConcurrentRequests = configuredConcurrentRequests;
            LOGGER.info("Translation executor service configured with {} concurrent threads.", currentConcurrentRequests);
        }
    }

    public static void translate(UUID messageId, Text originalMessage) {
        if (activeTranslationLines.containsKey(messageId)) {
            lineLocateRetryCounts.remove(messageId);
            return; // Already being translated
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ChatHud chatHud = client.inGameHud.getChatHud();
        ChatHudAccessor chatHudAccessor = (ChatHudAccessor) chatHud;
        List<ChatHudLine> messages = chatHudAccessor.getMessages();
        LineSearchResult searchResult = findTargetLine(messages, originalMessage);

        if (searchResult == null) {
            if (scheduleLineLocateRetry(messageId, originalMessage)) {
                return;
            }
            LOGGER.error("Could not find chat line to update for messageId: {} after {} retries", messageId, MAX_LINE_LOCATE_RETRIES);
            lineLocateRetryCounts.remove(messageId);
            MessageUtils.removeTrackedMessage(messageId);
            return;
        }
        lineLocateRetryCounts.remove(messageId);
        int lineIndex = searchResult.lineIndex();
        ChatHudLine targetLine = searchResult.line();

        updateExecutorServiceIfNeeded();

        ChatTranslateConfig.ChatOutputTranslateConfig chatOutputConfig = Translate_AllinOne.getConfig().chatTranslate.output;
        ApiProviderProfile providerProfile = ProviderRouteResolver.resolve(
                Translate_AllinOne.getConfig(),
                ProviderRouteResolver.Route.CHAT_OUTPUT
        );
        if (providerProfile == null) {
            LOGGER.warn("No routed model selected for chat output translation; showing temporary error for messageId={}", messageId);
            showTemporaryRouteError(messageId, chatHudAccessor, messages, lineIndex, targetLine);
            lineLocateRetryCounts.remove(messageId);
            return;
        }

        boolean isAutoTranslate = chatOutputConfig.auto_translate;
        boolean isStreaming = chatOutputConfig.streaming_response;
        Text placeholderText;

        if (isStreaming) {
            placeholderText = Text.literal("Connecting...").formatted(Formatting.GRAY);
        } else if (isAutoTranslate) {
            String plainText = AnimationManager.stripFormatting(originalMessage.getString());
            MutableText newText = Text.literal(plainText);

            Style baseStyle = originalMessage.getStyle();
            Style newStyle = baseStyle.withColor(Formatting.GRAY);
            newText.setStyle(newStyle);

            if (!originalMessage.getSiblings().isEmpty()) {
                MutableText fullText = Text.empty();
                originalMessage.getSiblings().forEach(sibling -> {
                    String plainSibling = AnimationManager.stripFormatting(sibling.getString());
                    fullText.append(Text.literal(plainSibling).setStyle(sibling.getStyle().withColor(Formatting.GRAY)));
                });
                placeholderText = fullText;
            } else {
                placeholderText = newText;
            }
        } else {
            placeholderText = Text.literal("Translating...").formatted(Formatting.GRAY);
        }

        ChatHudLine newLine = new ChatHudLine(targetLine.creationTick(), placeholderText, targetLine.signature(), targetLine.indicator());
        int scrolledLines = chatHudAccessor.getScrolledLines();
        messages.set(lineIndex, newLine);
        activeTranslationLines.put(messageId, newLine);
        chatHudAccessor.invokeRefresh();
        chatHudAccessor.setScrolledLines(scrolledLines);

        final int finalLineIndex = lineIndex;
        translationExecutor.submit(() -> {
            String requestContext = "route=chat_output,messageId=" + messageId;
            try {
                ProviderSettings settings = ProviderSettings.fromProviderProfile(providerProfile);
                LLM llm = new LLM(settings);

                StylePreserver.ExtractionResult extraction = StylePreserver.extractAndMarkWithTags(originalMessage);
                String textToTranslate = extraction.markedText;
                Map<Integer, Style> styleMap = extraction.styleMap;

                List<OpenAIRequest.Message> apiMessages = getMessages(providerProfile, chatOutputConfig.target_language, textToTranslate);
                requestContext = buildRequestContext(providerProfile, chatOutputConfig.target_language, textToTranslate, apiMessages, chatOutputConfig.streaming_response, messageId);

                LOGGER.info("Starting translation for message ID: {}. Marked text: {}", messageId, textToTranslate);

                if (chatOutputConfig.streaming_response) {
                    final StringBuilder rawResponseBuffer = new StringBuilder();
                    final StringBuilder visibleContentBuffer = new StringBuilder();
                    final AtomicBoolean inThinkTag = new AtomicBoolean(false);

                    llm.getStreamingCompletion(apiMessages).forEach(chunk -> {
                        rawResponseBuffer.append(chunk);

                        while (true) {
                            if (inThinkTag.get()) {
                                int endTagIndex = rawResponseBuffer.indexOf("</think>");
                                if (endTagIndex != -1) {
                                    inThinkTag.set(false);
                                    rawResponseBuffer.delete(0, endTagIndex + "</think>".length());
                                    updateInProgressChatLine(messageId, Text.literal(visibleContentBuffer.toString().replaceAll("</?s\\d+>", "")));
                                    continue;
                                } else {
                                    int startTagIndex = rawResponseBuffer.indexOf("<think>");
                                    if (startTagIndex != -1) {
                                        String thinkContent = rawResponseBuffer.substring(startTagIndex + "<think>".length());
                                        updateInProgressChatLine(messageId, Text.literal("Thinking: ").append(thinkContent).formatted(Formatting.GRAY));
                                    }
                                    break;
                                }
                            } else {
                                int startTagIndex = rawResponseBuffer.indexOf("<think>");
                                if (startTagIndex != -1) {
                                    String translationPart = rawResponseBuffer.substring(0, startTagIndex);
                                    visibleContentBuffer.append(translationPart);
                                    updateInProgressChatLine(messageId, Text.literal(visibleContentBuffer.toString().replaceAll("</?s\\d+>", "")));

                                    rawResponseBuffer.delete(0, startTagIndex);
                                    inThinkTag.set(true);
                                    continue;
                                } else {
                                    visibleContentBuffer.append(rawResponseBuffer.toString());
                                    rawResponseBuffer.setLength(0);
                                    updateInProgressChatLine(messageId, Text.literal(visibleContentBuffer.toString().replaceAll("</?s\\d+>", "")));
                                    break;
                                }
                            }
                        }
                    });

                    Text finalStyledText = StylePreserver.reapplyStylesFromTags(visibleContentBuffer.toString().stripLeading(), styleMap);
                    updateChatLineWithFinalText(messageId, finalStyledText);
                } else {
                    String result = llm.getCompletion(apiMessages).join();
                    LOGGER.info("Finished translation for message ID: {}. Result: {}", messageId, result);
                    final String finalTranslation = result.stripLeading();
                    Text finalStyledText = StylePreserver.reapplyStylesFromTags(finalTranslation, styleMap);
                    updateChatLineWithFinalText(messageId, finalStyledText);
                }
            } catch (Exception e) {
                LOGGER.error("[Translate-Thread] Exception for message ID: {}. context={}", messageId, requestContext, e);
                Text errorText = Text.literal("Translation Error: " + e.getMessage()).formatted(Formatting.RED);
                updateChatLineWithFinalText(messageId, errorText);
            }
        });
    }

    private static void updateInProgressChatLine(UUID messageId, Text newContent) {
        ChatHudLine lineToUpdate = activeTranslationLines.get(messageId);
        if (lineToUpdate == null) return;

        MinecraftClient.getInstance().execute(() -> {
            ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
            if (chatHud == null) return;

            ChatHudAccessor chatHudAccessor = (ChatHudAccessor) chatHud;
            List<ChatHudLine> messages = chatHudAccessor.getMessages();
            int scrolledLines = chatHudAccessor.getScrolledLines();

            int lineIndex = messages.indexOf(lineToUpdate);

            if (lineIndex != -1) {
                ChatHudLine newLine = new ChatHudLine(lineToUpdate.creationTick(), newContent, lineToUpdate.signature(), lineToUpdate.indicator());
                messages.set(lineIndex, newLine);
                activeTranslationLines.put(messageId, newLine);
                chatHudAccessor.invokeRefresh();
                chatHudAccessor.setScrolledLines(scrolledLines);
            }
        });
    }

    private static void updateChatLineWithFinalText(UUID messageId, Text finalContent) {
        lineLocateRetryCounts.remove(messageId);
        ChatHudLine lineToUpdate = activeTranslationLines.remove(messageId);
        if (lineToUpdate == null) return;

        MinecraftClient.getInstance().execute(() -> {
            ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
            if (chatHud == null) return;

            ChatHudAccessor chatHudAccessor = (ChatHudAccessor) chatHud;
            List<ChatHudLine> messages = chatHudAccessor.getMessages();
            int scrolledLines = chatHudAccessor.getScrolledLines();

            int lineIndex = messages.indexOf(lineToUpdate);

            if (lineIndex != -1) {
                ChatHudLine newLine = new ChatHudLine(lineToUpdate.creationTick(), finalContent, lineToUpdate.signature(), lineToUpdate.indicator());
                messages.set(lineIndex, newLine);
                chatHudAccessor.invokeRefresh();
                chatHudAccessor.setScrolledLines(scrolledLines);
            }
            MessageUtils.removeTrackedMessage(messageId);
        });
    }

    private static void showTemporaryRouteError(
            UUID messageId,
            ChatHudAccessor chatHudAccessor,
            List<ChatHudLine> messages,
            int lineIndex,
            ChatHudLine originalLine
    ) {
        int scrolledLines = chatHudAccessor.getScrolledLines();
        Text errorText = Text.literal("Translation Error: No routed model selected").formatted(Formatting.RED);
        ChatHudLine errorLine = new ChatHudLine(originalLine.creationTick(), errorText, originalLine.signature(), originalLine.indicator());
        messages.set(lineIndex, errorLine);
        chatHudAccessor.invokeRefresh();
        chatHudAccessor.setScrolledLines(scrolledLines);

        CompletableFuture.delayedExecutor(ROUTE_ERROR_DISPLAY_MS, TimeUnit.MILLISECONDS).execute(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return;
            }
            client.execute(() -> restoreLineAfterTemporaryError(messageId, errorLine, originalLine));
        });

        MessageUtils.removeTrackedMessage(messageId);
    }

    private static void restoreLineAfterTemporaryError(UUID messageId, ChatHudLine errorLine, ChatHudLine originalLine) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.inGameHud == null) {
            return;
        }

        ChatHud chatHud = client.inGameHud.getChatHud();
        if (chatHud == null) {
            return;
        }

        ChatHudAccessor chatHudAccessor = (ChatHudAccessor) chatHud;
        List<ChatHudLine> messages = chatHudAccessor.getMessages();
        int lineIndex = messages.indexOf(errorLine);
        if (lineIndex != -1) {
            int scrolledLines = chatHudAccessor.getScrolledLines();
            messages.set(lineIndex, originalLine);
            chatHudAccessor.invokeRefresh();
            chatHudAccessor.setScrolledLines(scrolledLines);
        }

        lineLocateRetryCounts.remove(messageId);
    }

    private static LineSearchResult findTargetLine(List<ChatHudLine> messages, Text originalMessage) {
        for (int i = 0; i < messages.size(); i++) {
            ChatHudLine line = messages.get(i);
            if (matchesTargetTextReference(line.content(), originalMessage)) {
                return new LineSearchResult(i, line);
            }
        }

        for (int i = 0; i < messages.size(); i++) {
            ChatHudLine line = messages.get(i);
            if (matchesTargetTextByContent(line.content(), originalMessage)) {
                return new LineSearchResult(i, line);
            }
        }
        return null;
    }

    private static boolean matchesTargetTextReference(Text lineContent, Text originalMessage) {
        if (lineContent.equals(originalMessage)) {
            return true;
        }
        return !lineContent.getSiblings().isEmpty() && lineContent.getSiblings().get(0).equals(originalMessage);
    }

    private static boolean matchesTargetTextByContent(Text lineContent, Text originalMessage) {
        String original = originalMessage.getString();
        if (lineContent.getString().equals(original)) {
            return true;
        }
        return !lineContent.getSiblings().isEmpty() && lineContent.getSiblings().get(0).getString().equals(original);
    }

    private static boolean scheduleLineLocateRetry(UUID messageId, Text originalMessage) {
        int attempt = lineLocateRetryCounts.merge(messageId, 1, Integer::sum);
        if (attempt > MAX_LINE_LOCATE_RETRIES) {
            return false;
        }

        CompletableFuture.delayedExecutor(LINE_LOCATE_RETRY_DELAY_MS, TimeUnit.MILLISECONDS).execute(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return;
            }
            client.execute(() -> translate(messageId, originalMessage));
        });
        return true;
    }

    private record LineSearchResult(int lineIndex, ChatHudLine line) {
    }

    @NotNull
    private static List<OpenAIRequest.Message> getMessages(ApiProviderProfile providerProfile, String targetLanguage, String textToTranslate) {
        String systemPrompt = PromptMessageBuilder.appendSystemPromptSuffix(
                "You are a deterministic translation engine.\n"
                        + "Target language: " + targetLanguage + ".\n"
                        + "\n"
                        + "Rules (highest priority first):\n"
                        + "1) Output only the final translated text. No explanation, markdown, or quotes.\n"
                        + "2) Preserve style tags exactly: <s0>...</s0>, <s1>...</s1>, ... Keep the same tag ids, counts, and order.\n"
                        + "3) Preserve tokens exactly: § color/style codes, placeholders (%s %d %f {d1}), URLs, numbers, <...>, {...}, \\n, \\t.\n"
                        + "4) If a term is uncertain, keep only that term unchanged and still translate surrounding text.\n"
                        + "5) If any rule cannot be guaranteed, return the original input unchanged.",
                providerProfile.activeSystemPromptSuffix()
        );
        return PromptMessageBuilder.buildMessages(
                systemPrompt,
                textToTranslate,
                providerProfile.activeSupportsSystemMessage(),
                providerProfile.model_id,
                providerProfile.activeInjectSystemPromptIntoUserMessage()
        );
    }

    private static String buildRequestContext(
            ApiProviderProfile profile,
            String targetLanguage,
            String markedText,
            List<OpenAIRequest.Message> messages,
            boolean streaming,
            UUID messageId
    ) {
        String providerId = profile == null ? "" : profile.id;
        String modelId = profile == null ? "" : profile.model_id;
        int messageCount = messages == null ? 0 : messages.size();
        String roles = messages == null
                ? "[]"
                : messages.stream().map(message -> message == null ? "null" : String.valueOf(message.role)).collect(java.util.stream.Collectors.joining(",", "[", "]"));
        String sample = truncate(normalizeWhitespace(markedText), 160);
        return "route=chat_output"
                + ", messageId=" + messageId
                + ", provider=" + providerId
                + ", model=" + modelId
                + ", target=" + (targetLanguage == null ? "" : targetLanguage)
                + ", streaming=" + streaming
                + ", messages=" + messageCount
                + ", roles=" + roles
                + ", sample=\"" + sample + "\"";
    }

    private static String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim();
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }
}
