package com.minecraft.game.bukkit.listener;

import com.minecraft.game.bukkit.command.vanilla.AllowedCommandIndex;
import com.minecraft.game.bukkit.command.vanilla.VanillaCommandCatalog;
import com.minecraft.game.bukkit.command.vanilla.VanillaCommandResponder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Intercepta comandos vanilla do Minecraft e responde com mensagens SavageMC.
 * Comandos do servidor e de plugins parceiros (LuckPerms, etc.) continuam funcionando.
 */
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
            VanillaCommandCatalog.Category category = VanillaCommandCatalog.category(root);
            VanillaCommandResponder.respond(player, root, category);
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
