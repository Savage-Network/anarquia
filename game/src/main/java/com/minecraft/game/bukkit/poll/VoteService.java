package com.minecraft.game.bukkit.poll;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.message.LanguageService;
import com.minecraft.game.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/** Gerencia a votacao ativa do servidor. */
public final class VoteService {

    private static final AtomicReference<VoteSession> ACTIVE = new AtomicReference<>();
    private static int taskId = -1;

    private VoteService() {}

    public static VoteSession active() {
        return ACTIVE.get();
    }

    public static boolean hasActive() {
        VoteSession s = ACTIVE.get();
        return s != null && !s.isExpired();
    }

    public static boolean start(Player creator, List<String> options, int durationSec) {
        if (creator == null || options == null || options.size() < 2 || options.size() > 4) {
            return false;
        }
        durationSec = Math.max(10, Math.min(durationSec, 600));
        if (hasActive()) return false;

        VoteSession session = new VoteSession(
                creator.getUniqueId(), creator.getName(), options, durationSec);
        ACTIVE.set(session);
        scheduleEnd(session);

        broadcast("poll.vote.started", Map.of(
                "creator", creator.getName(),
                "seconds", String.valueOf(durationSec),
                "options", String.join(" §7| §e", options)));

        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                new VoteMenu(online, session).open();
            }
        }, 5L);

        return true;
    }

    public static boolean vote(Player player, int optionIndex) {
        VoteSession session = ACTIVE.get();
        if (session == null || session.isExpired()) return false;
        return session.vote(player.getUniqueId(), optionIndex);
    }

    public static void endNow() {
        VoteSession session = ACTIVE.getAndSet(null);
        if (session == null) return;
        cancelTask();
        finish(session);
    }

    public static void shutdown() {
        cancelTask();
        ACTIVE.set(null);
    }

    private static void scheduleEnd(VoteSession session) {
        cancelTask();
        GamePlugin plugin = Core.getJavaPlugin();
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            VoteSession current = ACTIVE.get();
            if (current == null || current != session) {
                cancelTask();
                return;
            }
            if (current.isExpired()) {
                ACTIVE.set(null);
                cancelTask();
                finish(current);
            }
        }, 20L, 20L);
    }

    private static void finish(VoteSession session) {
        List<String> lines = session.resultLines();
        String results = String.join(" §7| ", lines);
        int winnerIndex = session.winningIndex();
        String winner = session.getOptions().get(winnerIndex);

        broadcast("poll.vote.ended", Map.of(
                "results", results,
                "winner", winner,
                "total", String.valueOf(session.totalVotes())));

        Player creator = Bukkit.getPlayer(session.getCreatorId());
        if (creator != null && creator.isOnline()) {
            creator.sendMessage(Messages.prefixed("poll.vote.creator-result", LanguageService.of(creator), Map.of(
                    "results", results,
                    "winner", winner,
                    "total", String.valueOf(session.totalVotes()),
                    "seconds", String.valueOf(session.getDurationSec()))));
        }
    }

    private static void broadcast(String key, Map<String, String> placeholders) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Messages.prefixed(key, LanguageService.of(p), placeholders));
        }
    }

    private static void cancelTask() {
        if (taskId >= 0) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}
