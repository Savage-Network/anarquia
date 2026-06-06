package com.minecraft.game.api.clan;

import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.tag.LegacyHex;

/**
 * Renderizador unificado de clantags — chat, tab, nametag e scoreboard usam a mesma lógica.
 */
public final class ClanTagFormatter {

    private ClanTagFormatter() {}

    /** Limite seguro de suffix/prefix em scoreboard teams (Paper 1.21). */
    public static final int TEAM_TEXT_LIMIT = 64;

    public enum DisplayMode {
        /** Chat, nametag, preview e menus. */
        FULL,
        /** Tab/scoreboard — truncado com {@link #fitTeamText}. */
        TEAM
    }

    public static String formatSuffix(Account account) {
        if (account == null || account.getToggle() == null || !account.getToggle().isShowClanTag()) {
            return "";
        }
        if (!account.hasClan()) return "";
        Clan clan = account.getClan();
        if (clan == null) return "";
        return fitTeamText(" " + render(clan, DisplayMode.TEAM));
    }

    public static String formatBracketed(Clan clan) {
        return render(clan, DisplayMode.FULL);
    }

    public static String formatCompact(Clan clan) {
        return render(clan, DisplayMode.TEAM);
    }

    public static String formatCompact(ClanColor color, String raw) {
        return render(color, normalizeRaw(raw), DisplayMode.TEAM);
    }

    public static String formatChatTag(Clan clan) {
        return formatBracketed(clan);
    }

    public static String formatPreview(ClanColor color, String tagSample) {
        String raw = normalizeRaw(tagSample);
        if (raw.length() > 8) raw = raw.substring(0, 8);
        return render(color, raw, DisplayMode.FULL);
    }

    public static String render(Clan clan, DisplayMode mode) {
        if (clan == null) return "";
        return render(ClanColor.byStored(clan.getTagColor()), normalizeRaw(clan.getTagWithoutColors()), mode);
    }

    public static String render(ClanColor color, String raw, DisplayMode mode) {
        if (color == null) color = ClanColor.DEFAULT;
        String text = normalizeRaw(raw);

        return switch (color.getRenderMode()) {
            case SOLID -> solidBracket(color.getCode(), text);
            case HEX -> hexBracket(text, color.getToneA(), color.getToneB());
            case YIN_YANG -> yinYangBracket(text);
            case ALTERNATE -> alternateBracket(text, color.getToneA(), color.getToneB());
        };
    }

    /** Garante que o texto legacy caiba no team suffix/prefix sem cortar códigos §x. */
    public static String fitTeamText(String legacy) {
        if (legacy == null || legacy.isEmpty()) return "";
        if (legacy.length() <= TEAM_TEXT_LIMIT) return legacy;

        StringBuilder out = new StringBuilder();
        int visible = 0;
        for (int i = 0; i < legacy.length(); ) {
            char c = legacy.charAt(i);
            if (c == '§' && i + 1 < legacy.length()) {
                char code = legacy.charAt(i + 1);
                if (code == 'x' && i + 13 < legacy.length()) {
                    out.append(legacy, i, i + 14);
                    i += 14;
                    continue;
                }
                out.append('§').append(code);
                i += 2;
                continue;
            }
            if (visible >= TEAM_TEXT_LIMIT) break;
            out.append(c);
            visible++;
            i++;
        }
        return out.toString();
    }

    private static String normalizeRaw(String raw) {
        if (raw == null || raw.isBlank()) return "CLAN";
        String cleaned = raw.replaceAll("§[0-9a-fA-Fk-oK-Or]", "")
                .replaceAll("&[0-9a-fA-Fk-oK-Or]", "")
                .trim();
        return cleaned.isEmpty() ? "CLAN" : cleaned.toUpperCase();
    }

    private static String solidBracket(String code, String text) {
        return code + "§l[" + code + "§l" + text + code + "§l]";
    }

    private static String hexBracket(String text, String fromHex, String toHex) {
        String open = LegacyHex.of(fromHex) + "§l[";
        String body = applyHexGradient(text, fromHex, toHex);
        String close = LegacyHex.of(toHex) + "§l]";
        return open + body + close;
    }

    private static String yinYangBracket(String text) {
        return "§f§l[" + applyAlternate(text, "§f§l", "§8§l") + "§7§l]";
    }

    private static String alternateBracket(String text, String toneA, String toneB) {
        String a = toneA.endsWith("§l") ? toneA : toneA + "§l";
        String b = toneB.endsWith("§l") ? toneB : toneB + "§l";
        return a + "[" + applyAlternate(text, a, b) + a + "]";
    }

    private static String applyAlternate(String text, String first, String second) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append(i % 2 == 0 ? first : second).append(text.charAt(i));
        }
        return sb.toString();
    }

    private static String applyHexGradient(String text, String fromHex, String toHex) {
        if (text == null || text.isEmpty()) return "";
        int from = parseRgb(fromHex);
        int to = parseRgb(toHex);
        if (from < 0 || to < 0) return text;

        StringBuilder sb = new StringBuilder();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            double t = len == 1 ? 0.0 : (double) i / (len - 1);
            sb.append(LegacyHex.of(toHex(lerpRgb(from, to, t)))).append("§l").append(text.charAt(i));
        }
        return sb.toString();
    }

    private static int parseRgb(String hex) {
        if (hex == null || hex.isBlank()) return -1;
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (h.length() != 6) return -1;
        try {
            return Integer.parseInt(h, 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String toHex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    private static int lerpRgb(int from, int to, double t) {
        int fr = (from >> 16) & 0xFF;
        int fg = (from >> 8) & 0xFF;
        int fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF;
        int tg = (to >> 8) & 0xFF;
        int tb = to & 0xFF;
        int r = (int) Math.round(fr + (tr - fr) * t);
        int g = (int) Math.round(fg + (tg - fg) * t);
        int b = (int) Math.round(fb + (tb - fb) * t);
        return (r << 16) | (g << 8) | b;
    }
}
