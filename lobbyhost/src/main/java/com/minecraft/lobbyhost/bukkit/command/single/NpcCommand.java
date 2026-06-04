package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.architect.ArchitectLoader;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.display.npc.NpcDefinition;
import com.minecraft.lobbyhost.display.npc.NpcManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NpcCommand implements CommandInheritor {

    @Command(name = "npc", permission = "lobbyhost.admin",
            description = "Gerencia NPCs de lobby (reload, list, spawn, remove)")
    public void execute(BukkitCommandContext ctx) {
        String[] args = ctx.getArgs();
        if (args.length == 0) {
            usage(ctx);
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> reload(ctx);
            case "list" -> list(ctx);
            case "spawn" -> spawn(ctx, args);
            case "remove", "rem" -> remove(ctx, args);
            default -> usage(ctx);
        }
    }

    private void usage(BukkitCommandContext ctx) {
        ctx.reply("§6/npc §7reload|list|spawn <id>|remove <id>");
    }

    private void reload(BukkitCommandContext ctx) {
        NpcManager.load(Core.getJavaPlugin());
        ArchitectLoader loader = Core.getJavaPlugin() instanceof com.minecraft.lobbyhost.LobbyHostPlugin lp
                ? lp.getArchitectLoader() : null;
        if (loader != null && loader.getArchitect() != null) {
            NpcManager.spawnForArchitect(loader.getArchitect());
        }
        ctx.reply("§aNPCs recarregados.");
    }

    private void list(BukkitCommandContext ctx) {
        ctx.reply("§6NPCs definidos:");
        for (NpcDefinition d : NpcManager.listDefinitions()) {
            boolean active = NpcManager.get(d.getId()) != null;
            ctx.reply("§7- §f" + d.getId() + " §7(" + d.getLocationKey() + ") "
                    + (active ? "§a[ativo]" : "§c[inativo]"));
        }
    }

    private void spawn(BukkitCommandContext ctx, String[] args) {
        Player player = ctx.player();
        if (player == null) {
            ctx.reply("§cApenas jogadores.");
            return;
        }
        if (args.length < 2) {
            ctx.reply("§cUso: /npc spawn <id>");
            return;
        }
        if (!NpcManager.spawnAtPlayer(player, args[1])) {
            ctx.reply("§cNPC '" + args[1] + "' nao encontrado em npcs.yml.");
            return;
        }
        ctx.reply("§aNPC §f" + args[1] + " §aspawnado na sua posicao.");
    }

    private void remove(BukkitCommandContext ctx, String[] args) {
        if (args.length < 2) {
            ctx.reply("§cUso: /npc remove <id>");
            return;
        }
        NpcManager.remove(args[1]);
        ctx.reply("§aNPC §f" + args[1] + " §aremovido.");
    }

    @Completer(name = "npc")
    public List<String> complete(BukkitCommandContext ctx) {
        String[] args = ctx.getArgs();
        if (args.length == 1) {
            return List.of("reload", "list", "spawn", "remove");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("remove"))) {
            List<String> ids = new ArrayList<>();
            for (NpcDefinition d : NpcManager.listDefinitions()) ids.add(d.getId());
            return ids;
        }
        return List.of();
    }
}
