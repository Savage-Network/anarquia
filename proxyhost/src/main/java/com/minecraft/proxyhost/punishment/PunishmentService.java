package com.minecraft.proxyhost.punishment;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import com.minecraft.proxyhost.api.punishment.Punishment;
import com.minecraft.proxyhost.api.punishment.PunishmentCategory;
import com.minecraft.proxyhost.api.punishment.PunishmentReason;
import com.minecraft.proxyhost.backend.data.AccountData;
import com.minecraft.proxyhost.backend.data.PunishmentData;
import com.minecraft.proxyhost.util.DisconnectMessages;
import com.minecraft.proxyhost.util.PunishmentTimeUtil;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PunishmentService {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

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

        Optional<Player> online = Core.getProxy().getPlayer(name);
        if (online.isPresent()) {
            UUID id = online.get().getUniqueId();
            Account cached = Core.getAccountController().of(id);
            if (cached != null) return CompletableFuture.completedFuture(Optional.of(cached));
            return Core.getAccountController().load(id, online.get().getUsername())
                    .thenApply(Optional::ofNullable);
        }
        return CompletableFuture.supplyAsync(() -> accounts.loadByName(name));
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

            String ip = target.player()
                    .map(p -> p.getRemoteAddress().getAddress().getHostAddress())
                    .orElse("127.0.0.1");
            String server = target.getCurrentServer();

            Punishment punishment = Punishment.create(
                    target.getId(),
                    authorId != null ? authorId : Constant.DEFAULT_ID,
                    ip,
                    server,
                    category,
                    reason,
                    cause,
                    expiresAt
            );

            data.save(punishment);
            appendHistoryId(target, punishment.getId());

            publishSync(punishment);
            enforceOnline(punishment, target);

            String staff = authorName != null ? authorName : "CONSOLE";
            Core.getLogger().info("[punish] " + staff + " puniu " + target.getName()
                    + " (" + category.name() + "/" + reason.name() + ") id=" + punishment.getId());

            return PunishmentResult.ok(punishment, target.getName());
        });
    }

    public static CompletableFuture<PunishmentResult> revoke(UUID authorId, String authorName,
                                                             String punishmentId, String reason) {
        PunishmentData data = Core.getPunishmentData();
        if (data == null) return CompletableFuture.completedFuture(
                PunishmentResult.fail("Sistema de punições indisponível."));

        return CompletableFuture.supplyAsync(() -> {
            Optional<Punishment> opt = data.findById(punishmentId);
            if (opt.isEmpty()) {
                return PunishmentResult.fail("Punição não encontrada.");
            }
            Punishment punishment = opt.get();
            if (!punishment.isActive()) {
                return PunishmentResult.fail("Esta punição já foi revogada.");
            }
            punishment.revoke(authorId, reason);
            data.update(punishment);
            publishSync(punishment);

            String targetName = Core.getAccountData() != null
                    ? Core.getAccountData().load(punishment.getPlayer()).map(Account::getName).orElse("?")
                    : "?";

            Core.getLogger().info("[unpunish] " + (authorName != null ? authorName : "CONSOLE")
                    + " revogou " + punishment.getId() + " de " + targetName + " (" + reason + ")");

            return PunishmentResult.ok(punishment, targetName);
        });
    }

    public static String banKickMessage(Punishment punishment) {
        return LegacyComponentSerializer.legacySection().serialize(DisconnectMessages.ban(punishment));
    }

    public static String muteNotice(Punishment punishment) {
        return DisconnectMessages.muteNotice(punishment);
    }

    public static CompletableFuture<KickResult> kick(UUID authorId, String authorName,
                                                     String targetName, String reason) {
        if (reason == null || reason.isBlank()) reason = "Não informado";
        String finalReason = reason;
        return resolveTarget(targetName).thenApply(optTarget -> {
            if (optTarget.isEmpty()) {
                return KickResult.fail("Jogador não encontrado.");
            }
            Account target = optTarget.get();
            Account actor = authorId != null ? Core.getAccountController().of(authorId) : null;
            if (!canPunish(actor, target)) {
                return KickResult.fail("Você não pode expulsar este jogador.");
            }
            Optional<Player> online = target.player();
            if (online.isEmpty()) {
                return KickResult.fail("Jogador offline.");
            }
            String staff = authorName != null ? authorName : "CONSOLE";
            online.get().disconnect(DisconnectMessages.staffKick(staff, finalReason));
            Core.getLogger().info("[kick] " + staff + " expulsou " + target.getName() + " (" + finalReason + ")");
            return KickResult.ok(target.getName());
        });
    }

    private static void enforceOnline(Punishment punishment, Account target) {
        if (punishment.getCategory() == PunishmentCategory.BAN) {
            target.player().ifPresent(p ->
                    p.disconnect(LEGACY.deserialize(banKickMessage(punishment))));
        } else if (punishment.getCategory() == PunishmentCategory.MUTE) {
            target.send(muteNotice(punishment));
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

    public static boolean requiresStaffRank(RankType min) {
        return min != null && min.isStaff();
    }

    public record PunishmentResult(boolean success, String message, Punishment punishment, String targetName) {
        static PunishmentResult ok(Punishment p, String target) {
            return new PunishmentResult(true, null, p, target);
        }

        static PunishmentResult fail(String msg) {
            return new PunishmentResult(false, msg, null, null);
        }
    }

    public record KickResult(boolean success, String message, String targetName) {
        static KickResult ok(String target) {
            return new KickResult(true, null, target);
        }

        static KickResult fail(String msg) {
            return new KickResult(false, msg, null);
        }
    }
}
