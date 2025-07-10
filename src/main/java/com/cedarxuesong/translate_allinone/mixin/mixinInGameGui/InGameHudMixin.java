package com.cedarxuesong.translate_allinone.mixin.mixinInGameGui;

import com.cedarxuesong.translate_allinone.utils.AnimationManager;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ScoreboardConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.ColorHelper;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Translate_AllinOne/InGameHudMixin");

    @Unique
    private static final ThreadLocal<Map<Text, Text>> translate_allinone$scoreboardReplacements = new ThreadLocal<>();

    @Unique
    private void translate_allinone$animateTextPart(MutableText target, Text source, long time, AtomicInteger charIndex) {
        source.visit((style, s) -> {
            for (int i = 0; i < s.length(); i++) {
                float sine = (float) (Math.sin(time / 200.0 + charIndex.get() / 5.0) + 1.0) / 2.0f;
                int color = ColorHelper.lerp(sine, 0x555555, 0xAAAAAA);
                Style newStyle = style.withColor(TextColor.fromRgb(color));
                target.append(Text.literal(String.valueOf(s.charAt(i))).setStyle(newStyle));
                charIndex.incrementAndGet();
            }
            return Optional.empty();
        }, Style.EMPTY);
    }

    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD")
    )
    private void onRenderScoreboardSidebarHead(DrawContext drawContext, ScoreboardObjective objective, CallbackInfo ci) {
        try {
            ScoreboardConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig().scoreboardConfig;
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

                        MutableText newName;
                        if (team != null) {
                            newName = Text.empty();
                            long time = System.currentTimeMillis();
                            AtomicInteger charIndex = new AtomicInteger(0);

                            Text prefix = team.getPrefix();
                            if (config.enabled_translate_prefix_and_suffix_name) {
                                translate_allinone$animateTextPart(newName, prefix, time, charIndex);
                            } else {
                                newName.append(prefix);
                            }

                            if (config.enabled_translate_player_name) {
                                Text playerName = scoreboardEntry.name();
                                newName.append(playerName);
                                if (config.enabled_translate_prefix_and_suffix_name) {
                                    playerName.visit((style, s) -> {
                                        charIndex.addAndGet(s.length());
                                        return Optional.empty();
                                    }, Style.EMPTY);
                                }
                            }

                            Text suffix = team.getSuffix();
                            if (config.enabled_translate_prefix_and_suffix_name) {
                                translate_allinone$animateTextPart(newName, suffix, time, charIndex);
                            } else {
                                newName.append(suffix);
                            }
                        } else {
                            if (config.enabled_translate_player_name) {
                                newName = scoreboardEntry.name().copy();
                            } else {
                                newName = Text.empty();
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