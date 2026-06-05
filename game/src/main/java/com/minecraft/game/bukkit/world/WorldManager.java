package com.minecraft.game.bukkit.world;

import com.minecraft.game.Core;
import com.minecraft.game.GameSettings;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Gerenciamento de mundos (load/unload/tp) estilo Multiverse. */
public final class WorldManager {

    private WorldManager() {}

    public static List<String> discoverWorldNames() {
        File container = Bukkit.getWorldContainer();
        if (container == null || !container.isDirectory()) return List.of();

        List<String> names = new ArrayList<>();
        File[] dirs = container.listFiles(File::isDirectory);
        if (dirs == null) return List.of();

        for (File dir : dirs) {
            if (new File(dir, "level.dat").exists()) {
                names.add(dir.getName());
            }
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public static List<String> loadedWorldNames() {
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public static boolean existsOnDisk(String name) {
        if (name == null || name.isBlank()) return false;
        File folder = new File(Bukkit.getWorldContainer(), name);
        return new File(folder, "level.dat").exists();
    }

    public static boolean isLoaded(String name) {
        return Bukkit.getWorld(name) != null;
    }

    public static Result load(String name) {
        String worldName = sanitize(name);
        if (worldName.isEmpty()) return Result.fail("Nome de mundo invalido.");

        if (isLoaded(worldName)) {
            return Result.ok("Mundo §f" + worldName + " §aja esta carregado.");
        }
        if (!existsOnDisk(worldName)) {
            return Result.fail("Pasta do mundo §f" + worldName + " §cnão existe (sem level.dat).");
        }

        try {
            World world = Bukkit.createWorld(WorldCreator.name(worldName));
            if (world == null) {
                return Result.fail("Falha ao carregar §f" + worldName + "§c.");
            }
            Core.getLogger().info("[Mundo] Carregado: " + worldName);
            SemiAnarchyConfig.onWorldLoaded(worldName);
            return Result.ok("Mundo §f" + worldName + " §acarregado.");
        } catch (Throwable t) {
            Core.getLogger().warning("[Mundo] Erro ao carregar " + worldName + ": " + t.getMessage());
            return Result.fail("Erro ao carregar: §f" + t.getMessage());
        }
    }

    public static Result unload(String name, boolean save) {
        String worldName = sanitize(name);
        if (worldName.isEmpty()) return Result.fail("Nome de mundo invalido.");

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return Result.fail("Mundo §f" + worldName + " §cnão esta carregado.");
        }

        if (Bukkit.getWorlds().size() <= 1) {
            return Result.fail("Não é possível descarregar o único mundo do servidor.");
        }

        long players = world.getPlayers().size();
        if (players > 0) {
            return Result.fail("Mundo §f" + worldName + " §ctem §f" + players + " §cjogador(es). Teleporte-os antes.");
        }

        if (!Bukkit.unloadWorld(world, save)) {
            return Result.fail("Falha ao descarregar §f" + worldName + "§c.");
        }

        Core.getLogger().info("[Mundo] Descarregado: " + worldName + (save ? " (salvo)" : ""));
        return Result.ok("Mundo §f" + worldName + " §edescarregado" + (save ? " e salvo" : "") + ".");
    }

    public static Result teleport(Player player, String name) {
        if (player == null) return Result.fail("Somente jogadores podem usar /mundo tp.");

        String worldName = sanitize(name);
        if (worldName.isEmpty()) return Result.fail("Nome de mundo invalido.");

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            if (existsOnDisk(worldName)) {
                Result loadResult = load(worldName);
                if (!loadResult.success()) return loadResult;
                world = Bukkit.getWorld(worldName);
            }
        }
        if (world == null) {
            return Result.fail("Mundo §f" + worldName + " §cnão encontrado. Use §e/mundo load " + worldName);
        }

        Location dest = resolveTeleportDestination(world);
        dest.getChunk().load(true);

        if (player.teleport(dest)) {
            return Result.ok("Teleportado para §f" + worldName + "§a.");
        }
        return Result.fail("Falha ao teleportar para §f" + worldName + "§c.");
    }

    private static Location resolveTeleportDestination(World world) {
        if (world.getName().equalsIgnoreCase(GameSettings.SPAWN_WORLD)) {
            SemiAnarchyConfig.onWorldLoaded(world.getName());
            return SemiAnarchyConfig.spawnPoint();
        }
        return world.getSpawnLocation().clone();
    }

    public static String formatList() {
        List<String> disk = discoverWorldNames();
        List<String> loaded = loadedWorldNames();

        StringBuilder sb = new StringBuilder();
        sb.append("§6§lMUNDOS §8(§7").append(loaded.size()).append(" carregados§8)\n");

        if (disk.isEmpty()) {
            sb.append("§7Nenhuma pasta de mundo encontrada.");
            return sb.toString();
        }

        for (String name : disk) {
            boolean on = loaded.contains(name);
            World w = on ? Bukkit.getWorld(name) : null;
            sb.append(on ? "§a● " : "§8○ ");
            sb.append("§f").append(name);
            if (on && w != null) {
                sb.append(" §7(").append(envLabel(w)).append(", §f")
                        .append(w.getPlayers().size()).append("§7 jogadores)");
            } else {
                sb.append(" §8(disco)");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    public static String formatInfo(String name) {
        String worldName = sanitize(name);
        World world = Bukkit.getWorld(worldName);
        boolean onDisk = existsOnDisk(worldName);

        if (world == null && !onDisk) {
            return "§cMundo §f" + worldName + " §cnão existe.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§6§l").append(worldName).append("\n");
        sb.append("§7Disco: ").append(onDisk ? "§aSim" : "§cNão").append("\n");
        sb.append("§7Carregado: ").append(world != null ? "§aSim" : "§cNão").append("\n");

        if (world != null) {
            sb.append("§7Ambiente: §f").append(envLabel(world)).append("\n");
            sb.append("§7Seed: §f").append(world.getSeed()).append("\n");
            sb.append("§7Jogadores: §f").append(world.getPlayers().size()).append("\n");
            Location spawn = world.getSpawnLocation();
            sb.append("§7Spawn: §f").append(fmt(spawn.getX()))
                    .append(" §7, §f").append(fmt(spawn.getY()))
                    .append(" §7, §f").append(fmt(spawn.getZ()));
        }
        return sb.toString();
    }

    public static List<String> suggestWorlds(String partial) {
        String lower = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        List<String> all = new ArrayList<>(discoverWorldNames());
        for (String loaded : loadedWorldNames()) {
            if (!all.contains(loaded)) all.add(loaded);
        }
        all.sort(String.CASE_INSENSITIVE_ORDER);
        return all.stream()
                .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(lower))
                .collect(Collectors.toList());
    }

    private static String sanitize(String name) {
        if (name == null) return "";
        return name.trim().replace(' ', '_');
    }

    private static String envLabel(World world) {
        return switch (world.getEnvironment()) {
            case NORMAL -> "Overworld";
            case NETHER -> "Nether";
            case THE_END -> "End";
            default -> world.getEnvironment().name();
        };
    }

    private static String fmt(double v) {
        return String.format(Locale.US, "%.1f", v);
    }

    public record Result(boolean success, String message) {
        static Result ok(String msg) { return new Result(true, msg); }
        static Result fail(String msg) { return new Result(false, msg); }
    }
}
