package com.minecraft.game.bukkit.shop;

import com.minecraft.game.api.shop.SignShop;
import com.minecraft.game.api.shop.SignShopType;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.block.SignChangeEvent;

public final class SignShopFormatter {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private SignShopFormatter() {}

    public static void apply(SignChangeEvent event, SignShop shop) {
        if (event == null || shop == null) return;
        String[] lines = formatLines(shop);
        for (int i = 0; i < 4; i++) {
            event.line(i, LEGACY.deserialize(lines[i]));
        }
    }

    public static void apply(Sign sign, SignShop shop) {
        if (sign == null || shop == null) return;
        String[] lines = formatLines(shop);
        var side = sign.getSide(Side.FRONT);
        for (int i = 0; i < 4; i++) {
            side.line(i, LEGACY.deserialize(lines[i]));
        }
        sign.update(true, false);
    }

    public static String[] formatLines(SignShop shop) {
        String symbol = SemiAnarchyConfig.currencySymbol();
        String header = shop.isServerShop() ? "§6§l[LojaServer]" : "§e§l[Loja]";
        String type = shop.getType() == SignShopType.BUY ? "§a§lComprar" : "§c§lVender";
        String material = ItemDisplayNames.signLine(shop.getMaterial());
        String price = "§a" + symbol + shop.getPricePerUnit() + " §7/un";
        return new String[] {header, type, material, price};
    }
}
