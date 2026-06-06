package com.minecraft.game.bukkit.chat;

import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.medal.Medal;
import com.minecraft.game.account.context.objects.toggle.TagPrefix;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.bukkit.manager.TagManager;

public final class ChatFormatter {

    private ChatFormatter() {}

    public static String formatContent(Account sender, String plainMessage) {
        if (sender.isAllowColoredChat()) {
            return "§f" + plainMessage.replace('&', '§');
        }
        return "§7" + plainMessage.replaceAll("§.", "");
    }

    public static String formatLine(Account sender, Account viewer, String content) {
        TagPrefix viewerPrefix = viewer != null ? viewer.getTagPrefix() : TagPrefix.DEFAULT;
        String tag = TagManager.getAdjustedPrefix(viewerPrefix, sender.getTag(), sender.getId());
        String medal = sender.getMedal() != Medal.NONE ? sender.getMedal().getColoredSymbol() : "";
        String clan = "";
        if (sender.getToggle().isShowClanTag() && sender.hasClan()) {
            var c = sender.getClan();
            if (c != null) clan = ClanTagFormatter.formatChatTag(c) + " ";
        }
        String nick = sender.getRankType().formatChatNickname(sender.getNickname());
        return medal + clan + tag + nick + "§7: " + content;
    }

    public static String applyMentions(String content, Iterable<org.bukkit.entity.Player> online) {
        String out = content;
        for (org.bukkit.entity.Player p : online) {
            String nick = p.getName();
            if (nick == null || nick.isBlank()) continue;
            out = out.replace("@" + nick, GameSettings.CHAT_MENTION_COLOR + "@" + nick + "§r");
        }
        return out;
    }
}
