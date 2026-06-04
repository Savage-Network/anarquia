package com.minecraft.lobbyhost.bukkit.listener;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.lobby.HotbarKeys;
import com.minecraft.lobbyhost.bukkit.lobby.HotbarLayout;
import com.minecraft.lobbyhost.bukkit.lobby.PlayerVisibilityService;
import com.minecraft.lobbyhost.bukkit.menu.LobbyMenus;
import org.bukkit.Sound;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

/**
 * Clique direito nos itens da hotbar do lobby (PDC {@link HotbarKeys#SLOT_KEY}).
 */
public final class HotbarInteractListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        EquipmentSlot hand = event.getHand();
        if (hand == null || hand == EquipmentSlot.OFF_HAND) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            item = event.getPlayer().getInventory().getItemInMainHand();
        }
        String slot = HotbarKeys.readSlot(item);
        if (slot == null) return;

        if (action == Action.RIGHT_CLICK_BLOCK) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
        event.setUseItemInHand(Event.Result.ALLOW);
        event.setCancelled(true);

        var player = event.getPlayer();

        switch (slot) {
            case HotbarLayout.PDC_COMPASS -> LobbyMenus.openNavigation(player);
            case HotbarLayout.PDC_COSMETIC -> LobbyMenus.openCosmetics(player);
            case HotbarLayout.PDC_PROFILE -> LobbyMenus.openProfile(player);
            case HotbarLayout.PDC_MISSION -> LobbyMenus.openMissions(player);
            case HotbarLayout.PDC_VISIBILITY -> handleVisibility(player);
            default -> {}
        }
    }

    private void handleVisibility(org.bukkit.entity.Player player) {
        Account account = com.minecraft.lobbyhost.Core.getAccountController().of(player.getUniqueId());
        if (account != null && account.hasCooldown(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY)) {
            long sec = Math.max(1, account.getCooldownRemaining(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY) / 1000);
            account.send("§cAguarde §e" + sec + "s §cpara alterar a visibilidade novamente.");
            return;
        }
        var mode = PlayerVisibilityService.cycle(player);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
        Account acc = com.minecraft.lobbyhost.Core.getAccountController().of(player.getUniqueId());
        int friends = acc != null
                ? com.minecraft.lobbyhost.social.FriendService.countFriendsOnlineInLobby(acc) : 0;
        String extra = friends > 0
                ? " <#64748B>(<" + com.minecraft.lobbyhost.util.ModernText.SUCCESS + ">"
                + friends + " amigo(s)<#64748B>)"
                : "";
        player.sendMessage(com.minecraft.lobbyhost.util.ModernText.parse(
                com.minecraft.lobbyhost.util.ModernText.mini("Visibilidade: ")
                        + mode.getDisplayNameMini() + extra));
        if (account != null) {
            long seconds = com.minecraft.lobbyhost.LobbySettings.VISIBILITY_COOLDOWN_SECONDS;
            account.setCooldown(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY,
                    TimeUnit.SECONDS.toMillis(Math.max(1, seconds)));
        }
    }
}
