package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.world.WorldManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MundoCommand implements CommandInheritor {

    private static final List<String> SUBCOMMANDS = List.of(
            "list", "lista", "load", "carregar",
            "unload", "descarregar", "tp", "teleport", "info");

    @Command(name = "mundo", aliases = {"world", "mv"}, playerOnly = false, permission = "game.admin",
            usage = "/mundo list|load|unload|tp|info <mundo>",
            description = "Gerencia mundos (Multiverse-style)")
    public void execute(BukkitCommandContext ctx) {
        String[] args = ctx.getArgs();

        if (args.length == 0) {
            ctx.reply(WorldManager.formatList());
            sendHelp(ctx);
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help", "ajuda" -> sendHelp(ctx);
            case "list", "lista" -> ctx.reply(WorldManager.formatList());
            case "load", "carregar" -> load(ctx, args);
            case "unload", "descarregar" -> unload(ctx, args);
            case "tp", "teleport", "ir" -> tp(ctx, args);
            case "info", "i" -> info(ctx, args);
            default -> legacyTp(ctx, args[0]);
        }
    }

    private void load(BukkitCommandContext ctx, String[] args) {
        if (args.length < 2) {
            ctx.reply("§cUso: /mundo load <mundo>");
            return;
        }
        WorldManager.Result result = WorldManager.load(args[1]);
        ctx.reply(result.success() ? "§a" + result.message() : "§c" + result.message());
    }

    private void unload(BukkitCommandContext ctx, String[] args) {
        if (args.length < 2) {
            ctx.reply("§cUso: /mundo unload <mundo>");
            return;
        }
        boolean save = args.length < 3 || !"false".equalsIgnoreCase(args[2]);
        WorldManager.Result result = WorldManager.unload(args[1], save);
        ctx.reply(result.success() ? "§e" + result.message() : "§c" + result.message());
    }

    private void tp(BukkitCommandContext ctx, String[] args) {
        if (args.length < 2) {
            ctx.reply("§cUso: /mundo tp <mundo>");
            return;
        }
        Player player = ctx.player();
        if (player == null) {
            ctx.reply("§cSomente jogadores podem se teleportar.");
            return;
        }
        teleportPlayer(ctx, player, args[1]);
    }

    private void info(BukkitCommandContext ctx, String[] args) {
        if (args.length < 2) {
            ctx.reply("§cUso: /mundo info <mundo>");
            return;
        }
        ctx.reply(WorldManager.formatInfo(args[1]));
    }

    /** Compat: /mundo <nome> teleporta direto. */
    private void legacyTp(BukkitCommandContext ctx, String worldName) {
        Player player = ctx.player();
        if (player == null) {
            ctx.reply("§cSomente jogadores. Use §e/mundo load " + worldName);
            return;
        }
        teleportPlayer(ctx, player, worldName);
    }

    private void teleportPlayer(BukkitCommandContext ctx, Player player, String worldName) {
        WorldManager.Result result = WorldManager.teleport(player, worldName);
        if (!result.success()) {
            ctx.reply("§c" + result.message());
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0),
                25, 0.3, 0.4, 0.3, 0.05);
        ctx.reply("§a" + result.message());
    }

    private void sendHelp(BukkitCommandContext ctx) {
        ctx.reply("§6/mundo list §7- mundos no disco e carregados");
        ctx.reply("§6/mundo load <m> §7- carrega mundo");
        ctx.reply("§6/mundo unload <m> §7- descarrega (salva)");
        ctx.reply("§6/mundo tp <m> §7- teleporta ao spawn");
        ctx.reply("§6/mundo info <m> §7- detalhes do mundo");
        ctx.reply("§6/mundo <m> §7- atalho de tp");
    }

    @Completer(name = "mundo", subCommands = {"world", "mv"})
    public List<String> tab(BukkitCommandContext ctx) {
        String[] args = ctx.getArgs();
        if (args.length <= 1) {
            return filter(SUBCOMMANDS, args.length == 1 ? args[0] : "");
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("load") || sub.equals("carregar")
                    || sub.equals("unload") || sub.equals("descarregar")
                    || sub.equals("tp") || sub.equals("teleport") || sub.equals("ir")
                    || sub.equals("info") || sub.equals("i")) {
                return WorldManager.suggestWorlds(args[1]);
            }
        }
        if (args.length == 3 && ("unload".equalsIgnoreCase(args[0]) || "descarregar".equalsIgnoreCase(args[0]))) {
            return filter(List.of("true", "false"), args[2]);
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String partial) {
        String lower = partial.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase(Locale.ROOT).startsWith(lower)) out.add(o);
        }
        return out;
    }
}
