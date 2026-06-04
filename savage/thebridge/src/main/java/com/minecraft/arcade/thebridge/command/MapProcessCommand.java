package com.minecraft.arcade.thebridge.command;

import com.google.gson.JsonArray;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.minecraft.arcade.thebridge.TheBridge;
import com.minecraft.arcade.thebridge.arcade.Arcade;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.team.preset.TeamPresetType;
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
        // Localizações gerais
        SIGN_MAPPINGS.put("spawn", "spawn");
        SIGN_MAPPINGS.put("map_limit_pos1", "map_limit_pos1");
        SIGN_MAPPINGS.put("map_limit_pos2", "map_limit_pos2");
        SIGN_MAPPINGS.put("map-limit-pos1", "map_limit_pos1");
        SIGN_MAPPINGS.put("map-limit-pos2", "map_limit_pos2");
        
        // Limites da ponte (onde jogadores podem quebrar/colocar blocos)
        SIGN_MAPPINGS.put("ponte-1", "ponte_1");
        SIGN_MAPPINGS.put("ponte-2", "ponte_2");
        SIGN_MAPPINGS.put("ponte_1", "ponte_1");
        SIGN_MAPPINGS.put("ponte_2", "ponte_2");

        // Localizações dos times (red e blue)
        for (TeamPresetType type : TeamPresetType.values()) {
            String code = type.name().toLowerCase(); // ex: red, blue
            
            // Formato com underscore: red_spawn
            SIGN_MAPPINGS.put(code + "_spawn", code + "_spawn");
            SIGN_MAPPINGS.put(code + "_portal", code + "_portal");
            
            // Formato com hífen: red-spawn (mais fácil de digitar)
            SIGN_MAPPINGS.put(code + "-spawn", code + "_spawn");
            SIGN_MAPPINGS.put(code + "-portal", code + "_portal");
            
            String alias;
            if (code.equals("yel")) {
                alias = "yellow";
            } else if (code.equals("oran")) {
                alias = "orange";
            } else if (code.equals("purp")) {
                alias = "purple";
            } else {
                alias = type.getName().toLowerCase();
            }

            if (!alias.equals(code)) {
                // Formato com underscore
                SIGN_MAPPINGS.put(alias + "_spawn", code + "_spawn");
                SIGN_MAPPINGS.put(alias + "_portal", code + "_portal");
                
                // Formato com hífen
                SIGN_MAPPINGS.put(alias + "-spawn", code + "_spawn");
                SIGN_MAPPINGS.put(alias + "-portal", code + "_portal");
            }
        }
    }

    @Command(name = "tbprocess", rank = RankType.ADMIN, onlyPlayer = false)
    public void processMaps(BukkitCommandContext context) {
        String[] args = context.getArgs();

        if (args.length < 3) {
            context.getSender().send("§cUso: /tbprocess <mundo> <modo> <build_limit>");
            context.getSender().send("§7Exemplo: /tbprocess mundo1 solo 80");
            context.getSender().send("§7Modos: solo");
            return;
        }

        String worldName = args[0];
        String mode = args[1];
        String buildLimitStr = args[2];

        CommandSender sender = context.getSender();

        try {
            int buildLimit = Integer.parseInt(buildLimitStr);

            // Validar modo
            String[] validModes = {"solo"};
            boolean validMode = false;
            for (String m : validModes) {
                if (m.equalsIgnoreCase(mode)) {
                    validMode = true;
                    break;
                }
            }

            if (!validMode) {
                sender.send("§cModo inválido! Use: solo");
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

            if (processWorld(sender, world, mapName, mode, buildLimit)) {
                reloadArcade(sender);
            }

        } catch (NumberFormatException e) {
            sender.send("§cBuild limit deve ser um número!");
        } catch (Exception e) {
            sender.send("§cErro ao processar mapa: " + e.getMessage());
            Core.getLogger().warning("Erro ao processar mapa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean processWorld(CommandSender sender, World world, String mapName, String mode, int buildLimit) {
        sender.send("§aEscaneando placas no mundo §e" + world.getName() + "§a...");

        List<MapLocation> foundLocations = scanWorld(world);

        if (foundLocations.isEmpty()) {
            sender.send("§cNenhuma placa válida encontrada no mundo §e" + world.getName());
            sender.send("§7Coloque placas com formato: §espawn§7, §ered-spawn§7, §ered-portal§7, §eponte-1§7, §eponte-2§7, etc.");
            sender.send("§7§lNÃO é necessário usar colchetes! Apenas escreva o nome na primeira linha.");
            return false;
        }

        sender.send("§aEncontradas §e" + foundLocations.size() + " §aplacas válidas");
        sender.send("§7Placas serão removidas automaticamente após o processamento.");

        // Criar estrutura de diretórios
        File mapsDir = new File(TheBridge.getInstance().getDataFolder(), "maps");
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
        config.addProperty("normal", config.has("normal") ? config.get("normal").getAsBoolean() : true);
        config.addProperty("build_limit", buildLimit);

        if (!config.has("area_protection")) {
            JsonObject areaProtection = new JsonObject();
            areaProtection.addProperty("frontAndBack", 9);
            areaProtection.addProperty("up", 7);
            areaProtection.addProperty("side", 6);
            config.add("area_protection", areaProtection);
        }

        // Mesclar locations por nome (substitui existentes com o mesmo nome, mantém demais)
        java.util.Map<String, JsonObject> nameToLoc = new java.util.LinkedHashMap<>();
        if (config.has("locations") && config.get("locations").isJsonArray()) {
            for (com.google.gson.JsonElement el : config.getAsJsonArray("locations")) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String n = obj.has("name") ? obj.get("name").getAsString() : null;
                if (n != null) nameToLoc.put(n, obj);
            }
        }

        for (MapLocation mapLoc : foundLocations) {
            Location l = mapLoc.location;
            JsonObject loc = new JsonObject();
            loc.addProperty("name", mapLoc.name);
            loc.addProperty("x", l.getX());
            loc.addProperty("y", l.getY());
            loc.addProperty("z", l.getZ());
            if (l.getYaw() != 0 || l.getPitch() != 0) {
                loc.addProperty("yaw", l.getYaw());
                loc.addProperty("pitch", l.getPitch());
            }
            nameToLoc.put(mapLoc.name, loc);
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
            sender.send("§aRecarregando TheBridge (arcades, mapas, times)...");
            boolean reloaded = false;
            for (ArcadeCategory category : ArcadeCategory.of(ServerType.THE_BRIDGE)) {
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
                sender.send("§cNenhum arcade de TheBridge encontrado para recarregar.");
            }
        } catch (Exception e) {
            sender.send("§cErro ao recarregar TheBridge: " + e.getMessage());
            Core.getLogger().warning("Erro ao recarregar TheBridge: " + e.getMessage());
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
                            
                            Core.getLogger().info("[TheBridge MapProcess] Placa encontrada em " + block.getLocation().getBlockX() + ", " + 
                                    block.getLocation().getBlockY() + ", " + block.getLocation().getBlockZ() + 
                                    " com texto: '" + firstLine + "'");
                            
                            // Aceitar tanto [red-spawn] quanto red-spawn (mas não exigir colchetes)
                            String key = firstLine;
                            if (key.startsWith("[") && key.endsWith("]")) {
                                key = key.substring(1, key.length() - 1);
                                Core.getLogger().info("[TheBridge MapProcess] Removidos colchetes, nova key: '" + key + "'");
                            }
                            
                            // Normalizar: substituir underline por hífen para buscar no mapeamento
                            String normalizedKey = key.replace("_", "-");
                            if (!key.equals(normalizedKey)) {
                                Core.getLogger().info("[TheBridge MapProcess] Key normalizada de '" + key + "' para '" + normalizedKey + "'");
                            }

                            if (SIGN_MAPPINGS.containsKey(key) || SIGN_MAPPINGS.containsKey(normalizedKey)) {
                                // Tentar primeiro com a key original, depois com a normalizada
                                String locationName = SIGN_MAPPINGS.containsKey(key) 
                                    ? SIGN_MAPPINGS.get(key) 
                                    : SIGN_MAPPINGS.get(normalizedKey);

                                Core.getLogger().info("[TheBridge MapProcess] ✓ Placa VÁLIDA: '" + firstLine + "' -> '" + locationName + "'");

                                // Base na posição da placa (centralizada no bloco)
                                Location loc = block.getLocation().clone();
                                loc.setX(loc.getBlockX() + 0.5);
                                loc.setZ(loc.getBlockZ() + 0.5);

                                // Direção do texto da placa
                                BlockFace face = getSignDirection(block);

                                // Para portais, procurar portal próximo (END_PORTAL ou END_PORTAL_FRAME)
                                if (locationName.contains("_portal")) {
                                    Location portalLoc = findNearbyPortal(world, block.getLocation());
                                    if (portalLoc != null) {
                                        loc = portalLoc.clone();
                                        loc.setX(portalLoc.getBlockX() + 0.5);
                                        loc.setY(portalLoc.getBlockY() + 0.5);
                                        loc.setZ(portalLoc.getBlockZ() + 0.5);
                                        Core.getLogger().info("[TheBridge MapProcess] Portal encontrado próximo à placa em " + portalLoc.getBlockX() + ", " + portalLoc.getBlockY() + ", " + portalLoc.getBlockZ());
                                    } else {
                                        Core.getLogger().warning("[TheBridge MapProcess] ⚠ Portal NÃO encontrado próximo à placa de '" + locationName + "'");
                                    }
                                }

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
                                Core.getLogger().info("[TheBridge MapProcess] Localização adicionada: " + locationName + " (Yaw: " + yaw + ", Pitch: " + pitch + ")");

                                // Remover imediatamente a placa do mundo
                                try { block.setType(Material.AIR); } catch (Exception ignore) {}
                            } else {
                                Core.getLogger().warning("[TheBridge MapProcess] ✗ Placa IGNORADA (não mapeada): '" + firstLine + "' (key: '" + key + "', normalized: '" + normalizedKey + "')");
                            }
                        } catch (Exception e) {
                            Core.getLogger().warning("[TheBridge MapProcess] Erro ao processar placa: " + e.getMessage());
                        }
                    }
                }
            }
        }
        Core.getLogger().info("[TheBridge MapProcess] Lidas " + foundLocations.size() + " localizações do mundo " + world.getName());

        return foundLocations;
    }

    private Location findNearbyPortal(World world, Location signLocation) {
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    Block block = world.getBlockAt(
                        signLocation.getBlockX() + x,
                        signLocation.getBlockY() + y,
                        signLocation.getBlockZ() + z
                    );

                    if (block.getType() == Material.ENDER_PORTAL || block.getType() == Material.ENDER_PORTAL_FRAME) {
                        return block.getLocation();
                    }
                }
            }
        }
        return null;
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
            // Direções intermediárias
            case NORTH_NORTH_EAST: return -157.5f;
            case NORTH_NORTH_WEST: return 157.5f;
            case SOUTH_SOUTH_EAST: return -22.5f;
            case SOUTH_SOUTH_WEST: return 22.5f;
            case EAST_NORTH_EAST: return -112.5f;
            case EAST_SOUTH_EAST: return -67.5f;
            case WEST_NORTH_WEST: return 112.5f;
            case WEST_SOUTH_WEST: return 67.5f;
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
