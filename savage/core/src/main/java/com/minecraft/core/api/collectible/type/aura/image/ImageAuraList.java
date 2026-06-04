package com.minecraft.core.api.collectible.type.aura.image;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import lombok.Getter;
import org.bukkit.Material;

import java.util.Arrays;

@Getter
public enum ImageAuraList {

    YIN_YANG("Yin-Yang", CollectibleRarity.RARE,
            "auras/yin_yang",
            null,
            0.055f, 1, false, Material.ENDER_PEARL),

    ESTRELA("Estrela", CollectibleRarity.RARE,
            "auras/estrela", null, 0.05f, 1, false, Material.NETHER_STAR),

    PERSONALIZADA("Personalizada", CollectibleRarity.EPIC,
            null, null, 0.055f, 1, true, Material.BOOK_AND_QUILL);

    private final ImageAuraCollectible cosmetic;

    ImageAuraList(String name, CollectibleRarity rarity, String resourcePath, String imageUrl,
                  float ratio, int scanQuality, boolean usesAccountUrl, Material icon) {
        ImageAuraCollectible aura = new ImageAuraCollectible(
                name, rarity, Arrays.asList(RankType.MEMBER, RankType.VIP),
                resourcePath, imageUrl, ratio, scanQuality, usesAccountUrl, icon,
                System.currentTimeMillis());
        aura.setLore(Arrays.asList(
                usesAccountUrl
                        ? "§7Cole um link do Imgur com §f/aura link <url>"
                        : "§7Imagem em partículas acima da cabeça.",
                usesAccountUrl ? "" : "§8Requer PNG em resources/auras/",
                "",
                "§7Raridade: " + rarity.getColoredName()));
        this.cosmetic = aura;
    }
}
