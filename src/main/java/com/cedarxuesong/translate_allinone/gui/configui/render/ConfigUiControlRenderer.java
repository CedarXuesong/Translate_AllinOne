package com.cedarxuesong.translate_allinone.gui.configui.render;

import com.cedarxuesong.translate_allinone.gui.configui.controls.ActionBlock;
import com.cedarxuesong.translate_allinone.gui.configui.controls.CheckboxBlock;
import com.cedarxuesong.translate_allinone.gui.configui.controls.GroupBox;
import com.cedarxuesong.translate_allinone.gui.configui.controls.IntSliderBlock;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

import static com.cedarxuesong.translate_allinone.gui.configui.render.ConfigUiDraw.drawOutline;

public final class ConfigUiControlRenderer {
    private ConfigUiControlRenderer() {
    }

    public static void drawActionBlocks(
            DrawContext context,
            TextRenderer textRenderer,
            List<ActionBlock> blocks,
            int mouseX,
            int mouseY,
            int borderColor
    ) {
        for (ActionBlock block : blocks) {
            boolean hovered = block.contains(mouseX, mouseY);
            context.fill(block.x(), block.y(), block.x() + block.width(), block.y() + block.height(), hovered ? block.hoverColor() : block.color());
            drawOutline(context, block.x(), block.y(), block.width(), block.height(), borderColor);

            Text label = block.label();
            int textY = block.y() + 6;
            int textX = block.centered()
                    ? block.x() + (block.width() - textRenderer.getWidth(label)) / 2
                    : block.x() + 6;
            context.drawText(textRenderer, label, textX, textY, block.textColor(), false);
        }
    }

    public static void drawSliderBlocks(DrawContext context, List<IntSliderBlock> sliders, int mouseX, int mouseY) {
        for (IntSliderBlock slider : sliders) {
            slider.render(context, mouseX, mouseY);
        }
    }

    public static void drawGroupBoxes(DrawContext context, TextRenderer textRenderer, List<GroupBox> boxes) {
        for (GroupBox box : boxes) {
            if (box.width() <= 0 || box.height() <= 0) {
                continue;
            }

            GroupBox.Style style = box.style();
            context.fill(box.x(), box.y(), box.x() + box.width(), box.y() + box.height(), style.backgroundColor());
            drawOutline(context, box.x(), box.y(), box.width(), box.height(), style.borderColor());

            Text title = box.title();
            if (title == null || title.getString().isBlank()) {
                continue;
            }

            int titleX = box.x() + 10;
            int titleY = box.y() + 5;
            int titleWidth = textRenderer.getWidth(title);
            context.fill(titleX - 3, titleY - 1, titleX + titleWidth + 3, titleY + 9, style.titleBackgroundColor());
            context.drawText(textRenderer, title, titleX, titleY, style.titleColor(), false);
        }
    }

    public static void drawCheckboxBlocks(
            DrawContext context,
            TextRenderer textRenderer,
            List<CheckboxBlock> blocks,
            int mouseX,
            int mouseY
    ) {
        for (CheckboxBlock block : blocks) {
            block.render(context, textRenderer, mouseX, mouseY);
        }
    }
}
