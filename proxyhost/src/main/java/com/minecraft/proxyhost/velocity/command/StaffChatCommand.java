package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.network.StaffChatService;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class StaffChatCommand implements SimpleCommand {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public void execute(Invocation invocation) {
        CommandSource src = invocation.source();
        String[] args = invocation.arguments();

        if (!(src instanceof Player sender)) {
            src.sendMessage(legacy("&c/sc só pode ser usado por jogadores."));
            return;
        }

        Account account = Core.getAccountController().of(sender.getUniqueId());
        if (account == null || (!account.isStaffer() && !sender.hasPermission("proxyhost.staffchat"))) {
            sender.sendMessage(legacy("&cVocê não tem permissão para usar o staff chat."));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(legacy("&cUso: /sc <mensagem...>"));
            return;
        }

        String message = String.join(" ", args);
        if (!StaffChatService.send(sender, account, message)) {
            sender.sendMessage(legacy("&cNão foi possível enviar a mensagem."));
        }
    }

    private net.kyori.adventure.text.Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }
}
