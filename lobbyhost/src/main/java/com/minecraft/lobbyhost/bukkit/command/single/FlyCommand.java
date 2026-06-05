package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FlyCommand implements CommandInheritor {

    /** UUIDs com fly ativado manualmente via /fly. */
    private static final Set<UUID> FLY_ENABLED = ConcurrentHashMap.newKeySet();

    public static boolean hasFlyEnabled(UUID id) {
        return FLY_ENABLED.contains(id);
    }

    public static void removeFly(UUID id) {
        FLY_ENABLED.remove(id);
    }

    @Command(name = "fly", aliases = {"voar"}, playerOnly = true,
            permission = "lobbyhost.fly",
            description = "Alterna modo voo")
    public void execute(BukkitCommandContext ctx) {
        Player player = ctx.player();
        Account account = ctx.account().orElse(null);

        boolean canFly = player.hasPermission("lobbyhost.fly")
                || (account != null && account.isVIP());

        if (!canFly) {
            ctx.reply("§cVocê precisa ser VIP para voar.");
            return;
        }

        // Togla baseado no estado rastreado, não em getAllowFlight()
        boolean enable = !FLY_ENABLED.contains(player.getUniqueId());

        if (enable) {
            FLY_ENABLED.add(player.getUniqueId());
            player.setAllowFlight(true);
            player.setFlying(true);
            ctx.reply("§aFly ativado.");
        } else {
            FLY_ENABLED.remove(player.getUniqueId());
            player.setAllowFlight(false);
            player.setFlying(false);
            ctx.reply("§eFly desativado.");
        }
    }

    @Completer(name = "fly", subCommands = {"voar"})
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return List.of("on", "off");
        }
        return List.of();
    }
}
