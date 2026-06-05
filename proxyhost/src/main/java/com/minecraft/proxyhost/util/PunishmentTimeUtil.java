package com.minecraft.proxyhost.util;

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
        try { getTime(timeFormat); return true; }
        catch (Exception e) { return false; }
    }

    /**
     * Formata o tempo restante até {@code expiresAt} (epoch ms).
     * Ex: "2d 3h 15m" ou "Permanente".
     */
    public static String formatRemaining(long expiresAt) {
        if (expiresAt <= 0) return "Permanente";
        long diff = expiresAt - System.currentTimeMillis();
        if (diff <= 0) return "Expirado";

        long secs  = diff / 1000;
        long mins  = secs / 60;
        long hours = mins / 60;
        long days  = hours / 24;

        if (days > 0)  return days  + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (mins % 60)  + "m";
        if (mins > 0)  return mins  + "m " + (secs % 60)  + "s";
        return secs + "s";
    }
}
