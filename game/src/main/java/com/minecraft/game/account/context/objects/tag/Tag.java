package com.minecraft.game.account.context.objects.tag;

import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.account.context.objects.toggle.TagPrefix;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Catalogo completo de tags. Espelha {@code com.minecraft.core.account.tag.Tag}
 * do Savage. Cada constante carrega:
 *
 * <ul>
 *   <li>{@code displayName} - nome amigavel exibido</li>
 *   <li>{@code order} - ordenacao do team scoreboard ({@code 0} = Owner topo)</li>
 *   <li>{@code prefix} - texto antes do nick (com cores legacy {@code §})</li>
 *   <li>{@code color} - cor base usada em {@link TagPrefix#COLOR}</li>
 *   <li>{@code source} - {@link RankType} que libera a tag (VIP / STAFF)</li>
 *   <li>{@code role} - regra de quem pode usar (ver {@link TagRole})</li>
 *   <li>{@code animated} + {@code animationFrames} - tags animadas (Owner)</li>
 *   <li>{@code alternatePrefix}/{@code thirdPrefix} - tags 2/3 cores (BETA)</li>
 *   <li>{@code surnames} - aliases reconhecidos por {@link #of(String)}</li>
 *   <li>{@code onlyThese} - UUIDs autorizados em {@link TagRole#ONLY_THESE}</li>
 * </ul>
 */
@Getter
public enum Tag {

    /* ---------- NONE (sem rank) ---------- */
    MEMBER ("Membro", "Z", LegacyHex.MEMBER, LegacyHex.MEMBER, RankType.MEMBER, TagRole.NONE),

    /* ---------- VIP ---------- */
    VIP        ("VIP",      "V", LegacyHex.tag("VIP", LegacyHex.VIP),          LegacyHex.VIP,     RankType.VIP,        TagRole.VIP),
    NATIVE     ("Native",   "U", LegacyHex.tag("NATIVE", LegacyHex.NATIVE),    LegacyHex.NATIVE,  RankType.NATIVE,     TagRole.VIP),
    EMBER      ("Ember",    "T", LegacyHex.tag("EMBER", LegacyHex.EMBER),      LegacyHex.EMBER,   RankType.EMBER,      TagRole.VIP),
    EMBER_PLUS ("Ember+",   "S", LegacyHex.tagPlus("EMBER", LegacyHex.EMBER, LegacyHex.ACCENT), LegacyHex.EMBER, RankType.EMBER_PLUS, TagRole.VIP),
    SAVAGE     ("Savage",   "R", LegacyHex.tag("SAVAGE", LegacyHex.SAVAGE),    LegacyHex.SAVAGE,  RankType.SAVAGE,     TagRole.VIP),

    /* ---------- SPECIAL ---------- */
    TAG_BETA      ("Beta",      "K", LegacyHex.tag("BETA", LegacyHex.BETA), LegacyHex.BETA, RankType.BETA, TagRole.SPECIAL,
                   /*animated*/false, null, LegacyHex.tag("BETA", LegacyHex.BETA_ALT1), LegacyHex.tag("BETA", LegacyHex.BETA_ALT2),
                   /*surnames*/Collections.singletonList("beta")),
    TAG_PRECIOUS  ("Precious",  "K", LegacyHex.tag("PRECIOUS", LegacyHex.PRECIOUS), LegacyHex.PRECIOUS, RankType.PRECIOUS, TagRole.SPECIAL,
                   /*surnames*/Collections.singletonList("precious")),
    TAG_CARNAVAL  ("Carnaval",  "L", LegacyHex.tag("CARNAVAL", LegacyHex.CARNAVAL), LegacyHex.CARNAVAL, RankType.MEMBER, TagRole.SPECIAL),
    TAG_FERIAS    ("Ferias",    "L", LegacyHex.tag("FERIAS", LegacyHex.FERIAS), LegacyHex.FERIAS, RankType.MEMBER, TagRole.SPECIAL),
    TAG_HALLOWEEN ("Halloween", "L", LegacyHex.tag("HALLOWEEN", LegacyHex.HALLOWEEN), LegacyHex.HALLOWEEN, RankType.MEMBER, TagRole.SPECIAL),
    TAG_NATAL     ("Natal",     "L", LegacyHex.tag("NATAL", LegacyHex.NATAL), LegacyHex.NATAL, RankType.MEMBER, TagRole.SPECIAL),
    TAG_CHAMPION  ("Champion",  "J", LegacyHex.tag("CHAMPION", LegacyHex.CHAMPION), LegacyHex.CHAMPION, RankType.MEMBER, TagRole.SPECIAL),
    TAG_2025      ("2025",      "M", LegacyHex.tag("2025", LegacyHex.YEAR), LegacyHex.YEAR, RankType.MEMBER, TagRole.SPECIAL),
    TAG_2026      ("2026",      "M", LegacyHex.tag("2026", LegacyHex.YEAR), LegacyHex.YEAR, RankType.MEMBER, TagRole.SPECIAL),
    CREATOR       ("Creator",   "I", LegacyHex.tag("CREATOR", LegacyHex.CREATOR), LegacyHex.CREATOR, RankType.MEMBER, TagRole.SPECIAL,
                   Arrays.asList("creator", "criador")),
    CREATOR_PLUS  ("Creator+",  "H", LegacyHex.tagPlus("CREATOR", LegacyHex.CREATOR, LegacyHex.ACCENT), LegacyHex.CREATOR, RankType.MEMBER, TagRole.SPECIAL,
                   Arrays.asList("creator+", "creatorplus")),

    /* ---------- STAFF ---------- */
    BUILDER  ("Builder",  "G", LegacyHex.tag("BUILDER", LegacyHex.BUILDER), LegacyHex.BUILDER, RankType.BUILDER, TagRole.STAFF),
    HELPER   ("Helper",   "F", LegacyHex.tag("HELPER", LegacyHex.HELPER), LegacyHex.HELPER, RankType.HELPER, TagRole.STAFF),
    MOD      ("Mod",      "E", LegacyHex.tag("MOD", LegacyHex.MOD), LegacyHex.MOD, RankType.MOD, TagRole.STAFF),
    MODPLUS  ("Mod+",     "D", LegacyHex.tagPlus("MOD", LegacyHex.MOD, LegacyHex.MOD_PLUS), LegacyHex.MOD_PLUS, RankType.MODPLUS, TagRole.STAFF),
    ADMIN    ("Admin",    "C", LegacyHex.tag("ADMIN", LegacyHex.ADMIN), LegacyHex.ADMIN, RankType.ADMIN, TagRole.STAFF),
    DIRETOR  ("Diretor",  "B", LegacyHex.tag("DIRETOR", LegacyHex.ADMIN), LegacyHex.ADMIN, RankType.DIRETOR, TagRole.STAFF),
    VERMELHO ("Vermelho", "A", LegacyHex.tag("VERMELHO", LegacyHex.VERMELHO), LegacyHex.VERMELHO, RankType.VERMELHO, TagRole.STAFF),

    /* ---------- LULU / ZW ---------- */
    TAG_LULU("LULU", "#", LegacyHex.tag("LULU", LegacyHex.LULU), LegacyHex.LULU,
            RankType.LULU, TagRole.STAFF, Collections.singletonList("lulu")),

    TAG_ZW("ZW", "!", LegacyHex.tag("ZW", LegacyHex.ZW), LegacyHex.ZW,
            RankType.ZW, TagRole.STAFF, Collections.singletonList("zw")),

    /* ---------- OWNER ---------- */
    TAG_OWNER("Owner", "0", LegacyHex.OWNER, LegacyHex.OWNER, RankType.OWNER, TagRole.STAFF,
              /*animated*/ true, LegacyHex.OWNER_FRAMES, null, null,
              Arrays.asList("owner", "ceo"));

    private final String displayName;
    private final String order;        // prefixo do team scoreboard
    private final String prefix;
    private final String color;
    private final RankType source;
    private final TagRole role;
    private final boolean animated;
    private final List<String> animationFrames;
    private final String alternatePrefix;
    private final String thirdPrefix;
    private final List<String> surnames;
    private final List<UUID> onlyThese;

    /* ----- construtores ----- */

    Tag(String displayName, String order, String prefix, String color,
        RankType source, TagRole role) {
        this(displayName, order, prefix, color, source, role,
             false, null, null, null, Collections.emptyList(), Collections.emptyList());
    }

    Tag(String displayName, String order, String prefix, String color,
        RankType source, TagRole role, List<String> surnames) {
        this(displayName, order, prefix, color, source, role,
             false, null, null, null, surnames, Collections.emptyList());
    }

    Tag(String displayName, String order, String prefix, String color,
        RankType source, TagRole role,
        boolean animated, List<String> animationFrames,
        String alternatePrefix, String thirdPrefix,
        List<String> surnames) {
        this(displayName, order, prefix, color, source, role,
             animated, animationFrames, alternatePrefix, thirdPrefix,
             surnames, Collections.emptyList());
    }

    Tag(String displayName, String order, String prefix, String color,
        RankType source, TagRole role,
        boolean animated, List<String> animationFrames,
        String alternatePrefix, String thirdPrefix,
        List<String> surnames, List<UUID> onlyThese) {
        this.displayName = displayName;
        this.order = order;
        this.prefix = prefix;
        this.color = color;
        this.source = source;
        this.role = role;
        this.animated = animated;
        this.animationFrames = animationFrames == null
                ? Collections.emptyList() : animationFrames;
        this.alternatePrefix = alternatePrefix;
        this.thirdPrefix = thirdPrefix;
        this.surnames = surnames == null ? Collections.emptyList() : surnames;
        this.onlyThese = onlyThese == null ? Collections.emptyList() : onlyThese;
    }

    /* ----- helpers ----- */

    public boolean isAnimated() {
        return animated && animationFrames != null && !animationFrames.isEmpty();
    }

    public boolean hasAlternates() {
        return alternatePrefix != null || thirdPrefix != null;
    }

    public boolean isRole(TagRole r) {
        return this.role == r;
    }

    public boolean isStaffTag() {
        return role == TagRole.STAFF;
    }

    /** Tag com permissao wildcard (*) — ZW e LULU. */
    public boolean hasWildcardAccess() {
        return this == TAG_ZW || this == TAG_LULU;
    }

    public static boolean isWildcardPermissionKey(String key) {
        if (key == null || key.isBlank()) return false;
        if ("*".equals(key)) return true;
        return "tag.tag_zw".equalsIgnoreCase(key) || "tag.tag_lulu".equalsIgnoreCase(key);
    }

    /** Nos de staff chat concedidos a quem possui tag staff. */
    public static final java.util.List<String> STAFF_CHAT_NODES = java.util.List.of(
            "game.staffchat",
            "lobbyhost.staffchat",
            "proxyhost.staffchat",
            "game.staff"
    );

    public static boolean isStaffChatNode(String key) {
        if (key == null || key.isBlank()) return false;
        for (String node : STAFF_CHAT_NODES) {
            if (com.minecraft.game.account.context.objects.rank.RankType.matchesPermission(node, key)) {
                return true;
            }
        }
        return false;
    }

    public String getPermission() {
        return "tag." + name().toLowerCase();
    }

    public String getColoredName() {
        return color + displayName;
    }

    /** Aplica o modo {@link TagPrefix} sobre {@link #prefix}. */
    public String getByPrefix(TagPrefix style) {
        if (style == null) style = TagPrefix.DEFAULT;
        return switch (style) {
            case DEFAULT             -> prefix;
            case COLOR               -> color;
            case BRACKETS            -> color + "[" + displayName.toUpperCase() + "] ";
            case BRACKETS_NORMAL_CASE -> color + "[" + displayName + "] ";
            case DEFAULT_WHITE       -> prefix + "§r";
            case DEFAULT_GRAY        -> prefix + "§r" + LegacyHex.MEMBER;
        };
    }

    /* ----- lookup ----- */

    /** Mapeia rank -> tag VIP/STAFF correspondente. */
    public static Tag of(RankType type) {
        if (type == null) return MEMBER;
        for (Tag tag : values()) {
            if (tag.source == type && (tag.role == TagRole.VIP || tag.role == TagRole.STAFF)) {
                return tag;
            }
        }
        return MEMBER;
    }

    /** Resolve por nome/surname (case-insensitive). */
    public static Tag of(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;

        for (Tag tag : values()) {
            if (tag.name().equalsIgnoreCase(trimmed)) return tag;
            if (tag.displayName.equalsIgnoreCase(trimmed)) return tag;
            for (String sn : tag.surnames) {
                if (sn.equalsIgnoreCase(trimmed)) return tag;
            }
        }
        // Aceita prefixo "tag_" omitido (carnaval -> TAG_CARNAVAL)
        for (Tag tag : values()) {
            if (tag.name().equalsIgnoreCase("TAG_" + trimmed)) return tag;
        }
        return null;
    }
}
