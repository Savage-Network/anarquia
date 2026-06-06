package com.minecraft.game.api.clan;

import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.tag.LegacyHex;

/**
 * Formata a clantag exibida no tab/nametag/chat.
 */
public final class ClanTagFormatter {

    private ClanTagFormatter() {}

    public static String formatSuffix(Account account) {
        if (account == null || account.getToggle() == null || !account.getToggle().isShowClanTag()) {
            return "";
        }
        if (!account.hasClan()) return "";
        Clan clan = account.getClan();
        if (clan == null) return "";
        return " " + formatBracketed(clan);
    }

    public static String formatBracketed(Clan clan) {
        String raw = clan.getTagWithoutColors().toUpperCase();
        if (raw.isEmpty()) raw = "CLAN";
        return formatTag(ClanColor.byStored(clan.getTagColor()), raw, clan.getTag());
    }

    public static String formatChatTag(Clan clan) {
        return formatBracketed(clan);
    }

    /** Preview usado no menu de cores (amostra curta se a tag for grande). */
    public static String formatPreview(ClanColor color, String tagSample) {
        String raw = tagSample == null || tagSample.isBlank()
                ? "TAG"
                : tagSample.toUpperCase();
        if (raw.length() > 6) raw = raw.substring(0, 6);
        return formatTag(color, raw, raw);
    }

    private static String formatTag(ClanColor color, String raw, String originalTag) {
        if (color.getGradientMode() == ClanColor.GradientMode.YIN_YANG) {
            return "§f[" + applyYinYang(raw) + "§f]";
        }
        if (color.getGradientMode() == ClanColor.GradientMode.HEX) {
            String inner = applyHexGradient(raw, color.getGradientFrom(), color.getGradientTo());
            return color.getCode() + "[" + inner + color.getCode() + "]";
        }
        String code = color.getCode();
        if (originalTag != null && (originalTag.contains("§") || originalTag.contains("&"))) {
            return code + "[" + stripLegacy(originalTag) + "]";
        }
        return code + "[" + raw + "]";
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
            sb.append(LegacyHex.of(toHex(lerpRgb(from, to, t)))).append(text.charAt(i));
        }
        return sb.toString();
    }

    private static String applyYinYang(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append(i % 2 == 0 ? "§f" : "§0").append(text.charAt(i));
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

    private static String stripLegacy(String tag) {
        return tag.replaceAll("§[0-9a-fA-Fk-oK-Or]", "").replaceAll("&[0-9a-fA-Fk-oK-Or]", "");
    }
}
