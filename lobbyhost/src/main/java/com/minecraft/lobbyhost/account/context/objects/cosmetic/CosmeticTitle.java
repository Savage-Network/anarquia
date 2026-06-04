package com.minecraft.lobbyhost.account.context.objects.cosmetic;

import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public enum CosmeticTitle {
    LENDARIO("legendario", "§6§lLENDÁRIO", RankType.VIP, "cosmetic.title.legendario", Material.NAME_TAG),
    DESTRUIDOR("destruidor", "§c§lDESTRUIDOR", RankType.EMBER, "cosmetic.title.destruidor", Material.NAME_TAG),
    MESTRE("mestre", "§b§lMESTRE", RankType.NATIVE, "cosmetic.title.mestre", Material.NAME_TAG),
    FANTASMA("fantasma", "§7§lFANTASMA", RankType.VIP, "cosmetic.title.fantasma", Material.NAME_TAG),
    SOMBRIO("sombrio", "§8§lSOMBRIO", RankType.NATIVE, "cosmetic.title.sombrio", Material.NAME_TAG),
    IMORTAL("imortal", "§e§lIMORTAL", RankType.EMBER_PLUS, "cosmetic.title.imortal", Material.NAME_TAG),
    DIVINO("divino", "§d§lDIVINO", RankType.SAVAGE, "cosmetic.title.divino", Material.NAME_TAG),
    INFERNAL("infernal", "§4§lINFERNAL", RankType.EMBER_PLUS, "cosmetic.title.infernal", Material.NAME_TAG),
    SUPREMO("supremo", "§5§lSUPREMO", RankType.SAVAGE, "cosmetic.title.supremo", Material.NAME_TAG),
    TITAN("titan", "§9§lTITÃ", RankType.SAVAGE, "cosmetic.title.titan", Material.NAME_TAG);

    private final String id;
    private final String displayName;
    private final RankType requiredRank;
    private final String permission;
    private final Material icon;

    public static CosmeticTitle fromId(String id) {
        for (CosmeticTitle t : values()) {
            if (t.getId().equalsIgnoreCase(id)) return t;
        }
        return null;
    }
}
