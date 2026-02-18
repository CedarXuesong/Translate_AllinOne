package com.cedarxuesong.translate_allinone.gui.configui.render;

import com.cedarxuesong.translate_allinone.gui.configui.model.UiRect;
import net.minecraft.client.gui.DrawContext;

public final class ConfigUiDraw {
    private ConfigUiDraw() {
    }

    public static void drawOutline(DrawContext context, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    public static void withScissor(DrawContext context, UiRect rect, Runnable drawer) {
        if (drawer == null) {
            return;
        }

        if (rect == null || rect.width <= 0 || rect.height <= 0) {
            drawer.run();
            return;
        }

        context.enableScissor(rect.x, rect.y, rect.right(), rect.bottom());
        try {
            drawer.run();
        } finally {
            context.disableScissor();
        }
    }
}
