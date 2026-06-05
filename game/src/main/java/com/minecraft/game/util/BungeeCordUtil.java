package com.minecraft.game.util;

import com.minecraft.game.Core;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * Mensagens BungeeCord/Velocity via plugin messaging channel.
 */
public final class BungeeCordUtil {

    private BungeeCordUtil() {}

    public static void connect(Player player, JavaPlugin plugin, String server) {
        if (player == null || plugin == null || server == null || server.isBlank()) return;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(plugin, "BungeeCord", bytes.toByteArray());
        } catch (Throwable t) {
            Core.getLogger().warning("[BungeeCord] Connect -> " + server + " falhou: " + t.getMessage());
        }
    }
}
