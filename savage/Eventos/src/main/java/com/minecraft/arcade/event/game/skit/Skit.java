package com.minecraft.arcade.event.game.skit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.util.list.serialization.Serialization;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Skit {

    private final String name;
    private final UUID author;

    private String data;

    private final long createdAt = System.currentTimeMillis();

    public String getAuthorName() {
        Account account = Core.getAccountController().of(author);

        return account != null ? account.getNickname() : "...";
    }

    public void apply(Player player) {
        player.getInventory().clear();

        Serialization.sendInventoryToPlayerFromBase64(player, data);

        player.sendMessage("§aO kit '" + name + "' foi adicionado no seu inventário.");
    }
}
