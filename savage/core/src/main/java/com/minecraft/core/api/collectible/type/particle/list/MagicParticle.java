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

public class MagicParticle extends ParticleCollectible {

    public MagicParticle() {
        super("Magia", CollectibleRarity.EPIC, Arrays.asList(RankType.MEMBER, RankType.VIP), System.currentTimeMillis());
        setIcon(Item.of(Material.ENCHANTMENT_TABLE));
        setLore(Arrays.asList("§7Partículas mágicas ao redor."));
    }

    @Override
    public void display(Player player, ParticleType type) {
        Location loc = player.getLocation();
        switch (type) {
            case SIMPLES:
                renderSimple(player, loc, Effect.WITCH_MAGIC, 3);
                break;
            case CIRCULAR:
                renderCircular(player, loc, Effect.WITCH_MAGIC, 2.2, 16);
                break;
            case ESPIRAL:
            case ESPIRAL_DUPLO:
                renderSpiral(player, loc, Effect.WITCH_MAGIC, 2.0, 12);
                break;
            default:
                renderSimple(player, loc, Effect.WITCH_MAGIC, 3);
                break;
        }
    }

    @Override
    public long getInterval() {
        return 8L;
    }
}
