package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.menu.anticheat.AnticheatHubMenu;

public final class AnticheatCommand implements CommandInheritor {

    @Command(name = "anticheat", aliases = {"ac"}, rank = RankType.OWNER, playerOnly = true,
            description = "Painel de configuracao do anticheat (Owner)",
            usage = "/ac")
    public void open(BukkitCommandContext ctx) {
        new AnticheatHubMenu(ctx.player()).open();
    }
}
