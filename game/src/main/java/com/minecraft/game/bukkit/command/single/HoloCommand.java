package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.hologram.HologramManager;
import com.minecraft.game.bukkit.hologram.HologramType;
import com.minecraft.game.bukkit.hologram.LeaderboardHologramManager;
import com.minecraft.game.leaderboard.LeaderboardType;
import com.minecraft.game.message.Messages;
import com.minecraft.game.util.TabCompleteUtil;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HoloCommand implements CommandInheritor {

    @Command(name = "holo", aliases = {"hologram", "holograma"},
            permission = "game.admin",
            description = "Gerencia hologramas TextDisplay do servidor",
            usage = "/holo <list|set|remove|reload|explicacao|kills|deaths|...>")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            list(ctx);
            return;
        }

        String sub = ctx.getArgs()[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list", "lista" -> list(ctx);
            case "set", "pos", "posicionar" -> set(ctx);
            case "remove", "remover", "delete" -> remove(ctx);
            case "reload", "recarregar" -> reload(ctx);
            default -> spawnById(ctx, sub);
        }
    }

    private void list(BukkitCommandContext ctx) {
        ctx.replyKey("holo.list-header");
        for (HologramType type : HologramType.values()) {
            replyListLine(ctx, type.id(), HologramManager.getSavedLocation(type),
                    HologramManager.isEnabled(type));
        }
        for (LeaderboardType type : LeaderboardType.values()) {
            replyListLine(ctx, type.holoId(),
                    LeaderboardHologramManager.getSavedLocation(type),
                    LeaderboardHologramManager.isEnabled(type));
        }
        ctx.replyKey("holo.list-footer");
        ctx.replyKey("holo.list-leaderboard-hint");
    }

    private void replyListLine(BukkitCommandContext ctx, String id, Location loc, boolean enabled) {
        String coords = loc != null
                ? loc.getWorld().getName() + " " + fmt(loc.getX()) + " " + fmt(loc.getY()) + " " + fmt(loc.getZ())
                : Messages.raw("holo.default-coords", ctx.lang());
        String status = Messages.raw(enabled ? "holo.status-on" : "holo.status-off", ctx.lang());
        ctx.replyKey("holo.list-line", Map.of(
                "id", id,
                "enabled", status,
                "coords", coords));
    }

    private void set(BukkitCommandContext ctx) {
        if (!ctx.isPlayer()) {
            ctx.replyKey("command.player-only");
            return;
        }
        if (ctx.getArgs().length < 2) {
            ctx.replyKey("holo.usage-set");
            return;
        }

        String raw = ctx.getArgs()[1];
        Location loc = ctx.player().getLocation();

        HologramType staticType = HologramType.fromId(raw).orElse(null);
        if (staticType != null) {
            HologramManager.savePosition(staticType, loc);
            HologramManager.setEnabled(staticType, true);
            HologramManager.spawn(staticType);
            replySet(ctx, staticType.id(), loc);
            return;
        }

        LeaderboardType leaderboardType = LeaderboardType.fromArg(raw);
        if (leaderboardType != null) {
            LeaderboardHologramManager.savePosition(leaderboardType, loc);
            LeaderboardHologramManager.setEnabled(leaderboardType, true);
            if (LeaderboardHologramManager.spawn(leaderboardType)) {
                replySet(ctx, leaderboardType.holoId(), loc);
            } else {
                ctx.replyKey("holo.spawn-failed", Map.of("id", leaderboardType.holoId()));
            }
            return;
        }

        ctx.replyKey("holo.unknown", Map.of("id", raw));
    }

    private void remove(BukkitCommandContext ctx) {
        if (ctx.getArgs().length < 2) {
            ctx.replyKey("holo.usage-remove");
            return;
        }

        String raw = ctx.getArgs()[1];
        HologramType staticType = HologramType.fromId(raw).orElse(null);
        if (staticType != null) {
            HologramManager.setEnabled(staticType, false);
            HologramManager.remove(staticType);
            ctx.replyKey("holo.removed", Map.of("id", staticType.id()));
            return;
        }

        LeaderboardType leaderboardType = LeaderboardType.fromArg(raw);
        if (leaderboardType != null) {
            LeaderboardHologramManager.setEnabled(leaderboardType, false);
            LeaderboardHologramManager.remove(leaderboardType);
            ctx.replyKey("holo.removed", Map.of("id", leaderboardType.holoId()));
            return;
        }

        ctx.replyKey("holo.unknown", Map.of("id", raw));
    }

    private void reload(BukkitCommandContext ctx) {
        HologramManager.initialize();
        LeaderboardHologramManager.initialize();
        ctx.replyKey("holo.reloaded");
    }

    private void spawnById(BukkitCommandContext ctx, String id) {
        HologramType staticType = HologramType.fromId(id).orElse(null);
        if (staticType != null) {
            if (HologramManager.spawn(staticType)) {
                ctx.replyKey("holo.spawned", Map.of("id", staticType.id()));
            } else {
                ctx.replyKey("holo.spawn-failed", Map.of("id", staticType.id()));
            }
            return;
        }

        LeaderboardType leaderboardType = LeaderboardType.fromArg(id);
        if (leaderboardType != null) {
            if (LeaderboardHologramManager.spawn(leaderboardType)) {
                ctx.replyKey("holo.spawned", Map.of("id", leaderboardType.holoId()));
            } else {
                ctx.replyKey("holo.spawn-failed", Map.of("id", leaderboardType.holoId()));
            }
            return;
        }

        ctx.replyKey("holo.unknown", Map.of("id", id));
    }

    private void replySet(BukkitCommandContext ctx, String id, Location loc) {
        ctx.replyKey("holo.set", Map.of(
                "id", id,
                "world", loc.getWorld().getName(),
                "x", fmt(loc.getX()),
                "y", fmt(loc.getY()),
                "z", fmt(loc.getZ())));
    }

    @Completer(name = "holo", subCommands = {"hologram", "holograma"})
    public List<String> tab(BukkitCommandContext ctx) {
        List<String> subs = new ArrayList<>(Arrays.asList("list", "set", "remove", "reload"));
        for (HologramType type : HologramType.values()) {
            subs.add(type.id());
        }
        for (LeaderboardType type : LeaderboardType.values()) {
            subs.add(type.holoId());
        }

        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.filter(ctx.getArgs()[0], subs);
        }
        if (ctx.getArgs().length == 2) {
            String first = ctx.getArgs()[0].toLowerCase(Locale.ROOT);
            if ("set".equals(first) || "remove".equals(first) || "pos".equals(first)) {
                List<String> ids = new ArrayList<>();
                for (HologramType type : HologramType.values()) ids.add(type.id());
                for (LeaderboardType type : LeaderboardType.values()) ids.add(type.holoId());
                return TabCompleteUtil.filter(ctx.getArgs()[1], ids);
            }
        }
        return List.of();
    }

    private static String fmt(double value) {
        return String.format(Locale.US, "%.1f", value);
    }
}
