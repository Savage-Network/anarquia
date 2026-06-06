package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.network.EventosAccessGuard;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.util.BungeeCordUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * {@code /evento} — envia o jogador ao backend de eventos (bloqueado no proxy enquanto em dev).
 */
public final class EventoCommand implements CommandInheritor {

    @Command(name = "evento", aliases = {"eventos"}, playerOnly = true,
            description = "Conecta ao servidor de eventos",
            usage = "/evento")
    public void execute(BukkitCommandContext ctx) {
        Player player = ctx.player();
        if (EventosAccessGuard.isBlockedMode(LobbySettings.EVENTOS_SERVER)) {
            EventosAccessGuard.sendBlocked(player);
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
        ctx.reply("§dConectando ao servidor de eventos...");
        BungeeCordUtil.connect(player, Core.getJavaPlugin(), LobbySettings.EVENTOS_SERVER);
    }
}
