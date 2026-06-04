package com.minecraft.game.bukkit.shop;

import com.minecraft.game.api.shop.SignShop;
import com.minecraft.game.api.shop.SignShopType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class SignShopParser {

    private SignShopParser() {}

    public static Optional<SignShop> parse(Sign sign, Player creator) {
        if (sign == null) return Optional.empty();
        String[] lines = new String[4];
        for (int i = 0; i < 4; i++) {
            lines[i] = SignShopConfig.stripColor(sign.getLine(i));
        }
        return parseLines(lines, sign.getLocation(), creator);
    }

    public static Optional<SignShop> parseLines(String[] lines, Location loc, Player creator) {
        if (lines == null || lines.length < 4 || loc == null || creator == null) {
            return Optional.empty();
        }

        boolean server = SignShopConfig.isServerHeader(lines[0]);
        if (!server && !SignShopConfig.isShopHeader(lines[0])) {
            return Optional.empty();
        }

        SignShopType type = null;
        if (SignShopConfig.isBuyWord(lines[1])) type = SignShopType.BUY;
        if (SignShopConfig.isSellWord(lines[1])) type = SignShopType.SELL;
        if (type == null) return Optional.empty();

        Material material = Material.matchMaterial(lines[2].toUpperCase(Locale.ROOT));
        if (material == null || !material.isItem()) return Optional.empty();

        int price;
        try {
            price = Integer.parseInt(lines[3].replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        if (price <= 0 || price > SignShopConfig.maxPrice()) return Optional.empty();

        if (server && !creator.hasPermission("game.shop.admin")) {
            return Optional.empty();
        }

        UUID owner = server ? UUID.fromString("00000000-0000-0000-0000-000000000000") : creator.getUniqueId();

        return Optional.of(new SignShop(
                SignShop.locationId(loc),
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ(),
                owner,
                type,
                material,
                price,
                server
        ));
    }
}
