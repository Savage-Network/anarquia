package com.minecraft.game.account.context.objects.cosmetic;

import com.minecraft.game.account.context.objects.rank.RankType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Particle;

@Getter
@RequiredArgsConstructor
public enum ParticleEffect {
    FLAME_AURA("flame_aura", "§cAura de Fogo", Particle.FLAME, RankType.VIP, "cosmetic.particle.flame_aura", Material.FIRE_CHARGE),
    HEARTS("hearts", "§dCorações Apaixonados", Particle.HEART, RankType.NATIVE, "cosmetic.particle.hearts", Material.POPPY),
    PORTAL("portal", "§5Portal do Fim", Particle.PORTAL, RankType.NATIVE, "cosmetic.particle.portal", Material.OBSIDIAN),
    NOTES("notes", "§aSinfonia Musical", Particle.NOTE, RankType.EMBER, "cosmetic.particle.notes", Material.NOTE_BLOCK),
    SMOKE_RING("smoke_ring", "§8Anel de Fumaça", Particle.CAMPFIRE_COSY_SMOKE, RankType.VIP, "cosmetic.particle.smoke", Material.CAMPFIRE),
    HAPPY_VILLAGER("happy_villager", "§aBrilho Verde", Particle.HAPPY_VILLAGER, RankType.EMBER_PLUS, "cosmetic.particle.green", Material.EMERALD),
    WITCH("witch", "§5Magia de Bruxa", Particle.WITCH, RankType.SAVAGE, "cosmetic.particle.witch", Material.BREWING_STAND),
    SNOWFLAKE("snowflake", "§bFlocos de Neve", Particle.SNOWFLAKE, RankType.VIP, "cosmetic.particle.snowflake", Material.SNOWBALL),
    DRAGON_BREATH("dragon_breath", "§5Sopro do Dragão", Particle.DRAGON_BREATH, RankType.SAVAGE, "cosmetic.particle.dragon_breath", Material.DRAGON_BREATH),
    ENCHANTED_HIT("enchanted_hit", "§6Golpes Encantados", Particle.ENCHANTED_HIT, RankType.EMBER_PLUS, "cosmetic.particle.enchanted_hit", Material.EXPERIENCE_BOTTLE);

    private final String id;
    private final String displayName;
    private final Particle particle;
    private final RankType requiredRank;
    private final String permission;
    private final Material icon;

    public static ParticleEffect fromId(String id) {
        for (ParticleEffect p : values()) {
            if (p.getId().equalsIgnoreCase(id)) return p;
        }
        return null;
    }
}
