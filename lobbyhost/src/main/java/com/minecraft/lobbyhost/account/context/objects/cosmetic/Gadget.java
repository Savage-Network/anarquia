package com.minecraft.lobbyhost.account.context.objects.cosmetic;

import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public enum Gadget {
    FIREWORK_LAUNCHER("firework_launcher", "§aLançador de Fogos", Material.FIREWORK_ROCKET, 3000L, RankType.VIP, "cosmetic.gadget.firework"),
    ICE_WALKER("ice_walker", "§bCaminhante do Gelo", Material.PACKED_ICE, 5000L, RankType.NATIVE, "cosmetic.gadget.ice_walker"),
    JUKEBOX("jukebox", "§eJukebox Portátil", Material.JUKEBOX, 8000L, RankType.EMBER, "cosmetic.gadget.jukebox"),
    TRAMPOLINE("trampoline", "§dSuper Trampolim", Material.SLIME_BLOCK, 4000L, RankType.EMBER_PLUS, "cosmetic.gadget.trampoline"),
    ENDER_PEARL("ender_pearl", "§5Pérola do Fim", Material.ENDER_PEARL, 2000L, RankType.SAVAGE, "cosmetic.gadget.ender_pearl");

    private final String id;
    private final String displayName;
    private final Material material;
    private final long cooldownMs;
    private final RankType requiredRank;
    private final String permission;

    public static Gadget fromId(String id) {
        for (Gadget g : values()) {
            if (g.getId().equalsIgnoreCase(id)) return g;
        }
        return null;
    }
}
