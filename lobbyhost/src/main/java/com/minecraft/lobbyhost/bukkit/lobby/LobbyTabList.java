package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Header e footer da tab list (Paper Adventure API).
 * Linhas em {@code config.yml} com placeholders: {@code {online}}, {@code {max}},
 * {@code {player}}, {@code {rank}}, {@code {ping}}, {@code {display-name}}, {@code {server}}.
 */
public final class LobbyTabList {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

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
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) text.append('\n');
            text.append(replace(lines.get(i), player));
        }
        return LEGACY.deserialize(text.toString());
    }

    private static String replace(String line, Player player) {
        if (line == null) return "";
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        Account account = Core.getAccountController().of(player.getUniqueId());
        String rank = "§7MEMBER";
        if (account != null) {
            RankType type = account.getRankType();
            rank = type.getColor() + type.name();
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
