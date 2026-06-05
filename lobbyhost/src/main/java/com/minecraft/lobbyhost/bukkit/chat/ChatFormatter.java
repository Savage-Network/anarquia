package com.minecraft.lobbyhost.bukkit.chat;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.medal.Medal;
import com.minecraft.lobbyhost.account.context.objects.toggle.TagPrefix;
import com.minecraft.lobbyhost.api.clan.ClanTagFormatter;
import com.minecraft.lobbyhost.LobbySettings;

public final class ChatFormatter {

    private ChatFormatter() {}

    public static String formatContent(Account sender, String plainMessage) {
        if (sender.isAllowColoredChat()) {
            return "§f" + colorize(plainMessage);
        }
        return "§7" + stripColorCodes(plainMessage);
    }

    public static String formatLine(Account sender, Account viewer, String content) {
        TagPrefix viewerPrefix = viewer != null ? viewer.getTagPrefix() : null;
        String tag = sender.getTag().getByPrefix(viewerPrefix);
        String medal = sender.getMedal() != Medal.NONE ? sender.getMedal().getColoredSymbol() : "";
        String clan = "";
        if (sender.getToggle().isShowClanTag() && sender.hasClan()) {
            var c = sender.getClan();
            if (c != null) clan = ClanTagFormatter.formatChatTag(c) + " ";
        }
        return medal + clan + tag + sender.getNickname() + "§7: " + content;
    }

    public static String applyMentions(String content, Iterable<org.bukkit.entity.Player> online) {
        String out = content;
        for (org.bukkit.entity.Player p : online) {
            String nick = p.getName();
            if (nick == null || nick.isBlank()) continue;
            out = out.replace("@" + nick, LobbySettings.CHAT_MENTION_COLOR + "@" + nick + "§r");
        }
        return out;
    }

    private static String colorize(String input) {
        return input.replace('&', '§');
    }

    private static String stripColorCodes(String input) {
        return input.replaceAll("§.", "").replace('&', ' ');
    }
}
