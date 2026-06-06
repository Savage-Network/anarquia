package com.minecraft.lobbyhost.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Textos modernos: hex, gradientes e tipografia via MiniMessage (Paper 1.21).
 */
public final class ModernText {

    public static final String BRAND_A = "#FF8C42";
    public static final String BRAND_B = "#FFD166";
    public static final String ACCENT_A = "#38BDF8";
    public static final String ACCENT_B = "#818CF8";
    public static final String MUTED = "#94A3B8";
    public static final String DIM = "#64748B";
    public static final String SUCCESS = "#3DDC97";
    public static final String ERROR = "#FF6B6B";
    public static final String WARN = "#FBBF24";

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_HEX = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private ModernText() {}

    public static Component parse(String mini) {
        if (mini == null || mini.isBlank()) return Component.empty();
        return MM.deserialize(mini);
    }

    public static String legacy(String mini) {
        return LEGACY_HEX.serialize(parse(mini));
    }

    public static String legacy(Component component) {
        return LEGACY_HEX.serialize(component);
    }

    /** Converte legacy § (incluindo hex §x§R§R§G§G§B§B) para Component. */
    public static Component fromLegacy(String legacy) {
        if (legacy == null || legacy.isEmpty()) return Component.empty();
        return LEGACY_HEX.deserialize(legacy.replace('&', '§'));
    }

    public static Component noItalic(Component c) {
        return c.decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
    }

    /** Título de menu / GUI. */
    public static String menuTitle(String name) {
        return "<!italic><gradient:" + BRAND_A + ":" + BRAND_B + "><bold>" + escape(name) + "</bold></gradient>";
    }

    /** Subtítulo ou linha secundária (visual “mini”). */
    public static String mini(String text) {
        return "<!italic><" + MUTED + ">" + escape(text);
    }

    public static String label(String text) {
        return "<!italic><#F1F5F9>" + escape(text);
    }

    public static String gradient(String text, String from, String to) {
        return "<!italic><gradient:" + from + ":" + to + ">" + escape(text) + "</gradient>";
    }

    public static String status(boolean on) {
        return on
                ? "<!italic><" + SUCCESS + "><bold>Ativado</bold>"
                : "<!italic><" + ERROR + ">Desativado";
    }

    public static String dot(boolean on) {
        return on ? "<" + SUCCESS + ">●" : "<" + DIM + ">○";
    }

    public static String action(String text) {
        return "<!italic><" + WARN + ">" + escape(text);
    }

    public static String hint(String command) {
        return "<!italic><" + DIM + ">/</" + DIM + "><" + MUTED + ">" + escape(command);
    }

    public static String brandGradient(String text) {
        return gradient(text, BRAND_A, BRAND_B);
    }

    public static String sidebarTitle(String text) {
        return gradient(text, ACCENT_A, ACCENT_B);
    }

    public static String joinMessage(String player) {
        return "<!italic><" + SUCCESS + ">+ <#F8FAFC>" + escape(player)
                + " <" + MUTED + ">entrou";
    }

    public static String quitMessage(String player) {
        return "<!italic><" + ERROR + ">- <#F8FAFC>" + escape(player)
                + " <" + MUTED + ">saiu";
    }

    private static String escape(String text) {
        return text == null ? "" : text.replace("<", "\\<");
    }
}
