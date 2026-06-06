package com.minecraft.auth.bukkit.listener;

import com.minecraft.auth.bukkit.command.vanilla.AllowedCommandIndex;
import com.minecraft.auth.bukkit.command.vanilla.VanillaCommandCatalog;
import com.minecraft.auth.bukkit.command.vanilla.VanillaCommandResponder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class VanillaCommandListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(VanillaCommandResponder.BYPASS_PERMISSION)) return;

        String root = parseRoot(event.getMessage());
        if (root.isBlank()) return;
        if (AllowedCommandIndex.isAllowed(root)) return;

        event.setCancelled(true);

        if (AllowedCommandIndex.isVanillaCommand(root)) {
            VanillaCommandResponder.respond(player, root, VanillaCommandCatalog.category(root));
        } else {
            VanillaCommandResponder.unknown(player, event.getMessage());
        }
    }

    private static String parseRoot(String message) {
        if (message == null || message.isBlank()) return "";
        String line = message.trim();
        if (line.startsWith("/")) line = line.substring(1);
        if (line.isEmpty()) return "";
        return VanillaCommandCatalog.normalizeRoot(line.split("\\s+")[0]);
    }
}
