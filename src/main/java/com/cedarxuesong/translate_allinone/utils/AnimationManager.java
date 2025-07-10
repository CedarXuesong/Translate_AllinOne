package com.cedarxuesong.translate_allinone.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.ColorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class AnimationManager {
    private static final int DARK_GREY = 0x555555;
    private static final int LIGHT_GREY = 0xAAAAAA;

    private static final Pattern STRIP_FORMATTING_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");


    public static String stripFormatting(String text) {
        return STRIP_FORMATTING_PATTERN.matcher(text).replaceAll("");
    }

    public static MutableText getAnimatedText(String text) {
        String plainText = stripFormatting(text);
        MutableText animatedText = Text.empty();
        long time = System.currentTimeMillis();

        for (int i = 0, charIndex = 0; i < plainText.length(); i += Character.charCount(plainText.codePointAt(i)), charIndex++) {
            int codePoint = plainText.codePointAt(i);
            float sine = (float) (Math.sin(time / 200.0 + charIndex / 5.0) + 1.0) / 2.0f;
            int color = ColorHelper.lerp(sine, DARK_GREY, LIGHT_GREY);
            animatedText.append(Text.literal(new String(Character.toChars(codePoint)))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
        }
        return animatedText;
    }

    public static MutableText getAnimatedStyledText(Text originalText) {
        MutableText animatedText = Text.empty();
        long time = System.currentTimeMillis();
        AtomicInteger charIndex = new AtomicInteger(0);

        originalText.visit((style, s) -> {
            for (int i = 0; i < s.length(); ) {
                int codePoint = s.codePointAt(i);
                float sine = (float) (Math.sin(time / 200.0 + charIndex.get() / 5.0) + 1.0) / 2.0f;
                int color = ColorHelper.lerp(sine, DARK_GREY, LIGHT_GREY);

                Style newStyle = style.withColor(TextColor.fromRgb(color));

                animatedText.append(Text.literal(new String(Character.toChars(codePoint))).setStyle(newStyle));
                charIndex.incrementAndGet();
                i += Character.charCount(codePoint);
            }
            return Optional.empty();
        }, Style.EMPTY);

        return animatedText;
    }
}