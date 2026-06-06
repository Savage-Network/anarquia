package com.minecraft.eventos.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/** Textos MiniMessage no estilo SavageMC. */
public final class ModernText {

    public static final String BRAND_A = "#FF8C42";
    public static final String BRAND_B = "#FFD166";
    public static final String MUTED = "#94A3B8";
    public static final String DIM = "#64748B";
    public static final String SUCCESS = "#3DDC97";
    public static final String ERROR = "#FF6B6B";
    public static final String WARN = "#FBBF24";

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private ModernText() {}

    public static Component parse(String mini) {
        if (mini == null || mini.isBlank()) return Component.empty();
        return MM.deserialize(mini).decoration(TextDecoration.ITALIC, false);
    }

    public static String legacy(String mini) {
        return LEGACY.serialize(parse(mini));
    }

    public static String escape(String text) {
        return text == null ? "" : text.replace("<", "\\<");
    }

    public static String header() {
        return "<!italic><gradient:" + BRAND_A + ":" + BRAND_B + "><bold>SavageMC</bold></gradient>";
    }

    public static String mini(String text) {
        return "<!italic><" + MUTED + ">" + escape(text);
    }
}
