package com.minecraft.game.anticheat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public enum AnticheatCheck {

    FLY(Category.MOVEMENT, "Fly", "fly", Material.ELYTRA,
            "Detecta levitação prolongada no ar."),
    SPEED(Category.MOVEMENT, "Speed", "speed", Material.SUGAR,
            "Velocidade horizontal acima do limite."),
    NO_FALL(Category.MOVEMENT, "NoFall", "no-fall", Material.FEATHER,
            "Queda longa sem acumular dano de queda."),
    HIGH_JUMP(Category.MOVEMENT, "HighJump", "high-jump", Material.RABBIT_FOOT,
            "Salto vertical impossível."),
    JESUS(Category.MOVEMENT, "Jesus", "jesus", Material.WATER_BUCKET,
            "Caminhar sobre água/lava."),
    STEP(Category.MOVEMENT, "Step", "step", Material.STONE_STAIRS,
            "Subir blocos sem pular."),
    ELYTRA(Category.MOVEMENT, "Elytra", "elytra", Material.ELYTRA,
            "Velocidade excessiva com elytra."),
    BLINK(Category.MOVEMENT, "Blink", "blink", Material.ENDER_PEARL,
            "Teleporte instantâneo (delta grande)."),

    REACH(Category.COMBAT, "Reach", "reach", Material.IRON_SWORD,
            "Atacar além do alcance legítimo."),
    KILL_AURA(Category.COMBAT, "KillAura", "kill-aura", Material.NETHERITE_SWORD,
            "Múltiplos alvos em curto intervalo."),
    AUTO_CLICK(Category.COMBAT, "AutoClick", "auto-click", Material.CLOCK,
            "CPS acima do limite humano."),
    NO_SWING(Category.COMBAT, "NoSwing", "no-swing", Material.STICK,
            "Ataque sem animação de braço."),
    VELOCITY(Category.COMBAT, "Velocity", "velocity", Material.PISTON,
            "Resistência a knockback."),

    FAST_BREAK(Category.WORLD, "FastBreak", "fast-break", Material.DIAMOND_PICKAXE,
            "Quebrar blocos mais rápido que o permitido."),
    FAST_PLACE(Category.WORLD, "FastPlace", "fast-place", Material.OAK_PLANKS,
            "Colocar blocos em rajada."),
    SCAFFOLD(Category.WORLD, "Scaffold", "scaffold", Material.SCAFFOLDING,
            "Construir em queda (scaffold/tower)."),

    FAST_EAT(Category.PLAYER, "FastEat", "fast-eat", Material.COOKED_BEEF,
            "Comer mais rápido que o normal."),
    FAST_BOW(Category.PLAYER, "FastBow", "fast-bow", Material.BOW,
            "Atirar arco sem tempo de puxada."),
    TIMER(Category.PLAYER, "Timer", "timer", Material.REPEATER,
            "Pacotes de movimento acelerados."),
    INVENTORY_MOVE(Category.PLAYER, "InvMove", "inventory-move", Material.CHEST,
            "Mover-se rápido com inventário aberto.");

    @Getter
    @RequiredArgsConstructor
    public enum Category {
        MOVEMENT("§bMovimento", Material.FEATHER),
        COMBAT("§cCombate", Material.IRON_SWORD),
        WORLD("§6Mundo", Material.GRASS_BLOCK),
        PLAYER("§dJogador", Material.PLAYER_HEAD);

        private final String display;
        private final Material icon;
    }

    private final Category category;
    private final String displayName;
    private final String configKey;
    private final Material icon;
    private final String description;

    public static AnticheatCheck fromConfigKey(String key) {
        if (key == null) return null;
        for (AnticheatCheck c : values()) {
            if (c.configKey.equalsIgnoreCase(key)) return c;
        }
        return null;
    }
}
