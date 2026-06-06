package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.menu.FriendMenu;
import com.minecraft.lobbyhost.social.FriendService;
import com.minecraft.lobbyhost.util.TabCompleteUtil;

import java.util.List;

public final class AmigoCommand implements CommandInheritor {

    @Command(name = "amigo", aliases = {"amigos", "friend", "friends"}, playerOnly = true,
            description = "Sistema de amigos do lobby",
            usage = "/amigo [add|aceitar|remover|lista] [nick]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.replyKey("errors.account-not-loaded");
            return;
        }
        String[] args = ctx.getArgs();
        if (args.length == 0) {
            FriendMenu.open(ctx.player());
            return;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "add", "adicionar", "enviar" -> {
                if (args.length < 2) {
                    ctx.reply("§eUso: /amigo add <nick>");
                    return;
                }
                FriendService.sendRequest(account, args[1], () -> {});
            }
            case "aceitar", "accept" -> {
                if (args.length < 2) {
                    ctx.reply("§eUso: /amigo aceitar <nick>");
                    return;
                }
                FriendService.acceptRequest(account, args[1], () -> {});
            }
            case "remover", "remove", "delete" -> {
                if (args.length < 2) {
                    ctx.reply("§eUso: /amigo remover <nick>");
                    return;
                }
                FriendService.removeFriend(account, args[1], () -> {});
            }
            case "lista", "list" -> {
                var friends = FriendService.listFriends(account);
                if (friends.isEmpty()) {
                    ctx.reply("§7Voce ainda nao tem amigos.");
                    return;
                }
                ctx.reply("§6§lAmigos §7(" + friends.size() + ")");
                for (var f : friends) {
                    boolean on = ctx.player().getServer().getPlayer(f.getId()) != null;
                    ctx.reply((on ? "§a" : "§7") + f.getName());
                }
            }
            default -> ctx.reply("§e/amigo §7| §e/amigo add|aceitar|remover|lista");
        }
    }

    @Completer(name = "amigo", subCommands = {"amigos", "friend", "friends"})
    public List<String> tab(BukkitCommandContext ctx) {
        String[] args = ctx.getArgs();
        if (args.length <= 1) {
            return TabCompleteUtil.filter(args.length == 1 ? args[0] : "",
                    "add", "adicionar", "enviar", "aceitar", "accept",
                    "remover", "remove", "delete", "lista", "list");
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("add") || sub.equals("adicionar") || sub.equals("enviar")
                    || sub.equals("aceitar") || sub.equals("accept")
                    || sub.equals("remover") || sub.equals("remove") || sub.equals("delete")) {
                return TabCompleteUtil.onlinePlayersExceptSender(ctx);
            }
        }
        return List.of();
    }
}
