package com.minecraft.lobbyhost.message;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.util.AccountSync;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LanguageService {

    private LanguageService() {}

    public static PlayerLanguage of(Account account) {
        if (account == null || account.getToggle() == null) {
            return PlayerLanguage.PT;
        }
        return PlayerLanguage.fromId(account.getToggle().getLanguage());
    }

    public static PlayerLanguage of(Player player) {
        if (player == null) return PlayerLanguage.PT;
        return of(Core.getAccountController().of(player.getUniqueId()));
    }

    public static PlayerLanguage of(CommandSender sender) {
        if (sender instanceof Player player) {
            return of(player);
        }
        return PlayerLanguage.PT;
    }

    public static void setLanguage(Account account, PlayerLanguage language) {
        if (account == null || language == null) return;
        account.getToggle().setLanguage(language.id());
        account.markDirty();
        AccountSync.saveAndBroadcast(account);
    }
}
