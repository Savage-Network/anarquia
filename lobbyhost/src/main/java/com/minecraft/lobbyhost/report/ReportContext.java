package com.minecraft.lobbyhost.report;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Sessão GUI de denúncia por jogador. */
public final class ReportContext {

    private static final Map<UUID, ReportContext> SESSIONS = new ConcurrentHashMap<>();

    private String targetName;
    private ReportReason reason;
    private String details = "";

    private ReportContext() {}

    public static ReportContext open(UUID playerId) {
        ReportContext ctx = new ReportContext();
        SESSIONS.put(playerId, ctx);
        return ctx;
    }

    public static ReportContext of(UUID playerId) {
        return SESSIONS.get(playerId);
    }

    public static void close(UUID playerId) {
        SESSIONS.remove(playerId);
    }

    public String targetName() { return targetName; }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public ReportReason reason() { return reason; }

    public void setReason(ReportReason reason) {
        this.reason = reason;
    }

    public String details() { return details; }

    public void setDetails(String details) {
        this.details = details == null ? "" : details;
    }

    public boolean isComplete() {
        return targetName != null && !targetName.isBlank() && reason != null;
    }
}
