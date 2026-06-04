package com.minecraft.lobbyhost.report;

import com.minecraft.lobbyhost.util.ModernText;

public enum ReportReason {

    CHEAT("cheat", "Trapaca / hack", "trapaça", "hack"),
    CHAT("chat", "Chat toxico / spam", "toxico", "spam"),
    HARASS("harass", "Assedio / bullying", "assedio", "bullying"),
    BUG("bug", "Abuso de bug", "bug_abuse"),
    OTHER("other", "Outro", "outros");

    private final String id;
    private final String label;
    private final String[] aliases;

    ReportReason(String id, String label, String... aliases) {
        this.id = id;
        this.label = label;
        this.aliases = aliases;
    }

    public String id() { return id; }
    public String label() { return label; }

    public String displayMini() {
        return switch (this) {
            case CHEAT -> ModernText.gradient(label, ModernText.ERROR, "#FCA5A5");
            case CHAT -> ModernText.gradient(label, ModernText.WARN, ModernText.BRAND_B);
            case HARASS -> ModernText.gradient(label, "#F472B6", "#C084FC");
            case BUG -> ModernText.gradient(label, ModernText.ACCENT_A, ModernText.ACCENT_B);
            case OTHER -> ModernText.mini(label);
        };
    }

    public static ReportReason fromInput(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String key = raw.trim().toLowerCase();
        for (ReportReason r : values()) {
            if (r.id.equals(key)) return r;
            for (String alias : r.aliases) {
                if (alias.equalsIgnoreCase(key)) return r;
            }
        }
        return null;
    }
}
