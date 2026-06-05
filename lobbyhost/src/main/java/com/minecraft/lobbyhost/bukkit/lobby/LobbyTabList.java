package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Header e footer da tab list (MiniMessage + hex).
 */
public final class LobbyTabList {

    private LobbyTabList() {}

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
        List<String> lines = "header".equals(section) ? LobbySettings.TAB_HEADER : LobbySettings.TAB_FOOTER;
        Component combined = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) combined = combined.append(Component.newline());
            String replaced = replace(lines.get(i), player);
            // MiniMessage — suporta gradientes, hex e todas as tags modernas
            combined = combined.append(ModernText.parse(replaced));
        }
        return combined;
    }

    private static String replace(String line, Player player) {
        if (line == null) return "";
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        Account account = Core.getAccountController().of(player.getUniqueId());
        String rank = "MEMBER";
        if (account != null) {
            RankType type = account.getRankType();
            rank = type.name();
        }

        return line
                .replace("{online}", String.valueOf(online))
                .replace("{max}", String.valueOf(max))
                .replace("{player}", player.getName())
                .replace("{ping}", String.valueOf(player.getPing()))
                .replace("{rank}", rank)
                .replace("{display-name}", LobbySettings.DISPLAY_NAME)
                .replace("{server}", LobbySettings.SERVER_MODE);
    }
}
