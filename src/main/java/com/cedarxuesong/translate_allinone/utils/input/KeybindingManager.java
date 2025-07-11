package com.cedarxuesong.translate_allinone.utils.input;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindingManager {
    public static KeyBinding itemTranslateKey;
    public static KeyBinding scoreboardTranslateKey;

    public static void register() {
        itemTranslateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.translate_allinone.item_translate_key", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                InputUtil.UNKNOWN_KEY.getCode(), // The default keycode of the keybinding.
                "category.translate_allinone.keybindings" // The translation key of the keybinding's category.
        ));

        scoreboardTranslateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.translate_allinone.scoreboard_translate_key", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                InputUtil.UNKNOWN_KEY.getCode(), // The default keycode of the keybinding.
                "category.translate_allinone.keybindings" // The translation key of the keybinding's category.
        ));
    }
} 