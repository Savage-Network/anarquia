package com.minecraft.arcade.bedwars.structure.tower;

import com.minecraft.arcade.bedwars.BedWars;
import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.core.arcade.room.map.rollback.RollbackBlock;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class PopUpTower {

    private String[] blocks;

    public PopUpTower(Block center, Arena arena, byte woolColor, BlockFace direction) {
        
        // Determinar a direção baseada no BlockFace
        String position = getPositionFromFace(direction);
        
        switch (position) {
            case "east": {
                blocks = new String[]{
                    "2, 0, -1, WOOL:{id}", "1, 0, -2, WOOL:{id}", "0, 0, -2, WOOL:{id}", "-1, 0, -1, WOOL:{id}", 
                    "-1, 0, 0, WOOL:{id}", "-1, 0, 1, WOOL:{id}", "0, 0, 2, WOOL:{id}", "1, 0, 2, WOOL:{id}", 
                    "2, 0, 1, WOOL:{id}", "0, 0, 0, LADDER:5",
                    
                    "2, 1, -1, WOOL:{id}", "1, 1, -2, WOOL:{id}", "0, 1, -2, WOOL:{id}", "-1, 1, -1, WOOL:{id}", 
                    "-1, 1, 0, WOOL:{id}", "-1, 1, 1, WOOL:{id}", "0, 1, 2, WOOL:{id}", "1, 1, 2, WOOL:{id}", 
                    "2, 1, 1, WOOL:{id}", "0, 1, 0, LADDER:5",
                    
                    "2, 2, -1, WOOL:{id}", "1, 2, -2, WOOL:{id}", "0, 2, -2, WOOL:{id}", "-1, 2, -1, WOOL:{id}", 
                    "-1, 2, 0, WOOL:{id}", "-1, 2, 1, WOOL:{id}", "0, 2, 2, WOOL:{id}", "1, 2, 2, WOOL:{id}", 
                    "2, 2, 1, WOOL:{id}", "0, 2, 0, LADDER:5",
                    
                    "2, 3, 0, WOOL:{id}", "2, 3, -1, WOOL:{id}", "1, 3, -2, WOOL:{id}", "0, 3, -2, WOOL:{id}", 
                    "-1, 3, -1, WOOL:{id}", "-1, 3, 0, WOOL:{id}", "-1, 3, 1, WOOL:{id}", "0, 3, 2, WOOL:{id}", 
                    "1, 3, 2, WOOL:{id}", "2, 3, 1, WOOL:{id}", "0, 3, 0, LADDER:5",
                    
                    "2, 4, 0, WOOL:{id}", "2, 4, -1, WOOL:{id}", "1, 4, -2, WOOL:{id}", "0, 4, -2, WOOL:{id}", 
                    "-1, 4, -1, WOOL:{id}", "-1, 4, 0, WOOL:{id}", "-1, 4, 1, WOOL:{id}", "0, 4, 2, WOOL:{id}", 
                    "1, 4, 2, WOOL:{id}", "2, 4, 1, WOOL:{id}", "0, 4, 0, LADDER:5",
                    
                    "-1, 5, -2, WOOL:{id}", "0, 5, -2, WOOL:{id}", "1, 5, -2, WOOL:{id}", "2, 5, -2, WOOL:{id}", 
                    "-1, 5, -1, WOOL:{id}", "0, 5, -1, WOOL:{id}", "1, 5, -1, WOOL:{id}", "2, 5, -1, WOOL:{id}", 
                    "-1, 5, 0, WOOL:{id}", "1, 5, 0, WOOL:{id}", "2, 5, 0, WOOL:{id}", "-1, 5, 1, WOOL:{id}", 
                    "0, 5, 0, LADDER:5", "0, 5, 1, WOOL:{id}", "1, 5, 1, WOOL:{id}", "2, 5, 1, WOOL:{id}", 
                    "-1, 5, 2, WOOL:{id}", "0, 5, 2, WOOL:{id}", "1, 5, 2, WOOL:{id}", "2, 5, 2, WOOL:{id}", 
                    
                    "2, 5, -3, WOOL:{id}", "2, 6, -3, WOOL:{id}", "2, 7, -3, WOOL:{id}", "1, 6, -3, WOOL:{id}", 
                    "0, 6, -3, WOOL:{id}", "-1, 5, -3, WOOL:{id}", "-1, 6, -3, WOOL:{id}", "-1, 7, -3, WOOL:{id}", 
                    "-2, 5, -2, WOOL:{id}", "-2, 6, -2, WOOL:{id}", "-2, 7, -2, WOOL:{id}", "-2, 6, -1, WOOL:{id}", 
                    "-2, 5, 0, WOOL:{id}", "-2, 6, 0, WOOL:{id}", "-2, 7, 0, WOOL:{id}", "-2, 6, 1, WOOL:{id}", 
                    "-2, 5, 2, WOOL:{id}", "-2, 6, 2, WOOL:{id}", "-2, 7, 2, WOOL:{id}", "2, 5, 3, WOOL:{id}", 
                    "2, 6, 3, WOOL:{id}", "2, 7, 3, WOOL:{id}", "1, 6, 3, WOOL:{id}", "0, 6, 3, WOOL:{id}", 
                    "-1, 5, 3, WOOL:{id}", "-1, 6, 3, WOOL:{id}", "-1, 7, 3, WOOL:{id}", "3, 5, -2, WOOL:{id}", 
                    "3, 6, -2, WOOL:{id}", "3, 7, -2, WOOL:{id}", "3, 6, -1, WOOL:{id}", "3, 5, 0, WOOL:{id}", 
                    "3, 6, 0, WOOL:{id}", "3, 7, 0, WOOL:{id}", "3, 6, 1, WOOL:{id}", "3, 5, 2, WOOL:{id}", 
                    "3, 6, 2, WOOL:{id}", "3, 7, 2, WOOL:{id}"
                };
                break;
            }
            case "north": {
                blocks = new String[]{
                    "-1, 0, -2, WOOL:{id}", "-2, 0, -1, WOOL:{id}", "-2, 0, 0, WOOL:{id}", "-1, 0, 1, WOOL:{id}", 
                    "0, 0, 1, WOOL:{id}", "1, 0, 1, WOOL:{id}", "2, 0, 0, WOOL:{id}", "2, 0, -1, WOOL:{id}", 
                    "1, 0, -2, WOOL:{id}", "0, 0, 0, LADDER:2",
                    
                    "-1, 1, -2, WOOL:{id}", "-2, 1, -1, WOOL:{id}", "-2, 1, 0, WOOL:{id}", "-1, 1, 1, WOOL:{id}", 
                    "0, 1, 1, WOOL:{id}", "1, 1, 1, WOOL:{id}", "2, 1, 0, WOOL:{id}", "2, 1, -1, WOOL:{id}", 
                    "1, 1, -2, WOOL:{id}", "0, 1, 0, LADDER:2",
                    
                    "-1, 2, -2, WOOL:{id}", "-2, 2, -1, WOOL:{id}", "-2, 2, 0, WOOL:{id}", "-1, 2, 1, WOOL:{id}", 
                    "0, 2, 1, WOOL:{id}", "1, 2, 1, WOOL:{id}", "2, 2, 0, WOOL:{id}", "2, 2, -1, WOOL:{id}", 
                    "1, 2, -2, WOOL:{id}", "0, 2, 0, LADDER:2",
                    
                    "0, 3, -2, WOOL:{id}", "-1, 3, -2, WOOL:{id}", "-2, 3, -1, WOOL:{id}", "-2, 3, 0, WOOL:{id}", 
                    "-1, 3, 1, WOOL:{id}", "0, 3, 1, WOOL:{id}", "1, 3, 1, WOOL:{id}", "2, 3, 0, WOOL:{id}", 
                    "2, 3, -1, WOOL:{id}", "1, 3, -2, WOOL:{id}", "0, 3, 0, LADDER:2",
                    
                    "0, 4, -2, WOOL:{id}", "-1, 4, -2, WOOL:{id}", "-2, 4, -1, WOOL:{id}", "-2, 4, 0, WOOL:{id}", 
                    "-1, 4, 1, WOOL:{id}", "0, 4, 1, WOOL:{id}", "1, 4, 1, WOOL:{id}", "2, 4, 0, WOOL:{id}", 
                    "2, 4, -1, WOOL:{id}", "1, 4, -2, WOOL:{id}", "0, 4, 0, LADDER:2",
                    
                    "-2, 5, 1, WOOL:{id}", "-2, 5, 0, WOOL:{id}", "-2, 5, -1, WOOL:{id}", "-2, 5, -2, WOOL:{id}", 
                    "-1, 5, 1, WOOL:{id}", "-1, 5, 0, WOOL:{id}", "-1, 5, -1, WOOL:{id}", "-1, 5, -2, WOOL:{id}", 
                    "0, 5, 1, WOOL:{id}", "0, 5, -1, WOOL:{id}", "0, 5, -2, WOOL:{id}", "1, 5, 1, WOOL:{id}", 
                    "0, 5, 0, LADDER:2", "1, 5, 0, WOOL:{id}", "1, 5, -1, WOOL:{id}", "1, 5, -2, WOOL:{id}", 
                    "2, 5, 1, WOOL:{id}", "2, 5, 0, WOOL:{id}", "2, 5, -1, WOOL:{id}", "2, 5, -2, WOOL:{id}",
                    
                    "-3, 5, -2, WOOL:{id}", "-3, 6, -2, WOOL:{id}", "-3, 7, -2, WOOL:{id}", "-3, 6, -1, WOOL:{id}", 
                    "-3, 6, 0, WOOL:{id}", "-3, 5, 1, WOOL:{id}", "-3, 6, 1, WOOL:{id}", "-3, 7, 1, WOOL:{id}", 
                    "-2, 5, 2, WOOL:{id}", "-2, 6, 2, WOOL:{id}", "-2, 7, 2, WOOL:{id}", "-1, 6, 2, WOOL:{id}", 
                    "0, 5, 2, WOOL:{id}", "0, 6, 2, WOOL:{id}", "0, 7, 2, WOOL:{id}", "1, 6, 2, WOOL:{id}", 
                    "2, 5, 2, WOOL:{id}", "2, 6, 2, WOOL:{id}", "2, 7, 2, WOOL:{id}", "3, 5, -2, WOOL:{id}", 
                    "3, 6, -2, WOOL:{id}", "3, 7, -2, WOOL:{id}", "3, 6, -1, WOOL:{id}", "3, 6, 0, WOOL:{id}", 
                    "3, 5, 1, WOOL:{id}", "3, 6, 1, WOOL:{id}", "3, 7, 1, WOOL:{id}", "-2, 5, -3, WOOL:{id}", 
                    "-2, 6, -3, WOOL:{id}", "-2, 7, -3, WOOL:{id}", "-1, 6, -3, WOOL:{id}", "0, 5, -3, WOOL:{id}", 
                    "0, 6, -3, WOOL:{id}", "0, 7, -3, WOOL:{id}", "1, 6, -3, WOOL:{id}", "2, 5, -3, WOOL:{id}", 
                    "2, 6, -3, WOOL:{id}", "2, 7, -3, WOOL:{id}"
                };
                break;
            }
            case "south": {
                blocks = new String[]{
                    "1, 0, 2, WOOL:{id}", "2, 0, 1, WOOL:{id}", "2, 0, 0, WOOL:{id}", "1, 0, -1, WOOL:{id}", 
                    "0, 0, -1, WOOL:{id}", "-1, 0, -1, WOOL:{id}", "-2, 0, 0, WOOL:{id}", "-2, 0, 1, WOOL:{id}", 
                    "-1, 0, 2, WOOL:{id}", "0, 0, 0, LADDER:3",
                    
                    "1, 1, 2, WOOL:{id}", "2, 1, 1, WOOL:{id}", "2, 1, 0, WOOL:{id}", "1, 1, -1, WOOL:{id}", 
                    "0, 1, -1, WOOL:{id}", "-1, 1, -1, WOOL:{id}", "-2, 1, 0, WOOL:{id}", "-2, 1, 1, WOOL:{id}", 
                    "-1, 1, 2, WOOL:{id}", "0, 1, 0, LADDER:3",
                    
                    "1, 2, 2, WOOL:{id}", "2, 2, 1, WOOL:{id}", "2, 2, 0, WOOL:{id}", "1, 2, -1, WOOL:{id}", 
                    "0, 2, -1, WOOL:{id}", "-1, 2, -1, WOOL:{id}", "-2, 2, 0, WOOL:{id}", "-2, 2, 1, WOOL:{id}", 
                    "-1, 2, 2, WOOL:{id}", "0, 2, 0, LADDER:3",
                    
                    "0, 3, 2, WOOL:{id}", "1, 3, 2, WOOL:{id}", "2, 3, 1, WOOL:{id}", "2, 3, 0, WOOL:{id}", 
                    "1, 3, -1, WOOL:{id}", "0, 3, -1, WOOL:{id}", "-1, 3, -1, WOOL:{id}", "-2, 3, 0, WOOL:{id}", 
                    "-2, 3, 1, WOOL:{id}", "-1, 3, 2, WOOL:{id}", "0, 3, 0, LADDER:3",
                    
                    "0, 4, 2, WOOL:{id}", "1, 4, 2, WOOL:{id}", "2, 4, 1, WOOL:{id}", "2, 4, 0, WOOL:{id}", 
                    "1, 4, -1, WOOL:{id}", "0, 4, -1, WOOL:{id}", "-1, 4, -1, WOOL:{id}", "-2, 4, 0, WOOL:{id}", 
                    "-2, 4, 1, WOOL:{id}", "-1, 4, 2, WOOL:{id}", "0, 4, 0, LADDER:3",
                    
                    "2, 5, -1, WOOL:{id}", "2, 5, 0, WOOL:{id}", "2, 5, 1, WOOL:{id}", "2, 5, 2, WOOL:{id}", 
                    "1, 5, -1, WOOL:{id}", "1, 5, 0, WOOL:{id}", "1, 5, 1, WOOL:{id}", "1, 5, 2, WOOL:{id}", 
                    "0, 5, -1, WOOL:{id}", "0, 5, 1, WOOL:{id}", "0, 5, 2, WOOL:{id}", "-1, 5, -1, WOOL:{id}", 
                    "0, 5, 0, LADDER:3", "-1, 5, 0, WOOL:{id}", "-1, 5, 1, WOOL:{id}", "-1, 5, 2, WOOL:{id}", 
                    "-2, 5, -1, WOOL:{id}", "-2, 5, 0, WOOL:{id}", "-2, 5, 1, WOOL:{id}", "-2, 5, 2, WOOL:{id}",
                    
                    "3, 5, 2, WOOL:{id}", "3, 6, 2, WOOL:{id}", "3, 7, 2, WOOL:{id}", "3, 6, 1, WOOL:{id}", 
                    "3, 6, 0, WOOL:{id}", "3, 5, -1, WOOL:{id}", "3, 6, -1, WOOL:{id}", "3, 7, -1, WOOL:{id}", 
                    "2, 5, -2, WOOL:{id}", "2, 6, -2, WOOL:{id}", "2, 7, -2, WOOL:{id}", "1, 6, -2, WOOL:{id}", 
                    "0, 5, -2, WOOL:{id}", "0, 6, -2, WOOL:{id}", "0, 7, -2, WOOL:{id}", "-1, 6, -2, WOOL:{id}", 
                    "-2, 5, -2, WOOL:{id}", "-2, 6, -2, WOOL:{id}", "-2, 7, -2, WOOL:{id}", "-3, 5, 2, WOOL:{id}", 
                    "-3, 6, 2, WOOL:{id}", "-3, 7, 2, WOOL:{id}", "-3, 6, 1, WOOL:{id}", "-3, 6, 0, WOOL:{id}", 
                    "-3, 5, -1, WOOL:{id}", "-3, 6, -1, WOOL:{id}", "-3, 7, -1, WOOL:{id}", "2, 5, 3, WOOL:{id}", 
                    "2, 6, 3, WOOL:{id}", "2, 7, 3, WOOL:{id}", "1, 6, 3, WOOL:{id}", "0, 5, 3, WOOL:{id}", 
                    "0, 6, 3, WOOL:{id}", "0, 7, 3, WOOL:{id}", "-1, 6, 3, WOOL:{id}", "-2, 5, 3, WOOL:{id}", 
                    "-2, 6, 3, WOOL:{id}", "-2, 7, 3, WOOL:{id}"
                };
                break;
            }
            case "west": {
                blocks = new String[]{
                    "-2, 0, 1, WOOL:{id}", "-1, 0, 2, WOOL:{id}", "0, 0, 2, WOOL:{id}", "1, 0, 1, WOOL:{id}", 
                    "1, 0, 0, WOOL:{id}", "1, 0, -1, WOOL:{id}", "0, 0, -2, WOOL:{id}", "-1, 0, -2, WOOL:{id}", 
                    "-2, 0, -1, WOOL:{id}", "0, 0, 0, LADDER:4",
                    
                    "-2, 1, 1, WOOL:{id}", "-1, 1, 2, WOOL:{id}", "0, 1, 2, WOOL:{id}", "1, 1, 1, WOOL:{id}", 
                    "1, 1, 0, WOOL:{id}", "1, 1, -1, WOOL:{id}", "0, 1, -2, WOOL:{id}", "-1, 1, -2, WOOL:{id}", 
                    "-2, 1, -1, WOOL:{id}", "0, 1, 0, LADDER:4",
                    
                    "-2, 2, 1, WOOL:{id}", "-1, 2, 2, WOOL:{id}", "0, 2, 2, WOOL:{id}", "1, 2, 1, WOOL:{id}", 
                    "1, 2, 0, WOOL:{id}", "1, 2, -1, WOOL:{id}", "0, 2, -2, WOOL:{id}", "-1, 2, -2, WOOL:{id}", 
                    "-2, 2, -1, WOOL:{id}", "0, 2, 0, LADDER:4",
                    
                    "-2, 3, 0, WOOL:{id}", "-2, 3, 1, WOOL:{id}", "-1, 3, 2, WOOL:{id}", "0, 3, 2, WOOL:{id}", 
                    "1, 3, 1, WOOL:{id}", "1, 3, 0, WOOL:{id}", "1, 3, -1, WOOL:{id}", "0, 3, -2, WOOL:{id}", 
                    "-1, 3, -2, WOOL:{id}", "-2, 3, -1, WOOL:{id}", "0, 3, 0, LADDER:4",
                    
                    "-2, 4, 0, WOOL:{id}", "-2, 4, 1, WOOL:{id}", "-1, 4, 2, WOOL:{id}", "0, 4, 2, WOOL:{id}", 
                    "1, 4, 1, WOOL:{id}", "1, 4, 0, WOOL:{id}", "1, 4, -1, WOOL:{id}", "0, 4, -2, WOOL:{id}", 
                    "-1, 4, -2, WOOL:{id}", "-2, 4, -1, WOOL:{id}", "0, 4, 0, LADDER:4",
                    
                    "1, 5, 2, WOOL:{id}", "0, 5, 2, WOOL:{id}", "-1, 5, 2, WOOL:{id}", "-2, 5, 2, WOOL:{id}", 
                    "1, 5, 1, WOOL:{id}", "0, 5, 1, WOOL:{id}", "-1, 5, 1, WOOL:{id}", "-2, 5, 1, WOOL:{id}", 
                    "1, 5, 0, WOOL:{id}", "-1, 5, 0, WOOL:{id}", "-2, 5, 0, WOOL:{id}", "1, 5, -1, WOOL:{id}", 
                    "0, 5, -1, WOOL:{id}", "-1, 5, -1, WOOL:{id}", "-2, 5, -1, WOOL:{id}", "0, 5, 0, LADDER:4", 
                    "1, 5, -2, WOOL:{id}", "0, 5, -2, WOOL:{id}", "-1, 5, -2, WOOL:{id}", "-2, 5, -2, WOOL:{id}",
                    
                    "-2, 5, 3, WOOL:{id}", "-2, 6, 3, WOOL:{id}", "-2, 7, 3, WOOL:{id}", "-1, 6, 3, WOOL:{id}", 
                    "0, 6, 3, WOOL:{id}", "1, 5, 3, WOOL:{id}", "1, 6, 3, WOOL:{id}", "1, 7, 3, WOOL:{id}", 
                    "2, 5, 2, WOOL:{id}", "2, 6, 2, WOOL:{id}", "2, 7, 2, WOOL:{id}", "2, 6, 1, WOOL:{id}", 
                    "2, 5, 0, WOOL:{id}", "2, 6, 0, WOOL:{id}", "2, 7, 0, WOOL:{id}", "2, 6, -1, WOOL:{id}", 
                    "2, 5, -2, WOOL:{id}", "2, 6, -2, WOOL:{id}", "2, 7, -2, WOOL:{id}", "-2, 5, -3, WOOL:{id}", 
                    "-2, 6, -3, WOOL:{id}", "-2, 7, -3, WOOL:{id}", "-1, 6, -3, WOOL:{id}", "0, 6, -3, WOOL:{id}", 
                    "1, 5, -3, WOOL:{id}", "1, 6, -3, WOOL:{id}", "1, 7, -3, WOOL:{id}", "-3, 5, 2, WOOL:{id}", 
                    "-3, 6, 2, WOOL:{id}", "-3, 7, 2, WOOL:{id}", "-3, 6, 1, WOOL:{id}", "-3, 5, 0, WOOL:{id}", 
                    "-3, 6, 0, WOOL:{id}", "-3, 7, 0, WOOL:{id}", "-3, 6, -1, WOOL:{id}", "-3, 5, -2, WOOL:{id}", 
                    "-3, 6, -2, WOOL:{id}", "-3, 7, -2, WOOL:{id}"
                };
                break;
            }
        }

        new BukkitRunnable() {
            int i = 0;
            int blocksPerTick = 5;

            @Override
            public void run() {
                for (int b = 0; b < blocksPerTick && i < blocks.length; b++, i++) {
                    String[] relativeBlock = blocks[i].split(", ");
                    String[] serialized = relativeBlock[3].split(":");
                    
                    Material type = Material.matchMaterial(serialized[0]);
                    byte data = serialized[1].contains("{id}") 
                        ? woolColor 
                        : (byte) Integer.parseInt(serialized[1]);
                    
                    relativeBlock[3] = "0";
                    int[] xyz = Arrays.stream(relativeBlock).mapToInt(Integer::parseInt).toArray();
                    
                    Block block = center.getRelative(xyz[0], xyz[1], xyz[2]);
                    
                    if (block.getType() == Material.AIR) {
                        if (!arena.isProtectedArea(block.getLocation())) {
                            block.getWorld().playSound(block.getLocation(), Sound.CHICKEN_EGG_POP, 1.0F, 0.5F);
                            block.setType(type);
                            block.setData(data);
                            arena.addRollBack(block, RollbackBlock.RollbackType.PLACE_BLOCK);
                        }
                    }
                }

                if (i >= blocks.length) {
                    this.cancel();
                    blocks = null;
                }
            }
        }.runTaskTimer(BedWars.getPlugin(BedWars.class), 0, 1L);
    }

    private String getPositionFromFace(BlockFace face) {
        switch (face) {
            case NORTH:
                return "north";
            case SOUTH:
                return "south";
            case EAST:
                return "east";
            case WEST:
                return "west";
            default:
                return "north";
        }
    }
}
