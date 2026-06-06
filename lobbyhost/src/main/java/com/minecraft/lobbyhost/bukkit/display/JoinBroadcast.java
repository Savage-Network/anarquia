package com.minecraft.lobbyhost.bukkit.display;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.message.Messages;
import com.minecraft.lobbyhost.message.PlayerLanguage;
import com.minecraft.lobbyhost.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class JoinBroadcast {

    private JoinBroadcast() {}

    public static Component create(Account account, Player player, String actionPath, PlayerLanguage lang) {
        String tag = account.getTag().getByPrefix(account.getTagPrefix());
        String separator = Messages.raw("join.separator", lang);
        String action = Messages.raw(actionPath, lang);
        Component line = ModernText.fromLegacy(tag + player.getName() + " ")
                .append(ModernText.parse(action));
        return ModernText.parse(separator + "\n")
                .append(line)
                .append(ModernText.parse("\n" + separator));
    }
}
