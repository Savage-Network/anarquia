package com.minecraft.core.api.collectible.type.jumpeffect;

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
public class JumpEffectCollectible extends Collectible {

    private final Effect effect;
    private final int amount;

    public JumpEffectCollectible(String name, CollectibleRarity rarity, List<RankType> ranks,
                                 Effect effect, int amount, Material icon, long releasedAt) {
        super(name, CollectibleCategory.JUMP_EFFECT, rarity, ranks, releasedAt);
        this.effect = effect;
        this.amount = amount;
        setIcon(Item.of(icon));
        Core.getCollectibleController().save(this);
    }

    public void burst(Player player) {
        Location loc = player.getLocation().add(0, 0.2, 0);
        for (int i = 0; i < amount; i++) {
            double ox = (Math.random() - 0.5) * 0.8;
            double oy = Math.random() * 0.6;
            double oz = (Math.random() - 0.5) * 0.8;
            player.getWorld().playEffect(loc.clone().add(ox, oy, oz), effect, 0);
        }
    }

    public static void handle() {
        for (JumpEffectList entry : JumpEffectList.values())
            Core.getCollectibleController().save(entry.getCosmetic());
    }
}
