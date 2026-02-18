package com.cedarxuesong.translate_allinone.gui.configui.controls;

import net.minecraft.text.Text;

import java.util.function.Supplier;

public final class ActionBlock {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Supplier<Text> labelSupplier;
    private final Runnable action;
    private final int color;
    private final int hoverColor;
    private final int textColor;
    private final boolean centered;

    public ActionBlock(
            int x,
            int y,
            int width,
            int height,
            Supplier<Text> labelSupplier,
            Runnable action,
            int color,
            int hoverColor,
            int textColor,
            boolean centered
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.labelSupplier = labelSupplier;
        this.action = action;
        this.color = color;
        this.hoverColor = hoverColor;
        this.textColor = textColor;
        this.centered = centered;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Text label() {
        return labelSupplier.get();
    }

    public void runAction() {
        action.run();
    }

    public int color() {
        return color;
    }

    public int hoverColor() {
        return hoverColor;
    }

    public int textColor() {
        return textColor;
    }

    public boolean centered() {
        return centered;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
