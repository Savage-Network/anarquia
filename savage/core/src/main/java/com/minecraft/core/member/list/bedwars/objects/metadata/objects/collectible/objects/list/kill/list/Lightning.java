package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill.FinalKillCollectible;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Lightning extends FinalKillCollectible {

    public Lightning() {
        super(CollectibleRarity.RARE, "Raio",
                Item.of(Material.BLAZE_ROD), 0L);
    }

    @Override
    public void execute(Player killer, Location location) {
        location.getWorld().strikeLightning(location);
    }
}
