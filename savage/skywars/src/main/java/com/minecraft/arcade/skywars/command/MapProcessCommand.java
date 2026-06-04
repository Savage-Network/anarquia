package com.minecraft.arcade.skywars.command;

import com.google.gson.JsonArray;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.minecraft.arcade.skywars.SkyWars;
import com.minecraft.arcade.skywars.arcade.Arcade;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapProcessCommand implements CommandInheritor {
    private static final Map<String, String> SIGN_MAPPINGS = new HashMap<>();

    static {
        SIGN_MAPPINGS.put("spawn", "spawn");
        SIGN_MAPPINGS.put("center", "center");
        SIGN_MAPPINGS.put("map_limit_pos1", "map_limit_pos1");
        SIGN_MAPPINGS.put("map_limit_pos2", "map_limit_pos2");

        // Ilhas (suporta até 12 ilhas)
        for (int i = 1; i <= 12; i++) {
            SIGN_MAPPINGS.put("island_" + i, "island_" + i);
            SIGN_MAPPINGS.put("island" + i, "island_" + i);
            SIGN_MAPPINGS.put("island_spawn_" + i, "island_" + i);
            SIGN_MAPPINGS.put("islandspawn_" + i, "island_" + i);
        }

        // Baús - Sistema do Duels (numérico)
        SIGN_MAPPINGS.put("chest_1", "chest_1");
        SIGN_MAPPINGS.put("chest_2", "chest_2");
        SIGN_MAPPINGS.put("chest_3", "chest_3");
        
        // Compatibilidade com nomes alternativos
        SIGN_MAPPINGS.put("chest1", "chest_1");
        SIGN_MAPPINGS.put("chest2", "chest_2");
        SIGN_MAPPINGS.put("chest3", "chest_3");
        SIGN_MAPPINGS.put("chest_b1", "chest_1");
        SIGN_MAPPINGS.put("chest_b2", "chest_2");
        SIGN_MAPPINGS.put("chest_feast", "chest_3");
        SIGN_MAPPINGS.put("chest_common", "chest_1");
        SIGN_MAPPINGS.put("chest_rare", "chest_2");
        SIGN_MAPPINGS.put("chest_epic", "chest_3");
    }

    @Command(name = "swprocess", rank = RankType.ADMIN, onlyPlayer = false)
    public void processMaps(BukkitCommandContext context) {
        String[] args = context.getArgs();

        if (args.length < 2) {
            context.getSender().send("§cUso: /swprocess <mundo> <modo>");
            context.getSender().send("§7Exemplo: /swprocess mundo1 solo");
            context.getSender().send("§7Modos: solo, duo, trio, quartet, soloversus, duoversus, trioversus, quartetversus");
            return;
        }

        String worldName = args[0];
        String mode = args[1];

        CommandSender sender = context.getSender();

        try {
            // Validar modo
            String[] validModes = {"solo", "duo", "trio", "quartet", "soloversus", "duoversus", "trioversus", "quartetversus"};
            boolean validMode = false;
            for (String m : validModes) {
                if (m.equalsIgnoreCase(mode)) {
                    validMode = true;
                    break;
                }
            }

            if (!validMode) {
                sender.send("§cModo inválido! Use: solo, duo, trio, quartet, soloversus, duoversus, trioversus, quartetversus");
                return;
            }

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.send("§cMundo não encontrado: §e" + worldName);
                sender.send("§7Mundos disponíveis:");
                for (World w : Bukkit.getWorlds()) {
                    sender.send("§7- §e" + w.getName());
                }
                return;
            }

            String mapName = worldName;
            if (mapName.toLowerCase().startsWith(mode.toLowerCase() + "-")) {
                mapName = mapName.substring(mode.length() + 1);
            }

            if (processWorld(sender, world, mapName, mode)) {
                reloadArcade(sender);
            }

        } catch (Exception e) {
            sender.send("§cErro ao processar mapa: " + e.getMessage());
            Core.getLogger().warning("Erro ao processar mapa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean processWorld(CommandSender sender, World world, String mapName, String mode) {
        sender.send("§aEscaneando placas no mundo §e" + world.getName() + "§a...");

        List<MapLocation> foundLocations = scanWorld(world);

        if (foundLocations.isEmpty()) {
            sender.send("§cNenhuma placa válida encontrada no mundo §e" + world.getName());
            sender.send("§7Coloque placas com formato:");
            sender.send("§e[spawn] [island_1] [center] [chest_1] [chest_2] [chest_3]");
            return false;
        }

        sender.send("§aEncontradas §e" + foundLocations.size() + " §aplacas válidas");
        sender.send("§7Placas serão removidas automaticamente após o processamento.");

        // Criar estrutura de diretórios
        File mapsDir = new File(SkyWars.getInstance().getDataFolder(), "maps");
        File modeDir = new File(mapsDir, mode);
        File mapDir = new File(modeDir, mapName);

        if (!mapDir.exists()) {
            mapDir.mkdirs();
        }

        File configFile = new File(mapDir, "config.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject config;
        if (configFile.exists()) {
            try (java.io.FileReader reader = new java.io.FileReader(configFile)) {
                config = new com.google.gson.JsonParser().parse(reader).getAsJsonObject();
            } catch (Exception e) {
                config = new JsonObject();
            }
        } else {
            config = new JsonObject();
        }

        if (!config.has("name")) config.addProperty("name", mapName);
        if (!config.has("normal")) config.addProperty("normal", true);
        if (!config.has("build_limit")) config.addProperty("build_limit", 256);

        if (!config.has("area_protection")) {
            JsonObject areaProtection = new JsonObject();
            areaProtection.addProperty("frontAndBack", 9);
            areaProtection.addProperty("up", 7);
            areaProtection.addProperty("side", 6);
            config.add("area_protection", areaProtection);
        }

        // Mesclar locations por nome (substitui existentes com o mesmo nome, mantém demais)
        // Para baús, permitir múltiplos com índices
        java.util.Map<String, JsonObject> nameToLoc = new java.util.LinkedHashMap<>();
        java.util.Map<String, Integer> chestCounters = new java.util.HashMap<>();
        
        if (config.has("locations") && config.get("locations").isJsonArray()) {
            for (com.google.gson.JsonElement el : config.getAsJsonArray("locations")) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String n = obj.has("name") ? obj.get("name").getAsString() : null;
                if (n != null) {
                    // IGNORAR baús, ilhas e spawn antigos - eles serão recriados do zero
                    if (n.startsWith("chest_") || n.startsWith("island_") || n.equals("spawn") || n.equals("center")) {
                        continue;
                    }
                    nameToLoc.put(n, obj);
                }
            }
        }

        for (MapLocation mapLoc : foundLocations) {
            Location l = mapLoc.location;
            String locationName = mapLoc.name;
            
            // Se for um baú, adicionar índice
            if (locationName.startsWith("chest_")) {
                String baseName = locationName;
                int index = chestCounters.getOrDefault(baseName, 0) + 1;
                chestCounters.put(baseName, index);
                locationName = baseName + "_" + index;
            }
            
            JsonObject loc = new JsonObject();
            loc.addProperty("name", locationName);
            loc.addProperty("x", l.getX());
            loc.addProperty("y", l.getY());
            loc.addProperty("z", l.getZ());
            if (l.getYaw() != 0 || l.getPitch() != 0) {
                loc.addProperty("yaw", l.getYaw());
                loc.addProperty("pitch", l.getPitch());
            }
            nameToLoc.put(locationName, loc);
        }

        JsonArray locations = new JsonArray();
        for (JsonObject obj : nameToLoc.values()) locations.add(obj);
        config.add("locations", locations);

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(gson.toJson(config));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sender.send("§a═══════════════════════════════════════");
        sender.send("§aMapa processado com sucesso!");
        sender.send("§a═══════════════════════════════════════");
        sender.send("§7Mundo: §e" + world.getName());
        sender.send("§7Nome do mapa: §e" + mapName);
        sender.send("§7Modo: §e" + mode);
        sender.send("§7Arquivo: §e" + configFile.getAbsolutePath());
        sender.send("§7Localizações: §e" + foundLocations.size());
        sender.send("§7Placas removidas automaticamente do mundo.");
        sender.send("§a═══════════════════════════════════════");

        return true;
    }

    private void reloadArcade(CommandSender sender) {
        try {
            sender.send("§aRecarregando SkyWars (arcades, mapas)...");
            boolean reloaded = false;
            for (ArcadeCategory category : ArcadeCategory.of(ServerType.SKYWARS)) {
                Object arcadeObj = BukkitCore.getManager().getArcade().read(category);
                if (arcadeObj instanceof Arcade) {
                    Arcade arcade = (Arcade) arcadeObj;

                    arcade.unload();
                    arcade.getMaps().clear();

                    if (arcade.load()) {
                        reloaded = true;
                        sender.send("§aArcade §e" + category.getName() + " §arecarregado!");
                        sender.send("§7Mapas: §e" + arcade.getMaps().size() + " §7Arenas: §e" + arcade.getRooms().size());
                    } else {
                        sender.send("§cFalha ao recarregar arcade §e" + category.getName());
                    }
                }
            }
            if (!reloaded) {
                sender.send("§cNenhum arcade de SkyWars encontrado para recarregar.");
            }
        } catch (Exception e) {
            sender.send("§cErro ao recarregar SkyWars: " + e.getMessage());
            Core.getLogger().warning("Erro ao recarregar SkyWars: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<MapLocation> scanWorld(World world) {
        List<MapLocation> foundLocations = new ArrayList<>();

        int minY = 0;
        int maxY = 255;

        Location center = world.getSpawnLocation();
        int radius = 500;

        for (int x = center.getBlockX() - radius; x <= center.getBlockX() + radius; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = center.getBlockZ() - radius; z <= center.getBlockZ() + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);

                    if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
                        try {
                            Sign sign = (Sign) block.getState();
                            String firstLine = sign.getLine(0).trim().toLowerCase();

                            if (firstLine.startsWith("[") && firstLine.endsWith("]")) {
                                String key = firstLine.substring(1, firstLine.length() - 1);

                                if (SIGN_MAPPINGS.containsKey(key)) {
                                    String locationName = SIGN_MAPPINGS.get(key);

                                    // Base na posição da placa (centralizada no bloco)
                                    Location loc = block.getLocation().clone();
                                    loc.setX(loc.getBlockX() + 0.5);
                                    loc.setZ(loc.getBlockZ() + 0.5);

                                    // Direção do texto da placa
                                    BlockFace face = getSignDirection(block);

                                    float yaw = 0;
                                    float pitch = 0;

                                    try {
                                        if (sign.getLine(1).trim().matches("-?\\d+(\\.\\d+)?")) {
                                            yaw = Float.parseFloat(sign.getLine(1).trim());
                                        }
                                        if (sign.getLine(2).trim().matches("-?\\d+(\\.\\d+)?")) {
                                            pitch = Float.parseFloat(sign.getLine(2).trim());
                                        }
                                    } catch (Exception e) {
                                        // ignore
                                    }

                                    if (yaw == 0) {
                                        if (face != null) yaw = getYawFromBlockFace(face);
                                    }

                                    loc.setYaw(yaw);
                                    loc.setPitch(pitch);

                                    foundLocations.add(new MapLocation(locationName, loc));

                                    // Remover imediatamente a placa do mundo
                                    try { block.setType(Material.AIR); } catch (Exception ignore) {}
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        Core.getLogger().info("[MapProcess] Lidas " + foundLocations.size() + " localizações do mundo " + world.getName());

        return foundLocations;
    }

    private static BlockFace getSignDirection(Block block) {
        try {
            Material type = block.getType();
            if (type == Material.WALL_SIGN) {
                org.bukkit.material.Sign signMaterial = (org.bukkit.material.Sign) block.getState().getData();
                BlockFace facing = signMaterial.getFacing();
                return getOppositeFace(facing);
            } else if (type == Material.SIGN_POST) {
                byte data = block.getData();
                int rotation = data & 0xF;
                switch (rotation) {
                    case 0: return BlockFace.SOUTH;
                    case 1: return BlockFace.SOUTH_SOUTH_WEST;
                    case 2: return BlockFace.SOUTH_WEST;
                    case 3: return BlockFace.WEST_SOUTH_WEST;
                    case 4: return BlockFace.WEST;
                    case 5: return BlockFace.WEST_NORTH_WEST;
                    case 6: return BlockFace.NORTH_WEST;
                    case 7: return BlockFace.NORTH_NORTH_WEST;
                    case 8: return BlockFace.NORTH;
                    case 9: return BlockFace.NORTH_NORTH_EAST;
                    case 10: return BlockFace.NORTH_EAST;
                    case 11: return BlockFace.EAST_NORTH_EAST;
                    case 12: return BlockFace.EAST;
                    case 13: return BlockFace.EAST_SOUTH_EAST;
                    case 14: return BlockFace.SOUTH_EAST;
                    case 15: return BlockFace.SOUTH_SOUTH_EAST;
                    default: return BlockFace.SOUTH;
                }
            }
        } catch (Exception e) {
        }
        return BlockFace.NORTH;
    }

    private static BlockFace getOppositeFace(BlockFace face) {
        switch (face) {
            case NORTH: return BlockFace.SOUTH;
            case SOUTH: return BlockFace.NORTH;
            case EAST: return BlockFace.WEST;
            case WEST: return BlockFace.EAST;
            case NORTH_EAST: return BlockFace.SOUTH_WEST;
            case NORTH_WEST: return BlockFace.SOUTH_EAST;
            case SOUTH_EAST: return BlockFace.NORTH_WEST;
            case SOUTH_WEST: return BlockFace.NORTH_EAST;
            default: return BlockFace.SOUTH;
        }
    }

    private static float getYawFromBlockFace(BlockFace face) {
        switch (face) {
            case NORTH: return 180.0f;
            case SOUTH: return 0.0f;
            case EAST: return -90.0f;
            case WEST: return 90.0f;
            case NORTH_EAST: return -135.0f;
            case NORTH_WEST: return 135.0f;
            case SOUTH_EAST: return -45.0f;
            case SOUTH_WEST: return 45.0f;
            default: return 0.0f;
        }
    }

    private static class MapLocation {
        final String name;
        final Location location;

        MapLocation(String name, Location location) {
            this.name = name;
            this.location = location;
        }
    }
}

