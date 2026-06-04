package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill.FinalKillCollectible;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BloodExplosion extends FinalKillCollectible {

    public BloodExplosion() {
        super(CollectibleRarity.COMUM, "Explosão de Sangue",
                Item.of(Material.REDSTONE), 0L);
    }

    @Override
    public void execute(Player killer, Location location) {
        location.getWorld().playEffect(location.clone().add(0, 0, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        location.getWorld().playEffect(location.clone().add(0, 1, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        location.getWorld().playEffect(location.clone().add(0, 2, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
    }
}
