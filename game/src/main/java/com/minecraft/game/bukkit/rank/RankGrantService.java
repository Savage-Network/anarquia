package com.minecraft.game.bukkit.rank;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.Rank;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.account.context.objects.tag.Tag;
import com.minecraft.game.account.context.objects.medal.RankMedals;
import com.minecraft.game.bukkit.display.GameTabList;
import com.minecraft.game.bukkit.display.PlayerDisplayService;
import com.minecraft.game.bukkit.manager.TagManager;
import com.minecraft.game.util.PunishmentTimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

public final class RankGrantService {

    private RankGrantService() {}

    public static long parseDurationMs(String tempo) {
        if (tempo == null || tempo.isBlank()) {
            throw new IllegalArgumentException("tempo vazio");
        }
        long expiresAt = PunishmentTimeUtil.getTime(tempo.trim());
        if (expiresAt < 0) return -1L;
        long duration = expiresAt - System.currentTimeMillis();
        if (duration <= 0) throw new IllegalArgumentException("duracao invalida");
        return duration;
    }

    public static long expiresAtFromDurationMs(long durationMs) {
        return durationMs < 0 ? 0L : System.currentTimeMillis() + durationMs;
    }

    public static String formatDuration(long durationMs) {
        if (durationMs < 0) return "Permanente";
        long totalSec = durationMs / 1000L;
        long days = totalSec / 86400L;
        long hours = (totalSec % 86400L) / 3600L;
        long minutes = (totalSec % 3600L) / 60L;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return Math.max(1, minutes) + "m";
    }

    public static String formatRankExpiry(Rank rank) {
        if (rank == null || rank.getExpiresAt() <= 0) return "Permanente";
        if (rank.hasExpired()) return "Expirado";
        return PunishmentTimeUtil.formatRemaining(rank.getExpiresAt());
    }

    public static Optional<RankType> parseRank(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        String normalized = raw.trim().toUpperCase(Locale.ROOT)
                .replace(" ", "")
                .replace("MOD+", "MODPLUS")
                .replace("MOD_PLUS", "MODPLUS");
        try {
            return Optional.of(RankType.valueOf(normalized));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static void grantPermanent(Account account, RankType type) {
        if (account == null || type == null) return;
        account.setRank(Rank.builder()
                .type(type)
                .givenAt(System.currentTimeMillis())
                .expiresAt(0L)
                .build());
        account.setTag(Tag.of(type));
        RankMedals.applyRankMedals(account, type);
        account.markDirty();
        refreshOnline(account);
    }

    public static void grantTemporary(Account account, RankType grantType, long durationMs) {
        if (account == null || grantType == null) return;

        long expiresAt = expiresAtFromDurationMs(durationMs);
        Rank grant = Rank.builder()
                .type(grantType)
                .givenAt(System.currentTimeMillis())
                .expiresAt(expiresAt)
                .build();

        Rank currentRank = account.getRank();
        RankType currentType = account.getRankType();

        if (currentType.isStaff() && !grantType.isStaff()) {
            mergeAvailableRank(account, grant, durationMs);
            return;
        }

        if (currentRank != null && currentRank.hasExpired()) {
            applyPrimary(account, grantType, grant);
            return;
        }

        if (!grantType.isStaff() && currentType.ordinal() > grantType.ordinal()) {
            mergeAvailableRank(account, grant, durationMs);
            return;
        }

        if (currentType == grantType && currentRank != null && !currentRank.hasExpired()
                && currentRank.getExpiresAt() > 0 && durationMs > 0) {
            long base = Math.max(currentRank.getExpiresAt(), System.currentTimeMillis());
            grant = Rank.builder()
                    .type(grantType)
                    .givenAt(currentRank.getGivenAt() > 0 ? currentRank.getGivenAt() : System.currentTimeMillis())
                    .expiresAt(base + durationMs)
                    .build();
        }

        applyPrimary(account, grantType, grant);
    }

    /**
     * Remove um rank especifico ou todos (type {@code null}).
     * Se o rank principal for removido, promove o melhor rank disponivel ou MEMBER.
     */
    public static RemoveRankOutcome removeRank(Account account, RankType type) {
        if (account == null) {
            return RemoveRankOutcome.fail("Conta invalida.");
        }

        if (type == null) {
            if (account.getRankType() == RankType.MEMBER && account.getAvailableRanks().isEmpty()) {
                return RemoveRankOutcome.fail("Jogador ja esta com rank MEMBER.");
            }
            account.getAvailableRanks().clear();
            account.setRank(Rank.builder()
                    .type(RankType.MEMBER)
                    .givenAt(System.currentTimeMillis())
                    .expiresAt(0L)
                    .build());
            account.markDirty();
            refreshOnline(account);
            return RemoveRankOutcome.ok(
                    "Todos os ranks de §f" + account.getName() + " §aforam removidos. Rank atual: §7MEMBER§a.",
                    null);
        }

        RankType primary = account.getRankType();
        if (primary == type) {
            account.getAvailableRanks().removeIf(r -> r != null && r.getType() == type);
            Rank replacement = bestAvailableRank(account);
            if (replacement != null) {
                account.setRank(replacement);
                account.setTag(Tag.of(replacement.getType()));
            } else {
                account.setRank(Rank.builder()
                        .type(RankType.MEMBER)
                        .givenAt(System.currentTimeMillis())
                        .expiresAt(0L)
                        .build());
            }
            account.markDirty();
            refreshOnline(account);
            return RemoveRankOutcome.ok(
                    "Rank §6" + type + " §aremovido de §f" + account.getName()
                            + "§a. Rank atual: §6" + account.getRankType() + "§a.",
                    type);
        }

        boolean removed = account.getAvailableRanks().removeIf(r -> r != null && r.getType() == type);
        if (removed) {
            account.markDirty();
            refreshOnline(account);
            return RemoveRankOutcome.ok(
                    "Rank §6" + type + " §aremovido dos ranks disponiveis de §f" + account.getName() + "§a.",
                    type);
        }

        return RemoveRankOutcome.fail("Jogador nao possui o rank §e" + type + "§c.");
    }

    /** Remove ranks expirados e rebaixa o rank principal se necessario. */
    public static boolean refreshRankState(Account account) {
        if (account == null) return false;

        boolean changed = false;
        var available = account.getAvailableRanks();
        if (available.removeIf(r -> r != null && r.hasExpired())) {
            changed = true;
        }

        Rank primary = account.getRank();
        if (primary != null && primary.hasExpired()) {
            changed = true;
            Rank replacement = bestAvailableRank(account);
            if (replacement != null) {
                account.setRank(replacement);
            } else {
                account.setRank(Rank.builder()
                        .type(RankType.MEMBER)
                        .givenAt(System.currentTimeMillis())
                        .expiresAt(0L)
                        .build());
            }
        }

        if (changed) account.markDirty();
        if (changed) refreshOnline(account);
        return changed;
    }

    private static Rank bestAvailableRank(Account account) {
        return account.getAvailableRanks().stream()
                .filter(r -> r != null && !r.hasExpired() && r.getType() != null)
                .max(Comparator.comparingInt(r -> r.getType().ordinal()))
                .orElse(null);
    }

    private static void applyPrimary(Account account, RankType grantType, Rank grant) {
        account.setRank(grant);
        account.setTag(Tag.of(grantType));
        RankMedals.applyRankMedals(account, grantType);
        account.markDirty();
        refreshOnline(account);
    }

    private static void mergeAvailableRank(Account account, Rank grant, long durationMs) {
        var list = account.getAvailableRanks();
        for (Rank existing : list) {
            if (existing == null || existing.getType() != grant.getType() || existing.hasExpired()) continue;
            if (grant.getExpiresAt() == 0) {
                existing.setExpiresAt(0);
            } else if (existing.getExpiresAt() > 0) {
                long base = Math.max(existing.getExpiresAt(), System.currentTimeMillis());
                existing.setExpiresAt(base + durationMs);
            } else {
                existing.setExpiresAt(grant.getExpiresAt());
            }
            account.markDirty();
            return;
        }
        list.add(grant);
        RankMedals.applyRankMedals(account, grant.getType());
        account.markDirty();
    }

    /** Reaplica tab, permissões, tag e nametag sem precisar relogar. */
    public static void refreshOnline(Account account) {
        if (account == null) return;
        Runnable task = () -> applyOnlineRefresh(account);
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), task);
        }
    }

    public static void notifyRankReceived(Account account, RankType rankType) {
        if (account == null || rankType == null) return;
        Runnable task = () -> {
            if (!account.isOnline()) return;
            account.send("§aVocê recebeu o rank " + rankType.getColor() + rankType.name() + "§a.");
        };
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), task);
        }
    }

    public static void notifyRankRemoved(Account account, RankType removedType) {
        if (account == null || !account.isOnline()) return;
        Runnable task = () -> {
            if (!account.isOnline()) return;
            if (removedType == null) {
                account.send("§cSeus ranks foram removidos. Rank atual: §7MEMBER§c.");
                return;
            }
            account.send("§cSeu rank " + removedType.getColor() + removedType.name() + " §cfoi removido.");
        };
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), task);
        }
    }

    public record RemoveRankOutcome(boolean success, String message, RankType removedType) {
        static RemoveRankOutcome ok(String message, RankType removedType) {
            return new RemoveRankOutcome(true, message, removedType);
        }

        static RemoveRankOutcome fail(String message) {
            return new RemoveRankOutcome(false, message, null);
        }
    }

    private static void applyOnlineRefresh(Account account) {
        if (account == null || !account.isOnline()) return;
        Player player = account.player();
        if (player == null || !player.isOnline()) return;

        PlayerDisplayService.applyAll(account, player);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(player)) continue;
            TagManager.applyTagForViewer(viewer, player);
        }
        GameTabList.refreshAll();
    }
}
