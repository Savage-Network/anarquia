package com.minecraft.core.api.collectible.type.particle.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.particle.ParticleCollectible;
import com.minecraft.core.api.collectible.type.particle.ParticleType;
import com.minecraft.core.api.item.Item;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SmokeParticle extends ParticleCollectible {

    public SmokeParticle() {
        super("Fumaça", CollectibleRarity.COMUM, Arrays.asList(RankType.MEMBER), System.currentTimeMillis());
        setIcon(Item.of(Material.SULPHUR));
        setLore(Arrays.asList("§7Fumaça ao redor do jogador."));
    }

    @Override
    public void display(Player player, ParticleType type) {
        Location loc = player.getLocation();
        switch (type) {
            case SIMPLES:
                renderSimple(player, loc, Effect.SMOKE, 4);
                break;
            case CIRCULAR:
                renderCircular(player, loc, Effect.SMOKE, 2.0, 14);
                break;
            case ESPIRAL:
            case ESPIRAL_DUPLO:
                renderSpiral(player, loc, Effect.SMOKE, 2.0, 10);
                break;
            default:
                renderSimple(player, loc, Effect.SMOKE, 3);
                break;
        }
    }

    @Override
    public long getInterval() {
        return 6L;
    }
}
