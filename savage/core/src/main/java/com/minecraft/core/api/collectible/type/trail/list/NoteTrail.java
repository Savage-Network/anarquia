package com.minecraft.core.api.collectible.type.trail.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.trail.TrailCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class NoteTrail extends TrailCollectible {

    public NoteTrail() {
        super("Notas Musicais", CollectibleRarity.EPIC, Arrays.asList(RankType.MEMBER, RankType.VIP), System.currentTimeMillis());
        setIcon(Item.of(Material.NOTE_BLOCK));
        setLore(Arrays.asList("§7Notas ao passar.", "", "§7Raridade: §5Épico"));
    }

    @Override
    public void applyTrail(Player player, Location location) {
        player.getWorld().playEffect(location.clone().subtract(0, 0.05, 0), Effect.NOTE, 0);
    }

    @Override
    public void removeTrail(Location location) {
    }
}
