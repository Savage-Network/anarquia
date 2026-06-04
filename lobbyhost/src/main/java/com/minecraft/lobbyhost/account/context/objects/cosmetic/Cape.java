package com.minecraft.lobbyhost.account.context.objects.cosmetic;

import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public enum Cape {
    ARCO_IRIS("arco_iris", "§aArco-Íris", "arco_iris.png", RankType.VIP, "cosmetic.cape.arco_iris", Material.PRISMARINE_SHARD),
    ASA_AZUL("asa_azul", "§bAsa Azul", "asa azul.png", RankType.VIP, "cosmetic.cape.asa_azul", Material.LAPIS_LAZULI),
    ASA_COLORIDA("asa_colorida", "§dAsa Colorida", "asa colorida.png", RankType.NATIVE, "cosmetic.cape.asa_colorida", Material.FEATHER),
    ASA_DE_FOGO("asa_de_fogo", "§cAsa de Fogo", "asa de fogo.png", RankType.EMBER, "cosmetic.cape.asa_de_fogo", Material.BLAZE_POWDER),
    ASA_INFERNAL("asa_infernal", "§4Asa Infernal", "asa infernal.png", RankType.EMBER_PLUS, "cosmetic.cape.asa_infernal", Material.NETHER_WART),
    ASA_PRETA_VERMELHA("asa_preta_vermelha", "§8Asa Preta & Vermelha", "asa preta e vermelha.png", RankType.SAVAGE, "cosmetic.cape.asa_preta_vermelha", Material.COAL),
    ASA_PRETA("asa_preta", "§0Asa Preta", "asa preta.png", RankType.SAVAGE, "cosmetic.cape.asa_preta", Material.BLACK_DYE),
    ASA_PURPLE("asa_purple", "§5Asa Roxa", "asa purple.png", RankType.EMBER_PLUS, "cosmetic.cape.asa_purple", Material.AMETHYST_SHARD),
    ASA_VERMELHA("asa_vermelha", "§cAsa Vermelha", "asa vermelha.png", RankType.EMBER, "cosmetic.cape.asa_vermelha", Material.REDSTONE),
    AZUL("azul", "§9Capa Azul", "azul.png", RankType.VIP, "cosmetic.cape.azul", Material.BLUE_DYE),
    CIRCULO_DE_FOGO("circulo_de_fogo", "§6Círculo de Fogo", "circulodefogo.png", RankType.EMBER, "cosmetic.cape.circulodefogo", Material.FIRE_CHARGE),
    CORACAO("coracao", "§dCoração", "coracao.png", RankType.NATIVE, "cosmetic.cape.coracao", Material.GLISTERING_MELON_SLICE),
    DEMON("demon", "§4Demônio", "demon.png", RankType.SAVAGE, "cosmetic.cape.demon", Material.OBSIDIAN),
    ESMERALDA("esmeralda", "§aCapa de Esmeralda", "esmeralda.png", RankType.NATIVE, "cosmetic.cape.esmeralda", Material.EMERALD),
    FANTASY("fantasy", "§dFantasia", "fantasy.png", RankType.SAVAGE, "cosmetic.cape.fantasy", Material.CHORUS_FRUIT);

    private final String id;
    private final String displayName;
    private final String fileName;
    private final RankType requiredRank;
    private final String permission;
    private final Material icon;

    public static Cape fromId(String id) {
        for (Cape cape : values()) {
            if (cape.getId().equalsIgnoreCase(id)) return cape;
        }
        return null;
    }
}
