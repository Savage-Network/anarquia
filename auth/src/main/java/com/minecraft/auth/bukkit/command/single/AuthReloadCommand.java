package com.minecraft.auth.bukkit.command.single;

import com.minecraft.auth.AuthPlugin;
import com.minecraft.auth.bukkit.command.annotation.Command;
import com.minecraft.auth.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.auth.bukkit.command.structure.CommandInheritor;

public final class AuthReloadCommand implements CommandInheritor {

    @Command(name = "authreload", permission = "auth.admin",
            description = "Recarrega configuracoes do auth.jar")
    public void execute(BukkitCommandContext ctx) {
        AuthPlugin.getInstance().reloadAll();
        ctx.reply("§aConfiguracoes do auth.jar recarregadas.");
    }
}
