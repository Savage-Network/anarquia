package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.api.vipkey.VipKey;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.rank.RankGrantService;
import com.minecraft.game.bukkit.vipkey.VipKeyService;
import com.minecraft.game.util.AccountSync;
import com.minecraft.game.util.TabCompleteUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

public final class AdminKeyCommand implements CommandInheritor {

    @Command(name = "adminkey", aliases = {"akey", "vipkey"},
            permission = "game.admin",
            description = "Gera, lista e concede VIP temporario",
            usage = "/adminkey gerar <RANK> <TEMPO> | /adminkey dar <jogador> <RANK> <TEMPO> | /adminkey listar")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            sendHelp(ctx);
            return;
        }

        String sub = ctx.getArgs()[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "gerar", "generate", "criar" -> generate(ctx);
            case "dar", "give", "conceder" -> give(ctx);
            case "listar", "list", "lista" -> list(ctx);
            default -> sendHelp(ctx);
        }
    }

    private void generate(BukkitCommandContext ctx) {
        if (ctx.getArgs().length < 3) {
            ctx.reply("§cUso: /adminkey gerar <RANK> <TEMPO>");
            ctx.reply("§7Ex.: /adminkey gerar VIP 30d");
            ctx.reply("§7Tempo: 7d, 30d, 1h, perm (permanente)");
            return;
        }

        var rankOpt = VipKeyService.parseVipRank(ctx.getArgs()[1]);
        if (rankOpt.isEmpty()) {
            ctx.reply("§cRank VIP invalido. Use: VIP, NATIVE, EMBER, EMBER_PLUS, SAVAGE, BETA, PRECIOUS");
            return;
        }

        long durationMs;
        try {
            durationMs = RankGrantService.parseDurationMs(ctx.getArgs()[2]);
        } catch (Exception e) {
            ctx.reply("§cTempo invalido: §e" + ctx.getArgs()[2]);
            ctx.reply("§7Ex.: 30d, 7d, 12h, 1h, perm");
            return;
        }

        Player creator = ctx.isPlayer() ? ctx.player() : null;
        VipKey key = VipKeyService.generate(creator, rankOpt.get(), durationMs);

        ctx.reply("§6§lSavageMC §7| §fKey VIP gerada");
        ctx.reply("§7Rank: §6" + key.getRankType().name()
                + " §7| Tempo: §f" + RankGrantService.formatDuration(key.getDurationMs()));
        ctx.reply("§7Codigo: §a§l" + key.getCode());
        ctx.reply("§7Formato: §fXXXX-XXXX-XXXX");
        ctx.reply("§7Jogador usa: §f/resgatar " + key.getCode());
    }

    private void give(BukkitCommandContext ctx) {
        if (ctx.getArgs().length < 4) {
            ctx.reply("§cUso: /adminkey dar <jogador> <RANK> <TEMPO>");
            ctx.reply("§7Ex.: /adminkey dar zrwfaWASD VIP 30d");
            return;
        }

        var rankOpt = VipKeyService.parseVipRank(ctx.getArgs()[2]);
        if (rankOpt.isEmpty()) {
            ctx.reply("§cRank VIP invalido.");
            return;
        }

        long durationMs;
        try {
            durationMs = RankGrantService.parseDurationMs(ctx.getArgs()[3]);
        } catch (Exception e) {
            ctx.reply("§cTempo invalido: §e" + ctx.getArgs()[3]);
            return;
        }

        RankType rankType = rankOpt.get();
        mutate(ctx, ctx.getArgs()[1], account -> {
            VipKeyService.grantDirect(account, rankType, durationMs);
            ctx.reply("§aVIP §6" + rankType.name() + " §apor §f"
                    + RankGrantService.formatDuration(durationMs)
                    + " §aconcedido a §f" + account.getName() + "§a.");
        });
    }

    private void list(BukkitCommandContext ctx) {
        List<VipKey> keys = VipKeyService.listUnredeemed(30);
        if (keys.isEmpty()) {
            ctx.reply("§7Nenhuma key VIP pendente.");
            return;
        }

        ctx.reply("§6§lKeys VIP pendentes §7(" + keys.size() + ")");
        for (VipKey key : keys) {
            ctx.reply("§8▪ §6" + key.getRankType().name()
                    + " §7| §f" + RankGrantService.formatDuration(key.getDurationMs())
                    + " §7| §a" + key.getCode()
                    + " §8(" + nullSafe(key.getCreatedByName()) + ")");
        }
    }

    private void mutate(BukkitCommandContext ctx, String playerName, Consumer<Account> action) {
        Player online = Bukkit.getPlayerExact(playerName);
        if (online != null) {
            Account account = Core.getAccountController().of(online.getUniqueId());
            if (account == null) {
                ctx.replyKey("errors.account-not-loaded");
                return;
            }
            action.accept(account);
            return;
        }

        GamePlugin plugin = GamePlugin.getInstance();
        ctx.reply("§7Carregando conta...");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Optional<Account> opt = Core.getAccountData().loadByName(playerName);
            Account account = opt.orElseGet(() -> {
                OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
                return new Account(op.getUniqueId(), playerName);
            });
            action.accept(account);
            AccountSync.saveAndBroadcast(account);
            AccountSync.broadcastUpdate(account);
        });
    }

    private static String nullSafe(String value) {
        return value == null || value.isBlank() ? "?" : value;
    }

    private static void sendHelp(BukkitCommandContext ctx) {
        ctx.reply("§6/adminkey gerar <RANK> <TEMPO> §7- gera key (XXXX-XXXX-XXXX)");
        ctx.reply("§6/adminkey dar <jogador> <RANK> <TEMPO> §7- da VIP direto");
        ctx.reply("§6/adminkey listar §7- keys nao resgatadas");
        ctx.reply("§7Ranks: VIP, NATIVE, EMBER, EMBER_PLUS, SAVAGE, BETA, PRECIOUS");
        ctx.reply("§7Tempo: 30d, 7d, 12h, perm");
    }

    @Completer(name = "adminkey", subCommands = {"akey", "vipkey"})
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.filter(ctx.getArgs()[0], "gerar", "dar", "listar");
        }
        if (isSub(ctx.getArgs()[0], "gerar") && ctx.getArgs().length == 2) {
            return vipRankNames(ctx.getArgs()[1]);
        }
        if (isSub(ctx.getArgs()[0], "gerar") && ctx.getArgs().length == 3) {
            return TabCompleteUtil.filter(ctx.getArgs()[2], TabCompleteUtil.punishmentTimes());
        }
        if (isSub(ctx.getArgs()[0], "dar") && ctx.getArgs().length == 2) {
            return TabCompleteUtil.onlineAccountNames(ctx.getArgs()[1]);
        }
        if (isSub(ctx.getArgs()[0], "dar") && ctx.getArgs().length == 3) {
            return vipRankNames(ctx.getArgs()[2]);
        }
        if (isSub(ctx.getArgs()[0], "dar") && ctx.getArgs().length == 4) {
            return TabCompleteUtil.filter(ctx.getArgs()[3], TabCompleteUtil.punishmentTimes());
        }
        return List.of();
    }

    private static List<String> vipRankNames(String partial) {
        List<String> ranks = new ArrayList<>();
        for (RankType type : VipKeyService.vipRanks()) ranks.add(type.name());
        return TabCompleteUtil.filter(partial, ranks);
    }

    private static boolean isSub(String sub, String expected) {
        if (sub == null) return false;
        String s = sub.toLowerCase(Locale.ROOT);
        return switch (expected) {
            case "gerar" -> s.equals("gerar") || s.equals("generate") || s.equals("criar");
            case "dar" -> s.equals("dar") || s.equals("give") || s.equals("conceder");
            default -> false;
        };
    }
}
