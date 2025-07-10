package com.cedarxuesong.translate_allinone.mixin.mixinInGameGui;

import com.cedarxuesong.translate_allinone.utils.AnimationManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.IntFunction;
import java.util.stream.Stream;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Translate_AllinOne/InGameHudMixin");

    @Redirect(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;toArray(Ljava/util/function/IntFunction;)[Ljava/lang/Object;",
                    remap = false
            )
    )
    private Object[] onRenderScoreboardSidebar(Stream<Object> stream, IntFunction<Object[]> generator) {
        Object[] sidebarEntries = stream.toArray(generator);

        if (sidebarEntries == null || sidebarEntries.length == 0) {
            return sidebarEntries;
        }
        try {
            Class<?> entryClass = sidebarEntries[0].getClass();
            Field nameField = entryClass.getDeclaredField("name");
            nameField.setAccessible(true);
            Field scoreField = entryClass.getDeclaredField("score");
            scoreField.setAccessible(true);
            Field scoreWidthField = entryClass.getDeclaredField("scoreWidth");
            scoreWidthField.setAccessible(true);
            Constructor<?> constructor = entryClass.getDeclaredConstructor(Text.class, Text.class, int.class);

            Object[] newEntries = (Object[]) Array.newInstance(entryClass, sidebarEntries.length);

            for (int i = 0; i < sidebarEntries.length; i++) {
                Object originalEntry = sidebarEntries[i];
                Text originalName = (Text) nameField.get(originalEntry);
                Text score = (Text) scoreField.get(originalEntry);
                int scoreWidth = scoreWidthField.getInt(originalEntry);

                Text newName = AnimationManager.getAnimatedStyledText(originalName);

                newEntries[i] = constructor.newInstance(newName, score, scoreWidth);
            }
            return newEntries;

        } catch (Exception e) {
            LOGGER.error("Failed to apply animation to scoreboard sidebar via Mixin", e);
            return sidebarEntries;
        }
    }

    @Redirect(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I",
                    ordinal = 1
            )
    )
    private int forceShadowForName(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
        return instance.drawText(textRenderer, text, x, y, color, true);
    }
} 