package com.cedarxuesong.translate_allinone.mixin.mixinItem;

import com.cedarxuesong.translate_allinone.utils.AnimationManager;
import com.cedarxuesong.translate_allinone.utils.cache.TextTemplateCache;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ItemTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.text.TemplateProcessor;
import com.cedarxuesong.translate_allinone.utils.text.StylePreserver;
import com.cedarxuesong.translate_allinone.utils.translate.ItemTranslateManager;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Formatting;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique
    private static final ThreadLocal<Boolean> isModifyingTooltip = ThreadLocal.withInitial(() -> false);
    @Unique
    private static ItemStack lastHoveredStack = null;

    @Inject(method = "getTooltip(Lnet/minecraft/item/Item$TooltipContext;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/tooltip/TooltipType;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void onGetTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (isModifyingTooltip.get()) {
            return;
        }

        ItemStack currentStack = (ItemStack) (Object) this;
        if (lastHoveredStack == null || !ItemStack.areEqual(lastHoveredStack, currentStack)) {
            TextTemplateCache.getInstance().clearPendingAndInProgress();
            lastHoveredStack = currentStack;
        }

        try {
            isModifyingTooltip.set(true);
            ItemTranslateConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig().itemTranslate;
            if (!config.enabled) {
                return;
            }

            List<Text> originalTooltip = cir.getReturnValue();
            if (originalTooltip.isEmpty()) {
                return;
            }

            List<Text> newTooltip = new ArrayList<>();
            boolean isFirstLine = true;
            int translatableLines = 0;
            boolean isCurrentItemStackPending = false;

            for (Text line : originalTooltip) {
                // Ignore lines that are empty or contain only whitespace and formatting codes.
                if (line.getString().trim().isEmpty()) {
                    newTooltip.add(line);
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

                if (shouldTranslate) {
                    translatableLines++;
                    // Preserving styles by extracting them first
                    StylePreserver.ExtractionResult styleResult = StylePreserver.extractAndMark(line);
                    TemplateProcessor.TemplateExtractionResult templateResult = TemplateProcessor.extract(styleResult.markedText);

                    String unicodeTemplate = templateResult.template;
                    String legacyTemplateKey = StylePreserver.toLegacyTemplate(unicodeTemplate, styleResult.styleMap);

                    TextTemplateCache.TranslationStatus status = TextTemplateCache.getInstance().getTemplateStatus(legacyTemplateKey);
                    if (status == TextTemplateCache.TranslationStatus.PENDING || status == TextTemplateCache.TranslationStatus.IN_PROGRESS) {
                        isCurrentItemStackPending = true;
                    }
                    String translatedTemplate = TextTemplateCache.getInstance().getOrQueue(legacyTemplateKey);

                    Text finalTooltipLine;

                    if (status == TextTemplateCache.TranslationStatus.TRANSLATED) {
                        String reassembledTranslated = TemplateProcessor.reassemble(translatedTemplate, templateResult.values);
                        finalTooltipLine = StylePreserver.fromLegacyText(reassembledTranslated);
                    } else if (status == TextTemplateCache.TranslationStatus.ERROR) {
                        String errorMessage = TextTemplateCache.getInstance().getError(legacyTemplateKey);
                        MutableText errorText = Text.literal("Error: " + errorMessage).formatted(Formatting.RED);

                        ItemTranslateManager.RateLimitStatus rateLimitStatus = ItemTranslateManager.getInstance().getRateLimitStatus();
                        if (rateLimitStatus.isRateLimited()) {
                            errorText.append(Text.literal(" (retry in " + rateLimitStatus.estimatedWaitSeconds() + "s)").formatted(Formatting.YELLOW));
                        }
                        finalTooltipLine = errorText;
                    } else {
                        // No translation found, display original reassembled text with animation.
                        String reassembledOriginal = TemplateProcessor.reassemble(unicodeTemplate, templateResult.values);
                        Text originalTextObject = StylePreserver.reapplyStyles(reassembledOriginal, styleResult.styleMap);
                        finalTooltipLine = AnimationManager.getAnimatedStyledText(originalTextObject);
                    }

                    newTooltip.add(finalTooltipLine);
                } else {
                    newTooltip.add(line);
                }
            }

            TextTemplateCache.CacheStats stats = TextTemplateCache.getInstance().getCacheStats();
            ItemTranslateManager.RateLimitStatus rateLimitStatus = ItemTranslateManager.getInstance().getRateLimitStatus();

            boolean isAnythingPending = stats.total() > stats.translated();
            boolean shouldShowStatus = isCurrentItemStackPending || (rateLimitStatus.isRateLimited() && isAnythingPending);

            if (translatableLines > 0 && shouldShowStatus) {
                newTooltip.add(createStatusLine(stats, rateLimitStatus));
            }

            cir.setReturnValue(newTooltip);
        } finally {
            isModifyingTooltip.set(false);
        }
    }

    @Unique
    private Text createStatusLine(TextTemplateCache.CacheStats stats, ItemTranslateManager.RateLimitStatus rateLimitStatus) {
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