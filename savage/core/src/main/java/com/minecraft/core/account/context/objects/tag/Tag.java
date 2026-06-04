package com.minecraft.core.account.context.objects.tag;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.core.account.context.objects.tag.role.TagRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Getter
@AllArgsConstructor
public enum Tag {

    MEMBER("Membro", "Z", "§7", emptyList(), RankType.MEMBER, TagRole.NONE, ChatColor.GRAY),

    /* Custom Role Tags */
    BLUE("Azul", "§9§lA §r§9", "B", TagRole.COLORED, ChatColor.BLUE),
    YEL("Amarelo", "§e§lA §r§e", "C", TagRole.COLORED, ChatColor.YELLOW),
    ORAN("Laranja", "§6§lL §r§6", "D", TagRole.COLORED, ChatColor.GOLD),
    PURP("Roxo", "§5§lR §r§5", "E", TagRole.COLORED, ChatColor.DARK_PURPLE),
    AQUA("Ciano", "§b§lC §r§b", "G", TagRole.COLORED, ChatColor.AQUA),
    WHIT("Branco", "§f§lB §r§f", "H", TagRole.COLORED, ChatColor.WHITE),
    GRAY("Cinza", "§8§lC §r§8", "I", TagRole.COLORED, ChatColor.DARK_GRAY),

    MASKED("Camuflada", "X", "§7§k", TagRole.COLORED, ChatColor.MAGIC),
    SPECTATOR("Espectador", "X", "§7", emptyList(), RankType.MEMBER, TagRole.COLORED, ChatColor.GRAY),

    /* Vip Role Tags */
    VIP("VIP", "K", "§6§lVIP §r§6", emptyList(), RankType.VIP, TagRole.VIP, ChatColor.GOLD),
    NATIVE("Native", "J", "§e§lNATIVE §r§e", emptyList(), RankType.NATIVE, TagRole.VIP, ChatColor.YELLOW),
    EMBER("Ember", "I", "§c§lEMBER §r§c", emptyList(), RankType.EMBER, TagRole.VIP, ChatColor.RED),
    EMBER_PLUS("Ember+", "H", "§6§lEMBER§5+ §6", emptyList(), RankType.EMBER_PLUS, TagRole.VIP, ChatColor.GOLD),
    SAVAGE("Savage", "H", "§2§lSAVAGE §r§2", emptyList(), RankType.SAVAGE, TagRole.VIP, ChatColor.DARK_GREEN),

    /* Tags */
    TAG_BETA("Beta", "§1§lBETA §1", "F", TagRole.SPECIAL, ChatColor.DARK_BLUE, true, "§9§lBETA §9", "§b§lBETA §b"),
    /** Roxo §5 + itálico §o antes do apelido (mesmo benefício tier que Beta). */
    TAG_PRECIOUS("Precious", "§5§o", "N", TagRole.SPECIAL, ChatColor.DARK_PURPLE),
    TAG_CARNAVAL("Carnaval", "§6§lCARNAVAL §6", "G", TagRole.SPECIAL, ChatColor.GOLD),
    TAG_FERIAS("Férias", "§a§lFÉRIAS §a", "H", TagRole.SPECIAL, ChatColor.GREEN),
    TAG_HALLOWEEN("Halloween", "§5§lHALLOWEEN §5", "I", TagRole.SPECIAL, ChatColor.DARK_PURPLE),
    TAG_NATAL("Natal", "§c§lNATAL §c", "J", TagRole.SPECIAL, ChatColor.RED),
    TAG_CHAMPION("Champion", "§6§lCHAMPION §6", "K", TagRole.SPECIAL, ChatColor.GOLD),
    TAG_2025("2025", "§b§l2025 §b", "L", TagRole.SPECIAL, ChatColor.AQUA),
    TAG_2026("2026", "§b§l2026 §b", "M", TagRole.SPECIAL, ChatColor.AQUA),

    /* Special Role Tags */
    CREATOR("Creator", "H", "§b§lMIDIA §r§b", singletonList("criador"), RankType.CREATOR, TagRole.SPECIAL, ChatColor.AQUA),
    CREATOR_PLUS("Creator+", "G", "§3§lMIDIA+ §3", singletonList("creatorplus"), RankType.CREATOR_PLUS, TagRole.SPECIAL, ChatColor.DARK_AQUA),

    /* Staff Role Tags */
    BUILDER("Builder", "F", "§2§lBUILDER §r§2", emptyList(), RankType.BUILDER, TagRole.STAFF, ChatColor.DARK_GREEN),
    HELPER("Helper", "E", "§9§lHELPER §r§9", emptyList(), RankType.HELPER, TagRole.STAFF, ChatColor.BLUE),
    MOD("Mod", "D", "§5§lMOD §r§5", emptyList(), RankType.MOD, TagRole.STAFF, ChatColor.DARK_PURPLE),
    MODPLUS("Mod+", "C", "§5§lMOD§5+ §r§5", emptyList(), RankType.MODPLUS, TagRole.STAFF, ChatColor.DARK_PURPLE),
    ADMIN("Admin", "B", "§4§lADMIN §r§4", emptyList(), RankType.ADMIN, TagRole.STAFF, ChatColor.DARK_RED),
    DIRETOR("Diretor", "A", "§4§lDIRETOR §r§4", emptyList(), RankType.DIRETOR, TagRole.STAFF, ChatColor.DARK_RED),
    VERMELHO("Vermelho", "1", "§c§o", emptyList(), RankType.VERMELHO, TagRole.STAFF, ChatColor.RED),
    TAG_OWNER("Owner",
            "0",
            "§4§lCEO§f✦§4§o",
            emptyList(),
            RankType.OWNER,
            TagRole.STAFF,
            ChatColor.DARK_RED,
            Arrays.asList(
                    "§4§lCEO§f✦§4§o",
                    "§4§lCEO§e✦§4§o",
                    "§4§lCEO§d✦§4§o",
                    "§4§lCEO§c✦§4§o",
                    "§4§lCEO§b✦§4§o",
                    "§4§lCEO§a✦§4§o",
                    "§4§lCEO§9✦§4§o",
                    "§4§lCEO§8✦§4§o",
                    "§4§lCEO§7✦§4§o",
                    "§4§lCEO§6✦§4§o",
                    "§4§lCEO§5✦§4§o",
                    "§4§lCEO§4✦§4§o",
                    "§4§lCEO§3✦§4§o",
                    "§4§lCEO§2✦§4§o",
                    "§4§lCEO§1✦§4§o",
                    "§4§lCEO§0✦§4§o"
            ));

    private final String name, order, prefix;

    private final List<String> surnames;

    private final RankType source;
    private final TagRole role;

    private final ChatColor color;

    private final List<UUID> onlyThese;
    
    private final boolean animated;
    private final String alternatePrefix;
    private final String thirdPrefix;

    private final List<String> animationFrames;

    /* Custom Tag Constructor */
    Tag(String name, String order, String prefix, List<String> surnames, RankType source, TagRole role, ChatColor color) {
        this(name, order, prefix, surnames, source, role, color, new ArrayList<>(), false, null, null, emptyList());
    }

    /* Staff tag animada por quadros arbitrários */
    Tag(String name, String order, String prefix, List<String> surnames, RankType source, TagRole role, ChatColor color, List<String> animationFrames) {
        this(name, order, prefix, surnames, source, role, color, new ArrayList<>(), true, null, null, animationFrames);
    }

    Tag(String name, String prefix, TagRole role, ChatColor color) {
        this(name, prefix, "Y", role, color, false, null, null);
    }

    Tag(String name, String prefix, String order, TagRole role, ChatColor color) {
        this(name, order, prefix, emptyList(), RankType.MEMBER, role, color, new ArrayList<>(), false, null, null, emptyList());
    }
    
    /* Animated Tag Constructor - 6 parameters to avoid conflict */
    Tag(String name, String prefix, String order, TagRole role, ChatColor color, boolean animated, String alternatePrefix) {
        this(name, order, prefix, emptyList(), RankType.MEMBER, role, color, new ArrayList<>(), animated, alternatePrefix, null, emptyList());
    }
    
    /* Animated Tag Constructor with 3 colors - 7 parameters */
    Tag(String name, String prefix, String order, TagRole role, ChatColor color, boolean animated, String alternatePrefix, String thirdPrefix) {
        this(name, order, prefix, emptyList(), RankType.MEMBER, role, color, new ArrayList<>(), animated, alternatePrefix, thirdPrefix, emptyList());
    }

    public static Tag of(String name) {
        return Arrays.stream(values())
                .filter(tag -> tag.name().equalsIgnoreCase(name) 
                        || tag.getName().equalsIgnoreCase(name) 
                        || (name.equalsIgnoreCase("beta") && tag == TAG_BETA)
                        || (name.equalsIgnoreCase("precious") && tag == TAG_PRECIOUS)
                        || ((name.equalsIgnoreCase("ceo") || name.equalsIgnoreCase("owner")) && tag == TAG_OWNER)
                        || tag.getSurnames().stream().anyMatch(surName -> surName.equalsIgnoreCase(name)))
                .findFirst()
                .orElse(null);
    }

    public static Tag of(RankType source) {
//        if (source.equals(RankType.CHIEF)) return ADMIN;

        return Arrays.stream(values())
                .filter(tag -> tag.getSource().equals(source))
                .findFirst()
                .orElse(null);
    }

    public String getByPrefix(TagPrefix prefix) {
        if (this == SPECTATOR || this == MEMBER || this == VERMELHO) return getPrefix();

        String prefixRegex = getPrefix().split(" ")[0];

        String result = prefixRegex;

        switch (prefix) {
            case DEFAULT: {
                result = getPrefix();
                break;
            }
            case COLOR: {
                result = getColor().toString();
                break;
            }
            case BRACKETS: {
                String name = isRole(TagRole.COLORED)
                        ? ChatColor.stripColor(getPrefix()).split(" ")[0]
                        : getName().toUpperCase();

                result = getColor() + "[" + name + "] ";
                break;
            }
            case BRACKETS_NORMAL_CASE: {
                String name = isRole(TagRole.COLORED)
                        ? ChatColor.stripColor(getPrefix()).split(" ")[0]
                        : getName();

                result = getColor() + "[" + name + "] ";
                break;
            }
            case DEFAULT_WHITE: {
                result = prefixRegex + " §r";
                break;
            }
            case DEFAULT_GRAY: {
                result = prefixRegex + " §r§7";
                break;
            }
        }

        return result;
    }

    public boolean isOnlyThese(UUID id) {
        return !onlyThese.isEmpty() && onlyThese.contains(id);
    }

    public boolean isRole(TagRole role) {
        return this.role.equals(role);
    }

    public String getColoredName() {
        return color + name;
    }

    public String getPermission() {
        return "tag." + name.toLowerCase();
    }
}
