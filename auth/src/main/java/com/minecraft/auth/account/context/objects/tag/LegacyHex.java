package com.minecraft.auth.account.context.objects.tag;

import java.util.List;

/** Converte hex (#RRGGBB) para legacy §x§R§R§G§G§B§B (Paper 1.21). */
public final class LegacyHex {

    private LegacyHex() {}

    public static String of(String hex) {
        if (hex == null || hex.isBlank()) return "";
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (h.length() != 6) return "";
        StringBuilder sb = new StringBuilder("§x");
        for (char c : h.toUpperCase().toCharArray()) {
            sb.append('§').append(c);
        }
        return sb.toString();
    }

    public static String tag(String label, String color) {
        return color + "§l" + label + " " + color;
    }

    public static String tagPlus(String label, String color, String plusColor) {
        return color + "§l" + label + plusColor + "+ " + color;
    }

    public static final String MEMBER    = of("#94A3B8");
    public static final String VIP       = of("#3DDC97");
    public static final String NATIVE    = of("#38BDF8");
    public static final String EMBER     = of("#FF8C42");
    public static final String ACCENT    = of("#818CF8");
    public static final String SAVAGE    = of("#059669");
    public static final String BETA      = of("#FFD166");
    public static final String BETA_ALT1 = of("#38BDF8");
    public static final String BETA_ALT2 = of("#C084FC");
    public static final String PRECIOUS  = of("#A855F7");
    public static final String CARNAVAL  = of("#EC4899");
    public static final String FERIAS    = of("#FBBF24");
    public static final String HALLOWEEN = of("#F97316");
    public static final String NATAL     = of("#EF4444");
    public static final String CHAMPION  = of("#EAB308");
    public static final String YEAR      = of("#22D3EE");
    public static final String CREATOR   = of("#E879F9");
    public static final String BUILDER   = of("#14B8A6");
    public static final String HELPER    = of("#3B82F6");
    public static final String MOD       = of("#06B6D4");
    public static final String ADMIN     = of("#DC2626");
    public static final String VERMELHO  = of("#FF5555");
    public static final String ZW        = of("#9D0000");
    public static final String OWNER     = of("#DC2626");

    public static final String BLUE      = of("#3B82F6");
    public static final String YELLOW    = of("#FBBF24");
    public static final String ORANGE    = of("#FF8C42");
    public static final String PURPLE    = of("#A855F7");
    public static final String AQUA      = of("#38BDF8");
    public static final String WHITE     = of("#F8FAFC");
    public static final String GRAY      = of("#94A3B8");
    public static final String DARK_GRAY = of("#64748B");

    public static final List<String> OWNER_FRAMES = List.of(
            of("#FF5555"), of("#FF8C42"), of("#FFD166"), of("#3DDC97"),
            of("#059669"), of("#38BDF8"), of("#14B8A6"), of("#3B82F6"),
            of("#1E40AF"), of("#A855F7"), of("#E879F9"), of("#F8FAFC"),
            of("#94A3B8"), of("#64748B"), of("#1E293B"), of("#DC2626")
    );
}
