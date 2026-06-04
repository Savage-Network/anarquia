package com.minecraft.game.report;

public enum ReportStatus {
    OPEN,
    CLOSED,
    ACTIONED;

    public static ReportStatus fromDb(String raw) {
        if (raw == null) return OPEN;
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OPEN;
        }
    }
}
