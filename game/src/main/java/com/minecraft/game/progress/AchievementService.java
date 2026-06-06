package com.minecraft.game.progress;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.stats.GameStats;
import com.minecraft.game.account.context.objects.stats.PlayerProgress;
import com.minecraft.game.network.ActivityFeedService;
import com.minecraft.game.network.NotificationService;
import org.bukkit.Bukkit;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;

public final class AchievementService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private AchievementService() {}

    public static void onLogin(Account account) {
        if (account == null) return;
        PlayerProgress p = progress(account);
        String today = LocalDate.now(ZoneOffset.UTC).format(DAY);
        if (today.equals(p.getLastLoginDay())) return;

        if (p.getLastLoginDay() != null && !p.getLastLoginDay().isBlank()) {
            LocalDate last = LocalDate.parse(p.getLastLoginDay(), DAY);
            LocalDate now = LocalDate.parse(today, DAY);
            p.setLoginStreak(last.plusDays(1).equals(now) ? p.getLoginStreak() + 1 : 1);
        } else {
            p.setLoginStreak(1);
        }
        p.setLastLoginDay(today);
        p.setTotalLoginDays(p.getTotalLoginDays() + 1);
        account.markDirty();

        unlock(account, Achievement.LOGIN_7, p.getLoginStreak() >= 7);
        unlock(account, Achievement.LOGIN_30, p.getLoginStreak() >= 30);

        if (p.getLoginStreak() >= 2) {
            account.send("§6§lSTREAK §7Login: §e" + p.getLoginStreak() + " §7dias seguidos!");
        }
    }

    public static void onKill(Account killer, int killStreak, int totalKills, boolean collectedBounty) {
        if (killer == null) return;
        unlock(killer, Achievement.FIRST_BLOOD, totalKills >= 1);
        unlock(killer, Achievement.KILLER_10, totalKills >= 10);
        unlock(killer, Achievement.KILLER_100, totalKills >= 100);
        unlock(killer, Achievement.STREAK_3, killStreak >= 3);
        unlock(killer, Achievement.STREAK_5, killStreak >= 5);
        unlock(killer, Achievement.STREAK_10, killStreak >= 10);
        if (collectedBounty) unlock(killer, Achievement.BOUNTY_HUNTER, true);
    }

    public static void onClanCreated(Account founder) {
        unlock(founder, Achievement.CLAN_FOUNDER, true);
    }

    public static void unlock(Account account, Achievement achievement, boolean condition) {
        if (account == null || achievement == null || !condition) return;
        PlayerProgress p = progress(account);
        if (p.getAchievements() == null) p.setAchievements(new java.util.HashSet<>());
        if (!p.getAchievements().add(achievement.getId())) return;

        if (achievement.getGoldReward() > 0) {
            account.addGolds(achievement.getGoldReward());
        }
        account.markDirty();

        String msg = "§6§lCONQUISTA §a" + achievement.getTitle()
                + " §7— " + achievement.getDescription()
                + (achievement.getGoldReward() > 0
                ? " §8(§a+" + achievement.getGoldReward() + "§8)" : "");
        account.send(msg);
        ActivityFeedService.publish("achievement", account.getNickname(),
                "desbloqueou " + achievement.getTitle(), Core.getJavaPlugin() != null
                        ? com.minecraft.game.GameSettings.SERVER_CHAT_ID : "game");
        NotificationService.broadcastAchievement(account.getNickname(), achievement.getTitle());
    }

    public static Set<Achievement> unlocked(Account account) {
        Set<Achievement> out = EnumSet.noneOf(Achievement.class);
        if (account == null) return out;
        PlayerProgress p = progress(account);
        if (p.getAchievements() == null) return out;
        for (Achievement a : Achievement.values()) {
            if (p.getAchievements().contains(a.getId())) out.add(a);
        }
        return out;
    }

    public static int unlockedCount(Account account) {
        return unlocked(account).size();
    }

    public static int totalCount() {
        return Achievement.values().length;
    }

    private static PlayerProgress progress(Account account) {
        if (account.getPlayerProgress() == null) account.setPlayerProgress(new PlayerProgress());
        return account.getPlayerProgress();
    }

    public static void checkStatsAchievements(Account account) {
        if (account == null) return;
        GameStats s = account.getGameStats();
        if (s == null) return;
        unlock(account, Achievement.FIRST_BLOOD, s.getKills() >= 1);
        unlock(account, Achievement.KILLER_10, s.getKills() >= 10);
        unlock(account, Achievement.KILLER_100, s.getKills() >= 100);
        unlock(account, Achievement.STREAK_3, s.getBestKillStreak() >= 3);
        unlock(account, Achievement.STREAK_5, s.getBestKillStreak() >= 5);
        unlock(account, Achievement.STREAK_10, s.getBestKillStreak() >= 10);
    }
}
