package com.minecraft.eventos.arena;

import com.minecraft.eventos.message.EventMessages;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Gera arenas proceduralmente no mundo (sem WorldEdit). */
public final class ArenaGenerator {

    private static final Random RANDOM = new Random();

    private ArenaGenerator() {}

    public enum Type {
        SUMO, SPLEEF, TNT_RUN, BLOCK_PARTY, PARKOUR, FFA
    }

    public static Type typeForMode(String modeId) {
        if (modeId == null) return Type.SUMO;
        return switch (modeId.toLowerCase()) {
            case "spleef" -> Type.SPLEEF;
            case "tntrun", "tnt_run" -> Type.TNT_RUN;
            case "blockparty", "block_party" -> Type.BLOCK_PARTY;
            case "parkour" -> Type.PARKOUR;
            case "ffa", "deathmatch" -> Type.FFA;
            default -> Type.SUMO;
        };
    }

    public static ArenaDefinition generate(Player staff, String arenaId, String modeId, int radius) {
        Location base = staff.getLocation().clone();
        World world = base.getWorld();
        if (world == null) throw new IllegalStateException("Mundo invalido");

        Type type = typeForMode(modeId);
        int r = Math.max(6, Math.min(radius, 40));
        int platformY = base.getBlockY();

        ArenaDefinition arena = new ArenaDefinition();
        arena.setId(arenaId);
        arena.setModeId(modeId);
        arena.setCenter(base);
        arena.setRadius(r);
        arena.setWidth(r * 2 + 1);
        arena.setDepth(r * 2 + 1);
        arena.setDieY(platformY - 8);
        arena.setGenerated(true);
        arena.setGeneratedAt(System.currentTimeMillis());

        switch (type) {
            case SUMO, FFA -> buildSumoPlatform(world, base, r, platformY);
            case SPLEEF -> buildSpleefArena(world, base, r, platformY);
            case TNT_RUN -> buildTntRunArena(world, base, r, platformY);
            case BLOCK_PARTY -> buildBlockPartyGrid(world, base, r, platformY);
            case PARKOUR -> buildParkourLine(world, base, r, platformY);
        }

        Location spawn = base.clone().add(0.5, 1, 0.5);
        arena.getSpawns().clear();
        arena.getSpawns().add(ArenaDefinition.SpawnPoint.of(spawn));
        arena.setDieY(platformY - 6);
        return arena;
    }

    public static int clearArena(ArenaDefinition arena) {
        if (arena == null || arena.getWorldName() == null) return 0;
        World world = org.bukkit.Bukkit.getWorld(arena.getWorldName());
        if (world == null) return 0;
        Location center = arena.center();
        if (center == null) return 0;

        int r = arena.getRadius() + 2;
        int y0 = center.getBlockY() - 10;
        int y1 = center.getBlockY() + 15;
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int removed = 0;

        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                for (int y = y0; y <= y1; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() != Material.AIR && b.getType() != Material.BEDROCK) {
                        b.setType(Material.AIR, false);
                        removed++;
                    }
                }
            }
        }
        return removed;
    }

    private static void buildSumoPlatform(World world, Location center, int radius, int y) {
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                double dx = x - cx + 0.5;
                double dz = z - cz + 0.5;
                if (dx * dx + dz * dz > (radius + 0.5) * (radius + 0.5)) continue;
                set(world, x, y, z, Material.QUARTZ_BLOCK);
                set(world, x, y - 1, z, Material.QUARTZ_BLOCK);
                if (dx * dx + dz * dz > (radius - 0.8) * (radius - 0.8)) {
                    set(world, x, y, z, Material.ORANGE_STAINED_GLASS);
                }
            }
        }
        // pilares decorativos
        for (int i = 0; i < 4; i++) {
            int px = cx + (i < 2 ? radius - 1 : -(radius - 1));
            int pz = cz + (i % 2 == 0 ? radius - 1 : -(radius - 1));
            for (int h = y + 1; h <= y + 3; h++) {
                set(world, px, h, pz, Material.GLOWSTONE);
            }
        }
    }

    private static void buildSpleefArena(World world, Location center, int radius, int y) {
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                boolean border = x == cx - radius || x == cx + radius
                        || z == cz - radius || z == cz + radius;
                set(world, x, y, z, border ? Material.STONE_BRICKS : Material.SNOW_BLOCK);
                set(world, x, y - 1, z, Material.STONE);
            }
        }
    }

    private static void buildTntRunArena(World world, Location center, int radius, int y) {
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                set(world, x, y - 2, z, Material.BEDROCK);
                set(world, x, y - 1, z, Material.CLAY);
                Material top = ((x + z) % 2 == 0) ? Material.SAND : Material.GRAVEL;
                set(world, x, y, z, top);
                if (RANDOM.nextInt(12) == 0) {
                    set(world, x, y + 1, z, Material.TNT);
                }
            }
        }
    }

    private static void buildBlockPartyGrid(World world, Location center, int radius, int y) {
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        Material[] colors = {
                Material.RED_CONCRETE, Material.BLUE_CONCRETE, Material.LIME_CONCRETE,
                Material.YELLOW_CONCRETE, Material.MAGENTA_CONCRETE, Material.CYAN_CONCRETE
        };
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                Material m = colors[Math.floorMod(x + z, colors.length)];
                set(world, x, y, z, m);
                set(world, x, y - 1, z, Material.STONE);
            }
        }
    }

    private static void buildParkourLine(World world, Location center, int radius, int y) {
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        List<Material> mats = List.of(
                Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.LAPIS_BLOCK,
                Material.EMERALD_BLOCK, Material.DIAMOND_BLOCK);
        int step = 0;
        for (int i = -radius; i <= radius; i++) {
            Material mat = mats.get(step % mats.size());
            set(world, cx + i, y, cz, mat);
            if (i % 3 == 0) {
                set(world, cx + i, y + 1, cz, Material.OAK_PRESSURE_PLATE);
            }
            step++;
        }
        set(world, cx + radius, y, cz + 2, Material.BEACON);
    }

    private static void set(World world, int x, int y, int z, Material material) {
        Block b = world.getBlockAt(x, y, z);
        if (b.getType() == Material.BEDROCK) return;
        b.setType(material, false);
    }
}
