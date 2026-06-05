package com.minecraft.game.bukkit.display;

import com.minecraft.game.Core;
import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.network.NetworkOnlineCache;
import com.minecraft.game.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/** Header e footer da tab list do game (MiniMessage + hex). */
public final class GameTabList {

    private GameTabList() {}

    public static void apply(Player player) {
        if (player == null) return;
        player.sendPlayerListHeader(build(player, "header"));
        player.sendPlayerListFooter(build(player, "footer"));
    }

    public static void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            apply(player);
        }
    }

    private static Component build(Player player, String section) {
        List<String> lines = "header".equals(section) ? GameSettings.TAB_HEADER : GameSettings.TAB_FOOTER;
        Component combined = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) combined = combined.append(Component.newline());
            combined = combined.append(ModernText.parse(replace(lines.get(i), player)));
        }
        return combined;
    }

    private static String replace(String line, Player player) {
        if (line == null) return "";
        int online = NetworkOnlineCache.get();
        Account account = Core.getAccountController().of(player.getUniqueId());
        String rank = RankType.MEMBER.name();
        if (account != null) {
            rank = account.getRankType().name();
        }

        return line
                .replace("{online}", String.valueOf(online))
                .replace("{player}", player.getName())
                .replace("{ping}", String.valueOf(player.getPing()))
                .replace("{rank}", rank)
                .replace("{display-name}", GameSettings.DISPLAY_NAME)
                .replace("{server}", GameSettings.SERVER_MODE);
    }
}
