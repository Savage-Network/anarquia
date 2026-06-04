package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class RtpCommand implements CommandInheritor {

    @Command(name = "rtp", aliases = {"randomtp", "teleportearb"}, playerOnly = true,
            description = "Teleporta você para um local aleatório e seguro no mundo", usage = "/rtp")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }

        Player player = ctx.player();

        // Cooldown de 30 segundos (Staff/Op isento)
        if (!player.isOp() && account.hasCooldown("rtp-cmd")) {
            long left = account.getCooldownRemaining("rtp-cmd");
            ctx.reply("§cAguarde " + String.format("%.1f", left / 1000.0) + "s para usar o RTP novamente.");
            return;
        }

        ctx.reply("§aProcurando um local seguro para teleporte...");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

        findSafeLocation(player, player.getWorld(), 0, loc -> {
            if (!player.isOnline()) return;

            if (loc == null) {
                player.sendMessage("§cNão foi possível encontrar um local seguro após várias tentativas. Tente novamente!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            // Define cooldown
            if (!player.isOp()) {
                account.setCooldown("rtp-cmd", 30000L); // 30s
            }

            player.teleport(loc);
            player.sendMessage("§aTeleportado com sucesso para §fX: " + loc.getBlockX() + ", Y: " + loc.getBlockY() + ", Z: " + loc.getBlockZ() + "§a!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
        });
    }

    private void findSafeLocation(Player player, World world, int attempts, Consumer<Location> callback) {
        if (attempts >= 5) {
            callback.accept(null);
            return;
        }

        // Gera coordenadas entre -4000 e 4000
        int x = ThreadLocalRandom.current().nextInt(-4000, 4000);
        int z = ThreadLocalRandom.current().nextInt(-4000, 4000);

        // Carrega o chunk assincronamente para evitar lag
        world.getChunkAtAsync(x >> 4, z >> 4).thenAccept(chunk -> {
            // Executa de volta na thread principal caso venha em outra (Paper garante na principal, mas por segurança rodamos com Scheduler se necessário)
            Bukkit.getScheduler().runTask(com.minecraft.game.Core.getJavaPlugin(), () -> {
                if (!player.isOnline()) return;

                int y = world.getHighestBlockYAt(x, z);
                Block block = world.getBlockAt(x, y, z);
                Block feet = world.getBlockAt(x, y + 1, z);
                Block head = world.getBlockAt(x, y + 2, z);

                Material type = block.getType();
                if (type.isSolid() 
                        && type != Material.LAVA 
                        && type != Material.WATER 
                        && type != Material.MAGMA_BLOCK 
                        && type != Material.CACTUS
                        && type != Material.FIRE 
                        && type != Material.SOUL_FIRE
                        && feet.getType().isAir() 
                        && head.getType().isAir()) {
                    
                    callback.accept(new Location(world, x + 0.5, y + 1.0, z + 0.5));
                } else {
                    findSafeLocation(player, world, attempts + 1, callback);
                }
            });
        });
    }
}
