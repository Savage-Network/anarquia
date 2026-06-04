package com.minecraft.lobbyhost.display.npc;

import com.minecraft.lobbyhost.Core;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * NPCs hardcoded (opcional). O hub principal usa {@link NpcManager} + {@code npcs.yml}.
 * Mantido vazio para evitar duplicar o {@code npc_anarquia} do YAML.
 */
public final class HardcodedNpcs {

    private static final List<LobbyNpc> spawnedNpcs = new ArrayList<>();

    private HardcodedNpcs() {}

    public static void spawnAll() {
        removeAll();

        World world = Bukkit.getWorld("world");
        if (world == null) {
            Core.getLogger().warning("[HardcodedNpcs] Mundo 'world' não encontrado!");
            return;
        }

        // NPCs do hub: configurar em plugins/lobbyhost/npcs.yml + architect.json
        Core.getLogger().fine("[HardcodedNpcs] Nenhum NPC hardcoded ativo (use npcs.yml).");
    }

    public static void removeAll() {
        for (LobbyNpc npc : spawnedNpcs) {
            if (npc != null) {
                npc.remove();
            }
        }
        spawnedNpcs.clear();
    }

    public static List<LobbyNpc> getSpawnedNpcs() {
        return new ArrayList<>(spawnedNpcs);
    }
}
