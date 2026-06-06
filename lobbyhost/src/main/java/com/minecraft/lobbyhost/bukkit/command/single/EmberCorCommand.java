package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.menu.CosmeticMenus;

public final class EmberCorCommand implements CommandInheritor {

    @Command(name = "embercor", aliases = {"emberpluscor", "epcor"}, playerOnly = true,
            description = "Menu de cores do rank EMBER+")
    public void execute(BukkitCommandContext ctx) {
        CosmeticMenus.openEmberColorMenu(ctx.player());
    }
}
