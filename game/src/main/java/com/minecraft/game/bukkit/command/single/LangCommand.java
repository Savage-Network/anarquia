package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.menu.lang.LangMenus;

public final class LangCommand implements CommandInheritor {

    @Command(name = "lang", aliases = {"language", "idioma"}, playerOnly = true,
            description = "Escolhe o idioma das mensagens do servidor",
            usage = "/lang")
    public void execute(BukkitCommandContext ctx) {
        LangMenus.open(ctx.player());
    }
}
