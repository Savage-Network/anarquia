package com.minecraft.lobbyhost.bukkit.vanish;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.bukkit.display.AboveNameDisplay;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VanishService {

    private static final Set<UUID> VANISHED = ConcurrentHashMap.newKeySet();

    private VanishService() {}

    public static boolean isVanished(UUID id) {
        return VANISHED.contains(id);
    }

    public static boolean isVanished(Player player) {
        return player != null && VANISHED.contains(player.getUniqueId());
    }

    public static boolean canVanish(Account account) {
        return account != null && (account.isStaffer()
                || account.player() != null && account.player().hasPermission("lobbyhost.vanish"));
    }

    public static void setVanished(Player player, boolean on) {
        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null || player == null) return;

        if (on) {
            VANISHED.add(player.getUniqueId());
            player.setGameMode(GameMode.CREATIVE);
            player.setAllowFlight(true);
            player.setFlying(true);
        } else {
            VANISHED.remove(player.getUniqueId());
            player.setFlying(false);
            player.setAllowFlight(false);
            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account != null && account.getRankType() == RankType.MEMBER) {
                player.setAllowFlight(false);
            }
            player.setGameMode(GameMode.ADVENTURE);
        }
        applyToAll(player);
        AboveNameDisplay.refresh(player);
        player.sendMessage(ModernText.parse(on
                ? "<!italic><" + ModernText.SUCCESS + ">Modo vanish <bold>ativado</bold>"
                : "<!italic><" + ModernText.MUTED + ">Modo vanish <" + ModernText.ERROR + ">desativado"));
    }

    public static void toggle(Player player) {
        setVanished(player, !isVanished(player));
    }

    /** Reaplica hide/show para todos os viewers. */
    public static void applyToAll(Player target) {
        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return;
        boolean hidden = isVanished(target);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) continue;
            if (hidden) {
                viewer.hidePlayer(plugin, target);
            } else {
                viewer.showPlayer(plugin, target);
            }
        }
    }

    /** Novo jogador nao ve staff em vanish. */
    public static void applyForJoining(Player joiner) {
        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return;
        for (UUID id : VANISHED) {
            Player vanished = Bukkit.getPlayer(id);
            if (vanished != null && vanished.isOnline() && !joiner.equals(vanished)) {
                joiner.hidePlayer(plugin, vanished);
            }
        }
    }

    public static void onQuit(Player player) {
        VANISHED.remove(player.getUniqueId());
    }

    public static void applyAutoOnJoin(Player player, Account account) {
        if (account == null || !canVanish(account)) return;
        if (account.getToggle().isAutoVanish()) {
            setVanished(player, true);
        }
    }
}
