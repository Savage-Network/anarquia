package com.minecraft.arcade.skywars.arcade.game;

import com.minecraft.arcade.skywars.arcade.arena.Arena;
import com.minecraft.arcade.skywars.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.map.location.SignedLocation;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.util.list.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lógica do SkyWars copiada do plugin Duels
 * Sistema de refill de baús e preenchimento automático
 */
public class SkyWarsGameLogic {

    private static final String REFILL_KEY = "refill";
    private static final int DEFAULT_REFILL_TIME = 90; // 90 segundos

    /**
     * Inicializa as propriedades da arena (chamado na fase STARTING)
     */
    public static void initializeProperties(Arena arena) {
        arena.writeProperty(REFILL_KEY, DEFAULT_REFILL_TIME);
    }

    /**
     * Timer executado a cada segundo durante a partida
     */
    public static void handleRefillTimer(Arena arena) {
        if (!arena.hasProperty(REFILL_KEY) || !arena.isPhase(RoomPhase.PLAYING)) 
            return;

        final int currentTime = (int) arena.getProperty(REFILL_KEY);
        arena.writeProperty(REFILL_KEY, currentTime - 1);

        int time = (int) arena.getProperty(REFILL_KEY);

        // Atualizar tempo na scoreboard de todos os jogadores
        arena.getPlayers().forEach(player -> {
            User user = (User) User.of(player.getUniqueId());
            if (user != null && user.getSidebar() != null) {
                Sidebar sidebar = user.getSidebar();
                sidebar.updateRow("refill", " Refill: §a" + TimeUtil.time(time));
            }
        });

        // Avisos sonoros
        if (time <= 3 && time >= 1) {
            arena.send("§e" + time + "...");
            arena.sound(Sound.NOTE_PLING);
        }

        // Reabastecer baús
        if (time <= 0) {
            arena.writeProperty(REFILL_KEY, DEFAULT_REFILL_TIME);
            refillChests(arena);
            arena.send("§aOs baús foram reabastecidos.");
            arena.sound(Sound.LEVEL_UP);
        }
    }

    /**
     * Preenche os baús na fase PLAYING (início do jogo)
     */
    public static void fillChestsOnStart(Arena arena) {
        refillChests(arena);
    }

    /**
     * Reabastece todos os baús da arena
     * Sistema ORIGINAL do Duels com busca de baú próximo
     */
    private static void refillChests(Arena arena) {
        List<SignedLocation> chests = arena.getSignedLocations(signed -> 
            signed.getName().toLowerCase().contains("chest"));

        if (chests.isEmpty()) {
            Core.getLogger().warning("[SkyWars] Não há baús configurados na arena " + arena.getIdentifier());
            return;
        }

        Core.getLogger().info("[SkyWars] Preenchendo " + chests.size() + " baús na arena " + arena.getIdentifier());

        chests.forEach(signed -> {
            Location location = signed.getSynthetic().of(arena.getWorld());

            if (location != null) {
                // Primeiro tenta na posição exata
                Block block = location.getBlock();
                
                // Se não for baú, procura ao redor (raio de 2 blocos)
                if (!block.getType().equals(Material.CHEST) && !block.getType().equals(Material.TRAPPED_CHEST)) {
                    block = findNearbyChest(location);
                }

                if (block != null && (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST))) {
                    Chest chest = (Chest) block.getState();

                    if (chest != null) {
                        try {
                            String[] parts = signed.getName().split("_");
                            // Formato: chest_TIPO_NUMERO (ex: chest_1_1, chest_2_3)
                            // Pegar o TIPO (segundo elemento), não o NUMERO (terceiro)
                            int id = parts.length >= 2 ? Integer.parseInt(parts[1]) : 1;

                            ChestCategory category = ChestCategory.fromId(id);
                            
                            if (category != null) {
                                fillChest(chest.getBlockInventory(), category);
                                Core.getLogger().info("[SkyWars] Baú preenchido: " + signed.getName() + " (Tipo " + id + ": " + category.name() + ")");
                            } else {
                                Core.getLogger().warning("[SkyWars] ID de baú inválido: " + signed.getName() + " (ID: " + id + "). Use chest_1, chest_2 ou chest_3");
                            }
                        } catch (Exception e) {
                            Core.getLogger().warning("[SkyWars] Erro ao processar baú: " + signed.getName() + " - " + e.getMessage());
                        }
                    }
                } else {
                    Core.getLogger().warning("[SkyWars] Baú não encontrado para: " + signed.getName() + " em " +
                        String.format("%.1f, %.1f, %.1f", location.getX(), location.getY(), location.getZ()));
                }
            }
        });
    }
    
    /**
     * Procura um baú próximo à localização (raio de 2 blocos)
     */
    private static Block findNearbyChest(Location center) {
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();
                    if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
                        return block;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Preenche um baú com itens da categoria especificada
     * Sistema ORIGINAL do Duels
     */
    private static void fillChest(Inventory menu, ChestCategory category) {
        List<ItemStack> contents = category.getContents();

        menu.clear();

        for (ItemStack content : contents) {
            if (content == null || content.getType() == Material.AIR || menu.contains(content)) continue;

            int randomPosition = Core.RANDOM.nextInt(menu.getSize());

            while (menu.getItem(randomPosition) != null)
                randomPosition = Core.RANDOM.nextInt(menu.getSize());

            menu.setItem(randomPosition, content);
        }
    }

    /**
     * Categorias de baús - CÓDIGO ORIGINAL DO DUELS + FEAST
     */
    @Getter
    @AllArgsConstructor
    public enum ChestCategory {
        PRIMARY(
            Item.of(Material.GOLDEN_APPLE), 
            Item.of(Material.WATER_BUCKET), 
            Item.of(Material.WOOD).amount(38),
            Item.of(Material.EGG).amount(8), 
            Item.of(Material.DIAMOND_SWORD)
        ),
        SECONDARY(
            Item.of(Material.EGG).amount(8), 
            Item.of(Material.EXP_BOTTLE).amount(12), 
            Item.of(Material.DIAMOND_BOOTS),
            Item.of(Material.LAVA_BUCKET), 
            Item.of(Material.WOOD).amount(32), 
            Item.of(Material.POTION, 16385)
        ),
        FEAST(
            Item.of(Material.DIAMOND_SWORD),
            Item.of(Material.DIAMOND_HELMET),
            Item.of(Material.DIAMOND_CHESTPLATE),
            Item.of(Material.DIAMOND_LEGGINGS),
            Item.of(Material.DIAMOND_BOOTS),
            Item.of(Material.GOLDEN_APPLE).amount(3),
            Item.of(Material.EXP_BOTTLE).amount(32),
            Item.of(Material.POTION, 16385), // Velocidade
            Item.of(Material.POTION, 16393), // Força
            Item.of(Material.BOW),
            Item.of(Material.ARROW).amount(64),
            Item.of(Material.LAVA_BUCKET)
        );

        private final List<ItemStack> contents = new ArrayList<>();

        ChestCategory(ItemStack... contents) {
            this.contents.addAll(Arrays.asList(contents));
        }

        public static ChestCategory fromId(int id) {
            return Arrays.stream(values()).filter(category -> (category.ordinal() + 1) == id).findFirst().orElse(null);
        }
    }
}
