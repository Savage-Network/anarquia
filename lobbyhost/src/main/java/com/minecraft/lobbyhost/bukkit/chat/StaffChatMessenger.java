package com.minecraft.lobbyhost.bukkit.chat;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;

/** Encaminha staff chat ao proxy via plugin messaging (sem Redis). */
public final class StaffChatMessenger {

    private StaffChatMessenger() {}

    public static void sendToProxy(Player player, String payload) {
        if (player == null || payload == null) return;
        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return;
        try {
            byte[] data = payload.getBytes(StandardCharsets.UTF_8);
            player.sendPluginMessage(plugin, Constant.STAFFCHAT_PLUGIN_CHANNEL, data);
        } catch (Throwable t) {
            Core.getLogger().warning("[StaffChat] Falha ao enviar ao proxy: " + t.getMessage());
        }
    }
}
