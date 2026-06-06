package com.minecraft.lobbyhost.bukkit.cosmetic;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.EmberPlusColor;
import com.minecraft.lobbyhost.account.context.objects.rank.Rank;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.account.context.objects.tag.LegacyHex;
import com.minecraft.lobbyhost.account.context.objects.tag.Tag;

import java.util.ArrayList;
import java.util.List;

public final class EmberPlusService {

    public static final int MAX_SUBSCRIPTION_MONTHS = 12;

    private static final String[] RAINBOW = {"§c", "§6", "§e", "§a", "§b", "§9", "§d"};

    private EmberPlusService() {}

    public static int effectiveMonths(Account account) {
        if (account == null) return 0;
        int months = account.getContext().getEmberPlusMonths();
        if (months <= 0 && account.hasRank(RankType.EMBER_PLUS)) {
            months = 1;
        }
        if (hasPermanentEmberPlus(account)) {
            months = Math.max(months, MAX_SUBSCRIPTION_MONTHS);
        }
        return months;
    }

    public static boolean hasPermanentEmberPlus(Account account) {
        if (account == null) return false;
        Rank primary = account.getRank();
        if (isPermanentEmberPlusRank(primary)) return true;
        for (Rank rank : account.getAvailableRanks()) {
            if (isPermanentEmberPlusRank(rank)) return true;
        }
        return false;
    }

    private static boolean isPermanentEmberPlusRank(Rank rank) {
        return rank != null
                && rank.getType() == RankType.EMBER_PLUS
                && !rank.hasExpired()
                && rank.getExpiresAt() <= 0;
    }

    public static EmberPlusColor resolveColor(Account account) {
        if (account == null) return EmberPlusColor.PURPLE;
        return EmberPlusColor.fromId(account.getEmberPlusColor());
    }

    public static List<EmberPlusColor> availableColors(Account account) {
        int months = effectiveMonths(account);
        List<EmberPlusColor> out = new ArrayList<>();
        for (EmberPlusColor color : EmberPlusColor.values()) {
            if (color.isUnlocked(months)) out.add(color);
        }
        return out;
    }

    public static String animatedPlusColor(EmberPlusColor color) {
        if (color == EmberPlusColor.RAINBOW) {
            return RAINBOW[(int) ((System.currentTimeMillis() / 400L) % RAINBOW.length)];
        }
        if (color == EmberPlusColor.YIN_YANG) {
            return (System.currentTimeMillis() / 500L) % 2L == 0L ? "§f" : "§0";
        }
        return color.getLegacyColor();
    }

    public static String formatEmberPlusPrefix(Account account, Tag tag) {
        if (account == null || tag == null) return null;
        EmberPlusColor selected = resolveColor(account);
        String plus = animatedPlusColor(selected);
        if (tag == Tag.EMBER_PLUS) {
            return LegacyHex.EMBER + "§lEMBER" + plus + "+ " + LegacyHex.EMBER;
        }
        if (tag == Tag.MODPLUS) {
            return LegacyHex.MOD + "§lMOD" + plus + "+ " + LegacyHex.MOD;
        }
        return null;
    }

    public static void recordSubscriptionMonth(Account account) {
        if (account == null) return;
        if (account.getContext().getEmberPlusSince() == 0L) {
            account.getContext().setEmberPlusSince(System.currentTimeMillis());
        }
        account.getContext().setEmberPlusMonths(account.getContext().getEmberPlusMonths() + 1);
        account.markDirty();
    }
}
