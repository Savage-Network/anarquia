package com.minecraft.core.api.collectible.type.title;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Agrupamento opcional dentro de {@link CollectibleTitleGroup#MUSICAS}
 * para o menu de seleção filtrar por artista/intérprete.
 */
@Getter
@AllArgsConstructor
public enum CollectibleMusicArtist {

    UNKNOWN("§8Sem artista"),
    BOLADIN211("§eBoladin 211"),
    CABELINHO("§6Cabelinho"),
    MC_GP("§cMC GP"),
    MC_LELE_JP("§dMC Lele JP"),
    MENO_K("§eMeno K"),
    /** Oruam — Liberdade e outros. */
    ORUAM("§8Oruam"),
    /** Matuê — nome interno Java sem acento. */
    MATUE("§5Matuê"),
    OROCHI("§dOrochi"),
    VINICIN("§aVinicin"),
    /** Trap nacional — Dos Prédio / vida chique. */
    VEIGH("§bVeigh"),
    WIU("§dWiu"),
    TETO("§3Teto"),
    HUNGRIA("§6Hungria"),
    FILIPE_RET("§eFilipe Ret"),
    L7NNON("§cL7nnon"),
    BK("§4BK"),
    MC_IG("§cMc IG"),
    MC_RYAN_SP("§cMC Ryan SP"),
    JUSTIN_BIEBER("§bJustin Bieber"),
    TRAVIS_SCOTT("§4Travis Scott"),
    HARRY_STYLES("§3Harry Styles"),
    SABRINA_CARPENTER("§dSabrina Carpenter"),
    KID_LAROI("§eThe Kid LAROI"),
    ONE_REPUBLIC("§fOneRepublic"),
    NICKI_MINAJ("§dNicki Minaj"),
    GNASH("§7gnash"),
    KAYBLACK("§8KayBlack"),
    CARTEL_MCS("§6Cartel MCs"),
    KENDRICK("§5Kendrick Lamar"),
    SEAN_KINGSTON("§6Sean Kingston"),
    CJOTA("§8CJota"),
    SID("§aSid");

    private final String coloredLabel;
}
