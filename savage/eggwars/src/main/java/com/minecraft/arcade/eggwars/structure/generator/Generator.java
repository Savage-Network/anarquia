package com.minecraft.arcade.eggwars.structure.generator;

import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.arcade.eggwars.structure.generator.objects.EggOre;
import com.minecraft.core.util.list.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

@Getter
@Setter
public abstract class Generator {

    private final EggOre ore;
    private final Location location;

    private GeneratorLevel level;

    private int maxStackSize;

    public Generator(EggOre ore, Location location) {
        this.ore = ore;
        this.location = location;

        this.level = GeneratorLevel.NONE;

        this.maxStackSize = -1;
    }

    public abstract boolean hasPendentUpdate();

    public ItemStack getDrop() {
        return new ItemStack(getOre().getMaterial());
    }

    public void drop(Arena arena, Location... centers) {
        int currentDroppedItems = Cuboid.getDroppedItemsCount(getLocation(), getOre().getMaterial(), 2, 1, 2);

        if (currentDroppedItems >= getMaxStackSize()) return;

        for (Location center : centers) {
            World world = center.getWorld();

            if (world == null) return;

            // Centraliza a localização exatamente no meio do bloco
            center = arena.getMap().isNormal() ? center.clone().add(0.5, 0, 0.5) : Cuboid.getBlockCenter(center);

            // Dropar abaixo do gerador (onde está o ArmorStand)
            center = center.clone().add(0, 0.2, 0);

            ItemStack stack = getDrop();

            // Adicionar display name único invisível para prevenir merge automático
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                // Usar código único invisível (cor reset + código aleatório)
                String uniqueCode = "§r" + StringUtil.generateExclusiveCode(8);
                meta.setDisplayName(uniqueCode);
            stack.setItemMeta(meta);
            }

            // Dropar com offset aleatório maior para espalhar mais
            double offsetX = (Math.random() - 0.5) * 0.6; // -0.3 a 0.3
            double offsetZ = (Math.random() - 0.5) * 0.6; // -0.3 a 0.3
            Location dropLocation = center.clone().add(offsetX, 0, offsetZ);

            Item item = world.dropItem(dropLocation, stack);

            // Velocidade aleatória maior para espalhar melhor
            double velX = (Math.random() - 0.5) * 0.15;
            double velZ = (Math.random() - 0.5) * 0.15;
            item.setVelocity(new Vector(velX, -0.1, velZ));
            
            // Usar pickupDelay variado para prevenir merge (1.8 compatible)
            item.setPickupDelay((int) (Math.random() * 5) + 5);
        }
    }
}
