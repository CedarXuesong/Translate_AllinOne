package com.cedarxuesong.translate_allinone.gui.configui.render;

import com.cedarxuesong.translate_allinone.gui.configui.model.UiRect;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import static com.cedarxuesong.translate_allinone.gui.configui.render.ConfigUiDraw.drawOutline;

public final class ConfigUiModalSupport {
    private ConfigUiModalSupport() {
    }

    public static boolean isAnyModalOpen(
            boolean addProviderModalOpen,
            boolean modelSettingsModalOpen,
            boolean customParametersModalOpen,
            boolean resetConfirmModalOpen,
            boolean updateNoticeModalOpen
    ) {
        return addProviderModalOpen || modelSettingsModalOpen || customParametersModalOpen || resetConfirmModalOpen || updateNoticeModalOpen;
    }

    public static boolean isInsideOpenModal(
            double x,
            double y,
            int screenWidth,
            int screenHeight,
            boolean addProviderModalOpen,
            boolean modelSettingsModalOpen,
            boolean customParametersModalOpen,
            boolean resetConfirmModalOpen,
            boolean updateNoticeModalOpen
    ) {
        if (updateNoticeModalOpen && updateNoticeModalRect(screenWidth, screenHeight).contains(x, y)) {
            return true;
        }
        if (resetConfirmModalOpen && resetConfirmModalRect(screenWidth, screenHeight).contains(x, y)) {
            return true;
        }
        if (customParametersModalOpen && customParametersModalRect(screenWidth, screenHeight).contains(x, y)) {
            return true;
        }
        if (addProviderModalOpen && addProviderModalRect(screenWidth, screenHeight).contains(x, y)) {
            return true;
        }
        return modelSettingsModalOpen && modelSettingsModalRect(screenWidth, screenHeight).contains(x, y);
    }

    public static UiRect addProviderModalRect(int screenWidth, int screenHeight) {
        int width = 420;
        int height = 236;
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;
        return new UiRect(x, y, width, height);
    }

    public static UiRect modelSettingsModalRect(int screenWidth, int screenHeight) {
        int width = 560;
        int height = 340;
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;
        return new UiRect(x, y, width, height);
    }

    public static UiRect customParametersModalRect(int screenWidth, int screenHeight) {
        int width = Math.min(900, screenWidth - 80);
        int height = Math.min(520, screenHeight - 90);
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;
        return new UiRect(x, y, width, height);
    }

    public static UiRect resetConfirmModalRect(int screenWidth, int screenHeight) {
        int width = 420;
        int height = 170;
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;
        return new UiRect(x, y, width, height);
    }

    public static UiRect updateNoticeModalRect(int screenWidth, int screenHeight) {
        int width = 460;
        int height = 190;
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;
        return new UiRect(x, y, width, height);
    }

    public static void renderModalShell(
            DrawContext context,
            TextRenderer textRenderer,
            UiRect rect,
            Text title,
            int backgroundColor,
            int borderColor,
            int textColor
    ) {
        context.fill(rect.x, rect.y, rect.right(), rect.bottom(), backgroundColor);
        drawOutline(context, rect.x, rect.y, rect.width, rect.height, borderColor);

        context.drawText(textRenderer, title, rect.x + 16, rect.y + 12, textColor, false);
        context.fill(rect.x + 16, rect.y + 34, rect.right() - 16, rect.y + 35, borderColor);
    }
}
