package com.minecraft.arcade.event.game.sumo;

import com.minecraft.arcade.event.game.Game;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Gera a arena de Sumô no mundo do evento.
 *
 * Estrutura (centro = spawn do game, Y = 100):
 *
 *  Y 100  — piso: disco sólido de QUARTZ_BLOCK, raio 5
 *  Y 100  — borda do piso: anel de BARRIER, raio 5 (nível do chão)
 *  Y 101–110 — parede cilíndrica de BARRIER, raio 6, altura 10
 *  Y 111  — tampa: disco de BARRIER, raio 6 (fecha o topo)
 *
 * O void fica abaixo de Y 100 — quem cair é eliminado pelo SumoListener.
 */
public class SumoMapGenerator {

    private static final int FLOOR_RADIUS  = 5;
    private static final int WALL_RADIUS   = 6;
    private static final int WALL_HEIGHT   = 10;
    private static final Material FLOOR    = Material.QUARTZ_BLOCK;
    private static final Material BARRIER  = Material.BARRIER;

    public static void generate(Game game) {
        World world = game.getWorld();
        Location center = game.getSpawn().clone();

        // Garante que o spawn fique em cima do piso
        center.setY(100);
        game.setSpawn(center.clone().add(0, 1, 0)); // jogadores spawnão em Y=101

        int cx = center.getBlockX();
        int cy = center.getBlockY(); // 100
        int cz = center.getBlockZ();

        // 1. Limpa a área (remove blocos antigos num cubo de segurança)
        clearArea(world, cx, cy, cz);

        // 2. Piso — disco sólido raio 5 em Y=100
        fillDisc(world, cx, cy, cz, FLOOR_RADIUS, FLOOR);

        // 3. Borda do piso — anel de BARRIER em Y=100, raio 5
        //    (anel = disco raio 5 menos disco raio 4)
        fillRing(world, cx, cy, cz, FLOOR_RADIUS, BARRIER);

        // 4. Parede cilíndrica — anel de BARRIER, raio 6, Y=101 até Y=110
        for (int y = cy + 1; y <= cy + WALL_HEIGHT; y++) {
            fillRing(world, cx, y, cz, WALL_RADIUS, BARRIER);
        }

        // 5. Tampa — disco de BARRIER em Y=111, raio 6
        fillDisc(world, cx, cy + WALL_HEIGHT + 1, cz, WALL_RADIUS, BARRIER);
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    /** Preenche um disco sólido (todos os blocos dentro do raio). */
    private static void fillDisc(World world, int cx, int y, int cz, int radius, Material mat) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    world.getBlockAt(cx + x, y, cz + z).setType(mat);
                }
            }
        }
    }

    /** Preenche apenas o anel externo (raio exato, espessura 1). */
    private static void fillRing(World world, int cx, int y, int cz, int radius, Material mat) {
        int inner = radius - 1;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int dist2 = x * x + z * z;
                if (dist2 <= radius * radius && dist2 > inner * inner) {
                    world.getBlockAt(cx + x, y, cz + z).setType(mat);
                }
            }
        }
    }

    /** Limpa um cubo ao redor do centro para não sobrar lixo de geração anterior. */
    private static void clearArea(World world, int cx, int cy, int cz) {
        int margin = WALL_RADIUS + 2;
        for (int x = -margin; x <= margin; x++) {
            for (int z = -margin; z <= margin; z++) {
                for (int y = cy; y <= cy + WALL_HEIGHT + 2; y++) {
                    world.getBlockAt(cx + x, y, cz + z).setType(Material.AIR);
                }
            }
        }
    }
}
