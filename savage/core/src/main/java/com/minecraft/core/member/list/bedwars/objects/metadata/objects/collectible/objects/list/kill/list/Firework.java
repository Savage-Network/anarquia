package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill.FinalKillCollectible;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Random;

public class Firework extends FinalKillCollectible {

    private static final Random RANDOM = new Random();

    public Firework() {
        super(CollectibleRarity.COMUM, "Fogos de Artifício",
                Item.of(Material.FIREWORK), 0L);
    }

    @Override
    public void execute(Player killer, Location location) {
        Core.getPlatform().runSync(() -> {
            org.bukkit.entity.Firework firework = location.getWorld().spawn(location, org.bukkit.entity.Firework.class);
            org.bukkit.inventory.meta.FireworkMeta meta = firework.getFireworkMeta();
            
            meta.setPower(1);
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL)
                    .withColor(Color.fromRGB(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256)))
                    .withFade(Color.fromRGB(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256)))
                    .flicker(RANDOM.nextBoolean())
                    .trail(RANDOM.nextBoolean())
                    .build());
            
            firework.setFireworkMeta(meta);
            
            // Detonar após 8 ticks
            Core.getPlatform().runSync(firework::detonate, 8L);
        });
    }
}
