package com.minecraft.lobbyhost.punishment;

import com.minecraft.lobbyhost.api.punishment.PunishmentCategory;
import com.minecraft.lobbyhost.api.punishment.PunishmentReason;

import java.util.ArrayList;
import java.util.List;

/** Presets derivados de {@link PunishmentRules} (compatibilidade com comandos). */
public final class PunishmentPresets {

    public record Entry(PunishmentCategory category, PunishmentReason reason,
                        String time, String label, String icon) {}

    private PunishmentPresets() {}

    public static List<Entry> all() {
        List<Entry> out = new ArrayList<>();
        for (PunishmentRules.Rule rule : PunishmentRules.all()) {
            String time = rule.steps().isEmpty() ? "-1" : rule.steps().get(0);
            out.add(new Entry(rule.category(), rule.reason(), time, rule.label(), rule.color()));
        }
        return out;
    }

    public static List<Entry> forCategory(PunishmentCategory category) {
        List<Entry> out = new ArrayList<>();
        for (PunishmentRules.Rule rule : PunishmentRules.forCategory(category)) {
            String time = rule.steps().isEmpty() ? "-1" : rule.steps().get(0);
            out.add(new Entry(rule.category(), rule.reason(), time, rule.label(), rule.color()));
        }
        return out;
    }

    public static List<Entry> filter(PunishmentCategory category, PunishmentReason reason) {
        List<Entry> out = new ArrayList<>();
        for (PunishmentRules.Rule rule : PunishmentRules.forCategory(category)) {
            if (rule.reason() != reason) continue;
            String time = rule.steps().isEmpty() ? "-1" : rule.steps().get(0);
            out.add(new Entry(rule.category(), rule.reason(), time, rule.label(), rule.color()));
        }
        return out;
    }

    public static String formatTime(String time) {
        return PunishmentRules.formatTime(time);
    }
}
