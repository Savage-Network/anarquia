package com.minecraft.game.anticheat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public enum AnticheatCheck {

    FLY(Category.MOVEMENT, "Fly", "fly", Material.ELYTRA,
            "Levitação prolongada no ar sem suporte."),
    SPEED(Category.MOVEMENT, "Speed", "speed", Material.SUGAR,
            "Velocidade horizontal acima do limite permitido."),
    NO_FALL(Category.MOVEMENT, "NoFall", "no-fall", Material.FEATHER,
            "Queda longa sem acumular dano de queda."),
    HIGH_JUMP(Category.MOVEMENT, "HighJump", "high-jump", Material.RABBIT_FOOT,
            "Salto vertical impossível."),
    JESUS(Category.MOVEMENT, "Jesus", "jesus", Material.WATER_BUCKET,
            "Caminhar sobre água/lava."),
    STEP(Category.MOVEMENT, "Step", "step", Material.STONE_STAIRS,
            "Subir blocos sem pular (step hack)."),
    ELYTRA(Category.MOVEMENT, "Elytra", "elytra", Material.ELYTRA,
            "Velocidade excessiva com elytra."),
    BLINK(Category.MOVEMENT, "Blink", "blink", Material.ENDER_PEARL,
            "Teleporte instantâneo (delta grande)."),
    GROUND_SPOOF(Category.MOVEMENT, "GroundSpoof", "ground-spoof", Material.LIGHT_GRAY_CARPET,
            "Cliente reporta no chão estando no ar."),
    NO_SLOW(Category.MOVEMENT, "NoSlow", "no-slow", Material.COBWEB,
            "Mover rápido em cobweb/soul sand/honey."),
    STRAFE(Category.MOVEMENT, "Strafe", "strafe", Material.ICE,
            "Mudança brusca de direção em alta velocidade."),

    REACH(Category.COMBAT, "Reach", "reach", Material.IRON_SWORD,
            "Atacar além do alcance legítimo (centro)."),
    HITBOX(Category.COMBAT, "Hitbox", "hitbox", Material.NETHERITE_SWORD,
            "Atacar fora da hitbox do alvo (reach AABB)."),
    WALL_HIT(Category.COMBAT, "WallHit", "wall-hit", Material.STONE,
            "Atacar através de blocos sólidos."),
    KILL_AURA(Category.COMBAT, "KillAura", "kill-aura", Material.DIAMOND_SWORD,
            "Múltiplos alvos em curto intervalo."),
    AUTO_CLICK(Category.COMBAT, "AutoClick", "auto-click", Material.CLOCK,
            "CPS acima do limite humano."),
    NO_SWING(Category.COMBAT, "NoSwing", "no-swing", Material.STICK,
            "Ataque sem animação de braço."),
    VELOCITY(Category.COMBAT, "Velocity", "velocity", Material.PISTON,
            "Resistência a knockback (anti-KB)."),
    CRITICALS(Category.COMBAT, "Criticals", "criticals", Material.GOLDEN_SWORD,
            "Crítico impossível (sem queda/onGround)."),
    AIM_ASSIST(Category.COMBAT, "AimAssist", "aim-assist", Material.SPYGLASS,
            "Snap de rotação suspeito ao acertar."),
    MULTI_HIT(Category.COMBAT, "MultiHit", "multi-hit", Material.TNT,
            "Múltiplos hits no mesmo tick."),

    FAST_BREAK(Category.WORLD, "FastBreak", "fast-break", Material.DIAMOND_PICKAXE,
            "Quebrar blocos mais rápido que o permitido."),
    FAST_PLACE(Category.WORLD, "FastPlace", "fast-place", Material.OAK_PLANKS,
            "Colocar blocos em rajada."),
    SCAFFOLD(Category.WORLD, "Scaffold", "scaffold", Material.SCAFFOLDING,
            "Construir em queda (scaffold/tower)."),
    NUKER(Category.WORLD, "Nuker", "nuker", Material.NETHERITE_PICKAXE,
            "Quebrar muitos blocos por segundo."),
    AIR_PLACE(Category.WORLD, "AirPlace", "air-place", Material.GRASS_BLOCK,
            "Colocar bloco sem suporte adjacente."),
    GHOST_HAND(Category.WORLD, "GhostHand", "ghost-hand", Material.ARMOR_STAND,
            "Interagir/quebrar além do alcance de braço."),

    FAST_EAT(Category.PLAYER, "FastEat", "fast-eat", Material.COOKED_BEEF,
            "Comer mais rápido que o normal."),
    FAST_BOW(Category.PLAYER, "FastBow", "fast-bow", Material.BOW,
            "Atirar arco sem tempo de puxada."),
    TIMER(Category.PLAYER, "Timer", "timer", Material.REPEATER,
            "Pacotes de movimento acelerados (timer)."),
    INVENTORY_MOVE(Category.PLAYER, "InvMove", "inventory-move", Material.CHEST,
            "Mover-se rápido com inventário aberto."),
    AUTO_TOTEM(Category.PLAYER, "AutoTotem", "auto-totem", Material.TOTEM_OF_UNDYING,
            "Trocar totem na offhand instantaneamente."),
    AUTO_ARMOR(Category.PLAYER, "AutoArmor", "auto-armor", Material.DIAMOND_CHESTPLATE,
            "Equipar armadura instantaneamente."),
    MULTI_ACTION(Category.PLAYER, "MultiAction", "multi-action", Material.STRUCTURE_BLOCK,
            "Ações simultâneas impossíveis (hit+break+place).");

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
