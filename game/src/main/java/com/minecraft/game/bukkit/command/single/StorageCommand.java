package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.storage.PlayerVaultService;

public final class StorageCommand implements CommandInheritor {

    @Command(name = "lixeira", aliases = {"lixo", "trash"}, playerOnly = true,
            description = "Abre a lixeira virtual", usage = "/lixeira")
    public void lixeira(BukkitCommandContext ctx) {
        PlayerVaultService.openTrash(ctx.player());
    }

    @Command(name = "bau", aliases = {"baú", "chest"}, playerOnly = true,
            description = "Abre o bau virtual", usage = "/bau")
    public void bau(BukkitCommandContext ctx) {
        PlayerVaultService.openBau(ctx.player());
    }

    @Command(name = "ec", aliases = {"enderchest", "echest"}, playerOnly = true,
            description = "Abre o ender chest virtual", usage = "/ec")
    public void ec(BukkitCommandContext ctx) {
        PlayerVaultService.openEc(ctx.player());
    }
}
