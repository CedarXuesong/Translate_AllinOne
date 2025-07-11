package com.cedarxuesong.translate_allinone.registration;

import com.cedarxuesong.translate_allinone.command.MainCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
 
public class CommandManager {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> MainCommand.register(dispatcher));
    }
} 