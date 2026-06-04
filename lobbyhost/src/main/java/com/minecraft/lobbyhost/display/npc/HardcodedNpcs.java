package com.minecraft.lobbyhost.display.npc;

import com.minecraft.lobbyhost.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * NPCs hardcoded diretamente no código Java.
 * Não dependem de arquivos YAML ou JSON.
 * 
 * Inclui sistema de rotação automática para itens customizados.
 */
public final class HardcodedNpcs {

    private static final List<LobbyNpc> spawnedNpcs = new ArrayList<>();
    private static org.bukkit.scheduler.BukkitTask rotationTask;

    private HardcodedNpcs() {}

    /**
     * Spawna todos os NPCs hardcoded no mundo.
     */
    public static void spawnAll() {
        removeAll();
        
        World world = Bukkit.getWorld("world");
        if (world == null) {
            Core.getLogger().warning("[HardcodedNpcs] Mundo 'world' não encontrado!");
            return;
        }

        // NPC de Semi Anarquia
        spawnSemiAnarchyNpc(world);

        Core.getLogger().info("[HardcodedNpcs] " + spawnedNpcs.size() + " NPCs hardcoded spawnados.");
        
        // Inicia rotação automática
        startRotationTask();
    }

    /**
     * Spawna o NPC de Semi Anarquia na coordenada -5, -60, 3
     * com uma PICARETA DE DIAMANTE (ao invés de cabeça) e tamanho 1.5x
     */
    private static void spawnSemiAnarchyNpc(World world) {
        // Coordenadas especificadas: -5, -60, 3
        Location location = new Location(world, -5.5, -60, 3.5, 0, 0);

        // Criação da definição do NPC com PICARETA
        NpcDefinition definition = NpcDefinition.builder()
                .id("npc_semi_anarquia")
                .locationKey("hardcoded_semi_anarquia")
                .displayName("Semi Anarquia")
                .hologramLines(List.of(
                        "&4&lSEMI ANARQUIA",
                        "&c&lSobreviva se puder!",
                        "&7Clique para entrar"
                ))
                .customItem("DIAMOND_PICKAXE")  // ← PICARETA ao invés de cabeça!
                .actionType(NpcActionType.BUNGEE)
                .actionValue("semi-anarquia")  // Nome do servidor BungeeCord
                .scale(1.5f)  // 1.5x maior que um jogador normal
                .build();

        try {
            // Spawna o NPC (usa automaticamente scale=1.5f da definição)
            LobbyNpc npc = new LobbyNpc(definition, location);
            spawnedNpcs.add(npc);
            
            Core.getLogger().info("[HardcodedNpcs] NPC 'Semi Anarquia' spawnado em " 
                    + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() 
                    + " com PICARETA DE DIAMANTE (escala 1.5x)");
        } catch (Exception e) {
            Core.getLogger().warning("[HardcodedNpcs] Erro ao spawnar NPC Semi Anarquia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Remove todos os NPCs hardcoded
     */
    public static void removeAll() {
        stopRotationTask();
        for (LobbyNpc npc : spawnedNpcs) {
            if (npc != null) {
                npc.remove();
            }
        }
        spawnedNpcs.clear();
    }
    
    /**
     * Inicia task de rotação para NPCs com itens customizados
     */
    private static void startRotationTask() {
        stopRotationTask();
        
        com.minecraft.lobbyhost.LobbyHostPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return;
        
        rotationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (LobbyNpc npc : spawnedNpcs) {
                // Rotaciona apenas NPCs com customItem definido
                if (npc.getDefinition().getCustomItem() != null && !npc.getDefinition().getCustomItem().isBlank()) {
                    rotateNpc(npc);
                }
            }
        }, 1L, 1L); // Atualiza a cada tick para rotação suave
    }
    
    /**
     * Para a task de rotação
     */
    private static void stopRotationTask() {
        if (rotationTask != null && !rotationTask.isCancelled()) {
            rotationTask.cancel();
            rotationTask = null;
        }
    }
    
    /**
     * Rotaciona um NPC no eixo Y (giro horizontal)
     */
    private static void rotateNpc(LobbyNpc npc) {
        org.bukkit.entity.ItemDisplay display = npc.getItemDisplay();
        if (display == null || display.isDead()) return;
        
        try {
            org.bukkit.util.Transformation transform = display.getTransformation();
            
            // Rotação suave: 2 graus por tick = 360 graus em 3 segundos (180 ticks)
            float rotationSpeed = (float) Math.toRadians(2.0);
            
            // Quaternion para rotação no eixo Y
            org.joml.Quaternionf currentRotation = new org.joml.Quaternionf(transform.getLeftRotation());
            org.joml.Quaternionf yRotation = new org.joml.Quaternionf().rotateY(rotationSpeed);
            currentRotation.mul(yRotation);
            
            // Aplica nova transformação
            org.bukkit.util.Transformation newTransform = new org.bukkit.util.Transformation(
                transform.getTranslation(),
                currentRotation,
                transform.getScale(),
                transform.getRightRotation()
            );
            
            display.setTransformation(newTransform);
        } catch (Exception e) {
            // Silenciosamente ignora erros
        }
    }

    /**
     * Exemplo: Adicione mais NPCs aqui
     */
    @SuppressWarnings("unused")
    private static void spawnExampleNpc(World world) {
        Location location = new Location(world, 10.5, 100, 10.5, 45, 0);

        NpcDefinition definition = NpcDefinition.builder()
                .id("npc_exemplo")
                .locationKey("hardcoded_exemplo")
                .displayName("Exemplo")
                .hologramLines(List.of(
                        "&e&lEXEMPLO",
                        "&7Clique aqui"
                ))
                .skinPlayer("Notch")
                .actionType(NpcActionType.MESSAGE)
                .actionValue("&aOlá! Este é um NPC de exemplo.")
                .scale(1.5f)
                .build();

        try {
            // Spawna o NPC (usa automaticamente scale=1.5f da definição)
            LobbyNpc npc = new LobbyNpc(definition, location);
            spawnedNpcs.add(npc);
        } catch (Exception e) {
            Core.getLogger().warning("[HardcodedNpcs] Erro ao spawnar NPC Exemplo: " + e.getMessage());
        }
    }

    /**
     * Retorna lista de NPCs spawnados
     */
    public static List<LobbyNpc> getSpawnedNpcs() {
        return new ArrayList<>(spawnedNpcs);
    }
}
