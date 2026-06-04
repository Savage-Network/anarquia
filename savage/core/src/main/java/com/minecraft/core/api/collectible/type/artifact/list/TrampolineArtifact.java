package com.minecraft.core.api.collectible.type.artifact.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TrampolineArtifact extends ArtifactCollectible {

    private final transient String BLOCK_METADATA = "trampoline_artifact";

    public TrampolineArtifact() {
        super("Trampolim", CollectibleRarity.EPIC, new ArrayList<>(), 1726524324887L);

        setIcon(Item.of(Material.SLIME_BLOCK));
        setLore(Arrays.asList("§7Construa um trampolim", "§7para pular a vontade."));
    }

    @Override
    public void handle(Player host) {
        if (!host.isOnGround()) {
            host.sendMessage("§cVocê precisa estar no chão para usar o Trampolim.");
            return;
        }

        if (hasCooldown(host)) return;

        Location location = host.getLocation().clone();
        Location cube1 = host.getLocation().clone().add(-3.0D, 0.0D, -3.0D);
        Location cube2 = host.getLocation().clone().add(3.0D, 20.0D, 3.0D);

        Block stairs1 = host.getLocation().clone().getBlock().getRelative(-4, 1, 0);
        Block stairs2 = host.getLocation().clone().getBlock().getRelative(-5, 0, 0);

        Cuboid cuboId = new Cuboid(cube1, cube2);
        boolean empty = true;

        for (Block block : cuboId.getBlocks(host.getWorld())) {
            if (block.getType() != Material.AIR) {
                empty = false;
                break;
            }
        }
        for (Entity entity : host.getNearbyEntities(3.0, 3.0, 3.0)) {
            if (entity.hasMetadata("NPC")) {
                empty = false;
                break;
            }
        }

        if (empty && stairs1.getType() == Material.AIR && stairs2.getType() == Material.AIR) {
            Map<Block, String> blocks = new HashMap<>();

            spawnTrampoline(location, blocks);
            getNearbyEntities(location, 3.0).forEach(le -> le.teleport(le.getLocation().add(0, 2.0, 0.0)));

            host.teleport(location.clone().add(0, 5.0, 0));

            Core.getPlatform().runSync(() -> {
                blocks.forEach((block, data) -> {
                    block.setType(Material.AIR);
                    block.removeMetadata(BLOCK_METADATA, Core.getJavaPlugin());

                    block.setData(Byte.parseByte(data.split(":")[1]));
                });

                blocks.clear();
            }, 20 * 30);

            setCooldown(host, TimeUnit.MINUTES.toMillis(2));
        } else {
            host.sendMessage("§cNão é possível colocar um trampolim neste local.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Block block = event.getTo().getBlock().getRelative(BlockFace.DOWN);

        if (block.hasMetadata(BLOCK_METADATA) && block.getType() == Material.WOOL && block.getData() == (byte) 15)
            player.setVelocity(new Vector(0, 3, 0));
    }

    private void spawnTrampoline(Location location, Map<Block, String> blocks) {
        // Set fence blocks
        for (int x = -3; x <= 3; x++) {
            this.setBlock(blocks, location.clone().add(x, 0, 3), Material.FENCE, (byte) 0);
            this.setBlock(blocks, location.clone().add(x, 0, -3), Material.FENCE, (byte) 0);
        }

        // Set wool blocks
        for (int x = -3; x <= 3; x++) {
            for (int z = -2; z <= 2; z++) {
                this.setBlock(blocks, location.clone().add(x, 1, z), Material.WOOL, (byte) (x == 3 || x == -3 ? 11 : 15));
            }
        }

        // Set wood stairs blocks
        this.setBlock(blocks, location.clone().add(-4, 1, 0), Material.WOOD_STAIRS, (byte) 0);
        this.setBlock(blocks, location.clone().add(-5, 0, 0), Material.WOOD_STAIRS, (byte) 0);
    }

    private void setBlock(Map<Block, String> blocks, Location location, Material material, byte data) {
        Block block = location.getBlock();

        block.setType(material);
        block.setMetadata(BLOCK_METADATA, new FixedMetadataValue(Core.getJavaPlugin(), true));

        block.setData(data);

        blocks.put(block, block.getType() + ":" + block.getData());
    }

    private List<LivingEntity> getNearbyEntities(Location location, double range) {
        List<LivingEntity> entities = new ArrayList<>();

        for (Entity entity : location.getWorld().getEntities()) {
            if (entity instanceof LivingEntity && !(entity instanceof ArmorStand) && entity.getLocation().distance(location) <= range)
                entities.add((LivingEntity) entity);
        }

        return entities;
    }
}
