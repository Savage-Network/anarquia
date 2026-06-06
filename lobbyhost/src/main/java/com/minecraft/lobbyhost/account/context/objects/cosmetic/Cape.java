package com.minecraft.lobbyhost.account.context.objects.cosmetic;

import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import java.awt.Color;

@Getter
@RequiredArgsConstructor
public enum Cape {
    ARCO_IRIS("arco_iris", "§aArco-Íris", "arco_iris.png", RankType.VIP, "cosmetic.cape.arco_iris", Material.PRISMARINE_SHARD, new Color(255, 0, 255), CapeStyle.WINGS_RAINBOW),
    ASA_AZUL("asa_azul", "§bAsa Azul", "asa_azul.png", RankType.VIP, "cosmetic.cape.asa_azul", Material.LAPIS_LAZULI, new Color(0, 0, 255), CapeStyle.WINGS),
    ASA_COLORIDA("asa_colorida", "§dAsa Colorida", "asa_colorida.png", RankType.NATIVE, "cosmetic.cape.asa_colorida", Material.FEATHER, new Color(255, 0, 255), CapeStyle.WINGS_RAINBOW),
    ASA_DE_FOGO("asa_de_fogo", "§cAsa de Fogo", "asa_de_fogo.png", RankType.EMBER, "cosmetic.cape.asa_de_fogo", Material.BLAZE_POWDER, new Color(255, 100, 0), CapeStyle.WINGS_FIRE),
    ASA_INFERNAL("asa_infernal", "§4Asa Infernal", "asa_infernal.png", RankType.EMBER_PLUS, "cosmetic.cape.asa_infernal", Material.NETHER_WART, new Color(150, 0, 0), CapeStyle.WINGS_FIRE),
    ASA_PRETA_VERMELHA("asa_preta_vermelha", "§8Asa Preta & Vermelha", "asa_preta_e_vermelha.png", RankType.SAVAGE, "cosmetic.cape.asa_preta_vermelha", Material.COAL, new Color(80, 0, 80), CapeStyle.DEMON),
    ASA_PRETA("asa_preta", "§0Asa Preta", "asa_preta.png", RankType.SAVAGE, "cosmetic.cape.asa_preta", Material.BLACK_DYE, new Color(0, 0, 0), CapeStyle.WINGS),
    ASA_PURPLE("asa_purple", "§5Asa Roxa", "asa_purple.png", RankType.EMBER_PLUS, "cosmetic.cape.asa_purple", Material.AMETHYST_SHARD, new Color(150, 0, 150), CapeStyle.WINGS),
    ASA_VERMELHA("asa_vermelha", "§cAsa Vermelha", "asa_vermelha.png", RankType.EMBER, "cosmetic.cape.asa_vermelha", Material.REDSTONE, new Color(255, 0, 0), CapeStyle.WINGS),
    AZUL("azul", "§9Capa Azul", "azul.png", RankType.VIP, "cosmetic.cape.azul", Material.BLUE_DYE, new Color(0, 0, 200), CapeStyle.WINGS),
    CIRCULO_DE_FOGO("circulo_de_fogo", "§6Círculo de Fogo", "circulodefogo.png", RankType.EMBER, "cosmetic.cape.circulodefogo", Material.FIRE_CHARGE, new Color(255, 165, 0), CapeStyle.HALO),
    CORACAO("coracao", "§dCoração", "coracao.png", RankType.NATIVE, "cosmetic.cape.coracao", Material.GLISTERING_MELON_SLICE, new Color(255, 105, 180), CapeStyle.HEART),
    DEMON("demon", "§4Demônio", "demon.png", RankType.SAVAGE, "cosmetic.cape.demon", Material.OBSIDIAN, new Color(100, 0, 100), CapeStyle.DEMON),
    ESMERALDA("esmeralda", "§aCapa de Esmeralda", "esmeralda.png", RankType.NATIVE, "cosmetic.cape.esmeralda", Material.EMERALD, new Color(0, 255, 0), CapeStyle.WINGS),
    FANTASY("fantasy", "§dFantasia", "fantasy.png", RankType.SAVAGE, "cosmetic.cape.fantasy", Material.CHORUS_FRUIT, new Color(255, 0, 255), CapeStyle.SPARKLE);

    private final String id;
    private final String displayName;
    private final String fileName;
    private final RankType requiredRank;
    private final String permission;
    private final Material icon;
    private final Color color;
    private final CapeStyle style;

    public static Cape fromId(String id) {
        for (Cape cape : values()) {
            if (cape.getId().equalsIgnoreCase(id)) return cape;
        }
        return null;
    }
}
