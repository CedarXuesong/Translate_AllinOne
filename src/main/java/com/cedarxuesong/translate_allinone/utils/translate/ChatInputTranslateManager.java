package com.cedarxuesong.translate_allinone.utils.translate;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.config.ProviderRouteResolver;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ChatTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.llmapi.LLM;
import com.cedarxuesong.translate_allinone.utils.llmapi.ProviderSettings;
import com.cedarxuesong.translate_allinone.utils.llmapi.openai.OpenAIRequest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ChatInputTranslateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatInputTranslateManager.class);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Chat-Input-Translate-Thread");
        t.setDaemon(true);
        return t;
    });

    private static final AtomicBoolean isTranslating = new AtomicBoolean(false);
    private static final AtomicReference<String> originalTextRef = new AtomicReference<>("");

    public static void translate(TextFieldWidget chatField) {
        if (!isTranslating.compareAndSet(false, true)) {
            return; // Already translating
        }

        ChatTranslateConfig.ChatInputTranslateConfig inputConfig = Translate_AllinOne.getConfig().chatTranslate.input;
        if (!inputConfig.enabled) {
            isTranslating.set(false);
            return;
        }

        String currentText = chatField.getText();
        if (currentText.trim().isEmpty()) {
            isTranslating.set(false);
            return;
        }
        originalTextRef.set(currentText);


        executor.submit(() -> {
            String requestContext = "route=chat_input";
            try {
                ApiProviderProfile providerProfile = ProviderRouteResolver.resolve(
                        Translate_AllinOne.getConfig(),
                        ProviderRouteResolver.Route.CHAT_INPUT
                );
                if (providerProfile == null) {
                    throw new IllegalStateException("No routed model selected for chat input translation");
                }

                ProviderSettings settings = ProviderSettings.fromProviderProfile(providerProfile);
                LLM llm = new LLM(settings);

                List<OpenAIRequest.Message> apiMessages = getMessages(providerProfile, inputConfig.target_language, originalTextRef.get());
                requestContext = buildRequestContext(providerProfile, inputConfig.target_language, originalTextRef.get(), apiMessages, inputConfig.streaming_response);

                if (inputConfig.streaming_response) {
                    final StringBuilder rawResponseBuffer = new StringBuilder();
                    final StringBuilder visibleContentBuffer = new StringBuilder();
                    final AtomicBoolean inThinkTag = new AtomicBoolean(false);

                    MinecraftClient.getInstance().execute(() -> chatField.setText("Connecting...")); // Clear field for streaming

                    llm.getStreamingCompletion(apiMessages).forEach(chunk -> {
                        rawResponseBuffer.append(chunk);

                        while (true) {
                            if (inThinkTag.get()) {
                                int endTagIndex = rawResponseBuffer.indexOf("</think>");
                                if (endTagIndex != -1) {
                                    inThinkTag.set(false);
                                    rawResponseBuffer.delete(0, endTagIndex + "</think>".length());

                                    // Restore the visible content so far
                                    String currentTranslation = visibleContentBuffer.toString().stripLeading();
                                    MinecraftClient.getInstance().execute(() -> {
                                        chatField.setText(currentTranslation);
                                        chatField.setCursor(currentTranslation.length(), false);
                                    });
                                    continue; // Check for more tags in the same chunk
                                }
                                break; // Incomplete tag, wait for more chunks
                            } else {
                                int startTagIndex = rawResponseBuffer.indexOf("<think>");
                                if (startTagIndex != -1) {
                                    // Found a think tag. Append content before it to visible buffer.
                                    String translationPart = rawResponseBuffer.substring(0, startTagIndex);
                                    visibleContentBuffer.append(translationPart);
                                    rawResponseBuffer.delete(0, startTagIndex + "<think>".length());
                                    inThinkTag.set(true);

                                    // Now display "Thinking..."
                                    MinecraftClient.getInstance().execute(() -> chatField.setText("Thinking..."));

                                    continue; // Check for more tags
                                } else {
                                    // No think tag, just regular content
                                    visibleContentBuffer.append(rawResponseBuffer.toString());
                                    rawResponseBuffer.setLength(0);
                                    String currentTranslation = visibleContentBuffer.toString().stripLeading();
                                    MinecraftClient.getInstance().execute(() -> {
                                        chatField.setText(currentTranslation);
                                        chatField.setCursor(currentTranslation.length(), false);
                                    });
                                    break; // Wait for more chunks
                                }
                            }
                        }
                    });

                    // Final update after stream is complete, using the accumulated visible content
                    MinecraftClient.getInstance().execute(() -> {
                        String finalTranslation = visibleContentBuffer.toString().stripLeading();
                        chatField.setText(finalTranslation);
                        chatField.setCursor(finalTranslation.length(), false);
                    });
                } else {
                    MinecraftClient.getInstance().execute(() -> chatField.setText("Translating..."));
                    String result = llm.getCompletion(apiMessages).join();
                    final String finalTranslation = result.stripLeading();
                    MinecraftClient.getInstance().execute(() -> {
                        chatField.setText(finalTranslation);
                        chatField.setCursor(finalTranslation.length(), false);
                    });
                }
            } catch (Exception e) {
                LOGGER.error("[Chat-Input-Translate] Exception during translation. context={}", requestContext, e);
                MinecraftClient.getInstance().execute(() -> {
                    Text errorMessage = Text.literal("Chat Input Translation Error: " + e.getMessage()).formatted(Formatting.RED);
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(errorMessage);
                    chatField.setText(originalTextRef.get()); // Restore original on error
                    chatField.setCursor(originalTextRef.get().length(), false);
                });
            } finally {
                isTranslating.set(false);
                originalTextRef.set("");
            }
        });
    }

    @NotNull
    private static List<OpenAIRequest.Message> getMessages(ApiProviderProfile providerProfile, String targetLanguage, String textToTranslate) {
        String systemPrompt = PromptMessageBuilder.appendSystemPromptSuffix(
                "You are a deterministic translation engine.\n"
                        + "Target language: " + targetLanguage + ".\n"
                        + "\n"
                        + "Rules (highest priority first):\n"
                        + "1) Output only the final translated text. No explanation, markdown, or quotes.\n"
                        + "2) Preserve tokens exactly: § color/style codes, placeholders (%s %d %f {d1}), URLs, numbers, command prefix (/), <...>, {...}, \\n, \\t.\n"
                        + "3) If a term is uncertain, keep only that term unchanged and still translate surrounding text.\n"
                        + "4) Keep punctuation and spacing stable unless translation naturally requires changes.\n"
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
            String originalText,
            List<OpenAIRequest.Message> messages,
            boolean streaming
    ) {
        String providerId = profile == null ? "" : profile.id;
        String modelId = profile == null ? "" : profile.model_id;
        int messageCount = messages == null ? 0 : messages.size();
        String roles = messages == null
                ? "[]"
                : messages.stream().map(message -> message == null ? "null" : String.valueOf(message.role)).collect(java.util.stream.Collectors.joining(",", "[", "]"));
        String sample = truncate(normalizeWhitespace(originalText), 160);
        return "route=chat_input"
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
