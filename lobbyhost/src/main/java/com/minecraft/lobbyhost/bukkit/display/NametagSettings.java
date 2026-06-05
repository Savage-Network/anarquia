package com.minecraft.lobbyhost.bukkit.display;

/**
 * Nametags estilo UnlimitedNameTags — valores fixos no codigo (sem YAML).
 *
 * @see <a href="https://github.com/alexdev03/UnlimitedNametags">UnlimitedNametags</a>
 */
public final class NametagSettings {

    private NametagSettings() {}

    /** Desativa nametag vanilla via scoreboard team. */
    public static final boolean DISABLE_VANILLA_NAMETAG = false;

    /** Exibe o proprio nametag em terceira pessoa (como Lunar "Show Own Nametag"). */
    public static final boolean SHOW_OWN_NAMETAG = true;

    /** TextDisplay: segue o jogador como passageiro (movimento client-side, sem tick de teleporte). */
    public static final boolean USE_PASSENGER_MOUNT = true;

    /**
     * Deslocamento Y do TextDisplay no passageiro (pes do jogador).
     * ~1.95 = nick logo acima da cabeça com espaçamento adequado.
     */
    public static final float MOUNT_Y_OFFSET = 1.95f;
    public static final float TEXT_SCALE = 0.8f;
    public static final int LINE_WIDTH = 200;
    public static final int BACKGROUND_ALPHA = 32;
    public static final boolean TEXT_SHADOW = true;
    public static final boolean SEE_THROUGH = true;

    /** Espacamento visual entre linhas no texto (quebra de linha). */
    public static final String LINE_SEPARATOR = "\n";
}
