package com.minecraft.lobbyhost.bukkit.display;

/**
 * Nametags estilo UnlimitedNameTags — valores fixos no codigo (sem YAML).
 *
 * @see <a href="https://github.com/alexdev03/UnlimitedNametags">UnlimitedNametags</a>
 */
public final class NametagSettings {

    private NametagSettings() {}

    /** Desativa nametag vanilla via scoreboard team. */
    public static final boolean DISABLE_VANILLA_NAMETAG = true;

    /** Exibe o proprio nametag em terceira pessoa (como Lunar "Show Own Nametag"). */
    public static final boolean SHOW_OWN_NAMETAG = true;

    /** TextDisplay: segue o jogador como passageiro (movimento client-side, sem tick de teleporte). */
    public static final boolean USE_PASSENGER_MOUNT = true;

    /**
     * Deslocamento Y do TextDisplay no passageiro (pes do jogador).
     * ~1.88 = nick logo acima da coroa; 2.05+ deixa o bloco inteiro flutuando alto.
     */
    public static final float MOUNT_Y_OFFSET = 1.88f;
    public static final float TEXT_SCALE = 1.0f;
    public static final int LINE_WIDTH = 240;
    public static final int BACKGROUND_ALPHA = 64;
    public static final boolean TEXT_SHADOW = true;
    public static final boolean SEE_THROUGH = false;

    /** Espacamento visual entre linhas no texto (quebra de linha). */
    public static final String LINE_SEPARATOR = "\n";
}
