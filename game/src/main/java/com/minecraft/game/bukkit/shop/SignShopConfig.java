package com.minecraft.game.bukkit.shop;

import com.minecraft.game.GamePlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SignShopConfig {

    private static boolean enabled = true;
    private static int maxPrice = 1_000_000;
    private static int taxPercent = 0;
    private static Set<String> shopHeaders = new HashSet<>();
    private static Set<String> serverHeaders = new HashSet<>();
    private static Set<String> buyWords = new HashSet<>();
    private static Set<String> sellWords = new HashSet<>();

    private SignShopConfig() {}

    public static void load(GamePlugin plugin) {
        FileConfiguration cfg = plugin.getConfig();
        enabled = cfg.getBoolean("sign-shops.enabled", true);
        maxPrice = cfg.getInt("sign-shops.max-price-per-unit", 1_000_000);
        taxPercent = cfg.getInt("sign-shops.tax-percent", 0);

        shopHeaders = new HashSet<>(normalizeList(cfg.getStringList("sign-shops.headers"),
                List.of("[LOJA]", "[SHOP]")));
        serverHeaders = new HashSet<>(normalizeList(cfg.getStringList("sign-shops.server-headers"),
                List.of("[LOJASERVER]", "[SHOPSERVER]", "[LOJAADMIN]")));
        buyWords = new HashSet<>(normalizeList(cfg.getStringList("sign-shops.buy-words"),
                List.of("COMPRAR", "BUY", "C")));
        sellWords = new HashSet<>(normalizeList(cfg.getStringList("sign-shops.sell-words"),
                List.of("VENDER", "SELL", "V")));
    }

    private static List<String> normalizeList(List<String> fromCfg, List<String> defaults) {
        List<String> src = fromCfg == null || fromCfg.isEmpty() ? defaults : fromCfg;
        return src.stream().map(s -> stripColor(s).toUpperCase(Locale.ROOT)).toList();
    }

    public static String stripColor(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }

    public static boolean enabled() { return enabled; }
    public static int maxPrice() { return maxPrice; }
    public static int taxPercent() { return taxPercent; }
    public static boolean isShopHeader(String line) {
        return shopHeaders.contains(stripColor(line).toUpperCase(Locale.ROOT));
    }
    public static boolean isServerHeader(String line) {
        return serverHeaders.contains(stripColor(line).toUpperCase(Locale.ROOT));
    }
    public static boolean isBuyWord(String line) {
        return buyWords.contains(stripColor(line).toUpperCase(Locale.ROOT));
    }
    public static boolean isSellWord(String line) {
        return sellWords.contains(stripColor(line).toUpperCase(Locale.ROOT));
    }
}
