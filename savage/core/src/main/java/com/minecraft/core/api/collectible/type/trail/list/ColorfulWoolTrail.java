package com.minecraft.core.api.collectible.type.trail.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.trail.TrailCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ColorfulWoolTrail extends TrailCollectible {

    private static final Map<Location, TrailBlock> trailBlocks = new HashMap<>();
    private static final Random random = new Random();
    
    // Cores de lã disponíveis (wool data values no 1.8)
    private static final byte[] WOOL_COLORS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    public ColorfulWoolTrail() {
        super("Lã Colorida", CollectibleRarity.RARE, Arrays.asList(RankType.MEMBER), System.currentTimeMillis());
        
        setIcon(Item.of(Material.WOOL, random.nextInt(16)));
        setLore(Arrays.asList(
                "§7Deixe um rastro de lãs",
                "§7coloridas alternando entre si!",
                "",
                "§7Raridade: §9Raro"
        ));
    }

    @Override
    public void applyTrail(Player player, Location location) {
        Location blockLoc = location.clone().subtract(0, 1, 0);
        Block block = blockLoc.getBlock();
        
        // Só aplicar em blocos sólidos
        if (block.getType() == Material.AIR) {
            return;
        }
        
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Core");
        if (plugin == null) return;
        
        // Se já existe um rastro nessa localização, não fazer nada
        if (trailBlocks.containsKey(blockLoc)) {
            return;
        }
        
        // Salvar bloco original
        Material originalType = block.getType();
        byte originalData = block.getData();
        
        // Colocar lã colorida inicial
        block.setType(Material.WOOL);
        byte initialColor = WOOL_COLORS[random.nextInt(WOOL_COLORS.length)];
        block.setData(initialColor);
        
        // Criar task para mudar as cores
        BukkitTask colorTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Block currentBlock = blockLoc.getBlock();
            if (currentBlock.getType() == Material.WOOL) {
                byte newColor = WOOL_COLORS[random.nextInt(WOOL_COLORS.length)];
                currentBlock.setData(newColor);
            }
        }, 5L, 5L); // Muda de cor a cada 5 ticks (0.25 segundos)
        
        // Salvar informações do rastro
        TrailBlock trailBlock = new TrailBlock(originalType, originalData, colorTask);
        trailBlocks.put(blockLoc, trailBlock);
        
        // Agendar para voltar ao normal após 2 segundos
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeTrail(blockLoc);
        }, 40L); // 2 segundos = 40 ticks
    }

    @Override
    public void removeTrail(Location location) {
        TrailBlock trailBlock = trailBlocks.remove(location);
        
        if (trailBlock != null) {
            // Cancelar task de mudança de cor
            trailBlock.getColorTask().cancel();
            
            // Restaurar bloco original
            Block block = location.getBlock();
            if (block.getType() == Material.WOOL) {
                block.setType(trailBlock.getOriginalType());
                block.setData(trailBlock.getOriginalData());
            }
        }
    }
    
    // Classe interna para armazenar informações do rastro
    private static class TrailBlock {
        private final Material originalType;
        private final byte originalData;
        private final BukkitTask colorTask;
        
        public TrailBlock(Material originalType, byte originalData, BukkitTask colorTask) {
            this.originalType = originalType;
            this.originalData = originalData;
            this.colorTask = colorTask;
        }
        
        public Material getOriginalType() {
            return originalType;
        }
        
        public byte getOriginalData() {
            return originalData;
        }
        
        public BukkitTask getColorTask() {
            return colorTask;
        }
    }
}
