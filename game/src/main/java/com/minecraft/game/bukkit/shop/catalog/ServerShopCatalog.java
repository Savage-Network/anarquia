package com.minecraft.game.bukkit.shop.catalog;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ServerShopCatalog {

    private static final Map<String, ShopCategory> CATEGORIES = new LinkedHashMap<>();

    private ServerShopCatalog() {}

    public static void load(GamePlugin plugin) {
        CATEGORIES.clear();
        File file = new File(plugin.getDataFolder(), "shops.yml");
        if (!file.exists()) plugin.saveResource("shops.yml", false);
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = cfg.getConfigurationSection("categories");
        if (root == null) return;

        for (String id : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(id);
            if (sec == null) continue;

            String name = colorize(sec.getString("name", id));
            Material icon = Material.matchMaterial(sec.getString("icon", "CHEST").toUpperCase(Locale.ROOT));
            if (icon == null) icon = Material.CHEST;
            int slot = sec.getInt("slot", 10);

            List<ShopProduct> products = new ArrayList<>();
            if (sec.isList("items")) {
                for (Object raw : sec.getList("items")) {
                    if (raw instanceof Map<?, ?> map) {
                        products.add(parseProduct(map));
                    }
                }
            }

            CATEGORIES.put(id.toLowerCase(Locale.ROOT), new ShopCategory(id, name, icon, slot, products));
        }
        Core.getLogger().info("Loja GUI: " + CATEGORIES.size() + " categorias.");
    }

    private static ShopProduct parseProduct(Map<?, ?> map) {
        Material mat = Material.matchMaterial(String.valueOf(map.get("material")).toUpperCase(Locale.ROOT));
        if (mat == null) mat = Material.STONE;
        int amount = map.get("amount") instanceof Number n ? n.intValue() : 1;
        int price = map.get("price") instanceof Number p ? p.intValue() : 10;
        return new ShopProduct(mat, Math.max(1, amount), Math.max(1, price));
    }

    public static Map<String, ShopCategory> categories() {
        return Collections.unmodifiableMap(CATEGORIES);
    }

    public static Optional<ShopCategory> category(String id) {
        return Optional.ofNullable(CATEGORIES.get(id.toLowerCase(Locale.ROOT)));
    }

    static String colorize(String input) {
        if (input == null) return "";
        return input.replace('&', '§');
    }
}
