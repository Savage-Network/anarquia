package com.minecraft.game.bukkit.storage;

import com.minecraft.game.Core;
import com.minecraft.game.backend.data.PlayerVaultData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerVaultService {

    public enum VaultType {
        TRASH,
        BAU,
        EC
    }

    private static final Map<UUID, VaultType> OPEN = new ConcurrentHashMap<>();

    private PlayerVaultService() {}

    public static void openTrash(Player player) {
        Inventory inv = Bukkit.createInventory(null, PlayerVaultData.BAU_SIZE, "§8§lLixeira");
        track(player, VaultType.TRASH);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 0.8f);
    }

    public static void openBau(Player player) {
        PlayerVaultData data = Core.getPlayerVaultData();
        ItemStack[] contents = data != null ? data.loadBau(player.getUniqueId()) : new ItemStack[PlayerVaultData.BAU_SIZE];
        Inventory inv = Bukkit.createInventory(null, PlayerVaultData.BAU_SIZE, "§8§lBaú Virtual");
        inv.setContents(contents);
        track(player, VaultType.BAU);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.6f, 1.1f);
    }

    public static void openEc(Player player) {
        PlayerVaultData data = Core.getPlayerVaultData();
        ItemStack[] contents = data != null ? data.loadEc(player.getUniqueId()) : new ItemStack[PlayerVaultData.EC_SIZE];
        Inventory inv = Bukkit.createInventory(null, PlayerVaultData.EC_SIZE, "§5§lEnder Chest");
        inv.setContents(contents);
        track(player, VaultType.EC);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.6f, 1.1f);
    }

    public static VaultType type(Player player) {
        return OPEN.get(player.getUniqueId());
    }

    public static void handleClose(Player player, Inventory inventory) {
        VaultType type = OPEN.remove(player.getUniqueId());
        if (type == null) return;

        switch (type) {
            case TRASH -> {
                inventory.clear();
                player.sendMessage("§7Itens descartados.");
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.6f, 0.9f);
            }
            case BAU -> {
                PlayerVaultData data = Core.getPlayerVaultData();
                if (data != null) {
                    data.saveBauAsync(player.getUniqueId(), inventory.getContents());
                }
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.0f);
            }
            case EC -> {
                PlayerVaultData data = Core.getPlayerVaultData();
                if (data != null) {
                    data.saveEcAsync(player.getUniqueId(), inventory.getContents());
                }
                player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 0.5f, 1.0f);
            }
        }
    }

    public static void flushOpen() {
        for (UUID id : List.copyOf(OPEN.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player == null || !player.isOnline()) {
                OPEN.remove(id);
                continue;
            }
            Inventory top = player.getOpenInventory().getTopInventory();
            handleClose(player, top);
            player.closeInventory();
        }
    }

    private static void track(Player player, VaultType type) {
        OPEN.put(player.getUniqueId(), type);
    }
}
