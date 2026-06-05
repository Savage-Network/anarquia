package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.GamePlugin;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.semi.CombatTagManager;
import com.minecraft.game.util.BungeeCordUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * /lobby e /l — envia o jogador de volta para o servidor lobby.
 * Bloqueado durante combate.
 */
public final class LobbyCommand implements CommandInheritor {

    private static final String LOBBY_SERVER = "lobby";

    @Command(name = "lobby", aliases = {"l", "hub"}, playerOnly = true,
            description = "Volta para o lobby", usage = "/lobby")
    public void lobby(BukkitCommandContext ctx) {
        Player player = ctx.player();

        if (CombatTagManager.isTagged(player)) {
            ctx.reply(CombatTagManager.blockedMessage(player));
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
        ctx.reply("§aConectando ao lobby...");
        BungeeCordUtil.connect(player, GamePlugin.getInstance(), LOBBY_SERVER);
    }
}
