package com.minecraft.core.api.collectible.type.particle.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.particle.ParticleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class FlameParticle extends ParticleCollectible {

    public FlameParticle() {
        super("Chamas", CollectibleRarity.EPIC, Arrays.asList(RankType.MEMBER), System.currentTimeMillis());
        setIcon(Item.of(Material.BLAZE_POWDER));
        setLore(Arrays.asList("§7Chamas ao redor quando parado."));
    }

    @Override
    public void display(Player player, com.minecraft.core.api.collectible.type.particle.ParticleType type) {
        Location loc = player.getLocation();
        switch (type) {
            case SIMPLES:
                renderSimple(player, loc, Effect.MOBSPAWNER_FLAMES, 3);
                break;
            case CIRCULAR:
                renderCircular(player, loc, Effect.MOBSPAWNER_FLAMES, 2.0, 16);
                break;
            default:
                renderSpiral(player, loc, Effect.MOBSPAWNER_FLAMES, 2.0, 10);
                break;
        }
    }

    @Override
    public long getInterval() {
        return 8L;
    }
}
