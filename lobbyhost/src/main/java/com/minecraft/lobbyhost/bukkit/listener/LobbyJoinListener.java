package com.minecraft.lobbyhost.bukkit.listener;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.menu.MenuManager;
import com.minecraft.lobbyhost.bukkit.display.AboveNameDisplay;
import com.minecraft.lobbyhost.bukkit.display.PlayerDisplayService;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyPlayerSetup;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyTabList;
import com.minecraft.lobbyhost.bukkit.lobby.PlayerVisibilityService;
import com.minecraft.lobbyhost.bukkit.vanish.VanishService;
import com.minecraft.lobbyhost.util.ModernText;
import com.minecraft.lobbyhost.social.FriendService;
import com.minecraft.lobbyhost.bukkit.manager.PermissionManager;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.bukkit.skin.SkinService;
import com.minecraft.lobbyhost.bukkit.menu.LobbyMenuHolder;
import net.kyori.adventure.text.Component;
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
                    Component.text(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Account joinAcc = Core.getAccountController().of(player.getUniqueId());
        if (VanishService.isVanished(player)
                || (joinAcc != null && !joinAcc.getToggle().isAllowJoinMessage())) {
            event.joinMessage(null);
        } else {
            event.joinMessage(ModernText.parse(ModernText.joinMessage(player.getName())));
        }

        Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () ->
                LobbyHostPlugin.getInstance().getArchitectLoader()
                        .getArchitect().onJoin(player));

        // Tag/medal/title (Fase 2) - delay para garantir Account em cache
        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;
            PlayerVisibilityService.onPlayerJoin(player);
            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account == null) return;
            PlayerDisplayService.applyAll(account, player);
            SkinService.refreshFromMojangIfStale(account, player);
            LobbyTabList.refreshAll();
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) continue;
                Account otherAcc = Core.getAccountController().of(other.getUniqueId());
                if (otherAcc != null) TagManager.applyTagForViewer(player, other);
            }
            int friendsHere = FriendService.countFriendsOnlineInLobby(account);
            if (friendsHere > 0) {
                player.sendMessage(Component.text(
                        "§e" + friendsHere + " amigo(s) §7online no lobby. §8/amigo"));
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.quitMessage(ModernText.parse(ModernText.quitMessage(player.getName())));

        try {
            LobbyHostPlugin.getInstance().getArchitectLoader()
                    .getArchitect().onQuit(player);
        } catch (Throwable ignored) { }

        TagManager.removeTag(player);
        AboveNameDisplay.remove(player);
        PlayerVisibilityService.onPlayerQuit(player);
        com.minecraft.lobbyhost.social.PartyService.onQuit(player);
        PermissionManager.unloadPermissions(player);

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
