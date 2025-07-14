package com.cedarxuesong.translate_allinone.mixin.mixinChatHud;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.registration.LifecycleEventManager;
import com.cedarxuesong.translate_allinone.utils.AnimationManager;
import com.cedarxuesong.translate_allinone.utils.MessageUtils;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.UUID;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Unique
    private static final ThreadLocal<Boolean> isModifyingMessage = ThreadLocal.withInitial(() -> false);

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), argsOnly = true)
    private Text onAddMessage(Text message) {
        if (isModifyingMessage.get() || !LifecycleEventManager.isReadyForTranslation) {
            return message;
        }

        try {
            isModifyingMessage.set(true);

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (config.chatTranslate.output.enabled) {
                String plainText = AnimationManager.stripFormatting(message.getString()).trim();
                if (plainText.isEmpty()) {
                    return message;
                }

            UUID messageId = UUID.randomUUID();
            MessageUtils.MESSAGES_BY_UUID.put(messageId, message);

                if (config.chatTranslate.output.auto_translate) {
                    MinecraftClient.getInstance().execute(() -> {
                        if (MinecraftClient.getInstance().player != null) {
                            MinecraftClient.getInstance().player.networkHandler.sendCommand("translate_allinone translatechatline " + messageId);
                        }
                    });
                    return message;
                } else {
            MutableText translateButton = Text.literal(" [T]");
            Style style = Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/translate_allinone translatechatline " + messageId))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("text.translate_allinone.translate_button_hover")));
            translateButton.setStyle(style);

            return Text.empty().append(message).append(translateButton);
                }
        }
        return message;
        } finally {
            isModifyingMessage.set(false);
        }
    }
} 