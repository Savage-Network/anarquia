package com.minecraft.lobbyhost.punishment;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.punishment.Punishment;
import com.minecraft.lobbyhost.api.punishment.PunishmentCategory;
import com.minecraft.lobbyhost.api.punishment.PunishmentReason;
import com.minecraft.lobbyhost.backend.data.AccountData;
import com.minecraft.lobbyhost.backend.data.PunishmentData;
import com.minecraft.lobbyhost.util.ModernText;
import com.minecraft.lobbyhost.util.PunishmentTimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PunishmentService {

    private PunishmentService() {}

    public static boolean canPunish(Account actor, Account target) {
        if (target == null) return false;
        if (target.getHistory() != null && target.getHistory().isUntouchable()) return false;
        if (actor != null && actor.getId().equals(target.getId())) return false;
        if (actor != null && !actor.getRankType().isAtLeast(target.getRankType())) return false;
        return true;
    }

    public static CompletableFuture<Optional<Account>> resolveTarget(String name) {
        AccountData accounts = Core.getAccountData();
        if (accounts == null) return CompletableFuture.completedFuture(Optional.empty());

        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            Account cached = Core.getAccountController().of(online.getUniqueId());
            if (cached != null) return CompletableFuture.completedFuture(Optional.of(cached));
            return Core.getAccountController().load(online.getUniqueId(), online.getName())
                    .thenApply(Optional::ofNullable);
        }
        return CompletableFuture.supplyAsync(() -> accounts.loadByName(name))
                .thenApply(opt -> {
                    if (opt.isPresent()) return opt;
                    OfflinePlayer off = Bukkit.getOfflinePlayer(name);
                    if (off.hasPlayedBefore() || off.isOnline()) {
                        Account stub = new Account(off.getUniqueId(), name);
                        return Optional.of(stub);
                    }
                    return Optional.empty();
                });
    }

    public static CompletableFuture<PunishmentResult> apply(UUID authorId, String authorName,
                                                              String targetName,
                                                              PunishmentCategory category,
                                                              PunishmentReason reason,
                                                              String timeArg, String cause) {
        return resolveTarget(targetName).thenApply(optTarget -> {
            if (optTarget.isEmpty()) {
                return PunishmentResult.fail("Jogador não encontrado.");
            }
            Account target = optTarget.get();
            Account actor = authorId != null ? Core.getAccountController().of(authorId) : null;
            if (!canPunish(actor, target)) {
                return PunishmentResult.fail("Você não pode punir este jogador.");
            }

            PunishmentData data = Core.getPunishmentData();
            if (data == null) return PunishmentResult.fail("Sistema de punições indisponível.");

            if (data.findActive(target.getId(), category).isPresent()) {
                return PunishmentResult.fail("O jogador já possui uma punição ativa deste tipo.");
            }

            long expiresAt = -1L;
            if (timeArg != null && !timeArg.isBlank()
                    && !timeArg.equalsIgnoreCase("-1")
                    && !timeArg.equalsIgnoreCase("perm")
                    && !timeArg.equalsIgnoreCase("perma")) {
                if (!PunishmentTimeUtil.isValidTime(timeArg)) {
                    return PunishmentResult.fail("Tempo inválido. Use 10m, 2h, 7d ou -1.");
                }
                expiresAt = PunishmentTimeUtil.getTime(timeArg);
            }

            String ip = "127.0.0.1";
            Player bp = Bukkit.getPlayer(target.getId());
            if (bp != null) ip = bp.getAddress() != null ? bp.getAddress().getHostString() : ip;

            Punishment punishment = Punishment.create(
                    target.getId(),
                    authorId != null ? authorId : Constant.DEFAULT_ID,
                    ip,
                    LobbySettings.SERVER_MODE.toLowerCase(),
                    category,
                    reason,
                    cause,
                    expiresAt
            );

            data.save(punishment);
            appendHistoryId(target, punishment.getId());
            publishSync(punishment);

            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> enforceOnline(punishment, target));

            Core.getLogger().info("[punish] " + (authorName != null ? authorName : "CONSOLE")
                    + " puniu " + target.getName() + " (" + category.name() + "/"
                    + reason.name() + ") id=" + punishment.getId());

            return PunishmentResult.ok(punishment, target.getName());
        });
    }

    public static CompletableFuture<PunishmentResult> revoke(UUID authorId, String authorName,
                                                             String punishmentId, String reason) {
        PunishmentData data = Core.getPunishmentData();
        if (data == null) {
            return CompletableFuture.completedFuture(PunishmentResult.fail("Sistema indisponível."));
        }
        return CompletableFuture.supplyAsync(() -> {
            Optional<Punishment> opt = data.findById(punishmentId);
            if (opt.isEmpty()) return PunishmentResult.fail("Punição não encontrada.");
            Punishment punishment = opt.get();
            if (!punishment.isActive()) return PunishmentResult.fail("Punição já revogada.");
            punishment.revoke(authorId, reason);
            data.update(punishment);
            publishSync(punishment);

            String targetName = Core.getAccountData() != null
                    ? Core.getAccountData().load(punishment.getPlayer()).map(Account::getName).orElse("?")
                    : "?";
            return PunishmentResult.ok(punishment, targetName);
        });
    }

    public static Optional<Punishment> activeMute(UUID playerId) {
        PunishmentData data = Core.getPunishmentData();
        if (data == null) return Optional.empty();
        return data.findActive(playerId, PunishmentCategory.MUTE);
    }

    public static String banKickMessage(Punishment punishment) {
        return String.format(Constant.BAN_TEMPLATE_MESSAGE,
                punishment.isTemporary() ? "temporariamente" : "permanentemente",
                punishment.getReason().getDisplayName(),
                punishment.getId());
    }

    private static void enforceOnline(Punishment punishment, Account target) {
        Player p = Bukkit.getPlayer(target.getId());
        if (p == null || !p.isOnline()) return;
        if (punishment.getCategory() == PunishmentCategory.BAN) {
            p.kickPlayer(banKickMessage(punishment));
        } else if (punishment.getCategory() == PunishmentCategory.MUTE) {
            target.send(ModernText.parse("<!italic><" + ModernText.ERROR + ">Você foi mutado. ID: <#F1F5F9>"
                    + punishment.getId()));
        }
    }

    private static void appendHistoryId(Account target, String punishmentId) {
        if (target.getHistory() == null) return;
        if (!target.getHistory().getPunishmentIds().contains(punishmentId)) {
            target.getHistory().getPunishmentIds().add(punishmentId);
            target.markDirty();
            if (Core.getAccountData() != null) {
                Core.getAccountData().save(target);
            }
        }
    }

    private static void publishSync(Punishment punishment) {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) return;
        String payload = punishment.getCategory().name() + "|"
                + punishment.getPlayer() + "|"
                + punishment.getId() + "|"
                + (punishment.isActive() ? "APPLY" : "REVOKE");
        Core.getRedis().publish(Constant.REDIS_ACCOUNT_PUNISHMENTS_CHANNEL, payload);
    }

    public record PunishmentResult(boolean success, String message, Punishment punishment, String targetName) {
        static PunishmentResult ok(Punishment p, String target) {
            return new PunishmentResult(true, null, p, target);
        }

        static PunishmentResult fail(String msg) {
            return new PunishmentResult(false, msg, null, null);
        }
    }
}
