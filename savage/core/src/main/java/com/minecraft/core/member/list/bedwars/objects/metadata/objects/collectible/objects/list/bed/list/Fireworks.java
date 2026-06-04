package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.Random;

public class Fireworks extends BedDestroyCollectible {

    private static final Random RANDOM = new Random();

    public Fireworks() {
        super(CollectibleRarity.COMUM, "Fogos de Artifício",
                Item.of(Material.FIREWORK), 0L);
    }

    @Override
    public void execute(Location location) {
        int diameter = 10;

        for (int i = 0; i < 3; i++) {
            Location newLocation = location.clone().add(
                    new Vector(Math.random() - 0.5, 0.0, Math.random() - 0.5).multiply(diameter)
            );
            
            Firework firework = newLocation.getWorld().spawn(newLocation, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            
            FireworkEffect.Type type = FireworkEffect.Type.BALL;
            FireworkEffect effect = FireworkEffect.builder()
                    .flicker(RANDOM.nextBoolean())
                    .withColor(Color.fromBGR(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256)))
                    .withFade(Color.fromBGR(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256)))
                    .with(type)
                    .trail(RANDOM.nextBoolean())
                    .build();
            
            meta.addEffect(effect);
            meta.setPower(0);
            firework.setFireworkMeta(meta);
            
            newLocation.getWorld().playEffect(newLocation, Effect.FIREWORKS_SPARK, 1);
        }
    }
}
