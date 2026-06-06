package com.minecraft.game.bukkit.semi;

import com.minecraft.game.Core;
import com.minecraft.game.bukkit.semi.CombatTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

/** Marca o local do combat log com cabeca + itens visiveis por tempo limitado. */
public final class CombatLogNpcService {

    private CombatLogNpcService() {}

    private static NamespacedKey markerKey() {
        return new NamespacedKey(Core.getJavaPlugin(), "combat_log_marker");
    }

    public static void spawnMarker(Player player, CombatTag tag) {
        if (player == null || Core.getJavaPlugin() == null) return;
        Location loc = player.getLocation().clone();
        if (loc.getWorld() == null) return;

        List<ItemStack> preview = collectItems(player);
        String label = "§cCombat Log §7" + player.getName()
                + (tag != null && tag.opponentName() != null ? " §8vs §f" + tag.opponentName() : "");

        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.customName(net.kyori.adventure.text.Component.text(label));
        stand.getPersistentDataContainer().set(markerKey(), PersistentDataType.BYTE, (byte) 1);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            head.setItemMeta(meta);
        }
        stand.getEquipment().setHelmet(head);

        int slot = 0;
        for (ItemStack item : preview) {
            if (item == null || item.getType().isAir()) continue;
            loc.getWorld().dropItemNaturally(loc.clone().add(0, 0.2 + slot * 0.05, 0), item.clone());
            slot++;
            if (slot >= 8) break;
        }

        Core.getJavaPlugin().getServer().getScheduler().runTaskLater(Core.getJavaPlugin(),
                stand::remove, 20L * 120L);
    }

    private static List<ItemStack> collectItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) items.add(item);
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && !item.getType().isAir()) items.add(item);
        }
        ItemStack off = player.getInventory().getItemInOffHand();
        if (off != null && !off.getType().isAir()) items.add(off);
        return items;
    }
}
