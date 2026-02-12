package com.cedarxuesong.translate_allinone.utils.input;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class KeybindingManager {
    public static KeyBinding itemTranslateKey;
    public static KeyBinding scoreboardTranslateKey;
    public static KeyBinding chatInputTranslateKey;

    private static final KeyBinding.Category KEYBINDING_CATEGORY = KeyBinding.Category.create(
            Identifier.of(Translate_AllinOne.MOD_ID, "keybindings")
    );

    public static void register() {
        itemTranslateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.translate_allinone.item_translate_key",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                KEYBINDING_CATEGORY
        ));

        scoreboardTranslateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.translate_allinone.scoreboard_translate_key",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(), 
                KEYBINDING_CATEGORY
        ));

        chatInputTranslateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.translate_allinone.chat_input_translate_key",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                KEYBINDING_CATEGORY
        ));
    }
}
