package com.cedarxuesong.translate_allinone.utils.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StylePreserver {

    private static final char PLACEHOLDER_START_CHAR = '\uE000';
    // Regex to find a placeholder, its content, and the closing placeholder.
    // Placeholders are characters in the Private Use Area.
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
            "([\uE000-\uF8FF])(.*?)\\1",
            Pattern.DOTALL
    );

    public static class ExtractionResult {
        public final String markedText;
        public final Map<Integer, Style> styleMap;

        public ExtractionResult(String markedText, Map<Integer, Style> styleMap) {
            this.markedText = markedText;
            this.styleMap = styleMap;
        }
    }

    public static ExtractionResult extractAndMark(Text message) {
        StringBuilder markedText = new StringBuilder();
        Map<Integer, Style> styleMap = new HashMap<>();
        AtomicInteger counter = new AtomicInteger(0); // Start from 0 to align with char offset

        message.visit((style, string) -> {
            if (!string.isEmpty()) {
                if (!style.isEmpty()) {
                    int id = counter.getAndIncrement();
                    styleMap.put(id, style);
                    char placeholder = (char) (PLACEHOLDER_START_CHAR + id);
                    markedText.append(placeholder);
                    markedText.append(string);
                    markedText.append(placeholder);
                } else {
                    markedText.append(string);
                }
            }
            return Optional.empty();
        }, Style.EMPTY);

        return new ExtractionResult(markedText.toString(), styleMap);
    }
    
    public static ExtractionResult extractAndMarkWithTags(Text message) {
        StringBuilder markedText = new StringBuilder();
        Map<Integer, Style> styleMap = new HashMap<>();
        AtomicInteger counter = new AtomicInteger(0);

        message.visit((style, string) -> {
            if (!string.isEmpty()) {
                if (!style.isEmpty()) {
                    int id = counter.getAndIncrement();
                    styleMap.put(id, style);
                    markedText.append("<s").append(id).append(">");
                    markedText.append(string);
                    markedText.append("</s").append(id).append(">");
                } else {
                    markedText.append(string);
                }
            }
            return Optional.empty();
        }, Style.EMPTY);

        return new ExtractionResult(markedText.toString(), styleMap);
    }

    public static Text reapplyStyles(String translatedText, Map<Integer, Style> styleMap) {
        MutableText resultText = Text.empty();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(translatedText);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                resultText.append(Text.literal(translatedText.substring(lastEnd, matcher.start())));
            }
            
            char placeholder = matcher.group(1).charAt(0);
            int id = placeholder - PLACEHOLDER_START_CHAR;
            String content = matcher.group(2);
            Style style = styleMap.getOrDefault(id, Style.EMPTY);
            
            resultText.append(Text.literal(content).setStyle(style));
            
            lastEnd = matcher.end();
        }

        if (lastEnd < translatedText.length()) {
            resultText.append(Text.literal(translatedText.substring(lastEnd)));
        }

        return resultText;
    }

    public static Text reapplyStylesFromTags(String translatedText, Map<Integer, Style> styleMap) {
        MutableText resultText = Text.empty();
        Pattern tagPattern = Pattern.compile("<s(\\d+)>(.*?)</s\\1>", Pattern.DOTALL);
        Matcher matcher = tagPattern.matcher(translatedText);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                resultText.append(Text.literal(translatedText.substring(lastEnd, matcher.start())));
            }

            int id = Integer.parseInt(matcher.group(1));
            String content = matcher.group(2);
            Style style = styleMap.getOrDefault(id, Style.EMPTY);

            resultText.append(Text.literal(content).setStyle(style));

            lastEnd = matcher.end();
        }

        if (lastEnd < translatedText.length()) {
            resultText.append(Text.literal(translatedText.substring(lastEnd)));
        }

        return resultText;
    }

    public static String toLegacyTemplate(String markedTemplate, Map<Integer, Style> styleMap) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(markedTemplate);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                sb.append(markedTemplate.substring(lastEnd, matcher.start()));
            }

            char placeholder = matcher.group(1).charAt(0);
            int id = placeholder - PLACEHOLDER_START_CHAR;
            String content = matcher.group(2);
            Style style = styleMap.getOrDefault(id, Style.EMPTY);

            sb.append(styleToLegacyFormatting(style));
            sb.append(content);

            lastEnd = matcher.end();
        }

        if (lastEnd < markedTemplate.length()) {
            sb.append(markedTemplate.substring(lastEnd));
        }

        return sb.toString();
    }

    private static String styleToLegacyFormatting(Style style) {
        if (style.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (style.getColor() != null) {
            for (Formatting f : Formatting.values()) {
                if (f.isColor() && f.getColorValue() != null && f.getColorValue().equals(style.getColor().getRgb())) {
                    sb.append('§').append(f.getCode());
                    break;
                }
            }
        }
        if (style.isBold()) sb.append("§l");
        if (style.isItalic()) sb.append("§o");
        if (style.isUnderlined()) sb.append("§n");
        if (style.isStrikethrough()) sb.append("§m");
        if (style.isObfuscated()) sb.append("§k");
        return sb.toString();
    }

    public static Text fromLegacyText(String text) {
        if (text == null || text.isEmpty()) {
            return Text.empty();
        }

        MutableText result = Text.empty();
        MutableText currentComponent = Text.literal("");
        Style currentStyle = Style.EMPTY;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§') {
                if (i + 1 >= text.length()) {
                    currentComponent.append("§");
                    break;
                }
                
                if (!currentComponent.getString().isEmpty()) {
                    result.append(currentComponent.setStyle(currentStyle));
                }
                currentComponent = Text.literal("");

                char formatChar = Character.toLowerCase(text.charAt(i + 1));
                Formatting formatting = Formatting.byCode(formatChar);

                if (formatting != null) {
                    if (formatting.isColor() || formatting == Formatting.RESET) {
                        currentStyle = Style.EMPTY.withFormatting(formatting);
                    } else {
                        currentStyle = currentStyle.withFormatting(formatting);
                    }
                }
                i++; 
            } else {
                currentComponent.append(String.valueOf(c));
            }
        }

        if (!currentComponent.getString().isEmpty()) {
            result.append(currentComponent.setStyle(currentStyle));
        }

        return result;
    }
} 