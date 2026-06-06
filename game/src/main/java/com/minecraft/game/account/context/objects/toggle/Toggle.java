package com.minecraft.game.account.context.objects.toggle;

import lombok.Getter;
import lombok.Setter;

/**
 * Preferencias do jogador (espelho do {@code Toggle} do Savage).
 *
 * <p>Editavel via {@code /prefs} (menu).
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
    private boolean allowLobbyChat = true;
    private boolean staffChat = false;
    private boolean autoVanish = false;

    private boolean acShowCombatTag = true;
    private boolean acNotifyOnFlag = false;
    private boolean acStaffAlerts = true;
    private boolean acStaffVerbose = false;

    private TagPrefix tagPrefix = TagPrefix.DEFAULT;

    private boolean showCosmeticParticles = true;
    private boolean showOtherCapes = true;

    private boolean notifyFriendOnline = true;
    private boolean notifyClanActivity = true;
    private boolean notifyBountyOnMe = true;
    private boolean notifyKillStreakGlobal = true;
    private boolean notifyFeed = true;
    private boolean compactChat = false;
    private boolean hideGlobalAnnouncements = false;

    /** Idioma do jogador: {@code pt} ou {@code en}. */
    private String language = "pt";
}
