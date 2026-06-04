package com.minecraft.game.util;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class PlayerLookup {

    private PlayerLookup() {}

    public static Optional<Account> accountByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            Account acc = Core.getAccountController().of(online.getUniqueId());
            return Optional.ofNullable(acc);
        }
        return Optional.empty();
    }
}
