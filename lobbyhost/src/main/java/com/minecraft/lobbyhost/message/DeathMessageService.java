package com.minecraft.lobbyhost.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;

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
        return Messages.component("death.generic", lang, Map.of("player", victim.getName()));
    }

    public static String advancementTitle(Component title, PlayerLanguage lang) {
        if (title == null) return "";
        return PLAIN.serialize(GlobalTranslator.render(title, lang.locale()));
    }
}
