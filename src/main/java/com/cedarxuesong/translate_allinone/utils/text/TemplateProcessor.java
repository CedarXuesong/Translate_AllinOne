package com.cedarxuesong.translate_allinone.utils.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateProcessor {

    // This regex captures:
    // 1. Numbers with thousand separators (e.g., 1,000,000)
    // 2. Decimal numbers (e.g., 7.5, .5)
    // 3. Simple integers (e.g., 7, 121)
    // 4. Version numbers or dates (e.g., 1.2.3, 2025-07-05)
    // 5. Numbers immediately after legacy color prefix '§' are excluded.
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(?<!§)(\\d+([.,/-]\\d+)+|\\d{1,3}(,\\d{3})*(\\.\\d+)?|\\d+\\.\\d+|\\.\\d+|\\d+)");

    public record TemplateExtractionResult(String template, List<String> values) {
    }

    /**
     * Extracts dynamic numerical values from a string and replaces them with placeholders.
     * This method assumes that style information is handled separately (e.g., by StylePreserver).
     *
     * @param text The input string, which may contain style markers but not raw color codes intended for this processor.
     * @return A result object containing the template and the list of extracted values.
     */
    public static TemplateExtractionResult extract(String text) {
        List<String> values = new ArrayList<>();
        Matcher numberMatcher = NUMBER_PATTERN.matcher(text);
        StringBuilder templateBuffer = new StringBuilder();

        while (numberMatcher.find()) {
            int matchStart = numberMatcher.start();
            int matchEnd = numberMatcher.end();

            if (isInsideNumericPlaceholder(text, matchStart, matchEnd)) {
                numberMatcher.appendReplacement(templateBuffer, Matcher.quoteReplacement(numberMatcher.group()));
                continue;
            }

            values.add(numberMatcher.group());
            String placeholder = "{d" + values.size() + "}";
            numberMatcher.appendReplacement(templateBuffer, Matcher.quoteReplacement(placeholder));
        }

        numberMatcher.appendTail(templateBuffer);
        return new TemplateExtractionResult(templateBuffer.toString(), values);
    }

    private static boolean isInsideNumericPlaceholder(String text, int start, int end) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        int runStart = start;
        while (runStart > 0 && Character.isDigit(text.charAt(runStart - 1))) {
            runStart--;
        }

        int runEnd = end;
        while (runEnd < text.length() && Character.isDigit(text.charAt(runEnd))) {
            runEnd++;
        }

        if (runStart < 2 || runEnd >= text.length()) {
            return false;
        }

        return text.charAt(runStart - 2) == '{'
                && text.charAt(runStart - 1) == 'd'
                && text.charAt(runEnd) == '}';
    }

    public static String reassemble(String translatedTemplate, List<String> values) {
        // This method remains the same, as the AI should preserve the color codes.
        for (int i = 0; i < values.size(); i++) {
            String placeholder = "{d" + (i + 1) + "}";
            // Use quoteReplacement to handle any special characters in the replacement string.
            translatedTemplate = translatedTemplate.replaceFirst(Pattern.quote(placeholder), Matcher.quoteReplacement(values.get(i)));
        }
        return translatedTemplate;
    }
} 
