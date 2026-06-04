package com.minecraft.arcade.skywars.structure.chest;

import com.minecraft.core.Core;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.*;

public class LootManager {

    private static final Random random = new Random();

    public enum ChestType {
        B1,     // Ilhas - Capacete + Calça de ferro, velocidade, neve
        B2,     // Ilhas - Botas + Peitoral (chance diamante), espada, blocos, regen
        FEAST   // Centro - Itens sortidos, diamante normal com chance de encantamentos leves
    }

    public static void load() {
        Core.getLogger().info("[SkyWars] Sistema de loot carregado (3 tipos de baús: B1, B2, FEAST)");
    }

    public static void fillChest(org.bukkit.block.Chest chest, ChestType type) {
        if (chest == null) {
            Core.getLogger().severe("[SkyWars] ERRO: Tentativa de preencher baú NULL!");
            return;
        }
        
        // Pegar o inventário do baú
        org.bukkit.inventory.Inventory inv = chest.getInventory();
        if (inv == null) {
            Core.getLogger().severe("[SkyWars] ERRO: Inventário do baú é NULL!");
            return;
        }
        
        // Limpar completamente o inventário
        inv.clear();
        
        Core.getLogger().info("[SkyWars] Preenchendo baú tipo: " + type + " na localização: " + chest.getLocation());

        switch (type) {
            case B1 -> fillB1Chest(inv);
            case B2 -> fillB2Chest(inv);
            case FEAST -> fillFeastChest(inv);
        }
        
        // Força atualização do BlockState
        chest.update(true, false);
    }

    // B1 - Capacete + Calça de ferro, poção de velocidade, bolas de neve
    private static void fillB1Chest(org.bukkit.inventory.Inventory inv) {
        Core.getLogger().info("[SkyWars] Preenchendo baú B1");
        
        // SEMPRE: Capacete e Calça de ferro
        addItemToChest(inv, new ItemStack(Material.IRON_HELMET, 1));
        addItemToChest(inv, new ItemStack(Material.IRON_LEGGINGS, 1));
        Core.getLogger().info("[SkyWars] B1: Capacete e calça de ferro");
        
        // Poção de velocidade
        Potion speedPotion = new Potion(PotionType.SPEED, 1);
        speedPotion.setSplash(false);
        addItemToChest(inv, speedPotion.toItemStack(1));
        
        // Bolas de neve (16)
        addItemToChest(inv, new ItemStack(Material.SNOW_BALL, 16));
    }

    // B2 - Botas + Peitoral (chance de diamante), espada, blocos, poção de regeneração
    private static void fillB2Chest(org.bukkit.inventory.Inventory inv) {
        Core.getLogger().info("[SkyWars] Preenchendo baú B2");
        
        // SEMPRE: Botas de ferro
        addItemToChest(inv, new ItemStack(Material.IRON_BOOTS, 1));
        Core.getLogger().info("[SkyWars] B2: Botas de ferro");
        
        // Peitoral: 50% chance de ser de DIAMANTE, senão ferro
        boolean diamondChestplate = random.nextBoolean();
        if (diamondChestplate) {
            addItemToChest(inv, new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
            Core.getLogger().info("[SkyWars] B2: Peitoral de DIAMANTE");
        } else {
            addItemToChest(inv, new ItemStack(Material.IRON_CHESTPLATE, 1));
            Core.getLogger().info("[SkyWars] B2: Peitoral de ferro");
        }
        
        // Espada de ferro
        addItemToChest(inv, new ItemStack(Material.IRON_SWORD, 1));
        
        // Blocos (32 madeira ou pedra)
        Material blockType = random.nextBoolean() ? Material.WOOD : Material.COBBLESTONE;
        addItemToChest(inv, new ItemStack(blockType, 32));
        
        // Poção de regeneração
        Potion regenPotion = new Potion(PotionType.REGEN, 1);
        regenPotion.setSplash(false);
        addItemToChest(inv, regenPotion.toItemStack(1));
        
        // Lava e água
        addItemToChest(inv, new ItemStack(Material.LAVA_BUCKET, 1));
        addItemToChest(inv, new ItemStack(Material.WATER_BUCKET, 1));
    }

    // FEAST - Itens sortidos do centro, não muito OPs
    private static void fillFeastChest(org.bukkit.inventory.Inventory inv) {
        Core.getLogger().info("[SkyWars] ========== PREENCHENDO BAÚ FEAST (CENTRO) ==========");
        
        if (inv == null) {
            Core.getLogger().severe("[SkyWars] ERRO: Inventário FEAST é NULL!");
            return;
        }
        
        // 1-2 peças de diamante NORMAIS (sem encantamento)
        int normalPieces = 1 + random.nextInt(2); // 1 ou 2 peças
        List<Material> diamondArmor = new ArrayList<>(Arrays.asList(
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS
        ));
        Collections.shuffle(diamondArmor);
        
        for (int i = 0; i < normalPieces; i++) {
            ItemStack piece = new ItemStack(diamondArmor.get(i), 1);
            addItemToChest(inv, piece);
            Core.getLogger().info("[SkyWars] FEAST: Adicionou " + diamondArmor.get(i) + " (normal)");
        }
        
        // 30% chance de spawnar UMA peça adicional COM Proteção I
        if (random.nextInt(100) < 30) {
            // Pegar uma peça que não foi usada ainda
            Material enchantedPiece = diamondArmor.get(normalPieces);
            ItemStack piece = new ItemStack(enchantedPiece, 1);
            piece.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            addItemToChest(inv, piece);
            Core.getLogger().info("[SkyWars] FEAST: BÔNUS! Adicionou " + enchantedPiece + " com Proteção I");
        }
        
        // Espada de diamante: SEMPRE Afiação I, 40% chance de Aspecto Flamejante
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
        
        if (random.nextInt(100) < 40) {
            sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            Core.getLogger().info("[SkyWars] FEAST: Espada com Afiação I + Aspecto Flamejante");
        } else {
            Core.getLogger().info("[SkyWars] FEAST: Espada com Afiação I");
        }
        addItemToChest(inv, sword);
        
        // Arco normal ou encantado
        if (random.nextBoolean()) {
            ItemStack bow = new ItemStack(Material.BOW, 1);
            bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1); // Power I
            addItemToChest(inv, bow);
            Core.getLogger().info("[SkyWars] FEAST: Arco Power I");
        } else {
            addItemToChest(inv, new ItemStack(Material.BOW, 1));
            Core.getLogger().info("[SkyWars] FEAST: Arco normal");
        }
        
        // Flechas (16-32)
        addItemToChest(inv, new ItemStack(Material.ARROW, 16 + random.nextInt(17)));
        
        // Ender pearls (1-3)
        addItemToChest(inv, new ItemStack(Material.ENDER_PEARL, 1 + random.nextInt(3)));
        
        // Maçãs douradas (1-2)
        addItemToChest(inv, new ItemStack(Material.GOLDEN_APPLE, 1 + random.nextInt(2)));
        
        // Blocos
        addItemToChest(inv, new ItemStack(Material.WOOD, 32));
        
        // Poção de velocidade I ou II
        int speedLevel = random.nextBoolean() ? 1 : 2;
        Potion speedPotion = new Potion(PotionType.SPEED, speedLevel);
        speedPotion.setSplash(false);
        addItemToChest(inv, speedPotion.toItemStack(1));
        
        Core.getLogger().info("[SkyWars] ========== BAÚ FEAST PREENCHIDO! ==========");
    }

    private static void addItemToChest(org.bukkit.inventory.Inventory inv, ItemStack item) {
        if (item == null) {
            Core.getLogger().warning("[SkyWars] Tentativa de adicionar item NULL ao baú!");
            return;
        }
        
        int slot = getRandomEmptySlot(inv);
        if (slot != -1) {
            inv.setItem(slot, item);
            Core.getLogger().info("[SkyWars] Item " + item.getType() + " x" + item.getAmount() + " adicionado no slot " + slot);
            
            // Verificar se realmente foi adicionado
            ItemStack checkItem = inv.getItem(slot);
            if (checkItem == null || checkItem.getType() == Material.AIR) {
                Core.getLogger().severe("[SkyWars] ERRO: Item não foi adicionado ao slot " + slot + "!");
            } else {
                Core.getLogger().info("[SkyWars] Verificação OK: Item " + checkItem.getType() + " está no slot " + slot);
            }
        } else {
            Core.getLogger().warning("[SkyWars] Nenhum slot vazio disponível para adicionar " + item.getType());
        }
    }

    private static int getRandomEmptySlot(org.bukkit.inventory.Inventory inv) {
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack currentItem = inv.getItem(i);
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                emptySlots.add(i);
            }
        }
        
        Core.getLogger().info("[SkyWars] Slots vazios disponíveis: " + emptySlots.size() + " de " + inv.getSize());
        
        if (emptySlots.isEmpty()) return -1;
        return emptySlots.get(random.nextInt(emptySlots.size()));
    }
}
