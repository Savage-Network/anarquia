package com.minecraft.game.bukkit.chat;

import com.minecraft.game.Core;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/** Entrega /tell cross-server nos backends Paper. */
public final class TellBridge {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private TellBridge() {}

    public static void deliverFromRedis(String raw) {
        Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> deliver(raw));
    }

    private static void deliver(String raw) {
        String[] parts = raw.split("\\|", 5);
        if (parts.length < 5) return;
        try {
            UUID senderId = UUID.fromString(parts[0].trim());
            UUID targetId = UUID.fromString(parts[1].trim());
            String fromColored = parts[2];
            String toColored = parts[3];
            String body = parts[4];

            Player target = Bukkit.getPlayer(targetId);
            if (target != null && target.isOnline()) {
                target.sendMessage(formatIncoming(fromColored, body));
            }

            Player sender = Bukkit.getPlayer(senderId);
            if (sender != null && sender.isOnline()) {
                sender.sendMessage(formatOutgoing(toColored, body));
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static Component formatIncoming(String coloredFrom, String body) {
        return LEGACY.deserialize("§8(§fde §7" + coloredFrom + "§8) §7" + body.replace('&', '§'));
    }

    private static Component formatOutgoing(String coloredTo, String body) {
        return LEGACY.deserialize("§8(§fpara §7" + coloredTo + "§8) §7" + body.replace('&', '§'));
    }
}
