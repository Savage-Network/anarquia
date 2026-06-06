package com.minecraft.lobbyhost.account.context.objects.toggle;

import lombok.Getter;
import lombok.Setter;

/**
 * Preferencias do jogador (espelho do {@code Toggle} do Savage).
 *
 * <p>Editavel via {@code /prefs}.
 */
@Getter
@Setter
public class Toggle {

    private boolean allowPrivateMessages = true;
    private boolean allowFriendRequests = true;
    private boolean allowPartyInvites = true;
    private boolean allowClanInvites = true;
    private boolean showStats = true;
    private boolean showClanTag = true;
    private boolean allowJoinMessage = true;
    /** Se false, o jogador nao pode falar no lobby. */
    private boolean allowLobbyChat = true;
    private boolean staffChat = false;
    private boolean autoVanish = false;

    /** Anticheat / combate — preferencias do jogador. */
    private boolean acShowCombatTag = true;
    private boolean acNotifyOnFlag = false;
    /** Staff: alertas de deteccao do anticheat. */
    private boolean acStaffAlerts = true;
    private boolean acStaffVerbose = false;

    private TagPrefix tagPrefix = TagPrefix.DEFAULT;

    /** Idioma do jogador: {@code pt} ou {@code en}. */
    private String language = "pt";
}
