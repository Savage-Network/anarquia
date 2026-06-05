package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.bukkit.chat.BlockListService;
import com.minecraft.game.bukkit.chat.ChatManager;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChatCommand implements CommandInheritor {

    @Command(name = "chat", aliases = {"c"}, playerOnly = true,
            description = "Chat — ignore, clearchat, lock",
            usage = "/chat [help|ignore|clear|lock|unlock|slow] ...")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply("§cConta nao carregada.");
            return;
        }
        String[] args = ctx.getArgs();
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(account, ctx.getSender());
            return;
        }
        switch (args[0].toLowerCase()) {
            case "ignore", "bloquear", "block" -> ignore(account, ctx, args);
            case "unignore", "desbloquear", "unblock" -> unignore(account, ctx, args);
            case "list", "lista", "ignorados" -> list(account);
            case "clear", "limpar", "clearchat" -> clearChat(ctx);
            case "lock", "travar", "chatlock" -> setLock(ctx, true);
            case "unlock", "destravar", "chatunlock" -> setLock(ctx, false);
            case "slow", "slowmode" -> slowMode(ctx, args);
            default -> sendHelp(account, ctx.getSender());
        }
    }

    @Command(name = "clearchat", rank = RankType.HELPER, permission = "game.chat.mod",
            description = "Limpa o chat", usage = "/clearchat")
    public void clearchatAlias(BukkitCommandContext ctx) {
        clearChat(ctx);
    }

    @Command(name = "chatlock", rank = RankType.MOD, permission = "game.chat.mod",
            description = "Trava o chat publico", usage = "/chatlock")
    public void chatlockAlias(BukkitCommandContext ctx) {
        setLock(ctx, true);
    }

    @Command(name = "chatunlock", rank = RankType.MOD, permission = "game.chat.mod",
            description = "Libera o chat publico", usage = "/chatunlock")
    public void chatunlockAlias(BukkitCommandContext ctx) {
        setLock(ctx, false);
    }

    @Completer(name = "chat", subCommands = {"c"})
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            List<String> opts = new ArrayList<>(List.of("help", "ignore", "unignore", "list"));
            if (canMod(ctx.getSender())) {
                opts.addAll(List.of("clear", "lock", "unlock", "slow"));
            }
            return filter(opts, ctx.getArgs()[0]);
        }
        if (ctx.getArgs().length == 2 && ctx.getArgs()[0].equalsIgnoreCase("slow") && canMod(ctx.getSender())) {
            return filter(List.of("0", "3", "5", "10", "15", "30"), ctx.getArgs()[1]);
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

    @Completer(name = "clearchat")
    public List<String> tabClear(BukkitCommandContext ctx) { return List.of(); }

    @Completer(name = "chatlock")
    public List<String> tabLock(BukkitCommandContext ctx) { return List.of(); }

    @Completer(name = "chatunlock")
    public List<String> tabUnlock(BukkitCommandContext ctx) { return List.of(); }

    private void clearChat(BukkitCommandContext ctx) {
        if (!canMod(ctx.getSender())) {
            ctx.reply("§cSem permissao.");
            return;
        }
        Player staff = ctx.isPlayer() ? ctx.player() : null;
        ChatManager.clearChat(staff);
        ctx.reply("§aChat limpo.");
    }

    private void setLock(BukkitCommandContext ctx, boolean lock) {
        if (!canMod(ctx.getSender())) {
            ctx.reply("§cSem permissao.");
            return;
        }
        ChatManager.setLocked(lock);
        String name = ctx.isPlayer() ? ctx.player().getName() : "CONSOLE";
        ChatManager.broadcastLockNotice(lock, name);
        ctx.reply(lock ? "§cChat travado." : "§aChat liberado.");
    }

    private void slowMode(BukkitCommandContext ctx, String[] args) {
        if (!canMod(ctx.getSender())) {
            ctx.reply("§cSem permissao.");
            return;
        }
        if (args.length < 2) {
            int current = ChatManager.getSlowModeSeconds();
            ctx.reply(current <= 0
                    ? "§7Slow mode: §fdesativado"
                    : "§7Slow mode: §f" + current + "s §7entre mensagens");
            ctx.reply("§e/chat slow <segundos> §7— use §e0 §7para desativar");
            return;
        }
        int seconds;
        try {
            seconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            ctx.reply("§cUse um numero valido. Ex: §e/chat slow 5");
            return;
        }
        if (seconds < 0 || seconds > 120) {
            ctx.reply("§cIntervalo entre 0 e 120 segundos.");
            return;
        }
        ChatManager.setSlowModeSeconds(seconds);
        String name = ctx.isPlayer() ? ctx.player().getName() : "CONSOLE";
        ChatManager.broadcastSlowNotice(seconds, name);
        ctx.reply(seconds <= 0 ? "§aSlow mode desativado." : "§eSlow mode: §f" + seconds + "s");
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
        account.send("§aVoce ignorou §f" + args[1] + "§a.");
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

    private void sendHelp(Account account, CommandSender sender) {
        account.send("§6§lChat");
        account.send("§7Formato: §ftag + nick §7+ mensagem §8(sem prefixo de servidor)");
        account.send("§7Menções: §e@Nick");
        account.send("§7Staff: §f! msg §7| §f/sc §7| modo em §e/prefs");
        account.send("§7Privado: §f/tell §7e §f/r");
        account.send("§e/chat ignore|unignore|list");
        if (canMod(sender)) {
            account.send("§cStaff:");
            account.send("§e/chat clear §7| §e/clearchat");
            account.send("§e/chat lock|unlock §7| §e/chatlock §7| §e/chatunlock");
            account.send("§e/chat slow <seg> §7— cooldown global (0 desliga)");
        }
    }

    private static boolean canMod(CommandSender sender) {
        return sender.hasPermission("game.chat.mod");
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
