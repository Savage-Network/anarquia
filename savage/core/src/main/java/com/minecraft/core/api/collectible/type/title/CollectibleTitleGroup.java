package com.minecraft.core.api.collectible.type.title;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Subcategorias para o filtro do menu de cosméticos (títulos).
 */
@Getter
@AllArgsConstructor
public enum CollectibleTitleGroup {

    PROFILE_STATS("§6Perfil §8& §eestatísticas"),
    BED_WARS("§cBed Wars"),
    SPECIAL("§6Especiais"),
    AURA("§bAura"),
    MUSICAS("§5Músicas");

    private final String coloredLabel;

}
