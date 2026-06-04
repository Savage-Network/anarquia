package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;

public final class MissionCommand implements CommandInheritor {

    @Command(name = "mission", aliases = {"missao", "missoes"}, playerOnly = true,
            description = "Abre o menu de missoes")
    public void execute(BukkitCommandContext ctx) {
        // Placeholder - menu sera implementado na proxima fase.
        ctx.reply("§eMenu de missoes ainda em desenvolvimento.");
    }
}
