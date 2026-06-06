package com.minecraft.game.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;

/** Mensagens de morte traduzidas por idioma do espectador. */
public final class DeathMessageService {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private DeathMessageService() {}

    public static Component build(PlayerDeathEvent event, PlayerLanguage lang) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && !killer.equals(victim)) {
            return Messages.component("death.pvp", lang, Map.of(
                    "victim", victim.getName(),
                    "killer", killer.getName()));
        }

        EntityDamageEvent last = victim.getLastDamageCause();
        if (last instanceof EntityDamageByEntityEvent byEntity) {
            Player attacker = resolvePlayerAttacker(byEntity.getDamager());
            if (attacker != null && !attacker.equals(victim)) {
                return Messages.component("death.pvp", lang, Map.of(
                        "victim", victim.getName(),
                        "killer", attacker.getName()));
            }

            if (byEntity.getDamager() instanceof LivingEntity mob && !(byEntity.getDamager() instanceof Player)) {
                return Messages.component("death.mob", lang, Map.of(
                        "victim", victim.getName(),
                        "mob", entityName(mob, lang)));
            }

            if (byEntity.getDamager() instanceof Projectile) {
                return Messages.component("death.projectile", lang, Map.of("player", victim.getName()));
            }
        }

        EntityDamageEvent.DamageCause cause = last != null
                ? last.getCause()
                : EntityDamageEvent.DamageCause.CUSTOM;

        if (cause == EntityDamageEvent.DamageCause.SUICIDE) {
            return Messages.component("death.suicide", lang, Map.of("player", victim.getName()));
        }

        return Messages.component(causePath(cause), lang, Map.of("player", victim.getName()));
    }

    public static String advancementTitle(Component title, PlayerLanguage lang) {
        if (title == null) return "";
        return PLAIN.serialize(GlobalTranslator.render(title, lang.locale()));
    }

    private static Player resolvePlayerAttacker(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        return null;
    }

    private static String entityName(LivingEntity entity, PlayerLanguage lang) {
        Component custom = entity.customName();
        if (custom != null) {
            return PLAIN.serialize(GlobalTranslator.render(custom, lang.locale()));
        }
        return PLAIN.serialize(GlobalTranslator.render(
                Component.translatable(entity.getType().translationKey()), lang.locale()));
    }

    private static String causePath(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case FALL -> "death.fall";
            case LAVA, MELTING -> "death.lava";
            case DROWNING -> "death.drowning";
            case FIRE -> "death.fire";
            case FIRE_TICK -> "death.fire-tick";
            case SUFFOCATION -> "death.suffocation";
            case STARVATION -> "death.starvation";
            case POISON -> "death.poison";
            case WITHER -> "death.wither";
            case LIGHTNING -> "death.lightning";
            case VOID -> "death.void";
            case BLOCK_EXPLOSION -> "death.block-explosion";
            case ENTITY_EXPLOSION -> "death.entity-explosion";
            case MAGIC -> "death.magic";
            case THORNS -> "death.thorns";
            case FALLING_BLOCK -> "death.falling-block";
            case CONTACT -> "death.contact";
            case HOT_FLOOR -> "death.hot-floor";
            case CRAMMING -> "death.cramming";
            case FREEZE -> "death.freeze";
            case SONIC_BOOM -> "death.sonic-boom";
            case PROJECTILE -> "death.projectile";
            default -> "death.generic";
        };
    }
}
