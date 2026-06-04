package com.minecraft.core.account.context.objects.rank.type;

import com.minecraft.core.account.context.objects.rank.type.role.RankRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Getter
@AllArgsConstructor
public enum RankType {

    MEMBER("Membro", RankRole.NONE, ChatColor.GRAY, emptyList()),
    VIP("VIP", RankRole.VIP, ChatColor.GOLD, emptyList()),
    NATIVE("Native", RankRole.VIP, ChatColor.YELLOW, emptyList()),
    EMBER("Ember", RankRole.VIP, ChatColor.RED, emptyList()),
    EMBER_PLUS("Ember+", RankRole.VIP, ChatColor.GOLD, emptyList()),
    SAVAGE("Savage", RankRole.VIP, ChatColor.DARK_GREEN, emptyList()),
    BETA("Beta", RankRole.VIP, ChatColor.BLUE, emptyList()),
    PRECIOUS("Precious", RankRole.VIP, ChatColor.DARK_PURPLE, emptyList()),
    CARNAVAL("Carnaval", RankRole.VIP, ChatColor.LIGHT_PURPLE, emptyList()),
    FERIAS("Férias", RankRole.VIP, ChatColor.GREEN, emptyList()),
    HALLOWEEN("Halloween", RankRole.VIP, ChatColor.DARK_PURPLE, emptyList()),
    NATAL("Natal", RankRole.VIP, ChatColor.RED, emptyList()),
    CREATOR("Creator", RankRole.SPECIAL, ChatColor.AQUA, emptyList()),
    CREATOR_PLUS("Creator+", RankRole.SPECIAL, ChatColor.DARK_AQUA, emptyList()),
    BUILDER("Builder", RankRole.STAFF, ChatColor.DARK_GREEN, emptyList()),
    HELPER("Helper", RankRole.STAFF, ChatColor.BLUE, emptyList()),
    MOD("Mod", RankRole.STAFF, ChatColor.DARK_PURPLE, emptyList()),
    MODPLUS("Mod+", RankRole.STAFF, ChatColor.DARK_PURPLE, emptyList()),
    ADMIN("Admin", RankRole.STAFF, ChatColor.DARK_RED, singletonList("*")),
    DIRETOR("Diretor", RankRole.STAFF, ChatColor.DARK_RED, singletonList("*")),
    CHEFE("Chefe", RankRole.STAFF, ChatColor.DARK_RED, singletonList("*")),
    VERMELHO("Vermelho", RankRole.STAFF, ChatColor.RED, singletonList("*")),
    OWNER("Owner", RankRole.STAFF, ChatColor.DARK_RED, singletonList("*"));

    private final String name;

    private final RankRole role;
    private final ChatColor color;

    private final List<String> permissions;

    public static RankType of(String name) {
        return Arrays.stream(values())
                .filter(rank -> rank.name().equalsIgnoreCase(name) || rank.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getColoredName() {
        return color + name;
    }

    public String getBoldColorName() {
        return color + "§l" + name;
    }

    public RankType bellow() {
        return this == MEMBER ? MEMBER : values()[ordinal() - 1];
    }
}
