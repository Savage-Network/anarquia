package com.minecraft.game.anticheat.checks;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.anticheat.AnticheatProfile;
import com.minecraft.game.anticheat.AnticheatProfiles;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

/** Checks de jogador: auto totem, auto armor, etc. */
public final class PlayerCheckListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (AnticheatEngine.isExempt(player)) return;

        ItemStack main = event.getMainHandItem();
        ItemStack off = event.getOffHandItem();
        boolean totemSwap = isTotem(main) || isTotem(off);
        if (!totemSwap) return;
        if (!AnticheatConfig.isCheckEnabled(AnticheatCheck.AUTO_TOTEM)) return;

        AnticheatProfile profile = AnticheatProfiles.of(player);
        long now = System.currentTimeMillis();
        long min = AnticheatConfig.numInt("auto-totem.min-ms", 150);
        if (profile.lastTotemSwapMs > 0 && now - profile.lastTotemSwapMs < min) {
            AnticheatEngine.flag(player, AnticheatCheck.AUTO_TOTEM,
                    (now - profile.lastTotemSwapMs) + "ms");
        }
        profile.lastTotemSwapMs = now;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (AnticheatEngine.isExempt(player)) return;
        if (!AnticheatConfig.isCheckEnabled(AnticheatCheck.AUTO_ARMOR)) return;

        if (event.getSlotType() != InventoryType.SlotType.ARMOR
                && event.getSlotType() != InventoryType.SlotType.QUICKBAR) {
            return;
        }

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (!isArmor(current) && !isArmor(cursor)) return;

        AnticheatProfile profile = AnticheatProfiles.of(player);
        long now = System.currentTimeMillis();
        long min = AnticheatConfig.numInt("auto-armor.min-ms", 120);
        if (profile.lastArmorSwapMs > 0 && now - profile.lastArmorSwapMs < min) {
            AnticheatEngine.flag(player, AnticheatCheck.AUTO_ARMOR,
                    (now - profile.lastArmorSwapMs) + "ms");
        }
        profile.lastArmorSwapMs = now;
    }

    private static boolean isTotem(ItemStack item) {
        return item != null && item.getType() == Material.TOTEM_OF_UNDYING;
    }

    private static boolean isArmor(ItemStack item) {
        if (item == null) return false;
        String name = item.getType().name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }
}
