package com.cedarxuesong.translate_allinone.utils.input;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindingManager {
    public static KeyBinding itemTranslateKey;
    public static KeyBinding scoreboardTranslateKey;
    public static KeyBinding chatInputTranslateKey;

    public static void register() {
        itemTranslateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.translate_allinone.item_translate_key",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                "category.translate_allinone.keybindings"
        ));

        scoreboardTranslateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.translate_allinone.scoreboard_translate_key",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(), 
                "category.translate_allinone.keybindings" 
        ));

        chatInputTranslateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.translate_allinone.chat_input_translate_key",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                "category.translate_allinone.keybindings"
        ));
    }
} 