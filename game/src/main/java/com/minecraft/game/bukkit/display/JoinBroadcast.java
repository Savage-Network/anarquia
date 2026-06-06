package com.minecraft.game.bukkit.display;

import com.minecraft.game.account.Account;
import com.minecraft.game.message.Messages;
import com.minecraft.game.message.PlayerLanguage;
import com.minecraft.game.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/** Mensagem broadcast de entrada no servidor. */
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
