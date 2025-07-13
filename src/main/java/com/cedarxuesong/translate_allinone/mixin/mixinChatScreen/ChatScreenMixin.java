package com.cedarxuesong.translate_allinone.mixin.mixinChatScreen;

import com.cedarxuesong.translate_allinone.utils.input.KeybindingManager;
import com.cedarxuesong.translate_allinone.utils.translate.ChatInputTranslateManager;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow
    protected TextFieldWidget chatField;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (KeybindingManager.chatInputTranslateKey.matchesKey(keyCode, scanCode)) {
            ChatInputTranslateManager.translate(this.chatField);
            cir.setReturnValue(true);
        }
    }
} 