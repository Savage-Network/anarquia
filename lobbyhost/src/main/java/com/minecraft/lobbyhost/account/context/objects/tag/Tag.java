package com.minecraft.lobbyhost.account.context.objects.tag;

import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.account.context.objects.toggle.TagPrefix;
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
    MEMBER ("Membro", "Z", "§7",            "§7", RankType.MEMBER, TagRole.NONE),

    /* ---------- VIP ---------- */
    VIP        ("VIP",      "V", "§a§lVIP §a",          "§a", RankType.VIP,        TagRole.VIP),
    NATIVE     ("Native",   "U", "§b§lNATIVE §b",       "§b", RankType.NATIVE,     TagRole.VIP),
    EMBER      ("Ember",    "T", "§6§lEMBER §6",        "§6", RankType.EMBER,      TagRole.VIP),
    EMBER_PLUS ("Ember+",   "S", "§6§lEMBER§5+ §6",     "§6", RankType.EMBER_PLUS, TagRole.VIP),
    SAVAGE     ("Savage",   "R", "§2§lSAVAGE §2",       "§2", RankType.SAVAGE,     TagRole.VIP),

    /* ---------- SPECIAL ---------- */
    TAG_BETA      ("Beta",      "K", "§e§lBETA §e",       "§e", RankType.BETA,     TagRole.SPECIAL,
                   /*animated*/false, null,  "§b§lBETA §b", "§d§lBETA §d",
                   /*surnames*/Collections.singletonList("beta")),
    TAG_PRECIOUS  ("Precious",  "K", "§5§lPRECIOUS §5",   "§5", RankType.PRECIOUS, TagRole.SPECIAL,
                   /*surnames*/Collections.singletonList("precious")),
    TAG_CARNAVAL  ("Carnaval",  "L", "§d§lCARNAVAL §d",   "§d", RankType.MEMBER,   TagRole.SPECIAL),
    TAG_FERIAS    ("Ferias",    "L", "§e§lFERIAS §e",     "§e", RankType.MEMBER,   TagRole.SPECIAL),
    TAG_HALLOWEEN ("Halloween", "L", "§6§lHALLOWEEN §6",  "§6", RankType.MEMBER,   TagRole.SPECIAL),
    TAG_NATAL     ("Natal",     "L", "§c§lNATAL §c",      "§c", RankType.MEMBER,   TagRole.SPECIAL),
    TAG_CHAMPION  ("Champion",  "J", "§e§lCHAMPION §e",   "§e", RankType.MEMBER,   TagRole.SPECIAL),
    TAG_2025      ("2025",      "M", "§b§l2025 §b",       "§b", RankType.MEMBER,   TagRole.SPECIAL),
    TAG_2026      ("2026",      "M", "§b§l2026 §b",       "§b", RankType.MEMBER,   TagRole.SPECIAL),
    CREATOR       ("Creator",   "I", "§d§lCREATOR §d",    "§d", RankType.MEMBER,   TagRole.SPECIAL,
                   Arrays.asList("creator", "criador")),
    CREATOR_PLUS  ("Creator+",  "H", "§d§lCREATOR§5+ §d", "§d", RankType.MEMBER,   TagRole.SPECIAL,
                   Arrays.asList("creator+", "creatorplus")),

    /* ---------- STAFF ---------- */
    BUILDER  ("Builder",  "G", "§3§lBUILDER §3",  "§3", RankType.BUILDER,  TagRole.STAFF),
    HELPER   ("Helper",   "F", "§9§lHELPER §9",   "§9", RankType.HELPER,   TagRole.STAFF),
    MOD      ("Mod",      "E", "§3§lMOD §3",      "§3", RankType.MOD,      TagRole.STAFF),
    MODPLUS  ("Mod+",     "D", "§3§lMOD§5+ §3",   "§3", RankType.MODPLUS,  TagRole.STAFF),
    ADMIN    ("Admin",    "C", "§4§lADMIN §4",    "§4", RankType.ADMIN,    TagRole.STAFF),
    DIRETOR  ("Diretor",  "B", "§4§lDIRETOR §4",  "§4", RankType.DIRETOR,  TagRole.STAFF),
    VERMELHO ("Vermelho", "A", "§c§o", "§c", RankType.VERMELHO, TagRole.STAFF),

    /* ---------- OWNER ---------- */
    TAG_OWNER("Owner", "0", "§o§4", "§o§4", RankType.OWNER, TagRole.STAFF,
              /*animated*/ true,
              Arrays.asList(
                  "§o§c",
                  "§o§6",
                  "§o§e",
                  "§o§a",
                  "§o§2",
                  "§o§b",
                  "§o§3",
                  "§o§9",
                  "§o§1",
                  "§o§5",
                  "§o§d",
                  "§o§f",
                  "§o§7",
                  "§o§8",
                  "§o§0"
              ),
              null, null,
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
            case DEFAULT_GRAY        -> prefix + "§r§7";
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
