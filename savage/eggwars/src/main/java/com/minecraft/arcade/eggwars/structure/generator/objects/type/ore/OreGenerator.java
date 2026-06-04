package com.minecraft.arcade.eggwars.structure.generator.objects.type.ore;

import com.minecraft.arcade.eggwars.EggWars;
import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.structure.generator.Generator;
import com.minecraft.arcade.eggwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.hologram.touch.Touch;
import com.minecraft.core.bukkit.api.hologram.touch.TouchHandler;
import com.minecraft.arcade.eggwars.structure.generator.objects.EggOre;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.Arrays;

@Getter
@Setter
public abstract class OreGenerator extends Generator {

    private final int id;

    private final Arena arena;

    private HologramServer hologram;
    
    private ArmorStand armorStand;

    private int updateTime, time;

    public OreGenerator(int id, Arena arena, EggOre ore, int updateTime) {
        this(id, arena, ore, updateTime, false);
    }

    public OreGenerator(int id, Arena arena, EggOre ore, int updateTime, boolean startBroken) {
        super(ore, arena.getLocation("ore_generator_" + ore.name().toLowerCase() + "_" + id)); // ore_generator_diamond_1

        this.id = id;
        this.arena = arena;

        this.updateTime = updateTime;
        this.time = updateTime;

        // Se for gerador do meio (ouro/diamante), começar quebrado
        setLevel(startBroken ? GeneratorLevel.NONE : GeneratorLevel.ONE);

        buildHeadRotation();
    }

    public abstract void spawn();
    
    /**
     * Retorna o tempo de geração baseado no nível atual do gerador
     */
    public abstract int getUpdateTimeForLevel(GeneratorLevel level);
    
    /**
     * Retorna o custo de upgrade para o próximo nível
     */
    public abstract int getUpgradeCost(GeneratorLevel nextLevel);
    
    /**
     * Retorna o material necessário para upgrade
     */
    public abstract Material getUpgradeMaterial();

    @Override
    public boolean hasPendentUpdate() {
        // Geradores quebrados não atualizam
        if (getLevel() == GeneratorLevel.NONE) {
            return false;
        }
        return time <= 0;
    }
    
    /**
     * Atualiza o tempo de geração quando o nível muda
     */
    public void updateTimeForLevel() {
        int newTime = getUpdateTimeForLevel(getLevel());
        setUpdateTime(newTime);
        setTime(newTime);
    }

    public String getIdentifier() {
        return "ore_generator_" + getOre().name().toLowerCase() + "_" + id;
    }

    public void buildHeadRotation() {
        Location location = getLocation();

        if (location != null) {
            location = arena.getMap().isNormal() ? location.clone().add(0.5, -1.7, 0.5) : Cuboid.getBlockCenter(location);

            Material blockMaterial = getOre().getMaterial();
            if (getOre() == EggOre.IRON) {
                blockMaterial = Material.IRON_BLOCK;
            } else if (getOre() == EggOre.GOLD) {
                blockMaterial = Material.GOLD_BLOCK;
            } else if (getOre() == EggOre.DIAMOND) {
                blockMaterial = Material.DIAMOND_BLOCK;
            }

            Location armorStandLoc = location.clone().add(0, 1.2, 0);
            this.armorStand = (ArmorStand) location.getWorld().spawnEntity(armorStandLoc, EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setBasePlate(false);
            armorStand.setArms(false);
            armorStand.setSmall(false);
            armorStand.setMarker(true);
            armorStand.setCustomNameVisible(false);
            armorStand.setHelmet(new ItemStack(blockMaterial));

            Location hologramLoc = armorStandLoc.clone().add(0, 0.5, 0);
            this.hologram = BukkitCore.getManager().getHologram().spawnServer(getIdentifier(), hologramLoc);
            updateHologramText();

            hologram.setTouch((player, touch) -> {
                if (touch == com.minecraft.core.bukkit.api.hologram.touch.Touch.RIGHT) {
                    new com.minecraft.arcade.eggwars.menu.generator.GeneratorUpgradeMenu(player, this).handle();
                }
            });

            startRotation();
        }
    }
    
    private void startRotation() {
        if (armorStand == null) return;
        
        Bukkit.getScheduler().runTaskTimer(EggWars.getInstance(), () -> {
            if (armorStand == null || armorStand.isDead()) {
                return;
            }
            
            float yaw = armorStand.getLocation().getYaw() + 5;
            Location loc = armorStand.getLocation();
            loc.setYaw(yaw);
            armorStand.teleport(loc);
        }, 1, 1);
    }
    
    public void remove() {
        if (hologram != null) {
            for (Player viewer : hologram.getViewers()) {
                hologram.despawnTo(viewer);
            }
            BukkitCore.getManager().getHologram().getHolograms().remove(hologram.getId());
        }
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.remove();
        }
    }

    public void updateHologramText() {
        if (hologram == null) return;
        
        // Se o gerador estiver quebrado, mostrar mensagem especial
        if (getLevel() == GeneratorLevel.NONE) {
            hologram.setText(Arrays.asList(
                    "§c§l✖ Quebrado!",
                    getOre().getColor() + "Gerador de " + getOre().getOreName(),
                    "§7Clique para reparar!"
            ));
            return;
        }
        
        int droppedCount = Cuboid.getDroppedItemsCount(getLocation(), getOre().getMaterial(), 2, 1, 2);

        hologram.setText(Arrays.asList(getOre().getColor() + "Gerador de " + getOre().getOreName() + " " + droppedCount + "x",
                "§7Nível " + getLevel().getTag()));
    }
}
