package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.win;

import com.minecraft.core.Core;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

@Getter
@Setter
public class WinAnimationCollectible extends BedCollectible {

    private static final Random RANDOM = new Random();
    private String animationType;

    public WinAnimationCollectible(CollectibleRarity rarity, String name, Item icon, String animationType, long createdAt) {
        super(BedCollectibleType.WIN_ANIMATION, rarity, name, icon, createdAt);
        this.animationType = animationType;
    }
    
    public void execute(Location location) {
        if (animationType == null) return;
        
        switch (animationType.toUpperCase()) {
            case "FIREWORKS":
                // Lançar fogos de artifício por 3 segundos
                for (int i = 0; i < 6; i++) {
                    final int delay = i * 10; // 0.5 segundos entre cada fogo
                    Core.getPlatform().runSync(() -> spawnRandomFirework(location), delay);
                }
                break;
            case "ENDER_DRAGON":
                // Implementar animação de Ender Dragon no futuro
                break;
            default:
                // Animação padrão: fogos de artifício
                spawnRandomFirework(location);
                break;
        }
    }
    
    private void spawnRandomFirework(Location location) {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        
        meta.setPower(1);
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.fromRGB(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256)))
                .withFade(Color.fromRGB(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256)))
                .flicker(RANDOM.nextBoolean())
                .trail(RANDOM.nextBoolean())
                .build());
        
        firework.setFireworkMeta(meta);
        
        // Detonar após 1 segundo
        Core.getPlatform().runSync(firework::detonate, 20L);
    }
    
    public boolean isNotEqual(Class<? extends WinAnimationCollectible> animationClass) {
        return !animationClass.isAssignableFrom(getClass());
    }
    
    public boolean isValid() {
        return !getClass().getSimpleName().equalsIgnoreCase("Nenhuma");
    }
}
