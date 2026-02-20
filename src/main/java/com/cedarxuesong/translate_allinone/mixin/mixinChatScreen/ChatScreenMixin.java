package com.cedarxuesong.translate_allinone.mixin.mixinChatScreen;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.gui.configui.render.ConfigUiDraw;
import com.cedarxuesong.translate_allinone.utils.input.KeybindingManager;
import com.cedarxuesong.translate_allinone.utils.translate.ChatInputTranslateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow
    protected TextFieldWidget chatField;

    @Unique
    private static final int PANEL_WIDTH = 308;
    @Unique
    private static final int PANEL_HEIGHT = 216;
    @Unique
    private static final int PANEL_PADDING = 8;
    @Unique
    private static final int HEADER_HEIGHT = 22;
    @Unique
    private static final int CARD_GAP = 8;
    @Unique
    private static final int CARD_PADDING = 6;
    @Unique
    private static final int CARD_TITLE_HEIGHT = 11;
    @Unique
    private static final int QUICK_ACTION_CARD_HEIGHT = 98;
    @Unique
    private static final int BUTTON_HEIGHT = 20;
    @Unique
    private static final int BUTTON_GAP = 4;
    @Unique
    private static final int INPUT_OUTER_HEIGHT = 24;
    @Unique
    private static final int APPLY_BUTTON_WIDTH = 70;
    @Unique
    private static final int INPUT_MAX_LENGTH = 240;

    @Unique
    private static final int COLOR_PANEL_SHADOW = 0x70000000;
    @Unique
    private static final int COLOR_PANEL_BG = 0xE5131822;
    @Unique
    private static final int COLOR_PANEL_BORDER = 0xFF52637D;
    @Unique
    private static final int COLOR_HEADER_BG = 0xD92A405A;
    @Unique
    private static final int COLOR_HEADER_TEXT = 0xFFF0F7FF;
    @Unique
    private static final int COLOR_HEADER_SUBTITLE = 0xFFB8C9DD;
    @Unique
    private static final int COLOR_HEADER_DIVIDER = 0xFF3B4E68;
    @Unique
    private static final int COLOR_HEADER_GRIP = 0xFF8DA4C1;
    @Unique
    private static final int COLOR_CARD_BG = 0xC71A2432;
    @Unique
    private static final int COLOR_CARD_BORDER = 0xFF41546E;
    @Unique
    private static final int COLOR_CARD_TITLE = 0xFFD4E5FA;
    @Unique
    private static final int COLOR_BUTTON_BG = 0xCC1E2A38;
    @Unique
    private static final int COLOR_BUTTON_BG_HOVER = 0xE0293A4E;
    @Unique
    private static final int COLOR_BUTTON_BORDER = 0xFF495D78;
    @Unique
    private static final int COLOR_BUTTON_BORDER_HOVER = 0xFF78A0CC;
    @Unique
    private static final int COLOR_BUTTON_SHINE = 0x33597A9F;
    @Unique
    private static final int COLOR_BUTTON_SHINE_HOVER = 0x4F82ABD0;
    @Unique
    private static final int COLOR_BUTTON_TEXT = 0xFFEAF2FB;
    @Unique
    private static final int COLOR_BUTTON_TEXT_HOVER = 0xFFFFFFFF;
    @Unique
    private static final int COLOR_ICON_BG = 0xAA142130;
    @Unique
    private static final int COLOR_ICON_BORDER = 0xFF637A99;
    @Unique
    private static final int COLOR_ICON_TEXT = 0xFFD2E7FF;
    @Unique
    private static final int COLOR_ICON_TEXT_HOVER = 0xFFFFFFFF;
    @Unique
    private static final int COLOR_INPUT_BG = 0xF0161F2B;
    @Unique
    private static final int COLOR_INPUT_BORDER = 0xFF586E8A;
    @Unique
    private static final int COLOR_APPLY_BG = 0xD42A4767;
    @Unique
    private static final int COLOR_APPLY_BG_HOVER = 0xE53A628C;
    @Unique
    private static final int COLOR_APPLY_BORDER = 0xFF6C8EB5;
    @Unique
    private static final int COLOR_APPLY_TEXT = 0xFFF3FAFF;
    @Unique
    private static final int COLOR_STATUS_NO_MODEL = 0xFFE7A4A4;
    @Unique
    private static final int COLOR_STATUS_LIMITED = 0xFFE9D89B;
    @Unique
    private static final int COLOR_BUTTON_BG_DISABLED = 0x9E1A232F;
    @Unique
    private static final int COLOR_BUTTON_BORDER_DISABLED = 0xFF3E4E64;
    @Unique
    private static final int COLOR_BUTTON_SHINE_DISABLED = 0x2236455A;
    @Unique
    private static final int COLOR_BUTTON_TEXT_DISABLED = 0xFF95A6BA;
    @Unique
    private static final int COLOR_ICON_BORDER_DISABLED = 0xFF4C5D74;
    @Unique
    private static final int COLOR_ICON_TEXT_DISABLED = 0xFF8EA1B8;
    @Unique
    private static final int COLOR_INPUT_BG_DISABLED = 0xB1131B26;
    @Unique
    private static final int COLOR_INPUT_BORDER_DISABLED = 0xFF47596F;
    @Unique
    private static final int COLOR_APPLY_BG_DISABLED = 0xA3253448;
    @Unique
    private static final int COLOR_APPLY_BORDER_DISABLED = 0xFF4E627D;
    @Unique
    private static final int COLOR_APPLY_TEXT_DISABLED = 0xFF9CAEC1;

    @Unique
    private static double panelX = -1;
    @Unique
    private static double panelY = -1;
    @Unique
    private static boolean panelDragging;
    @Unique
    private static double dragOffsetX;
    @Unique
    private static double dragOffsetY;
    @Unique
    private static String instructionDraft = "";

    @Unique
    private TextFieldWidget translate_allinone$instructionField;

    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void onInit(CallbackInfo ci) {
        if (!translate_allinone$isPanelVisible()) {
            return;
        }
        translate_allinone$ensurePanelPosition();
        translate_allinone$ensureInstructionField();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyInput keyInput, CallbackInfoReturnable<Boolean> cir) {
        ChatInputTranslateManager.PanelAvailability availability = translate_allinone$getPanelAvailability();

        if (translate_allinone$isInstructionFieldFocused()) {
            if (!translate_allinone$isInstructionEnabled(availability)) {
                this.translate_allinone$instructionField.setFocused(false);
            } else {
                if (translate_allinone$isEnterKey(keyInput)) {
                    translate_allinone$applyInstruction();
                    cir.setReturnValue(true);
                    return;
                }
                if (this.translate_allinone$instructionField.keyPressed(keyInput)) {
                    cir.setReturnValue(true);
                    return;
                }
                if (!KeybindingManager.isEscape(keyInput)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        if (KeybindingManager.matchesKeyInput(Translate_AllinOne.getConfig().chatTranslate.input.keybinding, keyInput)) {
            if (availability != ChatInputTranslateManager.PanelAvailability.NO_MODEL) {
                ChatInputTranslateManager.translate(this.chatField);
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, require = 0)
    private void onCharTyped(CharInput charInput, CallbackInfoReturnable<Boolean> cir) {
        if (translate_allinone$isInstructionFieldFocused()
                && translate_allinone$isInstructionEnabled(translate_allinone$getPanelAvailability())) {
            this.translate_allinone$instructionField.charTyped(charInput);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!translate_allinone$isPanelVisible()) {
            panelDragging = false;
            if (this.translate_allinone$instructionField != null) {
                this.translate_allinone$instructionField.setFocused(false);
            }
            return;
        }

        translate_allinone$ensurePanelPosition();
        translate_allinone$ensureInstructionField();
        ChatInputTranslateManager.PanelAvailability availability = translate_allinone$getPanelAvailability();

        if (!translate_allinone$isInstructionEnabled(availability) && this.translate_allinone$instructionField != null) {
            this.translate_allinone$instructionField.setFocused(false);
        }

        if (panelDragging) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
                panelDragging = false;
            } else {
                panelX = mouseX - dragOffsetX;
                panelY = mouseY - dragOffsetY;
                translate_allinone$clampPanelPosition();
            }
        }

        translate_allinone$renderPanel(context, mouseX, mouseY, delta, availability);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(Click click, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        panelDragging = false;

        if (!translate_allinone$isPanelVisible()) {
            return;
        }

        translate_allinone$ensurePanelPosition();
        ChatInputTranslateManager.PanelAvailability availability = translate_allinone$getPanelAvailability();
        double mouseX = click.x();
        double mouseY = click.y();
        PanelRect panelRect = translate_allinone$panelRect();
        if (!panelRect.contains(mouseX, mouseY)) {
            if (this.translate_allinone$instructionField != null) {
                this.translate_allinone$instructionField.setFocused(false);
            }
            return;
        }

        PanelRect headerRect = translate_allinone$headerRect(panelRect);
        if (headerRect.contains(mouseX, mouseY)) {
            panelDragging = true;
            dragOffsetX = mouseX - panelX;
            dragOffsetY = mouseY - panelY;
            if (this.translate_allinone$instructionField != null) {
                this.translate_allinone$instructionField.setFocused(false);
            }
            cir.setReturnValue(true);
            return;
        }

        PanelRect inputOuterRect = translate_allinone$instructionInputOuterRect(panelRect);
        if (inputOuterRect.contains(mouseX, mouseY)) {
            if (this.translate_allinone$instructionField != null && translate_allinone$isInstructionEnabled(availability)) {
                this.translate_allinone$instructionField.setFocused(true);
                this.translate_allinone$instructionField.mouseClicked(click, bl);
            } else if (this.translate_allinone$instructionField != null) {
                this.translate_allinone$instructionField.setFocused(false);
            }
            cir.setReturnValue(true);
            return;
        }

        if (this.translate_allinone$instructionField != null) {
            this.translate_allinone$instructionField.setFocused(false);
        }

        PanelRect applyRect = translate_allinone$instructionApplyButtonRect(panelRect);
        if (applyRect.contains(mouseX, mouseY) && translate_allinone$isInstructionEnabled(availability)) {
            translate_allinone$applyInstruction();
            cir.setReturnValue(true);
            return;
        }

        PanelAction action = translate_allinone$findActionAt(mouseX, mouseY, panelRect);
        if (action != null && translate_allinone$isActionEnabled(action, availability)) {
            translate_allinone$performAction(action);
        }
        cir.setReturnValue(true);
    }

    @Unique
    private boolean translate_allinone$isPanelVisible() {
        return this.chatField != null && Translate_AllinOne.getConfig().chatTranslate.input.enabled;
    }

    @Unique
    private boolean translate_allinone$isInstructionFieldFocused() {
        return this.translate_allinone$instructionField != null && this.translate_allinone$instructionField.isFocused();
    }

    @Unique
    private ChatInputTranslateManager.PanelAvailability translate_allinone$getPanelAvailability() {
        return ChatInputTranslateManager.getPanelAvailability();
    }

    @Unique
    private boolean translate_allinone$isInstructionEnabled(ChatInputTranslateManager.PanelAvailability availability) {
        return availability == ChatInputTranslateManager.PanelAvailability.FULL;
    }

    @Unique
    private boolean translate_allinone$isActionEnabled(PanelAction action, ChatInputTranslateManager.PanelAvailability availability) {
        if (availability == ChatInputTranslateManager.PanelAvailability.NO_MODEL) {
            return false;
        }
        if (availability == ChatInputTranslateManager.PanelAvailability.TRANSLATE_ONLY) {
            return action == PanelAction.TRANSLATE;
        }
        return true;
    }

    @Unique
    private Text translate_allinone$subtitleText(ChatInputTranslateManager.PanelAvailability availability) {
        return switch (availability) {
            case FULL -> Text.translatable("text.translate_allinone.chat_input_panel.subtitle");
            case NO_MODEL -> Text.translatable("text.translate_allinone.chat_input_panel.status_no_model");
            case TRANSLATE_ONLY -> Text.translatable("text.translate_allinone.chat_input_panel.status_translate_only");
        };
    }

    @Unique
    private int translate_allinone$subtitleColor(ChatInputTranslateManager.PanelAvailability availability) {
        return switch (availability) {
            case FULL -> COLOR_HEADER_SUBTITLE;
            case NO_MODEL -> COLOR_STATUS_NO_MODEL;
            case TRANSLATE_ONLY -> COLOR_STATUS_LIMITED;
        };
    }

    @Unique
    private Text translate_allinone$instructionPlaceholder(ChatInputTranslateManager.PanelAvailability availability) {
        return switch (availability) {
            case FULL -> Text.translatable("text.translate_allinone.chat_input_panel.instruction_placeholder");
            case NO_MODEL -> Text.translatable("text.translate_allinone.chat_input_panel.status_no_model");
            case TRANSLATE_ONLY -> Text.translatable("text.translate_allinone.chat_input_panel.status_translate_only_short");
        };
    }

    @Unique
    private Text translate_allinone$applyButtonLabel(boolean enabled, ChatInputTranslateManager.PanelAvailability availability) {
        if (enabled) {
            return Text.translatable("text.translate_allinone.chat_input_panel.apply_instruction");
        }
        return switch (availability) {
            case NO_MODEL -> Text.translatable("text.translate_allinone.chat_input_panel.status_no_model");
            case TRANSLATE_ONLY -> Text.translatable("text.translate_allinone.chat_input_panel.status_translate_only_short");
            case FULL -> Text.translatable("text.translate_allinone.chat_input_panel.apply_instruction");
        };
    }

    @Unique
    private void translate_allinone$ensurePanelPosition() {
        if (panelX < 0 || panelY < 0) {
            panelX = translate_allinone$screenWidth() - PANEL_WIDTH - 10;
            panelY = Math.max(24, translate_allinone$screenHeight() - PANEL_HEIGHT - 50);
        }
        translate_allinone$clampPanelPosition();
    }

    @Unique
    private void translate_allinone$clampPanelPosition() {
        int width = translate_allinone$screenWidth();
        int height = translate_allinone$screenHeight();
        panelX = Math.max(2, Math.min(panelX, width - PANEL_WIDTH - 2));
        panelY = Math.max(2, Math.min(panelY, height - PANEL_HEIGHT - 2));
    }

    @Unique
    private void translate_allinone$ensureInstructionField() {
        if (this.translate_allinone$instructionField != null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        PanelRect inputRect = translate_allinone$instructionInputInnerRect(translate_allinone$panelRect());
        this.translate_allinone$instructionField = new TextFieldWidget(
                client.textRenderer,
                inputRect.x(),
                inputRect.y(),
                inputRect.width(),
                inputRect.height(),
                Text.empty()
        );
        this.translate_allinone$instructionField.setMaxLength(INPUT_MAX_LENGTH);
        this.translate_allinone$instructionField.setPlaceholder(Text.translatable("text.translate_allinone.chat_input_panel.instruction_placeholder"));
        this.translate_allinone$instructionField.setText(instructionDraft);
        this.translate_allinone$instructionField.setChangedListener(value -> instructionDraft = value);
        this.translate_allinone$instructionField.setFocused(false);
    }

    @Unique
    private void translate_allinone$layoutInstructionField(PanelRect panelRect) {
        if (this.translate_allinone$instructionField == null) {
            return;
        }
        PanelRect inputRect = translate_allinone$instructionInputInnerRect(panelRect);
        this.translate_allinone$instructionField.setX(inputRect.x());
        this.translate_allinone$instructionField.setY(inputRect.y());
    }

    @Unique
    private void translate_allinone$renderPanel(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta,
            ChatInputTranslateManager.PanelAvailability availability
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        PanelRect panelRect = translate_allinone$panelRect();
        PanelRect headerRect = translate_allinone$headerRect(panelRect);
        PanelRect quickCardRect = translate_allinone$quickActionCardRect(panelRect);
        PanelRect instructionCardRect = translate_allinone$instructionCardRect(panelRect);

        context.fill(panelRect.x() + 2, panelRect.y() + 2, panelRect.right() + 2, panelRect.bottom() + 2, COLOR_PANEL_SHADOW);
        context.fill(panelRect.x(), panelRect.y(), panelRect.right(), panelRect.bottom(), COLOR_PANEL_BG);
        ConfigUiDraw.drawOutline(context, panelRect.x(), panelRect.y(), panelRect.width(), panelRect.height(), COLOR_PANEL_BORDER);

        context.fill(headerRect.x(), headerRect.y(), headerRect.right(), headerRect.bottom(), COLOR_HEADER_BG);
        context.fill(panelRect.x() + 1, panelRect.y() + HEADER_HEIGHT, panelRect.right() - 1, panelRect.y() + HEADER_HEIGHT + 1, COLOR_HEADER_DIVIDER);
        context.drawText(
                textRenderer,
                Text.translatable("text.translate_allinone.chat_input_panel.title"),
                panelRect.x() + PANEL_PADDING,
                panelRect.y() + 4,
                COLOR_HEADER_TEXT,
                false
        );
        context.drawText(
                textRenderer,
                translate_allinone$subtitleText(availability),
                panelRect.x() + PANEL_PADDING,
                panelRect.y() + 14,
                translate_allinone$subtitleColor(availability),
                false
        );
        context.drawText(textRenderer, ":::", panelRect.right() - 18, panelRect.y() + 8, COLOR_HEADER_GRIP, false);

        translate_allinone$drawCardShell(context, quickCardRect, Text.translatable("text.translate_allinone.chat_input_panel.quick_actions"), textRenderer);
        translate_allinone$drawCardShell(context, instructionCardRect, Text.translatable("text.translate_allinone.chat_input_panel.instruction"), textRenderer);

        PanelAction[] actions = PanelAction.values();
        for (int i = 0; i < actions.length; i++) {
            PanelRect buttonRect = translate_allinone$actionButtonRect(quickCardRect, i);
            boolean enabled = translate_allinone$isActionEnabled(actions[i], availability);
            boolean hovered = enabled && buttonRect.contains(mouseX, mouseY);

            int buttonBackground = enabled ? (hovered ? COLOR_BUTTON_BG_HOVER : COLOR_BUTTON_BG) : COLOR_BUTTON_BG_DISABLED;
            int borderColor = enabled ? (hovered ? COLOR_BUTTON_BORDER_HOVER : COLOR_BUTTON_BORDER) : COLOR_BUTTON_BORDER_DISABLED;
            int highlightColor = enabled ? (hovered ? COLOR_BUTTON_SHINE_HOVER : COLOR_BUTTON_SHINE) : COLOR_BUTTON_SHINE_DISABLED;
            int textColor = enabled ? (hovered ? COLOR_BUTTON_TEXT_HOVER : COLOR_BUTTON_TEXT) : COLOR_BUTTON_TEXT_DISABLED;
            int iconTextColor = enabled ? (hovered ? COLOR_ICON_TEXT_HOVER : COLOR_ICON_TEXT) : COLOR_ICON_TEXT_DISABLED;

            context.fill(buttonRect.x(), buttonRect.y(), buttonRect.right(), buttonRect.bottom(), buttonBackground);
            context.fill(buttonRect.x(), buttonRect.y(), buttonRect.x() + 3, buttonRect.bottom(), enabled ? actions[i].accentColor() : COLOR_BUTTON_BORDER_DISABLED);
            context.fill(buttonRect.x() + 1, buttonRect.y() + 1, buttonRect.right() - 1, buttonRect.y() + 2, highlightColor);
            ConfigUiDraw.drawOutline(context, buttonRect.x(), buttonRect.y(), buttonRect.width(), buttonRect.height(), borderColor);

            int iconSize = 12;
            int iconX = buttonRect.x() + 6;
            int iconY = buttonRect.y() + (BUTTON_HEIGHT - iconSize) / 2;
            context.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, COLOR_ICON_BG);
            ConfigUiDraw.drawOutline(
                    context,
                    iconX,
                    iconY,
                    iconSize,
                    iconSize,
                    enabled ? (hovered ? actions[i].accentColor() : COLOR_ICON_BORDER) : COLOR_ICON_BORDER_DISABLED
            );

            String icon = actions[i].icon();
            int iconTextX = iconX + (iconSize - textRenderer.getWidth(icon)) / 2;
            int textY = buttonRect.y() + (BUTTON_HEIGHT - textRenderer.fontHeight) / 2;
            context.drawText(textRenderer, icon, iconTextX, textY, iconTextColor, false);

            context.drawText(textRenderer, actions[i].label(), buttonRect.x() + 24, textY, textColor, false);
        }

        boolean instructionEnabled = translate_allinone$isInstructionEnabled(availability);
        PanelRect inputOuterRect = translate_allinone$instructionInputOuterRect(panelRect);
        context.fill(
                inputOuterRect.x(),
                inputOuterRect.y(),
                inputOuterRect.right(),
                inputOuterRect.bottom(),
                instructionEnabled ? COLOR_INPUT_BG : COLOR_INPUT_BG_DISABLED
        );
        ConfigUiDraw.drawOutline(
                context,
                inputOuterRect.x(),
                inputOuterRect.y(),
                inputOuterRect.width(),
                inputOuterRect.height(),
                instructionEnabled ? COLOR_INPUT_BORDER : COLOR_INPUT_BORDER_DISABLED
        );

        translate_allinone$layoutInstructionField(panelRect);
        if (this.translate_allinone$instructionField != null) {
            this.translate_allinone$instructionField.setEditable(instructionEnabled);
            this.translate_allinone$instructionField.setPlaceholder(translate_allinone$instructionPlaceholder(availability));
            if (instructionEnabled) {
                this.translate_allinone$instructionField.render(context, mouseX, mouseY, delta);
            } else {
                int hintY = inputOuterRect.y() + (inputOuterRect.height() - textRenderer.fontHeight) / 2;
                context.drawText(
                        textRenderer,
                        translate_allinone$instructionPlaceholder(availability),
                        inputOuterRect.x() + 6,
                        hintY,
                        translate_allinone$subtitleColor(availability),
                        false
                );
            }
        }

        PanelRect applyRect = translate_allinone$instructionApplyButtonRect(panelRect);
        boolean applyEnabled = instructionEnabled;
        boolean applyHovered = applyEnabled && applyRect.contains(mouseX, mouseY);
        context.fill(
                applyRect.x(),
                applyRect.y(),
                applyRect.right(),
                applyRect.bottom(),
                applyEnabled
                        ? (applyHovered ? COLOR_APPLY_BG_HOVER : COLOR_APPLY_BG)
                        : COLOR_APPLY_BG_DISABLED
        );
        ConfigUiDraw.drawOutline(
                context,
                applyRect.x(),
                applyRect.y(),
                applyRect.width(),
                applyRect.height(),
                applyEnabled ? COLOR_APPLY_BORDER : COLOR_APPLY_BORDER_DISABLED
        );
        Text applyLabel = translate_allinone$applyButtonLabel(applyEnabled, availability);
        int applyTextX = applyRect.x() + (applyRect.width() - textRenderer.getWidth(applyLabel)) / 2;
        int applyTextY = applyRect.y() + (applyRect.height() - textRenderer.fontHeight) / 2;
        context.drawText(
                textRenderer,
                applyLabel,
                applyTextX,
                applyTextY,
                applyEnabled ? COLOR_APPLY_TEXT : COLOR_APPLY_TEXT_DISABLED,
                false
        );
    }

    @Unique
    private void translate_allinone$drawCardShell(DrawContext context, PanelRect cardRect, Text title, TextRenderer textRenderer) {
        context.fill(cardRect.x(), cardRect.y(), cardRect.right(), cardRect.bottom(), COLOR_CARD_BG);
        ConfigUiDraw.drawOutline(context, cardRect.x(), cardRect.y(), cardRect.width(), cardRect.height(), COLOR_CARD_BORDER);
        context.drawText(textRenderer, title, cardRect.x() + CARD_PADDING, cardRect.y() + CARD_PADDING - 1, COLOR_CARD_TITLE, false);
    }

    @Unique
    private PanelAction translate_allinone$findActionAt(double mouseX, double mouseY, PanelRect panelRect) {
        PanelRect quickCardRect = translate_allinone$quickActionCardRect(panelRect);
        PanelAction[] actions = PanelAction.values();
        for (int i = 0; i < actions.length; i++) {
            if (translate_allinone$actionButtonRect(quickCardRect, i).contains(mouseX, mouseY)) {
                return actions[i];
            }
        }
        return null;
    }

    @Unique
    private void translate_allinone$performAction(PanelAction action) {
        switch (action) {
            case TRANSLATE -> ChatInputTranslateManager.translate(this.chatField);
            case PROFESSIONAL -> ChatInputTranslateManager.translateProfessional(this.chatField);
            case FRIENDLY -> ChatInputTranslateManager.translateFriendly(this.chatField);
            case EXPAND -> ChatInputTranslateManager.translateExpand(this.chatField);
            case CONCISE -> ChatInputTranslateManager.translateConcise(this.chatField);
            case RESTORE -> ChatInputTranslateManager.restoreOriginal(this.chatField);
        }
    }

    @Unique
    private void translate_allinone$applyInstruction() {
        if (this.translate_allinone$instructionField == null) {
            return;
        }
        String instruction = this.translate_allinone$instructionField.getText();
        ChatInputTranslateManager.rewriteByInstruction(this.chatField, instruction);
    }

    @Unique
    private boolean translate_allinone$isEnterKey(KeyInput keyInput) {
        Integer keyCode = translate_allinone$extractKeyCode(keyInput);
        if (keyCode == null) {
            return false;
        }
        return keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER;
    }

    @Unique
    private Integer translate_allinone$extractKeyCode(KeyInput keyInput) {
        if (keyInput == null) {
            return null;
        }

        try {
            Object code = keyInput.getClass().getMethod("keyCode").invoke(keyInput);
            if (code instanceof Integer i) {
                return i;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Object code = keyInput.getClass().getMethod("code").invoke(keyInput);
            if (code instanceof Integer i) {
                return i;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return null;
    }

    @Unique
    private PanelRect translate_allinone$panelRect() {
        return new PanelRect((int) Math.round(panelX), (int) Math.round(panelY), PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Unique
    private PanelRect translate_allinone$headerRect(PanelRect panelRect) {
        return new PanelRect(panelRect.x() + 1, panelRect.y() + 1, panelRect.width() - 2, HEADER_HEIGHT - 1);
    }

    @Unique
    private PanelRect translate_allinone$quickActionCardRect(PanelRect panelRect) {
        int x = panelRect.x() + PANEL_PADDING;
        int y = panelRect.y() + HEADER_HEIGHT + PANEL_PADDING;
        int width = panelRect.width() - PANEL_PADDING * 2;
        return new PanelRect(x, y, width, QUICK_ACTION_CARD_HEIGHT);
    }

    @Unique
    private PanelRect translate_allinone$instructionCardRect(PanelRect panelRect) {
        PanelRect quickCardRect = translate_allinone$quickActionCardRect(panelRect);
        int x = panelRect.x() + PANEL_PADDING;
        int y = quickCardRect.bottom() + CARD_GAP;
        int width = panelRect.width() - PANEL_PADDING * 2;
        int height = panelRect.bottom() - PANEL_PADDING - y;
        return new PanelRect(x, y, width, height);
    }

    @Unique
    private PanelRect translate_allinone$actionButtonRect(PanelRect quickCardRect, int index) {
        int columns = 2;
        int columnWidth = (quickCardRect.width() - CARD_PADDING * 2 - BUTTON_GAP) / columns;
        int column = index % columns;
        int row = index / columns;

        int x = quickCardRect.x() + CARD_PADDING + column * (columnWidth + BUTTON_GAP);
        int y = quickCardRect.y() + CARD_PADDING + CARD_TITLE_HEIGHT + 4 + row * (BUTTON_HEIGHT + BUTTON_GAP);
        return new PanelRect(x, y, columnWidth, BUTTON_HEIGHT);
    }

    @Unique
    private PanelRect translate_allinone$instructionInputOuterRect(PanelRect panelRect) {
        PanelRect instructionCardRect = translate_allinone$instructionCardRect(panelRect);
        int x = instructionCardRect.x() + CARD_PADDING;
        int y = instructionCardRect.y() + CARD_PADDING + CARD_TITLE_HEIGHT + 4;
        int width = instructionCardRect.width() - CARD_PADDING * 2 - APPLY_BUTTON_WIDTH - BUTTON_GAP;
        return new PanelRect(x, y, width, INPUT_OUTER_HEIGHT);
    }

    @Unique
    private PanelRect translate_allinone$instructionInputInnerRect(PanelRect panelRect) {
        PanelRect outerRect = translate_allinone$instructionInputOuterRect(panelRect);
        return new PanelRect(outerRect.x() + 2, outerRect.y() + 2, outerRect.width() - 4, outerRect.height() - 4);
    }

    @Unique
    private PanelRect translate_allinone$instructionApplyButtonRect(PanelRect panelRect) {
        PanelRect inputOuterRect = translate_allinone$instructionInputOuterRect(panelRect);
        return new PanelRect(inputOuterRect.right() + BUTTON_GAP, inputOuterRect.y(), APPLY_BUTTON_WIDTH, inputOuterRect.height());
    }

    @Unique
    private int translate_allinone$screenWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client == null ? 320 : client.getWindow().getScaledWidth();
    }

    @Unique
    private int translate_allinone$screenHeight() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client == null ? 240 : client.getWindow().getScaledHeight();
    }

    @Unique
    private enum PanelAction {
        TRANSLATE("text.translate_allinone.chat_input_panel.translate", "T", 0xFF569BE6),
        PROFESSIONAL("text.translate_allinone.chat_input_panel.professional", "P", 0xFF4CB08A),
        FRIENDLY("text.translate_allinone.chat_input_panel.friendly", "F", 0xFFE0A75B),
        EXPAND("text.translate_allinone.chat_input_panel.expand", "+", 0xFF79AEDC),
        CONCISE("text.translate_allinone.chat_input_panel.concise", "-", 0xFF7FC188),
        RESTORE("text.translate_allinone.chat_input_panel.restore", "R", 0xFFC77A7A);

        private final String key;
        private final String icon;
        private final int accentColor;

        PanelAction(String key, String icon, int accentColor) {
            this.key = key;
            this.icon = icon;
            this.accentColor = accentColor;
        }

        public Text label() {
            return Text.translatable(this.key);
        }

        public String icon() {
            return this.icon;
        }

        public int accentColor() {
            return this.accentColor;
        }
    }

    @Unique
    private record PanelRect(int x, int y, int width, int height) {
        private int right() {
            return x + width;
        }

        private int bottom() {
            return y + height;
        }

        private boolean contains(double px, double py) {
            return px >= x && px < right() && py >= y && py < bottom();
        }
    }
}
