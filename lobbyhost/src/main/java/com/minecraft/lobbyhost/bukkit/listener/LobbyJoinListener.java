package com.minecraft.lobbyhost.bukkit.listener;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.display.AboveNameDisplay;
import com.minecraft.lobbyhost.bukkit.display.JoinBroadcast;
import com.minecraft.lobbyhost.bukkit.display.PlayerDisplayService;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyTabList;
import com.minecraft.lobbyhost.bukkit.lobby.PlayerVisibilityService;
import com.minecraft.lobbyhost.bukkit.manager.PermissionManager;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.bukkit.menu.LobbyMenuHolder;
import com.minecraft.lobbyhost.bukkit.menu.MenuManager;
import com.minecraft.lobbyhost.bukkit.skin.SkinService;
import com.minecraft.lobbyhost.bukkit.vanish.VanishService;
import com.minecraft.lobbyhost.message.LanguageService;
import com.minecraft.lobbyhost.message.Messages;
import com.minecraft.lobbyhost.social.FriendPresence;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * Listener principal do lobby. Carrega a conta no pre-login, aplica
 * hotbar/sidebar no join e bloqueia interacoes destrutivas (build, break,
 * drop, dano, fome) - igual {@code Architect} do Savage.
 */
public final class LobbyJoinListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        try {
            Account account = Core.getAccountController()
                    .load(event.getUniqueId(), event.getName())
                    .get();
            account.setIpAddress(event.getAddress().getHostAddress());
            account.updateLastLogin();
        } catch (Throwable t) {
            Core.getLogger().severe("Falha ao carregar conta de " + event.getName()
                    + ": " + t.getMessage());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Messages.component("account.load-failed"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account != null && !VanishService.willAutoVanishOnJoin(account)
                && !VanishService.isVanished(player)
                && account.shouldBroadcastJoinMessage()) {
            event.joinMessage(null);
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                viewer.sendMessage(JoinBroadcast.create(
                        account, player, "join.lobby", LanguageService.of(viewer)));
            }
        } else {
            event.joinMessage(null);
        }

        Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () ->
                LobbyHostPlugin.getInstance().getArchitectLoader()
                        .getArchitect().onJoin(player));

        // Tag/medal/title (Fase 2) - delay para garantir Account em cache
        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;
            PlayerVisibilityService.onPlayerJoin(player);
            Account loaded = Core.getAccountController().of(player.getUniqueId());
            if (loaded == null) return;
            PlayerDisplayService.applyAll(loaded, player);
            SkinService.refreshFromMojangIfStale(loaded, player);
            LobbyTabList.refreshAll();
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) continue;
                Account otherAcc = Core.getAccountController().of(other.getUniqueId());
                if (otherAcc != null) TagManager.applyTagForViewer(player, other);
            }
            Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
                int count = FriendPresence.countFriendsOnlineOnNetwork(loaded);
                if (count <= 0) return;
                Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                    if (!player.isOnline()) return;
                    player.sendMessage(Messages.component("lobby.friends-online", player,
                            java.util.Map.of("count", String.valueOf(count))));
                });
            });
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.quitMessage(null);

        try {
            LobbyHostPlugin.getInstance().getArchitectLoader()
                    .getArchitect().onQuit(player);
        } catch (Throwable ignored) { }

        TagManager.removeTag(player);
        AboveNameDisplay.remove(player);
        PlayerVisibilityService.onPlayerQuit(player);
        PermissionManager.unloadPermissions(player);
        com.minecraft.lobbyhost.network.QueueManager.leave(player, false);
        com.minecraft.lobbyhost.bukkit.command.single.FlyCommand.removeFly(player.getUniqueId());
        
        // Cleanup do sistema de double jump
        com.minecraft.lobbyhost.bukkit.listener.DoubleJumpListener.cleanup(player);

        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(),
                () -> Core.getAccountController().unload(player.getUniqueId()));
    }

    /* ---------- bloqueios do lobby ---------- */

    @EventHandler public void onBreak(BlockBreakEvent e) {
        if (!e.getPlayer().hasPermission("lobbyhost.staff")) e.setCancelled(true);
    }

    @EventHandler public void onPlace(BlockPlaceEvent e) {
        if (!e.getPlayer().hasPermission("lobbyhost.staff")) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().hasPermission("lobbyhost.staff")) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent e) {
        if (!e.getPlayer().hasPermission("lobbyhost.staff")) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p && !p.hasPermission("lobbyhost.staff")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (p.hasPermission("lobbyhost.staff")) return;
        if (e.getView().getTopInventory().getHolder() instanceof LobbyMenuHolder) return;
        if (MenuManager.get(p) != null) return;
        ClickType click = e.getClick();
        if (click == ClickType.DROP || click == ClickType.CONTROL_DROP
                || click == ClickType.NUMBER_KEY || click == ClickType.SWAP_OFFHAND) {
            e.setCancelled(true);
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) e.setCancelled(true);
    }

    @EventHandler public void onFood(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) e.setCancelled(true);
    }
}
