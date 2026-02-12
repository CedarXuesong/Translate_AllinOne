package com.cedarxuesong.translate_allinone.mixin.mixinItem;

import com.cedarxuesong.translate_allinone.utils.AnimationManager;
import com.cedarxuesong.translate_allinone.utils.cache.ItemTemplateCache;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ItemTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.input.KeybindingManager;
import com.cedarxuesong.translate_allinone.utils.text.StylePreserver;
import com.cedarxuesong.translate_allinone.utils.text.TemplateProcessor;
import com.cedarxuesong.translate_allinone.utils.translate.ItemTranslateManager;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Screen.class)
public abstract class DrawContextItemTooltipMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Translate_AllinOne/DrawContextItemTooltipMixin");

    @Unique
    private static final ThreadLocal<Boolean> translate_allinone$isBuildingTooltipMirror = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "getTooltipFromItem(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/item/ItemStack;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void translate_allinone$mirrorTooltipForRendering(
            MinecraftClient client,
            ItemStack stack,
            CallbackInfoReturnable<List<Text>> cir
    ) {
        cir.setReturnValue(translate_allinone$buildTooltipMirror(cir.getReturnValue()));
    }

    @Unique
    private static List<Text> translate_allinone$buildTooltipMirror(List<Text> originalTooltip) {
        if (translate_allinone$isBuildingTooltipMirror.get()) {
            return originalTooltip;
        }

        if (originalTooltip == null || originalTooltip.isEmpty()) {
            return originalTooltip;
        }

        ItemTranslateConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig().itemTranslate;
        if (!config.enabled) {
            return originalTooltip;
        }

        boolean isKeyPressed = translate_allinone$isTranslateKeyPressed();
        boolean shouldShowOriginal = false;

        switch (config.keybinding.mode) {
            case HOLD_TO_TRANSLATE:
                if (!isKeyPressed) {
                    shouldShowOriginal = true;
                }
                break;
            case HOLD_TO_SEE_ORIGINAL:
                if (isKeyPressed) {
                    shouldShowOriginal = true;
                }
                break;
            case DISABLED:
                break;
        }

        if (shouldShowOriginal) {
            return originalTooltip;
        }

        try {
            translate_allinone$isBuildingTooltipMirror.set(true);

            List<Text> mirroredTooltip = new ArrayList<>();
            boolean isFirstLine = true;
            int translatableLines = 0;
            boolean isCurrentItemStackPending = false;

            for (Text line : originalTooltip) {
                if (line.getString().trim().isEmpty()) {
                    mirroredTooltip.add(line);
                    continue;
                }

                boolean shouldTranslate = false;
                if (isFirstLine) {
                    if (config.enabled_translate_item_custom_name) {
                        shouldTranslate = true;
                    }
                    isFirstLine = false;
                } else {
                    if (config.enabled_translate_item_lore) {
                        shouldTranslate = true;
                    }
                }

                if (!shouldTranslate) {
                    mirroredTooltip.add(line);
                    continue;
                }

                translatableLines++;

                StylePreserver.ExtractionResult styleResult = StylePreserver.extractAndMark(line);
                TemplateProcessor.TemplateExtractionResult templateResult = TemplateProcessor.extract(styleResult.markedText);
                String unicodeTemplate = templateResult.template;
                String legacyTemplateKey = StylePreserver.toLegacyTemplate(unicodeTemplate, styleResult.styleMap);

                ItemTemplateCache cache = ItemTemplateCache.getInstance();
                ItemTemplateCache.TranslationStatus status = cache.getTemplateStatus(legacyTemplateKey);
                if (status == ItemTemplateCache.TranslationStatus.PENDING || status == ItemTemplateCache.TranslationStatus.IN_PROGRESS) {
                    isCurrentItemStackPending = true;
                }

                String translatedTemplate = cache.getOrQueue(legacyTemplateKey);
                Text finalTooltipLine;

                if (status == ItemTemplateCache.TranslationStatus.TRANSLATED) {
                    String reassembledTranslated = TemplateProcessor.reassemble(translatedTemplate, templateResult.values);
                    finalTooltipLine = StylePreserver.fromLegacyText(reassembledTranslated);
                } else if (status == ItemTemplateCache.TranslationStatus.ERROR) {
                    String errorMessage = cache.getError(legacyTemplateKey);
                    MutableText errorText = Text.literal("Error: " + errorMessage).formatted(Formatting.RED);

                    ItemTranslateManager.RateLimitStatus rateLimitStatus = ItemTranslateManager.getInstance().getRateLimitStatus();
                    if (rateLimitStatus.isRateLimited()) {
                        errorText.append(Text.literal(" (retry in " + rateLimitStatus.estimatedWaitSeconds() + "s)")
                                .formatted(Formatting.YELLOW));
                    }
                    finalTooltipLine = errorText;
                } else {
                    String reassembledOriginal = TemplateProcessor.reassemble(unicodeTemplate, templateResult.values);
                    Text originalTextObject = StylePreserver.reapplyStyles(reassembledOriginal, styleResult.styleMap);
                    finalTooltipLine = AnimationManager.getAnimatedStyledText(originalTextObject);
                }

                mirroredTooltip.add(finalTooltipLine);
            }

            ItemTemplateCache.CacheStats stats = ItemTemplateCache.getInstance().getCacheStats();
            ItemTranslateManager.RateLimitStatus rateLimitStatus = ItemTranslateManager.getInstance().getRateLimitStatus();

            boolean isAnythingPending = stats.total() > stats.translated();
            boolean shouldShowStatus = isCurrentItemStackPending || (rateLimitStatus.isRateLimited() && isAnythingPending);

            if (translatableLines > 0 && shouldShowStatus) {
                mirroredTooltip.add(translate_allinone$createStatusLine(stats, rateLimitStatus));
            }

            return mirroredTooltip;
        } catch (Exception e) {
            LOGGER.error("Failed to build translated tooltip mirror", e);
            return originalTooltip;
        } finally {
            translate_allinone$isBuildingTooltipMirror.set(false);
        }
    }

    @Unique
    private static boolean translate_allinone$isTranslateKeyPressed() {
        try {
            if (KeybindingManager.itemTranslateKey == null) {
                return false;
            }

            InputUtil.Key key = ((KeyBindingAccessor) (Object) KeybindingManager.itemTranslateKey)
                    .translate_allinone$getBoundKey();
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.getWindow() == null || key == null) {
                return false;
            }

            int boundCode = key.getCode();
            if (boundCode < 0) {
                return false;
            }

            if (key.getCategory() == InputUtil.Type.KEYSYM) {
                return boundCode <= GLFW.GLFW_KEY_LAST && InputUtil.isKeyPressed(client.getWindow(), boundCode);
            }

            if (key.getCategory() == InputUtil.Type.MOUSE) {
                return boundCode <= GLFW.GLFW_MOUSE_BUTTON_LAST
                        && GLFW.glfwGetMouseButton(client.getWindow().getHandle(), boundCode) == GLFW.GLFW_PRESS;
            }

            return KeybindingManager.itemTranslateKey.isPressed();
        } catch (Exception e) {
            LOGGER.debug("Failed to read item translate key state", e);
            return KeybindingManager.itemTranslateKey != null && KeybindingManager.itemTranslateKey.isPressed();
        }
    }

    @Unique
    private static Text translate_allinone$createStatusLine(ItemTemplateCache.CacheStats stats, ItemTranslateManager.RateLimitStatus rateLimitStatus) {
        float percentage = (stats.total() > 0) ? ((float) stats.translated() / stats.total()) * 100 : 100;
        String progressText = String.format(" (%d/%d) - %.0f%%", stats.translated(), stats.total(), percentage);

        MutableText statusText;
        if (rateLimitStatus.isRateLimited() && rateLimitStatus.estimatedWaitSeconds() > 0) {
            Text rateLimitMessage = Text.literal("Rate limit reached, waiting " + rateLimitStatus.estimatedWaitSeconds() + "s")
                    .formatted(Formatting.RED);
            statusText = AnimationManager.getAnimatedStyledText(rateLimitMessage);
        } else {
            Text translatingMessage = Text.literal("Translating...").formatted(Formatting.GRAY);
            statusText = AnimationManager.getAnimatedStyledText(translatingMessage);
        }

        return statusText.append(Text.literal(progressText).formatted(Formatting.YELLOW));
    }
}
