package com.cedarxuesong.translate_allinone.mixin.mixinInGameGui;

import com.cedarxuesong.translate_allinone.utils.AnimationManager;
import com.cedarxuesong.translate_allinone.utils.cache.ScoreboardTextCache;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ScoreboardConfig;
import com.cedarxuesong.translate_allinone.utils.text.StylePreserver;
import com.cedarxuesong.translate_allinone.utils.text.TemplateProcessor;
import me.shedaniel.autoconfig.AutoConfig;
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
import java.util.Map;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Translate_AllinOne/InGameHudMixin");

    @Unique
    private static final ThreadLocal<Map<Text, Text>> translate_allinone$scoreboardReplacements = new ThreadLocal<>();

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
        } else if (status == ScoreboardTextCache.TranslationStatus.ERROR) {
            MutableText errorText = Text.literal("Error: " + cache.getError(legacyTemplateKey)).formatted(Formatting.RED);
            return errorText;
        } else {
            String reassembledOriginal = TemplateProcessor.reassemble(unicodeTemplate, templateResult.values);
            Text originalTextObject = StylePreserver.reapplyStyles(reassembledOriginal, styleResult.styleMap);
            return AnimationManager.getAnimatedStyledText(originalTextObject);
        }
    }

    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD")
    )
    private void onRenderScoreboardSidebarHead(DrawContext drawContext, ScoreboardObjective objective, CallbackInfo ci) {
        try {
            ScoreboardConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig().scoreboardTranslate;
            if (!config.enabled) {
                translate_allinone$scoreboardReplacements.set(null);
                return;
            }

            Scoreboard scoreboard = objective.getScoreboard();
            Comparator<ScoreboardEntry> comparator = InGameHudAccessor.getScoreboardEntryComparator();
            Map<Text, Text> replacements = new HashMap<>();

            scoreboard.getScoreboardEntries(objective).stream()
                    .filter(score -> !score.hidden())
                    .sorted(comparator)
                    .limit(15L)
                    .forEach(scoreboardEntry -> {
                        Team team = scoreboard.getScoreHolderTeam(scoreboardEntry.owner());
                        Text originalDecoratedName = Team.decorateName(team, scoreboardEntry.name());

                        MutableText newName = Text.empty();
                        if (team != null) {
                            Text prefix = config.enabled_translate_prefix_and_suffix_name ? translate_allinone$processTextForTranslation(team.getPrefix()) : team.getPrefix();
                            newName.append(prefix);

                            if (config.enabled_translate_player_name) {
                                newName.append(scoreboardEntry.name());
                            }
                            
                            Text suffix = config.enabled_translate_prefix_and_suffix_name ? translate_allinone$processTextForTranslation(team.getSuffix()) : team.getSuffix();
                            newName.append(suffix);

                        } else {
                            if (config.enabled_translate_player_name) {
                                newName.append(scoreboardEntry.name());
                            }
                        }
                        replacements.put(originalDecoratedName, newName);
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
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I",
                    ordinal = 1
            )
    )
    private int redirectNameDraw(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
        Map<Text, Text> replacements = translate_allinone$scoreboardReplacements.get();
        Text textToDraw = text;
        if (replacements != null && replacements.containsKey(text)) {
            textToDraw = replacements.get(text);
        }
        return instance.drawText(textRenderer, textToDraw, x, y, color, true);
    }

    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("RETURN")
    )
    private void onRenderScoreboardSidebarReturn(DrawContext drawContext, ScoreboardObjective objective, CallbackInfo ci) {
        translate_allinone$scoreboardReplacements.remove();
    }
}