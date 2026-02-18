package com.cedarxuesong.translate_allinone.gui.configui.controls;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class IntSliderBlock {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Text label;
    private final int min;
    private final int max;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private final TextRenderer textRenderer;
    private final Style style;

    private double visualPosition;
    private double targetPosition;
    private double segmentPulse;
    private int value;
    private boolean dragging;

    public IntSliderBlock(
            int x,
            int y,
            int width,
            int height,
            Text label,
            int min,
            int max,
            IntSupplier getter,
            IntConsumer setter,
            TextRenderer textRenderer,
            Style style
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
        this.getter = getter;
        this.setter = setter;
        this.textRenderer = textRenderer;
        this.style = style;

        int raw = getter.getAsInt();
        this.value = clampInt(raw, this.min, this.max);
        if (raw != this.value) {
            setter.accept(this.value);
        }
        this.visualPosition = valueToPosition(this.value);
        this.targetPosition = this.visualPosition;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void startDrag(double mouseX) {
        dragging = true;
        applyMousePosition(mouseX);
    }

    public void dragTo(double mouseX) {
        if (!dragging) {
            return;
        }
        applyMousePosition(mouseX);
    }

    public void release() {
        if (!dragging) {
            return;
        }
        dragging = false;
        int snapped = valueFromPosition(targetPosition);
        commitValue(snapped);
        targetPosition = valueToPosition(value);
    }

    public void update(double deltaSeconds) {
        if (deltaSeconds <= 0.0) {
            return;
        }

        if (!dragging) {
            int latest = clampInt(getter.getAsInt(), min, max);
            if (latest != value) {
                value = latest;
                targetPosition = valueToPosition(latest);
                segmentPulse = Math.max(segmentPulse, 0.45);
            }
        }

        double distance = Math.abs(targetPosition - visualPosition);
        double distanceBoost = 1.0 + easeOutCubic(Math.min(1.0, distance * 2.4)) * 0.75;
        double speed = (dragging ? 20.0 : 14.0) * distanceBoost;
        double blend = 1.0 - Math.exp(-deltaSeconds * speed);
        visualPosition += (targetPosition - visualPosition) * blend;
        if (Math.abs(targetPosition - visualPosition) < 0.0005) {
            visualPosition = targetPosition;
        }

        segmentPulse = Math.max(0.0, segmentPulse - deltaSeconds * 3.0);
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        boolean hovered = contains(mouseX, mouseY);
        int background = hovered || dragging ? style.blockHoverColor() : style.blockColor();
        context.fill(x, y, x + width, y + height, background);
        drawOutline(context, x, y, width, height, style.borderColor());

        int textY = y + 5;
        int trackY = y + height - 7;

        Text valueText = Text.literal(Integer.toString(value));
        int valueWidth = textRenderer.getWidth(valueText);
        int valueX = x + width - valueWidth - 6;
        int labelMaxWidth = Math.max(40, width - valueWidth - 20);
        String clippedLabel = textRenderer.trimToWidth(label.getString(), labelMaxWidth);
        context.drawText(textRenderer, clippedLabel, x + 6, textY, style.textColor(), false);
        context.drawText(textRenderer, valueText, valueX, textY, dragging ? style.textAccentColor() : style.textMutedColor(), false);

        int trackLeft = trackLeft();
        int trackRight = trackRight();
        context.fill(trackLeft, trackY, trackRight, trackY + 2, style.sliderTrackColor());

        int knobX = (int) Math.round(trackLeft + (trackRight - trackLeft) * visualPosition);
        double pulseFactor = easeOutCubic(segmentPulse);
        int fillColor = mixRgb(style.sliderFillColor(), style.sliderFillActiveColor(), pulseFactor);
        context.fill(trackLeft, trackY, knobX + 1, trackY + 2, fillColor);

        int knobHalf = dragging ? 4 : 3;
        int pulseSize = (int) Math.round(3.0 * pulseFactor);
        if (pulseSize > 0) {
            int glowAlpha = (int) Math.round(70 + pulseFactor * 90.0);
            int glowColor = withAlpha(style.blockAccentColor(), glowAlpha);
            context.fill(
                    knobX - knobHalf - pulseSize,
                    trackY - 3 - pulseSize / 2,
                    knobX + knobHalf + pulseSize + 1,
                    trackY + 5 + pulseSize / 2,
                    glowColor
            );
        }

        int knobColor = dragging ? style.sliderKnobActiveColor() : style.sliderKnobColor();
        context.fill(knobX - knobHalf, trackY - 3, knobX + knobHalf + 1, trackY + 5, knobColor);
    }

    private void applyMousePosition(double mouseX) {
        double position = positionFromMouse(mouseX);
        targetPosition = position;
        commitValue(valueFromPosition(position));
    }

    private void commitValue(int nextValue) {
        int clamped = clampInt(nextValue, min, max);
        if (clamped == value) {
            return;
        }
        value = clamped;
        setter.accept(value);
        segmentPulse = 1.0;
    }

    private double positionFromMouse(double mouseX) {
        int left = trackLeft();
        int right = trackRight();
        if (right <= left) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, (mouseX - left) / (double) (right - left)));
    }

    private double valueToPosition(int value) {
        int span = max - min;
        if (span <= 0) {
            return 0.0;
        }
        return (clampInt(value, min, max) - min) / (double) span;
    }

    private int valueFromPosition(double position) {
        int span = max - min;
        if (span <= 0) {
            return min;
        }
        int offset = (int) Math.round(Math.max(0.0, Math.min(1.0, position)) * span);
        return clampInt(min + offset, min, max);
    }

    private int trackLeft() {
        return x + 8;
    }

    private int trackRight() {
        return x + width - 8;
    }

    private void drawOutline(DrawContext context, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private int mixRgb(int fromColor, int toColor, double t) {
        double clamped = Math.max(0.0, Math.min(1.0, t));
        int fromA = fromColor >>> 24;
        int fromR = (fromColor >>> 16) & 0xFF;
        int fromG = (fromColor >>> 8) & 0xFF;
        int fromB = fromColor & 0xFF;
        int toA = toColor >>> 24;
        int toR = (toColor >>> 16) & 0xFF;
        int toG = (toColor >>> 8) & 0xFF;
        int toB = toColor & 0xFF;

        int a = (int) Math.round(fromA + (toA - fromA) * clamped);
        int r = (int) Math.round(fromR + (toR - fromR) * clamped);
        int g = (int) Math.round(fromG + (toG - fromG) * clamped);
        int b = (int) Math.round(fromB + (toB - fromB) * clamped);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int withAlpha(int color, int alpha) {
        int clampedAlpha = clampInt(alpha, 0, 255);
        return (clampedAlpha << 24) | (color & 0x00FFFFFF);
    }

    private double easeOutCubic(double t) {
        double clamped = Math.max(0.0, Math.min(1.0, t));
        double inverted = 1.0 - clamped;
        return 1.0 - inverted * inverted * inverted;
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Style(
            int blockColor,
            int blockHoverColor,
            int borderColor,
            int textColor,
            int textAccentColor,
            int textMutedColor,
            int blockAccentColor,
            int sliderTrackColor,
            int sliderFillColor,
            int sliderFillActiveColor,
            int sliderKnobColor,
            int sliderKnobActiveColor
    ) {
    }
}
