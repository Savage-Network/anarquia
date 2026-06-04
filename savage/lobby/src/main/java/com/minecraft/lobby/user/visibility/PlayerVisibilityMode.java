package com.minecraft.lobby.user.visibility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum PlayerVisibilityMode {

    ALL("§aJogadores: §fON", Material.INK_SACK, (short) 10), // Corante verde
    FRIENDS_ONLY("§bJogadores: §fApenas amigos", Material.INK_SACK, (short) 12), // Corante azul claro
    NONE("§cJogadores: §fDesativados", Material.INK_SACK, (short) 8); // Corante cinza

    private final String displayName;
    private final Material material;
    private final short data;

    public PlayerVisibilityMode next() {
        int nextOrdinal = (ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }
}
