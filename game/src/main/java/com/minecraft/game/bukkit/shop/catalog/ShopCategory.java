package com.minecraft.game.bukkit.shop.catalog;

import org.bukkit.Material;

import java.util.List;

public record ShopCategory(String id, String displayName, Material icon, int menuSlot, List<ShopProduct> products) {}
