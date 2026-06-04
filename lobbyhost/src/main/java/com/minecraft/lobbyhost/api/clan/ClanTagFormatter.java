package com.minecraft.lobbyhost.api.clan;

import com.minecraft.lobbyhost.account.Account;

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

        ClanColor color = ClanColor.byStored(clan.getTagColor());
        if (color == ClanColor.YINGYANG) {
            return "§f[" + applyYinYang(raw) + "§f]";
        }
        if (color == ClanColor.BRASIL) {
            return "§e[" + applyBrasil(raw) + "§e]";
        }
        String code = color.getCode();
        if (clan.getTag().contains("§") || clan.getTag().contains("&")) {
            return code + "[" + stripLegacy(clan.getTag()) + "]";
        }
        return code + "[" + raw + "]";
    }

    public static String formatChatTag(Clan clan) {
        return formatBracketed(clan);
    }

    private static String applyBrasil(String text) {
        int half = (int) Math.ceil(text.length() / 2.0);
        return "§e" + text.substring(0, half) + "§a" + text.substring(half);
    }

    private static String applyYinYang(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append(i % 2 == 0 ? "§f" : "§0").append(text.charAt(i));
        }
        return sb.toString();
    }

    private static String stripLegacy(String tag) {
        return tag.replaceAll("§[0-9a-fA-Fk-oK-Or]", "").replaceAll("&[0-9a-fA-Fk-oK-Or]", "");
    }
}
