package com.minecraft.game.bukkit.cosmetic;

import com.minecraft.game.GamePlugin;
import com.minecraft.game.account.context.objects.cosmetic.Gadget;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GadgetHandler {

    private final Map<Block, Material> changedBlocks = new ConcurrentHashMap<>();

    public void use(Player player, Gadget gadget) {
        if (player == null || gadget == null) return;

        switch (gadget) {
            case FIREWORK_LAUNCHER:
                executeFirework(player);
                break;
            case ICE_WALKER:
                executeIceWalker(player);
                break;
            case JUKEBOX:
                executeJukebox(player);
                break;
            case TRAMPOLINE:
                executeTrampoline(player);
                break;
            case ENDER_PEARL:
                executeEnderPearl(player);
                break;
        }
    }

    private void executeFirework(Player player) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection();
        Location spawnLoc = eye.clone().add(dir.multiply(0.5));
        Firework fw = player.getWorld().spawn(spawnLoc, Firework.class);
        
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.fromRGB((int)(Math.random()*256), (int)(Math.random()*256), (int)(Math.random()*256)))
                .withFade(Color.fromRGB((int)(Math.random()*256), (int)(Math.random()*256), (int)(Math.random()*256)))
                .with(FireworkEffect.Type.BALL)
                .trail(true)
                .flicker(true)
                .build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
        
        // Dá velocidade para a frente baseada na direção do jogador
        fw.setVelocity(dir.multiply(0.8));
    }

    private void executeIceWalker(Player player) {
        Location loc = player.getLocation().subtract(0, 1, 0);
        List<Block> toChange = new ArrayList<>();
        
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // Formato circular
                if (x*x + z*z > 5) continue;
                Block b = loc.clone().add(x, 0, z).getBlock();
                if (b.getType().isSolid() && b.getType() != Material.ICE && b.getType() != Material.PACKED_ICE && b.getType() != Material.BARRIER) {
                    toChange.add(b);
                }
            }
        }

        for (Block b : toChange) {
            if (!changedBlocks.containsKey(b)) {
                Material original = b.getType();
                changedBlocks.put(b, original);
                b.setType(Material.PACKED_ICE);
                
                // Reverte após 3 segundos (60 ticks)
                Bukkit.getScheduler().runTaskLater(GamePlugin.getInstance(), () -> {
                    if (changedBlocks.containsKey(b)) {
                        b.setType(original);
                        changedBlocks.remove(b);
                    }
                }, 60L);
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.2f);
    }

    private void executeJukebox(Player player) {
        for (int i = 0; i < 5; i++) {
            final int index = i;
            Bukkit.getScheduler().runTaskLater(GamePlugin.getInstance(), () -> {
                if (player.isOnline()) {
                    float pitch = 0.5f + (index * 0.15f);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, pitch);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, pitch + 0.2f);
                }
            }, i * 3L);
        }
    }

    private void executeTrampoline(Player player) {
        Location feet = player.getLocation().subtract(0, 1, 0);
        Block block = feet.getBlock();
        
        if (block.getType().isSolid() && block.getType() != Material.BARRIER) {
            Material original = block.getType();
            block.setType(Material.SLIME_BLOCK);
            
            player.setVelocity(new Vector(0, 1.4, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.0f, 1.0f);
            
            // Reverte após 1.5 segundos (30 ticks)
            Bukkit.getScheduler().runTaskLater(GamePlugin.getInstance(), () -> {
                block.setType(original);
            }, 30L);
        } else {
            // Lança mesmo no ar se não puder colocar bloco
            player.setVelocity(new Vector(0, 1.2, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.0f, 1.0f);
        }
    }

    private void executeEnderPearl(Player player) {
        Location loc = player.getLocation();
        Vector dir = loc.getDirection();
        Location target = loc.clone().add(dir.multiply(12));
        
        // Raytrace para não atravessar paredes
        org.bukkit.util.RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(), dir, 12, org.bukkit.FluidCollisionMode.NEVER, true
        );
        
        if (result != null && result.getHitBlock() != null) {
            target = result.getHitPosition().toLocation(player.getWorld()).subtract(dir.normalize().multiply(0.5));
        }
        
        target.setYaw(loc.getYaw());
        target.setPitch(loc.getPitch());
        
        player.teleport(target);
        player.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, target, 30, 0.5, 1.0, 0.5, 0.1);
    }

    public void shutdown() {
        // Restaura todos os blocos alterados antes de desligar
        changedBlocks.forEach((block, material) -> block.setType(material));
        changedBlocks.clear();
    }
}
