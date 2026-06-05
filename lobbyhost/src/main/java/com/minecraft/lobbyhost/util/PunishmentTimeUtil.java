package com.minecraft.lobbyhost.util;

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

    public static String formatRemaining(long expiresAt) {
        if (expiresAt <= 0) return "Permanente";
        long diff = expiresAt - System.currentTimeMillis();
        if (diff <= 0) return "Expirado";
        long days = diff / 86_400_000L;
        long hours = (diff % 86_400_000L) / 3_600_000L;
        long minutes = (diff % 3_600_000L) / 60_000L;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return Math.max(1, minutes) + "m";
    }
}
