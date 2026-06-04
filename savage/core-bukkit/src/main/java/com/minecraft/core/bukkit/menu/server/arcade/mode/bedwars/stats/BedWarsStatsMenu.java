package com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.stats;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.stats.BedStats;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BedWarsStatsMenu extends Menu {

    private final BedMember member;

    @Setter
    private Type type = Type.CASUAL;

    public BedWarsStatsMenu(Player player, Menu last) {
        super(player, "Estatísticas: Bed Wars", last, 6);

        Account account = Core.getAccountData().of(player.getUniqueId());

        this.member = (BedMember) account.loadMember(ServerType.BEDWARS);
    }

    @Override
    public void handle() {
        clear();

        List<String> totalLore = new ArrayList<>(Arrays.asList(
                "§7Seu nível: " + member.getLevelId(),
                "§7XP: §a" + Util.formatNumber(member.getLevelXp()),
                ""
        ));

        totalLore.addAll(getInfoLore(member.getTotalMatches(), member.getTotalBedDestruction(), member.getTotalFinalKills(),
                member.getTotalKills(), member.getTotalAssists(), member.getTotalWins(), member.getTotalDefeats(), member.getTotalWinstreak(),
                member.getTotalHighWinstreak()));

        addItem(4, Item.of(Material.PAPER, "§aGeral", totalLore));

        int normalSlot = 10, versusSlot = 28;
        for (ArcadeCategory arcade : ArcadeCategory.of(ServerType.BEDWARS)) {
            int id = arcade.getSlots().get(0).ordinal();

            if (!arcade.name().contains("VERSUS")) {
                BedStats stats = member.getStats(arcade, type);

                addItem(normalSlot, Item.of(Material.BED, "§a" + arcade.getName(), getInfoLore(stats))
                        .amount(id));

                normalSlot += 2;
            } else {
                BedStats stats = member.getStats(arcade, Type.CASUAL);

                addItem(versusSlot, Item.of(Material.BED, "§a" + arcade.getName(), getInfoLore(stats))
                        .amount(id));

                versusSlot += 2;
            }
        }

        List<String> filterLore = new ArrayList<>(Collections.singletonList(""));

        for (Type type : Type.values()) {
            if (type.equals(Type.CUSTOM)) continue;

            filterLore.add((this.type.equals(type) ? "§a" + Constant.ARROW_ALT_SYMBOL + " " : "§7") + type.getName());
        }

        filterLore.addAll(Arrays.asList(
                "",
                "§eClique para alternar!"
        ));

        if (isReturnable())
            addBackButton();

        display();
    }

    protected List<String> getInfoLore(BedStats stats) {
        return getInfoLore(stats.getMatches(), stats.getBedDestruction(), stats.getFinalKills(),
                stats.getKills(), stats.getAssists(), stats.getWins(), stats.getDefeats(),
                stats.getWinStreak(), stats.getHighWinStreak());
    }

    protected List<String> getInfoLore(int matches, int bedDestruction, int finalKills, int kills, int assists, int wins, int defeats, int winStreak, int highWinStreak) {
        return Arrays.asList(
                "§7Partidas jogadas: §a" + Util.formatNumber(matches),
                "",
                "§7Camas quebradas: §a" + Util.formatNumber(bedDestruction),
                "§7Kills finais: §a" + Util.formatNumber(finalKills),
                "",
                "§7Kills: §a" + Util.formatNumber(kills),
                "§7Assistências: §a" + Util.formatNumber(assists),
                "",
                "§7Vitórias: §a" + Util.formatNumber(wins),
                "§7Derrotas: §a" + Util.formatNumber(defeats),
                "",
                "§7Winstreak: §a" + Util.formatNumber(winStreak),
                "§7Maior Winstreak: §a" + Util.formatNumber(highWinStreak)
        );
    }
}
