package com.minecraft.arcade.bedwars.listener;

import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChestHologramListener implements Listener {

    private final Map<Location, HologramServer> chestHolograms = new HashMap<>();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        
        if (block.getType() != Material.CHEST && block.getType() != Material.ENDER_CHEST) {
            return;
        }

        Player player = event.getPlayer();
        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.inState(ArcadeState.ALIVE)) {
            return;
        }

        Team team = user.getTeam();
        
        if (team == null || !team.inIsland(block.getLocation())) {
            return;
        }

        Core.getPlatform().runSync(() -> {
            createChestHologram(block.getLocation(), block.getType());
            // Tentar novamente se falhou
            Core.getPlatform().runSync(() -> {
                if (!chestHolograms.containsKey(block.getLocation())) {
                    createChestHologram(block.getLocation(), block.getType());
                }
            }, 10L);
        }, 5L); // Aumentado para 5 ticks
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChestInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        
        if (block == null || (block.getType() != Material.CHEST && block.getType() != Material.ENDER_CHEST)) {
            return;
        }

        Player player = event.getPlayer();
        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.inState(ArcadeState.ALIVE)) {
            return;
        }

        Team team = user.getTeam();
        
        if (team == null || !team.inIsland(block.getLocation())) {
            return;
        }

        // Permitir clique direito para abrir baú, apenas cancelar clique esquerdo para depósito
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            return; // Não cancelar, permitir abrir o baú
        }

        // Cancelar apenas clique esquerdo para depósito
        event.setCancelled(true);

        // Verificar item na mão
        ItemStack handItem = player.getItemInHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            player.sendMessage("§cVocê não tem itens para depositar!");
            return;
        }

        // Verificar se é um Tornado
        if (handItem.getType() == Material.HOPPER && handItem.getItemMeta() != null && 
            handItem.getItemMeta().getDisplayName() != null && 
            handItem.getItemMeta().getDisplayName().equals("§6Tornado")) {
            player.sendMessage("§cVocê não pode depositar o Tornado no baú!");
            return;
        }

        // Não depositar equipamentos
        if (isEquipment(handItem.getType())) {
            player.sendMessage("§cVocê não pode depositar este item no baú!");
            return;
        }

        // Obter inventário do baú (normal ou do fim)
        Inventory chestInv;
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            chestInv = chest.getInventory();
        } else {
            // Para baú do fim, usar o inventário do ender chest do jogador
            chestInv = player.getEnderChest();
        }

        // Depositar apenas o item na mão
        ItemStack itemToDeposit = handItem.clone();
        int amount = itemToDeposit.getAmount();

        // Remover o item da mão
        player.getInventory().setItemInHand(null);

        HashMap<Integer, ItemStack> leftover = chestInv.addItem(itemToDeposit);

        if (!leftover.isEmpty()) {
            // Devolver itens que não couberam
            for (ItemStack remaining : leftover.values()) {
                player.getInventory().addItem(remaining);
            }

            int deposited = amount - leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
            player.sendMessage("§aVoce depositou §e" + deposited + "x " + getItemName(itemToDeposit.getType()) + "§a no " +
                    (block.getType() == Material.ENDER_CHEST ? "§5ender chest" : "§ebau") + "!");
            player.sendMessage("§c" + leftover.values().stream().mapToInt(ItemStack::getAmount).sum() + "x nao couberam e foram devolvidos!");
        } else {
            player.sendMessage("§aVoce depositou §e" + amount + "x " + getItemName(itemToDeposit.getType()) + "§a no " +
                    (block.getType() == Material.ENDER_CHEST ? "§5ender chest" : "§ebau") + "!");
        }

        player.updateInventory();
        player.playSound(player.getLocation(),
                block.getType() == Material.ENDER_CHEST ? Sound.ENDERMAN_TELEPORT : Sound.CHEST_OPEN,
                1.0f, 1.0f);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        if (block.getType() != Material.CHEST && block.getType() != Material.ENDER_CHEST) {
            return;
        }

        Location chestLoc = block.getLocation();
        
        if (chestHolograms.containsKey(chestLoc)) {
            removeHologram(chestLoc);
        }
    }

    private boolean isEquipment(Material type) {
        String name = type.name();
        
        // Armaduras
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || 
            name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) {
            return true;
        }
        
        // Ferramentas
        if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || 
            name.endsWith("_SHOVEL") || name.endsWith("_HOE")) {
            return true;
        }
        
        // Espadas
        if (name.endsWith("_SWORD")) {
            return true;
        }
        
        // Outros itens especiais
        if (type == Material.BOW || type == Material.FISHING_ROD || 
            type == Material.SHEARS || type == Material.FLINT_AND_STEEL) {
            return true;
        }
        
        return false;
    }

    private void createChestHologram(Location chestLocation, Material chestType) {
        if (chestHolograms.containsKey(chestLocation)) {
            return;
        }

        Location hologramLoc = chestLocation.clone().add(0.5, 1.5, 0.5);

        try {
            HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(
                    "chest-" + UUID.randomUUID().toString().substring(0, 8),
                    hologramLoc
            );
            
            // Definir texto ANTES de spawnar (amarelo sem colchetes)
            hologram.setText(Arrays.asList("§eBATA PARA", "§eDEPOSITAR!"));
            
            // Spawnar para todos os jogadores online no mundo com delay para garantir
            Core.getPlatform().runSync(() -> {
                chestLocation.getWorld().getPlayers().forEach(player -> {
                    hologram.spawnTo(player);
                });
            }, 5L); // 5 ticks de delay

            chestHolograms.put(chestLocation, hologram);
            
            // Debug para verificar se foi criado
            System.out.println("[ChestHologram] Holograma criado em: " + chestLocation + " para baú tipo: " + chestType);
            
            // Verificação adicional para garantir que apareceu
            Core.getPlatform().runSync(() -> {
                if (hologram.getViewers().isEmpty()) {
                    System.out.println("[ChestHologram] Tentando respawnar holograma (sem viewers)");
                    chestLocation.getWorld().getPlayers().forEach(player -> {
                        hologram.spawnTo(player);
                    });
                }
            }, 20L);
            
        } catch (Exception e) {
            System.err.println("[ChestHologram] Erro ao criar holograma: " + e.getMessage());
            e.printStackTrace();
            
            // Tentar método alternativo simples
            createSimpleHologram(chestLocation, chestType);
        }
    }

    public void removeHologram(Location chestLocation) {
        HologramServer hologram = chestHolograms.remove(chestLocation);
        
        if (hologram != null) {
            // Despawnar para todos os viewers
            new ArrayList<>(hologram.getViewers()).forEach(hologram::despawnTo);
            BukkitCore.getManager().getHologram().getHolograms().remove(hologram.getId());
        }
    }

    public void clearAllHolograms() {
        for (HologramServer hologram : chestHolograms.values()) {
            // Despawnar para todos os viewers
            new ArrayList<>(hologram.getViewers()).forEach(hologram::despawnTo);
            BukkitCore.getManager().getHologram().getHolograms().remove(hologram.getId());
        }
        chestHolograms.clear();
    }

    private String getItemName(Material material) {
        return switch (material) {
            case IRON_INGOT -> "Ferro";
            case GOLD_INGOT -> "Ouro";
            case DIAMOND -> "Diamante";
            case EMERALD -> "Esmeralda";
            case MAGMA_CREAM -> "Estilingue";
            case FIREBALL -> "Bola de Fogo";
            case SNOW_BALL -> "Bolinha de traca";
            case MONSTER_EGG -> "Defesa dos Sonhos";
            case TNT -> "Explosivo";
            case ENDER_PEARL -> "Ender Pearl";
            case WATER_BUCKET -> "Balde de Agua";
            case MILK_BUCKET -> "Leite Magico";
            case SLIME_BLOCK -> "Jumper";
            case SPONGE -> "Esponja";
            case EGG -> "Ovo de Pontes";
            case COMPASS -> "Rastreador";
            case HOPPER -> "Tornado";
            case BOOK -> "Loja portatil";
            default -> material.name().replace("_", " ").toLowerCase();
        };
    }

    private void createSimpleHologram(Location chestLocation, Material chestType) {
        System.out.println("[ChestHologram] Tentando criar holograma simples para: " + chestLocation);
        
        try {
            // Tentar criar holograma com configuração mínima
            HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(
                    "simple-chest-" + System.currentTimeMillis(),
                    chestLocation.clone().add(0.5, 1.5, 0.5)
            );
            
            hologram.setText(Arrays.asList("§eBATA PARA", "§eDEPOSITAR!"));
            
            // Spawnar imediatamente para todos os jogadores
            for (org.bukkit.entity.Player player : chestLocation.getWorld().getPlayers()) {
                try {
                    hologram.spawnTo(player);
                    System.out.println("[ChestHologram] Holograma spawnado para: " + player.getName());
                } catch (Exception ex) {
                    System.err.println("[ChestHologram] Erro ao spawnar para " + player.getName() + ": " + ex.getMessage());
                }
            }
            
            chestHolograms.put(chestLocation, hologram);
            System.out.println("[ChestHologram] Holograma simples criado com sucesso!");
            
        } catch (Exception e) {
            System.err.println("[ChestHologram] Falha total ao criar holograma simples: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
