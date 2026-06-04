package com.minecraft.auth.util;

import com.minecraft.auth.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public final class AuthRedirects {

    private AuthRedirects() {}

    public static void toHub(Player player) {
        int delay = Core.getJavaPlugin().getConfig().getInt("post-login.delay-ticks", 10);
        String target = Core.getJavaPlugin().getConfig().getString("post-login.target-server", "lobby");

        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;
            try {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                out.writeUTF("Connect");
                out.writeUTF(target);
                player.sendPluginMessage(Core.getJavaPlugin(), "BungeeCord", b.toByteArray());
            } catch (Throwable ignored) {
                player.teleport(player.getWorld().getSpawnLocation());
            }
        }, delay);
    }
}
