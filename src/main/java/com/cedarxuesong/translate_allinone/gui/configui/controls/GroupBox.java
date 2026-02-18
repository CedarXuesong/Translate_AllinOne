package com.cedarxuesong.translate_allinone.gui.configui.controls;

import net.minecraft.text.Text;

public record GroupBox(
        int x,
        int y,
        int width,
        int height,
        Text title,
        Style style
) {
    public record Style(
            int backgroundColor,
            int borderColor,
            int titleColor,
            int titleBackgroundColor
    ) {
    }
}
