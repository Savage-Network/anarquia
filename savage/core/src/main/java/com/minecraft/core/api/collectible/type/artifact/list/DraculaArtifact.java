package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DraculaArtifact extends ArtifactCollectible {

    private final Map<Bat, ArmorStand> batMap;

    public DraculaArtifact() {
        super("Dracula", CollectibleRarity.EPIC, new ArrayList<>(), 1726601469158L);

        this.batMap = new HashMap<>();

        setIcon(Item.of(Material.FERMENTED_SPIDER_EYE));
        setLore(Arrays.asList("§7Vire um morcego,", "§7e desapareça!"));
    }


    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;

        for (int i = 0; i < 8; ++i) {
            Bat bat = host.getWorld().spawn(host.getLocation().add(0.0D, 1.0D, 0.0D), Bat.class);
            ArmorStand ghost = bat.getWorld().spawn(bat.getLocation(), ArmorStand.class);

            ghost.setSmall(true);
            ghost.setGravity(false);
            ghost.setVisible(false);
            bat.setPassenger(ghost);

            host.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 240, 1));

            batMap.put(bat, ghost);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getJavaPlugin(), () -> {
            batMap.keySet().forEach(Entity::remove);
            batMap.values().forEach(ArmorStand::remove);
            batMap.clear();
        }, 220L);

        setCooldown(host, TimeUnit.MINUTES.toMillis(1));
    }
}
