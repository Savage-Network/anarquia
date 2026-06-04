package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;

public final class LobbyReloadCommand implements CommandInheritor {

    @Command(name = "lobbyreload", permission = "lobbyhost.admin",
            description = "Recarrega configuracoes do lobbyhost.jar")
    public void execute(BukkitCommandContext ctx) {
        LobbyHostPlugin.getInstance().reloadAll();
        ctx.reply("§aConfiguracoes do lobbyhost.jar recarregadas.");
    }
}
