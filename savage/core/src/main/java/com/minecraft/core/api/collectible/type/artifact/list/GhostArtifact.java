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

public class GhostArtifact extends ArtifactCollectible {

    private final Map<Bat, ArmorStand> ghostMap;

    public GhostArtifact() {
        super("Fantasmas", CollectibleRarity.EPIC, new ArrayList<>(), 1726601469158L);

        this.ghostMap = new HashMap<>();

        setIcon(Item.of(Material.WEB));
        setLore(Collections.singletonList("§7Invoque fantasmas."));
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
            ghost.setHelmet(Item.of(Material.SKULL_ITEM, 3)
                    .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY3MmI1YTNjZWNhNmNmOTYxOTZmNGZkOTkxMjllYmU4ZTM5MGJjN2YwYmJkYTcyYzg2MzRjMDk5OTgxZTFiZCJ9fX0="));

            bat.setPassenger(ghost);
            bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 240, 1));

            this.ghostMap.put(bat, ghost);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getJavaPlugin(), () -> {
            ghostMap.keySet().forEach(Entity::remove);
            ghostMap.values().forEach(ArmorStand::remove);
            ghostMap.clear();
        }, 220L);

        setCooldown(host, TimeUnit.MINUTES.toMillis(1));
    }
}
