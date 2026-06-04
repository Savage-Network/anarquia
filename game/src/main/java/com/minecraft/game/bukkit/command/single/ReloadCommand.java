package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.GamePlugin;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;

public final class ReloadCommand implements CommandInheritor {

    @Command(name = "gamereload", permission = "game.admin",
            description = "Recarrega configuracoes do plugin")
    public void execute(BukkitCommandContext ctx) {
        GamePlugin.getInstance().reloadAll();
        ctx.reply("§aConfiguracoes recarregadas.");
    }
}
