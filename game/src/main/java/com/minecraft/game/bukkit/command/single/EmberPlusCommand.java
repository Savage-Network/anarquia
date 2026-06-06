package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.menu.cosmetic.CosmeticMenus;

public final class EmberPlusCommand implements CommandInheritor {

    @Command(name = "plus", aliases = {"+", "mais", "emberplus", "ember+", "embertplus", "embermais"},
            playerOnly = true,
            rank = RankType.EMBER_PLUS,
            description = "Menu de configuracao EMBER+",
            usage = "/plus")
    public void execute(BukkitCommandContext ctx) {
        CosmeticMenus.openEmberPlusHub(ctx.player());
    }
}
