package com.minecraft.game.bukkit.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Estado global do chat publico (lock, slow mode, clear).
 */
public final class ChatManager {

    public static final String SLOW_COOLDOWN_KEY = "chat-slow-mode";

    private static volatile boolean locked;
    private static volatile int slowModeSeconds;

    private ChatManager() {}

    public static boolean isLocked() {
        return locked;
    }

    public static void setLocked(boolean value) {
        locked = value;
    }

    public static int getSlowModeSeconds() {
        return slowModeSeconds;
    }

    public static long getSlowModeMs() {
        return slowModeSeconds <= 0 ? 0L : slowModeSeconds * 1000L;
    }

    public static void setSlowModeSeconds(int seconds) {
        slowModeSeconds = Math.max(0, seconds);
    }

    public static void clearChat(Player staff) {
        String name = staff != null ? staff.getName() : "CONSOLE";
        Component blank = Component.text(" ");
        for (Player online : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 100; i++) {
                online.sendMessage(blank);
            }
        }
        broadcastLine("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        broadcastLine("§7Chat limpo por §e" + name);
        broadcastLine("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    public static void broadcastLockNotice(boolean nowLocked, String staffName) {
        if (nowLocked) {
            broadcastLine("§c§lCHAT TRAVADO §7por §e" + staffName);
            broadcastLine("§7Apenas staff pode falar no momento.");
        } else {
            broadcastLine("§a§lCHAT LIBERADO §7por §e" + staffName);
        }
    }

    public static void broadcastSlowNotice(int seconds, String staffName) {
        if (seconds <= 0) {
            broadcastLine("§aSlow mode desativado por §e" + staffName + "§a.");
        } else {
            broadcastLine("§eSlow mode: §f" + seconds + "s §7entre mensagens §8(§e" + staffName + "§8)");
        }
    }

    public static String lockedMessage() {
        return "§cO chat está travado. Apenas staff pode falar.";
    }

    private static void broadcastLine(String legacy) {
        Bukkit.getServer().sendMessage(Component.text(legacy));
    }
}
