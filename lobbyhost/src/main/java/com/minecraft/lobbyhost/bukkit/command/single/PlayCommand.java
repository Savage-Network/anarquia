package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;

public final class PlayCommand implements CommandInheritor {

    @Command(name = "play", playerOnly = true,
            description = "Abre o menu de selecao de modos")
    public void execute(BukkitCommandContext ctx) {
        ctx.reply("§eClique nos NPCs do hub para escolher um modo — ou use §6/play §equando o menu estiver pronto.");
    }
}
