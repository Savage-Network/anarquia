package com.minecraft.game.bukkit.display;

import com.minecraft.game.Core;
import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/** Header/footer da tab list (mesmo padrao do lobby). */
public final class GameTabList {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

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
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) text.append('\n');
            text.append(replace(lines.get(i), player));
        }
        return LEGACY.deserialize(text.toString());
    }

    private static String replace(String line, Player player) {
        if (line == null) return "";
        Account account = Core.getAccountController().of(player.getUniqueId());
        String rank = "§7MEMBER";
        if (account != null) {
            RankType type = account.getRankType();
            rank = type.getColor() + type.name();
        }
        return line
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("{player}", player.getName())
                .replace("{ping}", String.valueOf(player.getPing()))
                .replace("{rank}", rank)
                .replace("{display-name}", GameSettings.DISPLAY_NAME)
                .replace("{server}", GameSettings.SERVER_MODE);
    }
}
