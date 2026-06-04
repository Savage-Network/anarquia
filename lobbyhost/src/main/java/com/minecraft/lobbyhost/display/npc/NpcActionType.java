package com.minecraft.lobbyhost.display.npc;

/**
 * Acao executada ao clicar no NPC (Interaction 1.21).
 */
public enum NpcActionType {
    /** Envia mensagem ao jogador. */
    MESSAGE,
    /** Executa comando como jogador ({@code /play} etc.). */
    COMMAND,
    /** Redireciona via BungeeCord para outro servidor. */
    BUNGEE,
    /** Apenas feedback visual (sem acao). */
    NONE
}
