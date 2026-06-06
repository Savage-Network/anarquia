package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.menu.lang.LangMenus;

public final class LangCommand implements CommandInheritor {

    @Command(name = "lang", aliases = {"language", "idioma"}, playerOnly = true,
            description = "Escolhe o idioma das mensagens do servidor",
            usage = "/lang")
    public void execute(BukkitCommandContext ctx) {
        LangMenus.open(ctx.player());
    }
}
