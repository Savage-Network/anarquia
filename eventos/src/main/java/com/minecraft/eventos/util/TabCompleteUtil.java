package com.minecraft.eventos.util;

import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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

    public static List<String> onlinePlayersExceptSender(BukkitCommandContext ctx) {
        String exclude = ctx.isPlayer() ? ctx.player().getName() : null;
        String partial = ctx.getArgs().length == 0 ? "" : ctx.getArgs()[ctx.getArgs().length - 1];
        if (exclude == null) return onlinePlayers(partial);
        List<String> out = new ArrayList<>();
        String p = partial.toLowerCase(Locale.ROOT);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(exclude)) continue;
            if (player.getName().toLowerCase(Locale.ROOT).startsWith(p)) {
                out.add(player.getName());
            }
        }
        return out;
    }
}
