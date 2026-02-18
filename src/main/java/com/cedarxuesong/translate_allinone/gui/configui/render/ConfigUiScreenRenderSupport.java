package com.cedarxuesong.translate_allinone.gui.configui.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import static com.cedarxuesong.translate_allinone.gui.configui.render.ConfigUiDraw.drawOutline;

public final class ConfigUiScreenRenderSupport {
    private ConfigUiScreenRenderSupport() {
    }

    public static void renderChrome(
            DrawContext context,
            TextRenderer textRenderer,
            int screenWidth,
            int screenHeight,
            int topBarHeight,
            int leftPanelWidth,
            Text title,
            Text panelTitle,
            Text statusMessage,
            int statusColor,
            long statusExpireAtMillis,
            long nowMillis,
            boolean providersSectionSelected,
            Style style
    ) {
        context.fill(0, 0, screenWidth, screenHeight, style.colorBg());
        context.fill(0, 0, screenWidth, topBarHeight, style.colorTopBar());
        context.fill(0, topBarHeight, leftPanelWidth, screenHeight, style.colorLeftPanel());
        context.fill(leftPanelWidth, topBarHeight, screenWidth, screenHeight, style.colorMainPanel());

        drawOutline(context, 0, 0, screenWidth, topBarHeight, style.colorBorder());
        drawOutline(context, 0, topBarHeight, leftPanelWidth, screenHeight - topBarHeight, style.colorBorder());
        drawOutline(context, leftPanelWidth, topBarHeight, screenWidth - leftPanelWidth, screenHeight - topBarHeight, style.colorBorder());

        context.drawText(textRenderer, title, 14, 14, style.colorText(), false);
        context.drawText(textRenderer, panelTitle, leftPanelWidth + 20, topBarHeight + 8, style.colorText(), false);

        if (!statusMessage.getString().isEmpty() && nowMillis <= statusExpireAtMillis) {
            context.drawText(textRenderer, statusMessage, leftPanelWidth + 20, 14, statusColor, false);
        }

        if (providersSectionSelected) {
            // provider section groups are rendered through GroupBox controls.
        }
    }

    public static void renderModalOverlayAndShell(
            DrawContext context,
            TextRenderer textRenderer,
            int screenWidth,
            int screenHeight,
            int topBarHeight,
            boolean addProviderModalOpen,
            boolean modelSettingsModalOpen,
            boolean customParametersModalOpen,
            boolean resetConfirmModalOpen,
            boolean updateNoticeModalOpen,
            Text addProviderTitle,
            Text modelSettingsTitle,
            Text customParametersTitle,
            Text resetConfirmTitle,
            Text updateNoticeTitle,
            Style style
    ) {
        if (!ConfigUiModalSupport.isAnyModalOpen(addProviderModalOpen, modelSettingsModalOpen, customParametersModalOpen, resetConfirmModalOpen, updateNoticeModalOpen)) {
            return;
        }

        context.fill(0, topBarHeight, screenWidth, screenHeight, style.colorModalOverlay());
        if (updateNoticeModalOpen) {
            ConfigUiModalSupport.renderModalShell(
                    context,
                    textRenderer,
                    ConfigUiModalSupport.updateNoticeModalRect(screenWidth, screenHeight),
                    updateNoticeTitle,
                    style.colorMainPanel(),
                    style.colorBorder(),
                    style.colorText()
            );
        } else if (resetConfirmModalOpen) {
            ConfigUiModalSupport.renderModalShell(
                    context,
                    textRenderer,
                    ConfigUiModalSupport.resetConfirmModalRect(screenWidth, screenHeight),
                    resetConfirmTitle,
                    style.colorMainPanel(),
                    style.colorBorder(),
                    style.colorText()
            );
        } else if (customParametersModalOpen) {
            ConfigUiModalSupport.renderModalShell(
                    context,
                    textRenderer,
                    ConfigUiModalSupport.customParametersModalRect(screenWidth, screenHeight),
                    customParametersTitle,
                    style.colorMainPanel(),
                    style.colorBorder(),
                    style.colorText()
            );
        } else if (addProviderModalOpen) {
            ConfigUiModalSupport.renderModalShell(
                    context,
                    textRenderer,
                    ConfigUiModalSupport.addProviderModalRect(screenWidth, screenHeight),
                    addProviderTitle,
                    style.colorMainPanel(),
                    style.colorBorder(),
                    style.colorText()
            );
        } else if (modelSettingsModalOpen) {
            ConfigUiModalSupport.renderModalShell(
                    context,
                    textRenderer,
                    ConfigUiModalSupport.modelSettingsModalRect(screenWidth, screenHeight),
                    modelSettingsTitle,
                    style.colorMainPanel(),
                    style.colorBorder(),
                    style.colorText()
            );
        }
    }

    public record Style(
            int colorBg,
            int colorTopBar,
            int colorLeftPanel,
            int colorMainPanel,
            int colorBorder,
            int colorText,
            int colorModalOverlay
    ) {
    }
}
