package com.minecraft.eventos.game.team;

import lombok.Getter;
import org.bukkit.Color;

/**
 * Cores de time, espelho de {@code TeamPresetType} do Savage.
 */
@Getter
public enum TeamPresetType {

    VERMELHO ("§c", Color.RED),
    AZUL     ("§9", Color.BLUE),
    AMARELO  ("§e", Color.YELLOW),
    VERDE    ("§a", Color.GREEN),
    BRANCO   ("§f", Color.WHITE),
    PRETO    ("§8", Color.BLACK),
    ROSA     ("§d", Color.FUCHSIA),
    AZUL_CLARO("§b", Color.AQUA);

    private final String colorCode;
    private final Color leatherColor;

    TeamPresetType(String colorCode, Color leatherColor) {
        this.colorCode = colorCode;
        this.leatherColor = leatherColor;
    }
}
