package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;

public final class AnuncioCommand implements CommandInheritor {

    @Command(name = "anuncio", aliases = {"broadcast", "alerta"}, playerOnly = false,
            permission = "lobbyhost.staff",
            description = "Anuncio global na rede via Redis",
            usage = "/anuncio <mensagem>")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            ctx.reply("§eUso: /anuncio <mensagem>");
            return;
        }
        String msg = String.join(" ", ctx.getArgs());
        String sender = ctx.isPlayer() ? ctx.player().getName() : "Console";
        publishAlert(sender, msg);
        ctx.reply("§aAnuncio enviado para a rede.");
    }

    public static void publishAlert(String senderLabel, String message) {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) {
            Core.getLogger().warning("Redis off — anuncio apenas local.");
            var plugin = Core.getJavaPlugin();
            if (plugin != null) {
                String line = "§6§l" + Constant.SERVER_NAME + " §8» §f" + message;
                plugin.getServer().getOnlinePlayers()
                        .forEach(p -> p.sendMessage(net.kyori.adventure.text.Component.text(line)));
            }
            return;
        }
        String payload = senderLabel + "|" + message.replace('|', ' ');
        Core.getRedis().publish(Constant.REDIS_NETWORK_ALERT_CHANNEL, payload);
    }
}
