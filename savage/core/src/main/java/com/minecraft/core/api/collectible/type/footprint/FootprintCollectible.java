package com.minecraft.core.api.collectible.type.footprint;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
public class FootprintCollectible extends Collectible {

    private final Effect effect;
    private final int amount;

    public FootprintCollectible(String name, CollectibleRarity rarity, List<RankType> ranks,
                                Effect effect, int amount, Material icon, long releasedAt) {
        super(name, CollectibleCategory.FOOTPRINT, rarity, ranks, releasedAt);
        this.effect = effect;
        this.amount = amount;
        setIcon(Item.of(icon));
        Core.getCollectibleController().save(this);
    }

    public void apply(Player player, Location location) {
        Location feet = location.clone().subtract(0, 0.05, 0);
        for (int i = 0; i < amount; i++) {
            double ox = (Math.random() - 0.5) * 0.4;
            double oz = (Math.random() - 0.5) * 0.4;
            player.getWorld().playEffect(feet.clone().add(ox, 0, oz), effect, 0);
        }
    }

    public static void handle() {
        for (FootprintList entry : FootprintList.values())
            Core.getCollectibleController().save(entry.getCosmetic());
    }
}
