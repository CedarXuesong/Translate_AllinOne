package com.cedarxuesong.translate_allinone.utils.translate;

import com.cedarxuesong.translate_allinone.mixin.mixinChatHud.ChatHudAccessor;
import com.cedarxuesong.translate_allinone.utils.AnimationManager;
import com.cedarxuesong.translate_allinone.utils.MessageUtils;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ChatTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.Provider;
import com.cedarxuesong.translate_allinone.utils.llmapi.LLM;
import com.cedarxuesong.translate_allinone.utils.llmapi.ProviderSettings;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;
import com.cedarxuesong.translate_allinone.utils.text.StylePreserver;
import me.shedaniel.autoconfig.AutoConfig;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatOutputTranslateManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatOutputTranslateManager.class);
    private static final Map<UUID, ChatHudLine> activeTranslationLines = new ConcurrentHashMap<>();
    private static ExecutorService translationExecutor;
    private static int currentConcurrentRequests = -1;

    private static synchronized void updateExecutorServiceIfNeeded() {
        int configuredConcurrentRequests = AutoConfig.getConfigHolder(ModConfig.class).getConfig().chatTranslate.output.max_concurrent_requests;
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
            return; // Already being translated
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ChatHud chatHud = client.inGameHud.getChatHud();
        ChatHudAccessor chatHudAccessor = (ChatHudAccessor) chatHud;
        List<ChatHudLine> messages = chatHudAccessor.getMessages();
        int lineIndex = -1;
        ChatHudLine targetLine = null;

        for (int i = 0; i < messages.size(); i++) {
            ChatHudLine line = messages.get(i);
            Text lineContent = line.content();

            if (lineContent.equals(originalMessage) || (!lineContent.getSiblings().isEmpty() && lineContent.getSiblings().get(0).equals(originalMessage))) {
                lineIndex = i;
                targetLine = line;
                break;
            }
        }

        if (targetLine == null) {
            LOGGER.error("Could not find chat line to update for messageId: {}", messageId);
            MessageUtils.MESSAGES_BY_UUID.remove(messageId);
            return;
        }

        updateExecutorServiceIfNeeded();

        ModConfig modConfig = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        boolean isAutoTranslate = modConfig.chatTranslate.output.auto_translate;
        boolean isStreaming = modConfig.chatTranslate.output.streaming_response;
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
            try {
                ChatTranslateConfig.ChatOutputTranslateConfig chatOutPutConfig = modConfig.chatTranslate.output;

                ProviderSettings settings = ProviderSettings.fromChatOutputConfig(chatOutPutConfig);
                LLM llm = new LLM(settings);

                StylePreserver.ExtractionResult extraction = StylePreserver.extractAndMarkWithTags(originalMessage);
                String textToTranslate = extraction.markedText;
                Map<Integer, Style> styleMap = extraction.styleMap;

                List<OpenAIRequest.Message> apiMessages = getMessages(chatOutPutConfig, textToTranslate);

                LOGGER.info("Starting translation for message ID: {}. Marked text: {}", messageId, textToTranslate);

                if (chatOutPutConfig.streaming_response) {
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
                LOGGER.error("[Translate-Thread] Exception for message ID: {}", messageId, e);
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
            MessageUtils.MESSAGES_BY_UUID.remove(messageId);
        });
    }

    @NotNull
    private static List<OpenAIRequest.Message> getMessages(ChatTranslateConfig.ChatOutputTranslateConfig chatOutputTranslateConfig, String textToTranslate) {
        String suffix;

        if (chatOutputTranslateConfig.llm_provider == Provider.OPENAI) {
            suffix = chatOutputTranslateConfig.openapi.system_prompt_suffix;
        } else {
            suffix = chatOutputTranslateConfig.ollama.system_prompt_suffix;
        }

        String systemPrompt = "You are a chat translation assistant, translating text into " + chatOutputTranslateConfig.target_language + ". You will receive text with style tags, such as `s0>text</s0>`. Please keep these tags wrapping the translated text paragraphs. For example, `<s0>Hello</s0> world` translated into French is `<s0>Bonjour</s0> le monde`. Only output the translation result, keeping all formatting characters, and keeping all words that are uncertain to translate." + suffix;
        return List.of(
                new OpenAIRequest.Message("system", systemPrompt),
                new OpenAIRequest.Message("user", textToTranslate)
        );
    }
} 