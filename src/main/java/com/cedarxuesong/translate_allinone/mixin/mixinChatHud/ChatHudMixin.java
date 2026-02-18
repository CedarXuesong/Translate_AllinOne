package com.cedarxuesong.translate_allinone.mixin.mixinChatHud;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.registration.LifecycleEventManager;
import com.cedarxuesong.translate_allinone.utils.AnimationManager;
import com.cedarxuesong.translate_allinone.utils.MessageUtils;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Unique
    private static final long AUTO_TRANSLATE_COMMAND_DELAY_MS = 25L;

    @Unique
    private static final ThreadLocal<Boolean> isModifyingMessage = ThreadLocal.withInitial(() -> false);

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), argsOnly = true)
    private Text onAddMessage(Text message) {
        if (isModifyingMessage.get() || !LifecycleEventManager.isReadyForTranslation) {
            return message;
        }

        try {
            isModifyingMessage.set(true);

        ModConfig config = Translate_AllinOne.getConfig();
        if (config.chatTranslate.output.enabled) {
                String plainText = AnimationManager.stripFormatting(message.getString()).trim();
                if (plainText.isEmpty()) {
                    return message;
                }

            UUID messageId = UUID.randomUUID();
            MessageUtils.putTrackedMessage(messageId, message);

                if (config.chatTranslate.output.auto_translate) {
                    queueAutoTranslateCommand(messageId);
                    return message;
                } else {
            MutableText translateButton = Text.literal(" [T]");
            Style style = Style.EMPTY
                    .withClickEvent(new ClickEvent.RunCommand("/translate_allinone translatechatline " + messageId))
                    .withHoverEvent(new HoverEvent.ShowText(Text.translatable("text.translate_allinone.translate_button_hover")));
            translateButton.setStyle(style);

            return Text.empty().append(message).append(translateButton);
                }
        }
        return message;
        } finally {
            isModifyingMessage.set(false);
        }
    }

    @Unique
    private static void queueAutoTranslateCommand(UUID messageId) {
        CompletableFuture.delayedExecutor(AUTO_TRANSLATE_COMMAND_DELAY_MS, TimeUnit.MILLISECONDS).execute(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return;
            }
            client.execute(() -> {
                if (client.player != null && client.player.networkHandler != null) {
                    client.player.networkHandler.sendChatCommand("translate_allinone translatechatline " + messageId);
                }
            });
        });
    }
}
