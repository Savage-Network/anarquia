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
    private String ruleId;
    private String time = "-1";
    private String cause = "Não informado";
    private int offenseNumber = 1;
    private boolean kickMode;
    private boolean silent;

    private PunishContext(UUID staffId, String targetName) {
        this.staffId = staffId;
        this.targetName = targetName;
        this.silent = PunishmentRules.silentDefault();
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
    public String ruleId() { return ruleId; }
    public String time() { return time; }
    public String cause() { return cause; }
    public int offenseNumber() { return offenseNumber; }
    public boolean kickMode() { return kickMode; }
    public boolean silent() { return silent; }

    public void setCategory(PunishmentCategory category) { this.category = category; }
    public void setReason(PunishmentReason reason) { this.reason = reason; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public void setTime(String time) { this.time = time; }
    public void setCause(String cause) { this.cause = cause != null ? cause : "Não informado"; }
    public void setOffenseNumber(int offenseNumber) { this.offenseNumber = Math.max(1, offenseNumber); }
    public void setKickMode(boolean kickMode) { this.kickMode = kickMode; }
    public void setSilent(boolean silent) { this.silent = silent; }
    public void toggleSilent() { this.silent = !this.silent; }
}
