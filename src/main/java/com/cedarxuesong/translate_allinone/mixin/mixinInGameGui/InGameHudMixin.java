package com.cedarxuesong.translate_allinone.mixin.mixinInGameGui;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.AnimationManager;
import com.cedarxuesong.translate_allinone.utils.cache.ScoreboardTextCache;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ScoreboardConfig;
import com.cedarxuesong.translate_allinone.utils.input.KeybindingManager;
import com.cedarxuesong.translate_allinone.utils.text.StylePreserver;
import com.cedarxuesong.translate_allinone.utils.text.TemplateProcessor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Translate_AllinOne/InGameHudMixin");

    @Unique
    private static final String MISSING_KEY_HINT = "missing key";

    @Unique
    private static final ThreadLocal<Map<String, Text>> translate_allinone$scoreboardReplacements = new ThreadLocal<>();

    @Unique
    private Text translate_allinone$processTextForTranslation(Text originalText) {
        if (originalText == null || originalText.getString().trim().isEmpty()) {
            return originalText;
        }

        StylePreserver.ExtractionResult styleResult = StylePreserver.extractAndMark(originalText);
        TemplateProcessor.TemplateExtractionResult templateResult = TemplateProcessor.extract(styleResult.markedText);
        String unicodeTemplate = templateResult.template;
        String legacyTemplateKey = StylePreserver.toLegacyTemplate(unicodeTemplate, styleResult.styleMap);

        ScoreboardTextCache cache = ScoreboardTextCache.getInstance();
        ScoreboardTextCache.TranslationStatus status = cache.getTemplateStatus(legacyTemplateKey);
        String translatedTemplate = cache.getOrQueue(legacyTemplateKey);

        if (status == ScoreboardTextCache.TranslationStatus.TRANSLATED) {
            String reassembledTranslated = TemplateProcessor.reassemble(translatedTemplate, templateResult.values);
            return StylePreserver.fromLegacyText(reassembledTranslated);
        }

        String reassembledOriginal = TemplateProcessor.reassemble(unicodeTemplate, templateResult.values);
        Text originalTextObject = StylePreserver.reapplyStyles(reassembledOriginal, styleResult.styleMap);

        if (status == ScoreboardTextCache.TranslationStatus.ERROR) {
            String errorMessage = cache.getError(legacyTemplateKey);
            if (translate_allinone$isMissingKeyIssue(errorMessage)) {
                return AnimationManager.getAnimatedStyledText(originalTextObject, legacyTemplateKey, true);
            }
            MutableText errorText = Text.literal("Error: " + errorMessage).formatted(Formatting.RED);
            return errorText;
        }

        return AnimationManager.getAnimatedStyledText(originalTextObject, legacyTemplateKey, false);
    }

    @Unique
    private static boolean translate_allinone$isMissingKeyIssue(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return false;
        }
        return errorMessage.toLowerCase(Locale.ROOT).contains(MISSING_KEY_HINT);
    }

    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD")
    )
    private void onRenderScoreboardSidebarHead(DrawContext drawContext, ScoreboardObjective objective, CallbackInfo ci) {
        try {
            ScoreboardConfig config = Translate_AllinOne.getConfig().scoreboardTranslate;
            if (!config.enabled) {
                translate_allinone$scoreboardReplacements.set(null);
                return;
            }

            boolean isKeyPressed = KeybindingManager.isPressed(config.keybinding.binding);
            boolean shouldShowOriginal = false;

            switch (config.keybinding.mode) {
                case HOLD_TO_TRANSLATE:
                    if (!isKeyPressed) shouldShowOriginal = true;
                    break;
                case HOLD_TO_SEE_ORIGINAL:
                    if (isKeyPressed) shouldShowOriginal = true;
                    break;
                case DISABLED:
                    break;
            }

            if (shouldShowOriginal) {
                translate_allinone$scoreboardReplacements.set(null);
                return;
            }

            Scoreboard scoreboard = objective.getScoreboard();
            Comparator<ScoreboardEntry> comparator = InGameHudAccessor.getScoreboardEntryComparator();
            Map<String, Text> replacements = new HashMap<>();

            scoreboard.getScoreboardEntries(objective).stream()
                    .filter(score -> !score.hidden())
                    .sorted(comparator)
                    .limit(15L)
                    .forEach(scoreboardEntry -> {
                        Team team = scoreboard.getScoreHolderTeam(scoreboardEntry.owner());
                        Text plainOwnerName = Text.literal(scoreboardEntry.owner());
                        String originalDecoratedNameKey = Team.decorateName(team, plainOwnerName).getString();

                        MutableText newName = Text.empty();
                        if (team != null) {
                            Text prefix = config.enabled_translate_prefix_and_suffix_name ? translate_allinone$processTextForTranslation(team.getPrefix()) : team.getPrefix();
                            newName.append(prefix);

                            if (config.enabled_translate_player_name) {
                                newName.append(plainOwnerName);
                            }
                            
                            Text suffix = config.enabled_translate_prefix_and_suffix_name ? translate_allinone$processTextForTranslation(team.getSuffix()) : team.getSuffix();
                            newName.append(suffix);

                        } else {
                            if (config.enabled_translate_player_name) {
                                newName.append(plainOwnerName);
                            }
                        }
                        replacements.put(originalDecoratedNameKey, newName);
                    });

            translate_allinone$scoreboardReplacements.set(replacements);
        } catch (Exception e) {
            LOGGER.error("Failed to prepare scoreboard sidebar replacements", e);
            translate_allinone$scoreboardReplacements.set(null);
        }
    }

    @Redirect(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V",
                    ordinal = 1
            )
    )
    private void redirectNameDraw(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
        Map<String, Text> replacements = translate_allinone$scoreboardReplacements.get();
        Text textToDraw = text;
        if (replacements != null) {
            Text replacement = replacements.get(text.getString());
            if (replacement != null) {
                textToDraw = replacement;
            }
        }
        instance.drawText(textRenderer, textToDraw, x, y, color, true);
    }

    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("RETURN")
    )
    private void onRenderScoreboardSidebarReturn(DrawContext drawContext, ScoreboardObjective objective, CallbackInfo ci) {
        translate_allinone$scoreboardReplacements.remove();
    }
}
