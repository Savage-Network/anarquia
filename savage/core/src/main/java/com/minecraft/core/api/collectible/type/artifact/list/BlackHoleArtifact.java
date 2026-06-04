package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.collectible.util.particle.ParticleEffect;
import com.minecraft.core.api.item.Item;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BlackHoleArtifact extends ArtifactCollectible {

    public BlackHoleArtifact() {
        super("Buraco Negro", CollectibleRarity.EPIC, new ArrayList<>(), 1726526700284L);

        setIcon(Item.of(Material.OBSIDIAN));
        setLore(Arrays.asList("§7Faça surgir um", "§7buraco negro no mapa."));
    }


    @Override
    public void handle(Player host) {
        if (hasCooldown(host)) return;

        int strands = 6;
        int particles = 25;
        float radius = 5;
        float curve = 10;
        double rotation = Math.PI / 4;

        Location location = host.getLocation();

        List<Player> nearByEntities = location.getWorld().getNearbyEntities(location, 5, 3, 5)
                .stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());

        for (int i = 1; i <= strands; i++) {
            for (int j = 1; j <= particles; j++) {
                float ratio = (float) j / particles;
                double angle = curve * ratio * 2 * Math.PI / strands + (2 * Math.PI * i / strands) + rotation;
                double x = Math.cos(angle) * ratio * radius;
                double z = Math.sin(angle) * ratio * radius;

                location.add(x, 0, z);

                ParticleEffect.SMOKE_LARGE.display((float) Math.floor(Math.random() * 2.0F), 1.5F, (float) Math.floor(Math.random() *
                        2.0F), 0.2F, 7, location);

                location.subtract(x, 0, z);
            }
        }

        nearByEntities.forEach(player -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
            player.playSound(player.getLocation(), Sound.PORTAL_TRAVEL, 1.5f, 2);
        });

        setCooldown(host, TimeUnit.SECONDS.toMillis(30));
    }
}

