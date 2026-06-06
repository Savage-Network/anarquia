package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.chat.StaffChatBridge;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;

import java.util.List;

public final class StaffChatCommand implements CommandInheritor {

    @Command(name = "staffchat", aliases = {"sc"}, playerOnly = true,
            description = "Chat da equipe (rede)",
            usage = "/sc <mensagem>")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.replyKey("errors.account-not-loaded");
            return;
        }
        if (!StaffChatBridge.canUseStaffChat(account, ctx.player())) {
            ctx.reply("§cSem permissao para staff chat.");
            return;
        }
        if (ctx.getArgs().length == 0) {
            ctx.reply("§eUso: /sc <mensagem>");
            return;
        }
        StaffChatBridge.send(account, ctx.joinArgs(0));
        ctx.reply("§7(Staff chat)");
    }

    @Completer(name = "staffchat", subCommands = {"sc"})
    public List<String> tab(BukkitCommandContext ctx) {
        return List.of();
    }
}
