package com.cedarxuesong.translate_allinone.command;

import com.cedarxuesong.translate_allinone.utils.MessageUtils;
import com.cedarxuesong.translate_allinone.utils.translate.ChatOutputTranslateManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.UUID;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ChatHudTranslateCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> getArgumentBuilder() {
        return literal("translatechatline")
                .then(argument("messageId", StringArgumentType.string())
                        .executes(ChatHudTranslateCommand::run));
    }

    private static int run(CommandContext<FabricClientCommandSource> context) {
        String messageIdStr = StringArgumentType.getString(context, "messageId");
        UUID messageId;
        try {
            messageId = UUID.fromString(messageIdStr);
        } catch (IllegalArgumentException e) {
            context.getSource().sendError(Text.literal("Invalid message ID: " + messageIdStr));
            return 0;
        }

        Text originalMessage = MessageUtils.getTrackedMessage(messageId);
        if (originalMessage == null) {
            context.getSource().sendError(Text.literal("Message not found for ID: " + messageIdStr));
            return 0;
        }

        ChatOutputTranslateManager.translate(messageId, originalMessage);

        return 1;
    }
}
