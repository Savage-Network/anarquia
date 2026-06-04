package com.minecraft.arcade.event.command;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.game.sumo.SumoMapGenerator;
import com.minecraft.arcade.event.game.variable.Variable;

public class SumoCommand implements CommandInheritor {

    @Command(
            name = "sumo",
            rank = RankType.ADMIN
    )
    public void sumo(BukkitCommandContext context) {
        CommandSender sender = context.getSender();

        Game game = Event.getGame();

        if (game.isSumo()) {
            sender.send("§cO modo Sumô já está ativo.");
            return;
        }

        sender.send("§ePreparando o modo §6§lSumô§e...");

        // 1. Ativa o modo sumô e gera o mapa
        Variable.SUMO.setValue(true);

        // 2. Sem dano direto, sem drops, sem comida, sem build
        Variable.DAMAGE_PLAYER.setValue(false);
        Variable.DAMAGE_NORMAL.setValue(false);
        Variable.DROPS.setValue(false);
        Variable.FOOD.setValue(false);
        Variable.BUILD.setValue(false);
        Variable.BUCKET.setValue(false);

        // 3. Quem cair vira spec (não sai do servidor)
        Variable.SPEC.setValue(true);

        // 4. Morte ativa (cair = morrer)
        Variable.SET_DIE.setValue(true);

        // 5. Libera entrada de jogadores
        Variable.JOIN.setValue(true);

        // 6. Nome do jogo na sidebar
        Variable.GAME_NAME.setValue("&6&lSUMÔ");

        sender.send("§aO modo §6§lSumô §afoi ativado! Mapa gerado.");
        sender.send("§7Spawn: §e" + formatLoc(game.getSpawn()));
        sender.send("§7Use §e/start §7para iniciar o evento.");

        game.send("§6§lSUMÔ §eestá sendo preparado! Aguarde o início.");
    }

    private String formatLoc(org.bukkit.Location loc) {
        if (loc == null) return "N/A";
        return "%.0f, %.0f, %.0f".formatted(loc.getX(), loc.getY(), loc.getZ());
    }
}
