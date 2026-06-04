package com.minecraft.core.arcade.room.custom;

import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ShopConfig {

    private boolean freeShop; // Tudo de graça
    private Map<BedWarsItem, ItemConfig> customItems;

    public ShopConfig() {
        this.freeShop = false;
        this.customItems = new HashMap<>();
    }

    public ItemConfig getItemConfig(BedWarsItem item) {
        return customItems.getOrDefault(item, new ItemConfig(item.getOre(), item.getPrice(), item.getStack().getAmount()));
    }

    public void setItemConfig(BedWarsItem item, BedOre ore, int price, int quantity) {
        customItems.put(item, new ItemConfig(ore, price, quantity));
    }

    public void resetItemConfig(BedWarsItem item) {
        customItems.remove(item);
    }

    public void resetAll() {
        customItems.clear();
        freeShop = false;
    }

    public boolean hasCustomConfig(BedWarsItem item) {
        return customItems.containsKey(item);
    }

    @Getter
    @Setter
    public static class ItemConfig {
        private BedOre ore;
        private int price;
        private int quantity;

        public ItemConfig(BedOre ore, int price, int quantity) {
            this.ore = ore;
            this.price = price;
            this.quantity = quantity;
        }
    }
}
