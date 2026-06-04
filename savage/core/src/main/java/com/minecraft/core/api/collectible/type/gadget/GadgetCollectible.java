package com.minecraft.core.api.collectible.type.gadget;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
public class GadgetCollectible extends Collectible {

    private final Material material;
    private final short data;
    private final int hotbarSlot;

    public GadgetCollectible(String name, CollectibleRarity rarity, List<RankType> ranks,
                             Material material, short data, int hotbarSlot, long releasedAt) {
        super(name, CollectibleCategory.GADGET, rarity, ranks, releasedAt);
        this.material = material;
        this.data = data;
        this.hotbarSlot = hotbarSlot;
        setIcon(Item.of(material, data));
        Core.getCollectibleController().save(this);
    }

    public ItemStack createItem() {
        return Item.of(material, data)
                .name("§6" + getName())
                .lore("§7Acessório cosmético", "§8Apenas visual")
                .make();
    }

    public static void handle() {
        for (GadgetList entry : GadgetList.values())
            Core.getCollectibleController().save(entry.getCosmetic());
    }
}
