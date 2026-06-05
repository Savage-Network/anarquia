package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.GamePlugin;
import com.minecraft.game.GameSettings;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.semi.CombatTagManager;
import com.minecraft.game.util.BungeeCordUtil;
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

        if (CombatTagManager.isTagged(player)) {
            ctx.reply(CombatTagManager.blockedMessage(player));
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
        ctx.reply("§dConectando ao servidor de eventos...");
        BungeeCordUtil.connect(player, GamePlugin.getInstance(), GameSettings.EVENTOS_SERVER);
    }
}
