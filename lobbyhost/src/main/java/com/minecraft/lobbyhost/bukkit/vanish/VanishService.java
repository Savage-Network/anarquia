package com.minecraft.lobbyhost.bukkit.vanish;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.display.AboveNameDisplay;
import com.minecraft.lobbyhost.bukkit.display.JoinBroadcast;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Sistema completo de vanish staff (invisibilidade cross-viewer). */
public final class VanishService {

    private static final Set<UUID> VANISHED = ConcurrentHashMap.newKeySet();
    private static BukkitTask actionBarTask;

    private VanishService() {}

    public static void start(JavaPlugin plugin) {
        VanishConfig.load(com.minecraft.lobbyhost.LobbyHostPlugin.getInstance());
        restartActionBarTask(plugin);
    }

    public static void reload(JavaPlugin plugin) {
        VanishConfig.load(com.minecraft.lobbyhost.LobbyHostPlugin.getInstance());
        restartActionBarTask(plugin);
        for (UUID id : new ArrayList<>(VANISHED)) {
            Player p = Bukkit.getPlayer(id);
            if (p != null && p.isOnline()) applyEffects(p);
        }
    }

    public static void shutdown() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
        VANISHED.clear();
    }

    public static boolean isVanished(UUID id) {
        return id != null && VANISHED.contains(id);
    }

    public static boolean isVanished(Player player) {
        return player != null && VANISHED.contains(player.getUniqueId());
    }

    public static boolean canVanish(Account account) {
        if (account == null || !VanishConfig.isEnabled()) return false;
        Player p = account.player();
        return account.isStaffer()
                || p != null && p.hasPermission(VanishConfig.permUse());
    }

    public static boolean canSeeVanished(Player viewer) {
        if (viewer == null) return false;
        if (viewer.hasPermission(VanishConfig.permSee())) return true;
        if (!VanishConfig.isStaffSeeVanished()) return false;
        Account acc = Core.getAccountController().of(viewer.getUniqueId());
        return acc != null && acc.isStaffer();
    }

    public static boolean willAutoVanishOnJoin(Account account) {
        return account != null && canVanish(account) && account.getToggle().isAutoVanish();
    }

    public static void toggle(Player player) {
        setVanished(player, !isVanished(player));
    }

    public static void setVanished(Player player, boolean on) {
        if (player == null || !player.isOnline()) return;
        if (on == isVanished(player)) return;

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return;

        if (on) {
            VANISHED.add(player.getUniqueId());
            if (VanishConfig.isFakeQuitOnEnable()) {
                broadcastFakeQuit(player, account);
            }
        } else {
            VANISHED.remove(player.getUniqueId());
            if (VanishConfig.isFakeJoinOnDisable()) {
                broadcastFakeJoin(player, account);
            }
        }

        applyEffects(player);
        applyVisibility(player);
        refreshDisplays(player);

        player.sendMessage(ModernText.parse(on ? VanishConfig.msgEnabled() : VanishConfig.msgDisabled()));
    }

    public static void applyEffects(Player player) {
        if (player == null || !player.isOnline()) return;
        if (!isVanished(player)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            restoreFlight(player);
            return;
        }

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
        if (VanishConfig.isNightVision()) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
        }
        if (VanishConfig.isAutoFly()) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
    }

    private static void restoreFlight(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        boolean canFly = account != null && (account.isVIP()
                || player.hasPermission("lobbyhost.fly"));
        player.setAllowFlight(canFly);
        if (!canFly) player.setFlying(false);
    }

    public static void applyVisibility(Player target) {
        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null || target == null) return;

        boolean hidden = isVanished(target);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) continue;
            if (hidden && !canSeeVanished(viewer)) {
                viewer.hidePlayer(plugin, target);
            } else {
                viewer.showPlayer(plugin, target);
            }
        }
    }

    public static void applyForJoining(Player joiner) {
        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null || joiner == null) return;

        for (UUID id : VANISHED) {
            Player vanished = Bukkit.getPlayer(id);
            if (vanished == null || !vanished.isOnline() || vanished.equals(joiner)) continue;
            if (!canSeeVanished(joiner)) {
                joiner.hidePlayer(plugin, vanished);
            }
        }
    }

    public static void applyAutoOnJoin(Player player, Account account) {
        if (account == null || !canVanish(account)) return;
        if (account.getToggle().isAutoVanish()) {
            setVanished(player, true);
        }
    }

    public static void onQuit(Player player) {
        if (player == null) return;
        VANISHED.remove(player.getUniqueId());
    }

    public static List<Player> vanishedOnline() {
        List<Player> out = new ArrayList<>();
        for (UUID id : VANISHED) {
            Player p = Bukkit.getPlayer(id);
            if (p != null && p.isOnline()) out.add(p);
        }
        return out;
    }

    public static void refreshDisplays(Player player) {
        if (isVanished(player)) {
            AboveNameDisplay.remove(player);
        } else {
            AboveNameDisplay.refresh(player);
        }
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account != null) TagManager.updateTag(account);
    }

    public static void sendFakeJoin(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;
        broadcastFakeJoin(player, account);
    }

    public static void sendFakeQuit(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;
        broadcastFakeQuit(player, account);
    }

    private static void broadcastFakeQuit(Player player, Account account) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            online.sendMessage(JoinBroadcast.create(
                    account, player, "join.quit-lobby",
                    com.minecraft.lobbyhost.message.LanguageService.of(online)));
        }
    }

    private static void broadcastFakeJoin(Player player, Account account) {
        if (!account.shouldBroadcastJoinMessage()) return;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            online.sendMessage(JoinBroadcast.create(
                    account, player, "join.lobby",
                    com.minecraft.lobbyhost.message.LanguageService.of(online)));
        }
    }

    private static void restartActionBarTask(JavaPlugin plugin) {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
        if (!VanishConfig.isActionBar()) return;
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, VanishService::tickActionBar, 20L, 20L);
    }

    private static void tickActionBar() {
        if (VANISHED.isEmpty()) return;
        Component bar = ModernText.parse(VanishConfig.getActionBarText());
        for (UUID id : VANISHED) {
            Player p = Bukkit.getPlayer(id);
            if (p != null && p.isOnline()) {
                p.sendActionBar(bar);
            }
        }
    }
}
