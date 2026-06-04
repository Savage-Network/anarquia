package com.minecraft.arcade.eggwars.structure.egg;

import com.minecraft.arcade.eggwars.EggWars;
import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.core.arcade.room.map.rollback.RollbackBlock;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Getter
public class EggBridgeTask implements Runnable {

    private final BukkitTask task;

    private final Egg egg;
    private final Player shooter;

    private final Arena arena;
    private final ChatColor color;

    public EggBridgeTask(Egg egg, Player shooter, Arena arena, ChatColor color) {
        this.egg = egg;
        this.shooter = shooter;

        this.arena = arena;
        this.color = color;

        this.task = Bukkit.getScheduler().runTaskTimer(EggWars.getPlugin(EggWars.class), this, 0L, 1L);
    }

    public void end() {
        task.cancel();
    }

    protected void buildBridgeBlocks(Location... locations) {
        for (Location location : locations) {
            Block block = location.getBlock();

            if (block.isEmpty() && !arena.isProtectedArea(location)) {
                block.setType(Material.WOOL);
                block.setData((byte) ColorUtil.getIdByColor(color));

                location.getWorld().playEffect(location, Effect.FLAME, 3);

                arena.addRollBack(block, RollbackBlock.RollbackType.PLACE_BLOCK);

                shooter.playSound(shooter.getLocation(), Sound.CHICKEN_EGG_POP, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public void run() {
        Location local = egg.getLocation(),
                shooterLocal = shooter.getLocation().clone();

        if (egg.isDead() || arena.isProtectedArea(shooter.getLocation()) || shooterLocal.distance(local) > 35 || shooterLocal.getY() - local.getY() > 9) {
            EggBridgeManager.end(egg);
            return;
        }

        if (shooterLocal.distance(local) > 4) {
            buildBridgeBlocks(
                    local.clone().subtract(0, 2, 0),
                    local.clone().subtract(1, 2, 0),
                    local.clone().subtract(0, 2, 1)
            );
        }
    }
}