package com.minecraft.core.api.collectible.type.aura.image;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Getter
public class ImageAuraCollectible extends Collectible {

    /** Recurso no jar: ex. {@code auras/yin_yang.png} */
    private final String resourcePath;

    /** URL remota (Imgur). Usada se {@code resourcePath} for nulo. */
    private final String imageUrl;

    private final float ratio;
    private final int scanQuality;

    /** Usa a URL salva na conta ({@link com.minecraft.core.account.context.objects.aura.ImageAuraMetadata}). */
    private final boolean usesAccountUrl;

    public ImageAuraCollectible(String name, CollectibleRarity rarity, List<RankType> ranks,
                                String resourcePath, String imageUrl, float ratio, int scanQuality,
                                boolean usesAccountUrl, Material icon, long releasedAt) {
        super(name, CollectibleCategory.AURA, rarity, ranks, releasedAt);
        this.resourcePath = resourcePath;
        this.imageUrl = imageUrl;
        this.ratio = ratio;
        this.scanQuality = scanQuality;
        this.usesAccountUrl = usesAccountUrl;
        setIcon(Item.of(icon));
        Core.getCollectibleController().save(this);
    }

    public static void handle() {
        for (ImageAuraList preset : ImageAuraList.values())
            Core.getCollectibleController().save(preset.getCosmetic());
    }
}
