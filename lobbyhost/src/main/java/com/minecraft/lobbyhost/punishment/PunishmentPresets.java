package com.minecraft.lobbyhost.punishment;

import com.minecraft.lobbyhost.api.punishment.PunishmentCategory;
import com.minecraft.lobbyhost.api.punishment.PunishmentReason;

import java.util.ArrayList;
import java.util.List;

/** Presets de punição (estilo RedeSky / Savage). */
public final class PunishmentPresets {

    public record Entry(PunishmentCategory category, PunishmentReason reason,
                        String time, String label, String icon) {}

    private static final List<Entry> ALL = List.of(
            new Entry(PunishmentCategory.BAN, PunishmentReason.CHEATING, "-1",
                    "Uso de Trapaças", "§4"),
            new Entry(PunishmentCategory.BAN, PunishmentReason.CHEATING, "30d",
                    "Cheating — 30 dias", "§c"),
            new Entry(PunishmentCategory.BAN, PunishmentReason.CHEATING, "7d",
                    "Cheating — 7 dias", "§c"),
            new Entry(PunishmentCategory.BAN, PunishmentReason.COMMUNITY, "-1",
                    "Ban permanente (comunidade)", "§4"),
            new Entry(PunishmentCategory.BAN, PunishmentReason.COMMUNITY, "14d",
                    "Discriminação — 14 dias", "§6"),
            new Entry(PunishmentCategory.BAN, PunishmentReason.COMMUNITY, "7d",
                    "Assédio sexual — 7 dias", "§6"),
            new Entry(PunishmentCategory.BAN, PunishmentReason.COMMUNITY, "3d",
                    "Skin inapropriada — 3 dias", "§6"),
            new Entry(PunishmentCategory.MUTE, PunishmentReason.COMMUNITY, "1h",
                    "Spam / Flood — 1 hora", "§e"),
            new Entry(PunishmentCategory.MUTE, PunishmentReason.COMMUNITY, "6h",
                    "Linguagem inadequada — 6h", "§e"),
            new Entry(PunishmentCategory.MUTE, PunishmentReason.COMMUNITY, "24h",
                    "Toxicidade — 24 horas", "§e"),
            new Entry(PunishmentCategory.MUTE, PunishmentReason.COMMUNITY, "-1",
                    "Mute permanente", "§4")
    );

    private PunishmentPresets() {}

    public static List<Entry> all() {
        return ALL;
    }

    public static List<Entry> forCategory(PunishmentCategory category) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : ALL) {
            if (e.category() == category) out.add(e);
        }
        return out;
    }

    public static List<Entry> filter(PunishmentCategory category, PunishmentReason reason) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : ALL) {
            if (e.category() == category && e.reason() == reason) out.add(e);
        }
        return out;
    }

    public static String formatTime(String time) {
        if (time == null || time.equals("-1") || time.equalsIgnoreCase("perm")) {
            return "Permanente";
        }
        return time;
    }
}
