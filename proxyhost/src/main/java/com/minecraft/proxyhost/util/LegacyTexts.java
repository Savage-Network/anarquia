package com.minecraft.proxyhost.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public final class LegacyTexts {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private LegacyTexts() {}

    public static Component component(String legacy) {
        if (legacy == null || legacy.isEmpty()) return Component.empty();
        return LEGACY.deserialize(legacy.replace('&', '§'));
    }

    public static Component joinLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) return Component.empty();
        List<Component> parts = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) parts.add(Component.newline());
            parts.add(component(lines.get(i)));
        }
        return Component.text().append(parts).build();
    }

    public static List<String> colorize(List<String> lines) {
        if (lines == null) return List.of();
        return lines.stream().map(s -> s == null ? "" : s.replace('&', '§')).toList();
    }
}
