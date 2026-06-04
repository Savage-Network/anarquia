package com.minecraft.lobbyhost.bukkit.listener;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.Cape;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.Gadget;
import com.minecraft.lobbyhost.bukkit.lobby.HotbarKeys;
import com.minecraft.lobbyhost.bukkit.manager.CosmeticManager;
import com.minecraft.lobbyhost.bukkit.skin.SkinService;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public final class CosmeticListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Pequeno delay para garantir que a conta foi carregada
        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;

            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account == null) return;

            // Ativa capa
            Cape cape = account.getActiveCape();
            if (cape != null) {
                CosmeticManager.getCapeRenderer().spawn(player, cape);
            }

            SkinService.apply(account, player);

            if (account.getActiveGadget() != null) {
                com.minecraft.lobbyhost.bukkit.lobby.HotbarLayout.applyDefault(player);
            }
        }, 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        CosmeticManager.getCapeRenderer().remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // Re-spawna a capa após o teleport
        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;

            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account == null) return;

            Cape cape = account.getActiveCape();
            if (cape != null) {
                CosmeticManager.getCapeRenderer().spawn(player, cape);
            }
            SkinService.apply(account, player);
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // Re-spawna a capa após mudar de mundo
        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;

            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account == null) return;

            Cape cape = account.getActiveCape();
            if (cape != null) {
                CosmeticManager.getCapeRenderer().spawn(player, cape);
            }
            SkinService.apply(account, player);
        }, 5L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            item = player.getInventory().getItemInMainHand();
        }
        if (HotbarKeys.readSlot(item) != null) return;
        if (item == null) return;

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        Gadget gadget = account.getActiveGadget();
        if (gadget == null) return;

        // Verifica se é o gadget no slot 2 (3º slot)
        if (player.getInventory().getHeldItemSlot() == 2 && item.getType() == gadget.getMaterial()) {
            event.setCancelled(true);

            String cdKey = "gadget-" + gadget.getId();
            if (account.hasCooldown(cdKey)) {
                long left = account.getCooldownRemaining(cdKey);
                player.sendMessage("§cAguarde §e" + String.format("%.1f", left / 1000.0) + "s §cpara usar o gadget novamente.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            // Executa e define recarga
            CosmeticManager.getGadgetHandler().use(player, gadget);
            account.setCooldown(cdKey, gadget.getCooldownMs());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Impede mover o gadget do slot 2 da hotbar
        if (event.getSlot() == 2 && event.getClickedInventory() == player.getInventory()) {
            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account != null && account.getActiveGadget() != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().hasPermission("lobbyhost.staff")) return;
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (HotbarKeys.readSlot(dropped) != null) {
            event.setCancelled(true);
            return;
        }
        Account account = Core.getAccountController().of(event.getPlayer().getUniqueId());
        if (account != null && account.getActiveGadget() != null
                && event.getPlayer().getInventory().getHeldItemSlot() == 2) {
            event.setCancelled(true);
        }
    }
}
