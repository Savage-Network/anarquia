package com.minecraft.auth.util;

import com.minecraft.auth.Core;
import com.minecraft.auth.config.AuthConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public final class AuthRedirects {

    private AuthRedirects() {}

    public static void toHub(Player player) {
        AuthConfig cfg = AuthConfig.get();
        int delay = cfg.getPostLoginDelayTicks();
        String target = cfg.getPostLoginTarget();

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
