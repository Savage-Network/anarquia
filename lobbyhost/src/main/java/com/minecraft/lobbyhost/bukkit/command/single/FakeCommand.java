package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.fake.FakeService;
import com.minecraft.lobbyhost.bukkit.menu.FakeMenus;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FakeCommand implements CommandInheritor {

    @Command(name = "fake", aliases = {"fakenick", "nickfake", "disguise"}, playerOnly = true,
            description = "Sistema de fake nick (VIP+)", usage = "/fake [menu|random|off|<nick>]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.replyKey("errors.account-not-loaded");
            return;
        }
        if (!FakeService.canUse(account)) {
            ctx.reply("§cApenas VIP+ pode usar fake nick.");
            return;
        }

        Player player = ctx.player();
        String[] args = ctx.getArgs();

        if (args.length == 0 || "menu".equalsIgnoreCase(args[0])) {
            FakeMenus.openMainMenu(player);
            return;
        }

        String sub = args[0].toLowerCase();
        if ("off".equals(sub) || "reset".equals(sub) || "remover".equals(sub)) {
            FakeService.disable(account, player, ctx::reply);
            return;
        }
        if ("random".equals(sub) || "aleatorio".equals(sub) || "rand".equals(sub)) {
            FakeService.enable(account, player, FakeService.randomNick(), ctx::reply, ctx::reply);
            return;
        }
        if ("info".equals(sub) || "status".equals(sub)) {
            if (account.isFake()) {
                ctx.reply("§aFake ativo: §f" + account.getNickname() + " §7(real: §8" + account.getRealName() + "§7)");
            } else {
                ctx.reply("§7Você não está usando fake nick.");
            }
            return;
        }

        String nick = args[0];
        FakeService.enable(account, player, nick, ctx::reply, ctx::reply);
    }

    @Completer(name = "fake")
    public List<String> tab(BukkitCommandContext ctx) {
        List<String> out = new ArrayList<>();
        String partial = ctx.getArgs().length == 0 ? "" : ctx.getArgs()[0].toLowerCase();
        for (String sub : Arrays.asList("menu", "random", "off", "info")) {
            if (sub.startsWith(partial)) out.add(sub);
        }
        return out;
    }
}
