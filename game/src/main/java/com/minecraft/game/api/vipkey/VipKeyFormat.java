package com.minecraft.game.api.vipkey;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * Formato padrao de keys: {@code XXXX-XXXX-XXXX} (ex.: {@code 2BL3-M3BI-G2FY}).
 * Charset sem 0/O para evitar confusao visual.
 */
public final class VipKeyFormat {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CODE_CHARS = "ABCDEFGHIJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int SEGMENT_LEN = 4;
    private static final int SEGMENTS = 3;

    private VipKeyFormat() {}

    /** Gera uma key no formato {@code XXXX-XXXX-XXXX}. */
    public static String generate() {
        StringBuilder compact = new StringBuilder(SEGMENT_LEN * SEGMENTS);
        for (int i = 0; i < SEGMENT_LEN * SEGMENTS; i++) {
            compact.append(CODE_CHARS[RANDOM.nextInt(CODE_CHARS.length)]);
        }
        return formatCompact(compact.toString());
    }

    /**
     * Normaliza entrada do jogador para lookup.
     * Aceita com/sem hifens e espacos. Keys legadas ({@code VIP-XXXXXXXXXX}) permanecem intactas.
     */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) return "";

        String upper = raw.trim().toUpperCase(Locale.ROOT);
        String compact = upper.replace("-", "").replace(" ", "");

        if (compact.length() == SEGMENT_LEN * SEGMENTS && isValidCompact(compact)) {
            return formatCompact(compact);
        }
        return upper;
    }

    public static boolean isStandardFormat(String code) {
        if (code == null || code.isBlank()) return false;
        String[] parts = code.split("-");
        if (parts.length != SEGMENTS) return false;
        for (String part : parts) {
            if (part.length() != SEGMENT_LEN || !isValidCompact(part)) return false;
        }
        return true;
    }

    private static String formatCompact(String compact12) {
        return compact12.substring(0, SEGMENT_LEN)
                + "-"
                + compact12.substring(SEGMENT_LEN, SEGMENT_LEN * 2)
                + "-"
                + compact12.substring(SEGMENT_LEN * 2);
    }

    private static boolean isValidCompact(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!isCodeChar(value.charAt(i))) return false;
        }
        return true;
    }

    private static boolean isCodeChar(char c) {
        for (char allowed : CODE_CHARS) {
            if (allowed == c) return true;
        }
        return false;
    }
}
