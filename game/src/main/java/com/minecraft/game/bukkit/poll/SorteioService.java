package com.minecraft.game.bukkit.poll;

import com.minecraft.game.bukkit.vanish.VanishService;
import com.minecraft.game.message.LanguageService;
import com.minecraft.game.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Sorteia jogadores online aleatoriamente. */
public final class SorteioService {

    private SorteioService() {}

    public static List<Player> eligiblePlayers() {
        List<Player> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (VanishService.isVanished(p)) continue;
            list.add(p);
        }
        return list;
    }

    public static Player drawOne() {
        List<Player> pool = eligiblePlayers();
        if (pool.isEmpty()) return null;
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    public static List<Player> drawMany(int count) {
        List<Player> pool = new ArrayList<>(eligiblePlayers());
        if (pool.isEmpty()) return List.of();
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(count, pool.size()));
    }

    public static void announceWinner(Player host, Player winner) {
        if (winner == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Messages.prefixed("poll.sorteio.winner", LanguageService.of(p), Map.of(
                    "player", winner.getName(),
                    "host", host != null ? host.getName() : "Staff")));
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.4f);
        }
        if (host != null && host.isOnline()) {
            host.sendMessage(Messages.prefixed("poll.sorteio.host-result", LanguageService.of(host), Map.of(
                    "player", winner.getName())));
        }
    }
}
