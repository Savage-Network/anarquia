package com.minecraft.lobbyhost.punishment;

import com.minecraft.lobbyhost.api.punishment.PunishmentCategory;
import com.minecraft.lobbyhost.api.punishment.PunishmentReason;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Sessão de menu de punição por staff. */
public final class PunishContext {

    private static final ConcurrentHashMap<UUID, PunishContext> SESSIONS = new ConcurrentHashMap<>();

    private final UUID staffId;
    private final String targetName;
    private PunishmentCategory category;
    private PunishmentReason reason;
    private String time = "-1";
    private String cause = "Não informado";
    private boolean kickMode;

    private PunishContext(UUID staffId, String targetName) {
        this.staffId = staffId;
        this.targetName = targetName;
    }

    public static PunishContext open(UUID staffId, String targetName) {
        PunishContext ctx = new PunishContext(staffId, targetName);
        SESSIONS.put(staffId, ctx);
        return ctx;
    }

    public static PunishContext of(UUID staffId) {
        return SESSIONS.get(staffId);
    }

    public static void close(UUID staffId) {
        SESSIONS.remove(staffId);
    }

    public UUID staffId() { return staffId; }
    public String targetName() { return targetName; }
    public PunishmentCategory category() { return category; }
    public PunishmentReason reason() { return reason; }
    public String time() { return time; }
    public String cause() { return cause; }
    public boolean kickMode() { return kickMode; }

    public void setCategory(PunishmentCategory category) { this.category = category; }
    public void setReason(PunishmentReason reason) { this.reason = reason; }
    public void setTime(String time) { this.time = time; }
    public void setCause(String cause) { this.cause = cause != null ? cause : "Não informado"; }
    public void setKickMode(boolean kickMode) { this.kickMode = kickMode; }
}
