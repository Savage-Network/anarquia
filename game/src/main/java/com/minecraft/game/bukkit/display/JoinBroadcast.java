package com.minecraft.game.bukkit.display;

import com.minecraft.game.account.Account;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/** Mensagem broadcast de entrada no servidor. */
public final class JoinBroadcast {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final String SEPARATOR = "§8§m===========================";

    private JoinBroadcast() {}

    public static Component create(Account account, Player player, String action) {
        String tag = account.getTag().getByPrefix(account.getTagPrefix());
        String body = tag + player.getName() + " §7" + action;
        return LEGACY.deserialize(SEPARATOR + "\n" + body + "\n" + SEPARATOR);
    }
}
