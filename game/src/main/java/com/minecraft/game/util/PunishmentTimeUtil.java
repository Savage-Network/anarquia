package com.minecraft.game.util;

public final class PunishmentTimeUtil {

    private PunishmentTimeUtil() {}

    public static long getTime(String timeFormat) {
        if (timeFormat == null) throw new IllegalArgumentException("null");
        String t = timeFormat.trim().toLowerCase();
        if (t.equals("-1") || t.equals("perm") || t.equals("perma")
                || t.equals("never") || t.equals("n")) {
            return -1L;
        }

        int day = 0, hour = 0, minute = 0, second = 0;
        for (String part : t.split(",")) {
            part = part.trim();
            if (part.endsWith("d")) day = Integer.parseInt(part.substring(0, part.length() - 1));
            else if (part.endsWith("h")) hour = Integer.parseInt(part.substring(0, part.length() - 1));
            else if (part.endsWith("m")) minute = Integer.parseInt(part.substring(0, part.length() - 1));
            else if (part.endsWith("s")) second = Integer.parseInt(part.substring(0, part.length() - 1));
        }
        long seconds = day * 86400L + hour * 3600L + minute * 60L + second;
        return System.currentTimeMillis() + seconds * 1000L;
    }

    public static boolean isValidTime(String timeFormat) {
        try {
            getTime(timeFormat);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
