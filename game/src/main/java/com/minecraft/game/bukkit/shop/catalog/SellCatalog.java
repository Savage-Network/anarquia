package com.minecraft.game.bukkit.shop.catalog;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SellCatalog {

    private static final Map<Material, Integer> PRICES = new HashMap<>();

    private SellCatalog() {}

    public static void load(GamePlugin plugin) {
        PRICES.clear();
        File file = new File(plugin.getDataFolder(), "sell-prices.yml");
        if (!file.exists()) plugin.saveResource("sell-prices.yml", false);
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        var sec = cfg.getConfigurationSection("prices");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            Material mat = Material.matchMaterial(key.toUpperCase(Locale.ROOT));
            if (mat != null) PRICES.put(mat, sec.getInt(key, 0));
        }
        Core.getLogger().info("Precos de venda: " + PRICES.size() + " itens.");
    }

    public static int price(Material mat) {
        return PRICES.getOrDefault(mat, 0);
    }

    public static Map<Material, Integer> all() {
        return Collections.unmodifiableMap(PRICES);
    }

    public static int sellStack(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return 0;
        int unit = price(stack.getType());
        return unit > 0 ? unit * stack.getAmount() : 0;
    }
}
