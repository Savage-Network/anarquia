package com.minecraft.core.api.collectible.type.aura;

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

import java.util.Arrays;
import java.util.List;

@Getter
public class AuraCollectible extends Collectible {

    private final Effect effect;
    private final double radius;
    private final int points;

    public AuraCollectible(String name, CollectibleRarity rarity, List<RankType> ranks,
                           Effect effect, double radius, int points, Material icon, long releasedAt) {
        super(name, CollectibleCategory.AURA, rarity, ranks, releasedAt);
        this.effect = effect;
        this.radius = radius;
        this.points = points;
        setIcon(Item.of(icon));
        Core.getCollectibleController().save(this);
    }

    public void tick(Player player) {
        Location center = player.getLocation().add(0, 0.2, 0);
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points + (System.currentTimeMillis() / 400.0);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            player.getWorld().playEffect(center.clone().add(x, 0, z), effect, 0);
        }
    }

    public static void handle() {
        for (AuraList aura : AuraList.values())
            Core.getCollectibleController().save(aura.getCosmetic());
    }
}
