package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.kit.KitDefinition;
import com.minecraft.game.bukkit.kit.KitManager;
import com.minecraft.game.bukkit.menu.kit.KitHubMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class KitCommand implements CommandInheritor {

    @Command(name = "kit", aliases = {"kits"}, playerOnly = true,
            description = "Lista e recebe kits", usage = "/kit [nome]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply("§cConta nao carregada.");
            return;
        }

        if (ctx.getArgs().length == 0) {
            new KitHubMenu(ctx.player(), account).open();
            return;
        }

        String result = KitManager.apply(account, ctx.player(), ctx.getArgs()[0]);
        ctx.reply(result);
    }

    @Completer(name = "kit", subCommands = {"kits"})
    public List<String> tab(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) return List.of();
        String partial = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase(Locale.ROOT) : "";
        List<String> out = new ArrayList<>();
        for (KitDefinition kit : KitManager.availableFor(account)) {
            if (kit.getId().startsWith(partial)) out.add(kit.getId());
        }
        return out;
    }
}
