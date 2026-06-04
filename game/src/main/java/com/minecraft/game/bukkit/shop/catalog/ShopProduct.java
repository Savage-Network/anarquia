package com.minecraft.game.bukkit.shop.catalog;

import org.bukkit.Material;

public record ShopProduct(Material material, int amount, int price) {}
