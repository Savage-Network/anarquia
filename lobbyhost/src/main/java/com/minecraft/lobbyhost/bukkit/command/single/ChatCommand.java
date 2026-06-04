package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.chat.BlockListService;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChatCommand implements CommandInheritor {

    @Command(name = "chat", aliases = {"c"}, playerOnly = true,
            description = "Ajuda e opcoes de chat",
            usage = "/chat [help|ignore|unignore|list] [nick]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply("§cConta nao carregada.");
            return;
        }
        String[] args = ctx.getArgs();
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(account);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "ignore", "bloquear", "block" -> ignore(account, ctx, args);
            case "unignore", "desbloquear", "unblock" -> unignore(account, ctx, args);
            case "list", "lista", "ignorados" -> list(account);
            default -> sendHelp(account);
        }
    }

    @Completer(name = "chat")
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return filter(List.of("help", "ignore", "unignore", "list"), ctx.getArgs()[0]);
        }
        if (ctx.getArgs().length == 2 && (ctx.getArgs()[0].equalsIgnoreCase("ignore")
                || ctx.getArgs()[0].equalsIgnoreCase("unignore"))) {
            String partial = ctx.getArgs()[1].toLowerCase();
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(partial)) names.add(p.getName());
            }
            return names;
        }
        return List.of();
    }

    private void ignore(Account account, BukkitCommandContext ctx, String[] args) {
        if (args.length < 2) {
            account.send("§eUso: /chat ignore <nick>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        UUID tid = target != null ? target.getUniqueId() : resolveUuid(args[1]);
        if (tid == null) {
            account.send("§cJogador nao encontrado.");
            return;
        }
        if (tid.equals(account.getId())) {
            account.send("§cVoce nao pode ignorar a si mesmo.");
            return;
        }
        BlockListService.block(account, tid);
        account.send("§aVoce ignorou §f" + args[1] + "§a. Mensagens publicas dele nao aparecerao para voce.");
    }

    private void unignore(Account account, BukkitCommandContext ctx, String[] args) {
        if (args.length < 2) {
            account.send("§eUso: /chat unignore <nick>");
            return;
        }
        UUID tid = resolveUuid(args[1]);
        if (tid == null) {
            account.send("§cJogador nao encontrado na sua lista.");
            return;
        }
        BlockListService.unblock(account, tid);
        account.send("§aVoce desbloqueou §f" + args[1] + "§a.");
    }

    private void list(Account account) {
        int n = BlockListService.count(account);
        if (n == 0) {
            account.send("§7Nenhum jogador ignorado.");
            return;
        }
        account.send("§6Ignorados §7(" + n + "):");
        for (var b : account.getContext().getBlocks()) {
            if (b.getId() == null) continue;
            var off = Bukkit.getOfflinePlayer(b.getId());
            account.send("§7- §f" + (off.getName() != null ? off.getName() : b.getId().toString().substring(0, 8)));
        }
    }

    private void sendHelp(Account account) {
        account.send("§6§lChat");
        account.send("§7Chat publico: §f[Lobby] §7+ tag + nick");
        account.send("§7Menções: §e@Nick §7no texto");
        account.send("§7Staff: §f! msg §7ou §f/sc msg §7| modo em §e/prefs");
        account.send("§7Privado: §f/tell §7e §f/r §7(no proxy)");
        account.send("§e/chat ignore|unignore|list §7— bloquear jogadores");
    }

    private UUID resolveUuid(String name) {
        Player p = Bukkit.getPlayerExact(name);
        if (p != null) return p.getUniqueId();
        for (var b : Core.getAccountController().list()) {
            if (b.getName().equalsIgnoreCase(name)) return b.getId();
        }
        return null;
    }

    private List<String> filter(List<String> options, String partial) {
        String p = partial == null ? "" : partial.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(p)).toList();
    }
}
