package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.menu.cosmetic.CosmeticMenus;

public final class EmberCorCommand implements CommandInheritor {

    @Command(name = "embercor", aliases = {"emberpluscor", "epcor"}, playerOnly = true,
            description = "Menu de configuracao EMBER+")
    public void execute(BukkitCommandContext ctx) {
        CosmeticMenus.openEmberPlusHub(ctx.player());
    }
}
