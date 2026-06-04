package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FinalKillCollectible extends BedCollectible {

    public FinalKillCollectible(CollectibleRarity rarity, String name, Item icon, long createdAt) {
        super(BedCollectibleType.FINAL_KILL, rarity, name, icon, createdAt);
    }

    public void execute(Player killer, Location location) {
        // Implementação padrão vazia
    }

    public boolean isNotEqual(Class<? extends FinalKillCollectible> killClass) {
        return !killClass.isAssignableFrom(getClass());
    }

    public boolean isValid() {
        return !getClass().getSimpleName().equalsIgnoreCase("Nenhuma");
    }
}
