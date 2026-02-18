package com.cedarxuesong.translate_allinone.gui.configui.controls;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.cedarxuesong.translate_allinone.gui.configui.render.ConfigUiDraw.drawOutline;

public final class CheckboxBlock {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Supplier<Text> labelSupplier;
    private final Consumer<Boolean> changed;
    private final Style style;

    private boolean checked;
    private double visualChecked;
    private double targetChecked;

    public CheckboxBlock(
            int x,
            int y,
            int width,
            int height,
            Supplier<Text> labelSupplier,
            boolean checked,
            Consumer<Boolean> changed,
            Style style
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.labelSupplier = labelSupplier;
        this.checked = checked;
        this.changed = changed;
        this.style = style;
        this.visualChecked = checked ? 1.0 : 0.0;
        this.targetChecked = this.visualChecked;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public void toggle() {
        checked = !checked;
        targetChecked = checked ? 1.0 : 0.0;
        changed.accept(checked);
    }

    public void update(double deltaSeconds) {
        if (deltaSeconds <= 0.0) {
            return;
        }

        targetChecked = checked ? 1.0 : 0.0;
        double blend = 1.0 - Math.exp(-deltaSeconds * 16.0);
        visualChecked += (targetChecked - visualChecked) * blend;
        visualChecked = clamp01(visualChecked);
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        boolean hovered = contains(mouseX, mouseY);
        int background = checked ? style.blockSelectedColor() : style.blockColor();
        int backgroundHover = checked ? style.blockSelectedHoverColor() : style.blockHoverColor();
        context.fill(x, y, x + width, y + height, hovered ? backgroundHover : background);
        drawOutline(context, x, y, width, height, style.borderColor());

        int boxSize = 12;
        int boxX = x + 6;
        int boxY = y + (height - boxSize) / 2;

        double eased = easeOutCubic(visualChecked);
        int boxFill = style.checkboxOffColor();
        if (hovered) {
            boxFill = mixRgb(boxFill, style.checkboxOnColor(), 0.16);
        }
        context.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, boxFill);

        int boxBorder = mixRgb(style.checkboxBorderOffColor(), style.checkboxBorderOnColor(), eased);
        drawOutline(context, boxX, boxY, boxSize, boxSize, boxBorder);

        double indicatorScale = clamp01(eased * 0.78);
        int maxInset = 5;
        int inset = (int) Math.round((1.0 - indicatorScale) * maxInset);
        int innerLeft = boxX + inset;
        int innerTop = boxY + inset;
        int innerRight = boxX + boxSize - inset;
        int innerBottom = boxY + boxSize - inset;
        if (innerRight > innerLeft && innerBottom > innerTop) {
            int alpha = (int) Math.round(255.0 * indicatorScale);
            int indicatorColor = withAlpha(style.textCheckedColor(), alpha);
            context.fill(innerLeft, innerTop, innerRight, innerBottom, indicatorColor);
        }

        Text label = labelSupplier.get();
        int textX = boxX + boxSize + 6;
        int textY = y + 6;
        int textColor = mixRgb(style.textColor(), style.textCheckedColor(), eased);
        context.drawText(textRenderer, label, textX, textY, textColor, false);
    }

    private static double easeOutCubic(double value) {
        double clamped = clamp01(value);
        double inverse = 1.0 - clamped;
        return 1.0 - inverse * inverse * inverse;
    }

    private static double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }

    private static int withAlpha(int rgbColor, int alpha) {
        int clampedAlpha = Math.max(0, Math.min(255, alpha));
        return (clampedAlpha << 24) | (rgbColor & 0x00FFFFFF);
    }

    private static int mixRgb(int fromColor, int toColor, double ratio) {
        double t = clamp01(ratio);
        int fromA = (fromColor >>> 24) & 0xFF;
        int fromR = (fromColor >>> 16) & 0xFF;
        int fromG = (fromColor >>> 8) & 0xFF;
        int fromB = fromColor & 0xFF;

        int toA = (toColor >>> 24) & 0xFF;
        int toR = (toColor >>> 16) & 0xFF;
        int toG = (toColor >>> 8) & 0xFF;
        int toB = toColor & 0xFF;

        int a = (int) Math.round(fromA + (toA - fromA) * t);
        int r = (int) Math.round(fromR + (toR - fromR) * t);
        int g = (int) Math.round(fromG + (toG - fromG) * t);
        int b = (int) Math.round(fromB + (toB - fromB) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public record Style(
            int blockColor,
            int blockHoverColor,
            int blockSelectedColor,
            int blockSelectedHoverColor,
            int borderColor,
            int textColor,
            int textCheckedColor,
            int checkboxOffColor,
            int checkboxOnColor,
            int checkboxBorderOffColor,
            int checkboxBorderOnColor
    ) {
    }
}
