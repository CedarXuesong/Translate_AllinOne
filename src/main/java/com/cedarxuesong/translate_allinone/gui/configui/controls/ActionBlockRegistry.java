package com.cedarxuesong.translate_allinone.gui.configui.controls;

import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Supplier;

public final class ActionBlockRegistry {
    private final List<ActionBlock> blocks;
    private final int defaultColor;
    private final int defaultHoverColor;
    private final int defaultTextColor;

    public ActionBlockRegistry(List<ActionBlock> blocks, int defaultColor, int defaultHoverColor, int defaultTextColor) {
        this.blocks = blocks;
        this.defaultColor = defaultColor;
        this.defaultHoverColor = defaultHoverColor;
        this.defaultTextColor = defaultTextColor;
    }

    public void add(int x, int y, int width, int height, Text label, Runnable action) {
        add(x, y, width, height, () -> label, action, defaultColor, defaultHoverColor, defaultTextColor, false);
    }

    public void add(int x, int y, int width, int height, Supplier<Text> labelSupplier, Runnable action) {
        add(x, y, width, height, labelSupplier, action, defaultColor, defaultHoverColor, defaultTextColor, false);
    }

    public void add(
            int x,
            int y,
            int width,
            int height,
            Text label,
            Runnable action,
            int color,
            int hoverColor,
            int textColor,
            boolean centered
    ) {
        add(x, y, width, height, () -> label, action, color, hoverColor, textColor, centered);
    }

    public void add(
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
        blocks.add(new ActionBlock(x, y, width, height, labelSupplier, action, color, hoverColor, textColor, centered));
    }
}
