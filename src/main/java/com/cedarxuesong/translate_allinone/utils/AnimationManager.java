package com.cedarxuesong.translate_allinone.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.ColorHelper;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class AnimationManager {
    private static final int DARK_GREY = 0x555555;
    private static final int LIGHT_GREY = 0xAAAAAA;
    private static final int DARK_RED = 0x7A1E1E;
    private static final int LIGHT_RED = 0xFF5A5A;
    private static final long STATE_CLEANUP_INTERVAL_MS = 10_000L;
    private static final long STATE_STALE_AFTER_MS = 30_000L;

    private static final Pattern STRIP_FORMATTING_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    private static final ConcurrentHashMap<String, AlertTransitionState> ALERT_TRANSITIONS = new ConcurrentHashMap<>();
    private static volatile long lastStateCleanupTime = 0L;

    private static final class AlertTransitionState {
        private float alertProgress;
        private long lastUpdateTime;
        private long lastAccessTime;

        private AlertTransitionState(long now) {
            this.lastUpdateTime = now;
            this.lastAccessTime = now;
            this.alertProgress = 0.0f;
        }
    }


    public static String stripFormatting(String text) {
        return STRIP_FORMATTING_PATTERN.matcher(text).replaceAll("");
    }

    public static MutableText getAnimatedText(String text) {
        String plainText = stripFormatting(text);
        MutableText animatedText = Text.empty();
        long time = System.currentTimeMillis();

        for (int i = 0; i < plainText.length(); i++) {
            float sine = (float) (Math.sin(time / 200.0 + i / 5.0) + 1.0) / 2.0f;
            int color = ColorHelper.lerp(sine, DARK_GREY, LIGHT_GREY);
            animatedText.append(Text.literal(String.valueOf(plainText.charAt(i)))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
        }
        return animatedText;
    }

    public static MutableText getAnimatedStyledText(Text originalText) {
        return getAnimatedStyledText(originalText, null, false);
    }

    public static MutableText getAnimatedStyledText(Text originalText, String animationKey, boolean alertMissingKeys) {
        MutableText animatedText = Text.empty();
        long time = System.currentTimeMillis();
        AtomicInteger charIndex = new AtomicInteger(0);
        float alertProgress = getAlertProgress(animationKey, alertMissingKeys, time);

        originalText.visit((style, s) -> {
            for (int i = 0; i < s.length(); i++) {
                float baseSine = (float) (Math.sin(time / 200.0 + charIndex.get() / 5.0) + 1.0) / 2.0f;
                float alertSine = (float) (Math.sin(time / 120.0 + charIndex.get() / 2.5) + 1.0) / 2.0f;

                int baseColor = ColorHelper.lerp(baseSine, DARK_GREY, LIGHT_GREY);
                int alertColor = ColorHelper.lerp(alertSine, DARK_RED, LIGHT_RED);
                int color = ColorHelper.lerp(alertProgress, baseColor, alertColor);

                Style newStyle = style.withColor(TextColor.fromRgb(color));

                animatedText.append(Text.literal(String.valueOf(s.charAt(i))).setStyle(newStyle));
                charIndex.incrementAndGet();
            }
            return Optional.empty();
        }, Style.EMPTY);

        return animatedText;
    }

    private static float getAlertProgress(String animationKey, boolean alertMissingKeys, long now) {
        if (animationKey == null || animationKey.isEmpty()) {
            return alertMissingKeys ? 1.0f : 0.0f;
        }

        AlertTransitionState state = ALERT_TRANSITIONS.computeIfAbsent(animationKey, key -> new AlertTransitionState(now));
        float target = alertMissingKeys ? 1.0f : 0.0f;

        synchronized (state) {
            long elapsedMs = Math.max(1L, now - state.lastUpdateTime);
            float elapsedSeconds = Math.min(elapsedMs / 1000.0f, 0.25f);
            float blend = 1.0f - (float) Math.exp(-6.0f * elapsedSeconds);
            state.alertProgress += (target - state.alertProgress) * blend;
            state.lastUpdateTime = now;
            state.lastAccessTime = now;
        }

        cleanupTransitionStates(now);
        return state.alertProgress;
    }

    private static void cleanupTransitionStates(long now) {
        if (now - lastStateCleanupTime < STATE_CLEANUP_INTERVAL_MS) {
            return;
        }
        lastStateCleanupTime = now;

        ALERT_TRANSITIONS.entrySet().removeIf(entry -> {
            AlertTransitionState state = entry.getValue();
            synchronized (state) {
                return (now - state.lastAccessTime) > STATE_STALE_AFTER_MS && state.alertProgress < 0.01f;
            }
        });
    }
}
