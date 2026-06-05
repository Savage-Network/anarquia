package com.minecraft.lobbyhost.util;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.clan.ClanColor;
import com.minecraft.lobbyhost.api.punishment.PunishmentCategory;
import com.minecraft.lobbyhost.api.punishment.PunishmentReason;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.leaderboard.LeaderboardType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/** Helpers reutilizáveis para tab-complete de comandos. */
public final class TabCompleteUtil {

    private TabCompleteUtil() {}

    public static List<String> filter(String partial, Collection<String> options) {
        String p = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o != null && o.toLowerCase(Locale.ROOT).startsWith(p)) out.add(o);
        }
        return out;
    }

    public static List<String> filter(String partial, String... options) {
        return filter(partial, List.of(options));
    }

    public static String partial(String[] args) {
        return args.length == 0 ? "" : args[args.length - 1];
    }

    public static List<String> sub(BukkitCommandContext ctx, String... subs) {
        if (ctx.getArgs().length != 1) return List.of();
        return filter(ctx.getArgs()[0], subs);
    }

    public static List<String> onlinePlayers(String partial) {
        List<String> out = new ArrayList<>();
        String p = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase(Locale.ROOT).startsWith(p)) {
                out.add(player.getName());
            }
        }
        return out;
    }

    public static List<String> onlinePlayersExcept(String partial, String excludeName) {
        if (excludeName == null) return onlinePlayers(partial);
        List<String> out = new ArrayList<>();
        String p = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(excludeName)) continue;
            if (player.getName().toLowerCase(Locale.ROOT).startsWith(p)) {
                out.add(player.getName());
            }
        }
        return out;
    }

    public static List<String> onlinePlayersExceptSender(BukkitCommandContext ctx) {
        String exclude = ctx.isPlayer() ? ctx.player().getName() : null;
        return onlinePlayersExcept(partial(ctx.getArgs()), exclude);
    }

    public static List<String> onlineAccountNames(String partial) {
        List<String> out = new ArrayList<>();
        String p = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        for (Account account : Core.getAccountController().list()) {
            if (account.getName().toLowerCase(Locale.ROOT).startsWith(p)) {
                out.add(account.getName());
            }
        }
        return out;
    }

    public static List<String> punishmentCategories() {
        List<String> out = new ArrayList<>();
        for (PunishmentCategory c : PunishmentCategory.values()) out.add(c.name());
        return out;
    }

    public static List<String> punishmentReasons() {
        List<String> out = new ArrayList<>();
        for (PunishmentReason r : PunishmentReason.values()) out.add(r.name());
        return out;
    }

    public static List<String> punishmentTimes() {
        return List.of("-1", "perm", "perma", "10m", "30m", "1h", "6h", "12h", "24h", "3d", "7d", "14d", "30d");
    }

    public static List<String> leaderboardTypes() {
        List<String> out = new ArrayList<>();
        for (LeaderboardType t : LeaderboardType.values()) {
            out.add(t.name().toLowerCase(Locale.ROOT));
            if (t == LeaderboardType.GOLDS) out.add("money");
            if (t == LeaderboardType.CLANS) out.add("clans");
        }
        return out;
    }

    public static List<String> clanColorIds() {
        List<String> out = new ArrayList<>();
        for (ClanColor color : ClanColor.values()) {
            out.add(color.getId());
            out.add(color.getName());
        }
        return out;
    }
}
