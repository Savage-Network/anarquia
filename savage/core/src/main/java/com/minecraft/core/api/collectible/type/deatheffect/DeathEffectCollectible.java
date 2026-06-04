package com.minecraft.core.api.collectible.type.deatheffect;

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
public class DeathEffectCollectible extends Collectible {

    private final Effect effect;
    private final int amount;

    public DeathEffectCollectible(String name, CollectibleRarity rarity, List<RankType> ranks,
                                  Effect effect, int amount, Material icon, long releasedAt) {
        super(name, CollectibleCategory.DEATH_EFFECT, rarity, ranks, releasedAt);
        this.effect = effect;
        this.amount = amount;
        setIcon(Item.of(icon));
        Core.getCollectibleController().save(this);
    }

    public void play(Player player, Location location) {
        for (int i = 0; i < amount; i++) {
            double ox = (Math.random() - 0.5) * 1.5;
            double oy = Math.random() * 1.8;
            double oz = (Math.random() - 0.5) * 1.5;
            player.getWorld().playEffect(location.clone().add(ox, oy, oz), effect, 0);
        }
    }

    public static void handle() {
        for (DeathEffectList entry : DeathEffectList.values())
            Core.getCollectibleController().save(entry.getCosmetic());
    }
}
