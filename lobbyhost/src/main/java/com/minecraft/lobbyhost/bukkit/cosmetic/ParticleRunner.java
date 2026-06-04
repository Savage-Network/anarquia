package com.minecraft.lobbyhost.bukkit.cosmetic;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class ParticleRunner extends BukkitRunnable {

    private double angle = 0;

    @Override
    public void run() {
        angle += Math.PI / 16; // Incremento angular suave (roda a cada 2 ticks)
        if (angle >= 2 * Math.PI) {
            angle = 0;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account == null) continue;

            ParticleEffect effect = account.getActiveParticle();
            if (effect == null) continue;

            Location loc = player.getLocation();
            Particle particle = effect.getParticle();

            switch (effect) {
                case FLAME_AURA:
                    // Aura ascendente espiralada
                    for (int i = 0; i < 3; i++) {
                        double h = (System.currentTimeMillis() / 300.0 + i * 0.6) % 2.0;
                        double r = 0.45;
                        double x = r * Math.cos(angle + i * (2 * Math.PI / 3));
                        double z = r * Math.sin(angle + i * (2 * Math.PI / 3));
                        player.getWorld().spawnParticle(particle, loc.clone().add(x, h, z), 1, 0, 0, 0, 0);
                    }
                    break;

                case HEARTS:
                    // Coraçoes flutuando aleatoriamente
                    if (Math.random() < 0.15) {
                        double rx = (Math.random() - 0.5) * 1.1;
                        double ry = Math.random() * 2.0;
                        double rz = (Math.random() - 0.5) * 1.1;
                        player.getWorld().spawnParticle(particle, loc.clone().add(rx, ry, rz), 1, 0, 0.05, 0, 0);
                    }
                    break;

                case PORTAL:
                    // Portal espiralando ao redor do jogador
                    for (int i = 0; i < 2; i++) {
                        double r = 0.55;
                        double x = r * Math.cos(angle + i * Math.PI);
                        double z = r * Math.sin(angle + i * Math.PI);
                        player.getWorld().spawnParticle(particle, loc.clone().add(x, Math.random() * 1.8, z), 1, 0, 0, 0, 0);
                    }
                    break;

                case NOTES:
                    // Notas orbitando na altura do peito
                    double nx = 0.55 * Math.cos(angle);
                    double nz = 0.55 * Math.sin(angle);
                    // Com count = 0, offsetX dita a cor da nota
                    player.getWorld().spawnParticle(particle, loc.clone().add(nx, 1.0, nz), 0, Math.random(), 0.0, 0.0, 1.0);
                    break;

                case SMOKE_RING:
                    // Halo de fumaça acima da cabeça
                    double hx = 0.35 * Math.cos(angle * 2);
                    double hz = 0.35 * Math.sin(angle * 2);
                    player.getWorld().spawnParticle(particle, loc.clone().add(hx, 2.15, hz), 1, 0, 0, 0, 0);
                    break;

                case HAPPY_VILLAGER:
                    // Fagulhas verdes flutuando
                    if (Math.random() < 0.25) {
                        double rx = (Math.random() - 0.5) * 1.2;
                        double ry = Math.random() * 1.8;
                        double rz = (Math.random() - 0.5) * 1.2;
                        player.getWorld().spawnParticle(particle, loc.clone().add(rx, ry, rz), 1, 0, 0, 0, 0);
                    }
                    break;

                case WITCH:
                    // Magia de bruxa em contra-rotação
                    double wx = 0.5 * Math.cos(-angle);
                    double wz = 0.5 * Math.sin(-angle);
                    player.getWorld().spawnParticle(particle, loc.clone().add(wx, Math.random() * 1.8, wz), 1, 0, 0, 0, 0);
                    break;

                case SNOWFLAKE:
                    // Neve caindo do topo
                    if (Math.random() < 0.3) {
                        double rx = (Math.random() - 0.5) * 1.4;
                        double rz = (Math.random() - 0.5) * 1.4;
                        player.getWorld().spawnParticle(particle, loc.clone().add(rx, 2.2, rz), 1, 0, -0.02, 0, 0);
                    }
                    break;

                case DRAGON_BREATH:
                    // Sopro do dragão subindo
                    if (Math.random() < 0.2) {
                        double rx = (Math.random() - 0.5) * 0.5;
                        double rz = (Math.random() - 0.5) * 0.5;
                        player.getWorld().spawnParticle(particle, loc.clone().add(rx, 0.15, rz), 1, 0, 0.1, 0, 0.02);
                    }
                    break;

                case ENCHANTED_HIT:
                    // Partículas de critico caindo em espiral
                    double ex = 0.5 * Math.cos(angle * 2.5);
                    double ez = 0.5 * Math.sin(angle * 2.5);
                    double ey = 2.0 - ((System.currentTimeMillis() / 400.0) % 2.0);
                    player.getWorld().spawnParticle(particle, loc.clone().add(ex, ey, ez), 1, 0, 0, 0, 0);
                    break;
            }
        }
    }
}
