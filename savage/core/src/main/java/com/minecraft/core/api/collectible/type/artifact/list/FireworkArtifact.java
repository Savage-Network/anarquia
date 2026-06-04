package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.collectible.util.firework.FireworkUtil;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class FireworkArtifact extends ArtifactCollectible {

    public FireworkArtifact() {
        super("Foguete", CollectibleRarity.EPIC, new ArrayList<>(), 1726529495972L);

        setIcon(Item.of(Material.FIREWORK));
        setLore(Arrays.asList("§7Vá até o céu", "§7com o seu foguete."));
    }

    @Override
    public void handle(Player host) {
        if (!host.isOnGround()) {
            host.sendMessage("§cVocê precisa estar no chão para usar o Foguete.");
            return;
        }

        if (hasCooldown(host)) return;

        Firework firework = host.getWorld().spawn(host.getLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.setPower(2);
        meta.addEffect(FireworkUtil.getRandomEffect());

        firework.setFireworkMeta(meta);
        firework.setPassenger(host);

        setCooldown(host, TimeUnit.SECONDS.toMillis(15));
    }
}
