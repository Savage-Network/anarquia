package com.minecraft.game.bukkit.shop;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class SignShopStorageUtil {

    private SignShopStorageUtil() {}

    public static Inventory findChestInventory(Block signBlock) {
        Block storage = findStorageBlock(signBlock);
        if (storage == null) return null;
        if (storage.getState() instanceof InventoryHolder holder) {
            return holder.getInventory();
        }
        return null;
    }

    public static Block findStorageBlock(Block signBlock) {
        if (signBlock.getBlockData() instanceof WallSign wall) {
            Block attached = signBlock.getRelative(wall.getFacing().getOppositeFace());
            if (isStorage(attached)) return attached;
        }
        Block below = signBlock.getRelative(BlockFace.DOWN);
        if (isStorage(below)) return below;
        Block above = signBlock.getRelative(BlockFace.UP);
        if (isStorage(above)) return above;
        return null;
    }

    private static boolean isStorage(Block block) {
        if (block == null) return false;
        return block.getState() instanceof Chest
                || block.getState() instanceof org.bukkit.block.Barrel;
    }
}
