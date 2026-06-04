package com.minecraft.arcade.bedwars.structure.generator.objects.type.forge.list;

import com.minecraft.arcade.bedwars.BedWars;
import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.arcade.bedwars.structure.generator.objects.type.forge.ForgeGenerator;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class IronForge extends ForgeGenerator {

    public IronForge(Arena arena, Team team) {
        super(BedOre.IRON, arena, team, TimeUnit.MILLISECONDS.toMillis(400)); // Ultra rápido: 0.4 segundos

        setMaxStackSize(64);
    }

    @Override
    public void spawn() {
        Location center = getLocation();

        if (center != null) {
            Location[] relatives = Cuboid.getRelativeLocations(center);

            GeneratorLevel level = getLevel();

            switch (getArena().getSlot()) {
                case DUO: {
                    Location[] twoLocations = {relatives[0], relatives[1]};

                    drop(getArena(), twoLocations);

                    Core.getPlatform().runSync(() -> drop(getArena(), relatives[2], relatives[3]), 15);
                    break;
                }
                case TRIO: {
                    // Dividir em 4 localizações para os 4 times
                    drop(getArena(), relatives);
                    break;
                }
                case QUARTET: {
                    drop(getArena(), relatives);
                    break;
                }
                default: {
                    if (!level.equals(GeneratorLevel.NONE)) {
                        // Definindo localizações específicas para nível ONE
                        Location[] primarySpawn = {relatives[1], relatives[2]},
                                secondarySpawn = {relatives[3], relatives[0]};

                        drop(getArena(), primarySpawn);

                        Core.getPlatform().runSync(() -> drop(getArena(), secondarySpawn), 15);
                    } else
                        sequentialDrops(relatives);
                    break;
                }
            }
        }
    }

    public void sequentialDrops(Location... locations) {
        setDone(false);

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= locations.length) {
                    setDone(true);
                    cancel();
                    return;
                }

                drop(getArena(), locations[index]);
                index++;
            }
        }.runTaskTimer(BedWars.getInstance(), 0, 14); // Intervalo de 15 ticks entre drops
    }
}
