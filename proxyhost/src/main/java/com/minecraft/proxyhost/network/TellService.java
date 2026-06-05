package com.minecraft.proxyhost.network;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.util.LegacyTexts;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Mensagens privadas cross-server via Redis + entrega no proxy. */
public final class TellService {

    private TellService() {}

    public enum Result {
        OK,
        SENDER_NOT_LOADED,
        TARGET_OFFLINE,
        SELF,
        TARGET_NOT_LOADED,
        TARGET_PM_DISABLED,
        BLOCKED,
        COOLDOWN,
        REDIS_OFFLINE
    }

    public record SendContext(Result result, Player target, Account senderAcc, Account targetAcc, String message) {}

    public static SendContext prepare(Player sender, String targetName, String message) {
        Account senderAcc = Core.getAccountController().of(sender.getUniqueId());
        if (senderAcc == null) {
            return new SendContext(Result.SENDER_NOT_LOADED, null, null, null, message);
        }

        if (!senderAcc.isVIP() && senderAcc.hasCooldown(Constant.TELL_COOLDOWN_KEY)) {
            return new SendContext(Result.COOLDOWN, null, senderAcc, null, message);
        }

        Player target = findPlayer(targetName).orElse(null);
        if (target == null) {
            return new SendContext(Result.TARGET_OFFLINE, null, senderAcc, null, message);
        }
        if (target.getUniqueId().equals(sender.getUniqueId())) {
            return new SendContext(Result.SELF, target, senderAcc, null, message);
        }

        Account targetAcc = Core.getAccountController().of(target.getUniqueId());
        if (targetAcc == null) {
            return new SendContext(Result.TARGET_NOT_LOADED, target, senderAcc, null, message);
        }
        if (!targetAcc.getToggle().isAllowPrivateMessages()) {
            return new SendContext(Result.TARGET_PM_DISABLED, target, senderAcc, targetAcc, message);
        }
        if (isBlockedEither(senderAcc, targetAcc)) {
            return new SendContext(Result.BLOCKED, target, senderAcc, targetAcc, message);
        }

        String sanitized = sanitize(message);
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) {
            return new SendContext(Result.REDIS_OFFLINE, target, senderAcc, targetAcc, sanitized);
        }
        return new SendContext(Result.OK, target, senderAcc, targetAcc, sanitized);
    }

    public static void dispatch(SendContext ctx, Player sender) {
        if (ctx.result() != Result.OK || ctx.target() == null) return;

        Account senderAcc = ctx.senderAcc();
        Account targetAcc = ctx.targetAcc();
        Player target = ctx.target();
        String message = ctx.message();

        String payload = sender.getUniqueId() + "|" + target.getUniqueId() + "|"
                + senderAcc.getColoredName() + "|" + targetAcc.getColoredName() + "|" + message;
        Core.getRedis().publish(Constant.REDIS_PROXY_TELL_CHANNEL, payload);

        senderAcc.getContext().setLastMessage(target.getUniqueId());
        targetAcc.getContext().setLastMessage(sender.getUniqueId());
        senderAcc.markDirty();
        targetAcc.markDirty();

        com.minecraft.proxyhost.velocity.command.ReplyCommand.registerLastConversation(
                sender.getUniqueId(), target.getUniqueId());
        com.minecraft.proxyhost.velocity.command.ReplyCommand.registerLastConversation(
                target.getUniqueId(), sender.getUniqueId());

        if (!senderAcc.isVIP()) {
            senderAcc.setCooldown(Constant.TELL_COOLDOWN_KEY, Constant.TELL_COOLDOWN_MS);
        }
    }

    public static void deliverFromRedis(String raw) {
        String[] parts = raw.split("\\|", 5);
        if (parts.length < 5) return;
        try {
            UUID senderId = UUID.fromString(parts[0].trim());
            UUID targetId = UUID.fromString(parts[1].trim());
            String fromColored = parts[2];
            String toColored = parts[3];
            String body = parts[4];

            Component incoming = formatIncoming(fromColored, body);
            Component outgoing = formatOutgoing(toColored, body);

            Core.getProxy().getPlayer(targetId).ifPresent(p -> p.sendMessage(incoming));
            Core.getProxy().getPlayer(senderId).ifPresent(p -> p.sendMessage(outgoing));
        } catch (IllegalArgumentException ignored) {
        }
    }

    public static Component formatIncoming(String coloredFrom, String body) {
        return LegacyTexts.component("&8(&fde &7" + coloredFrom + "&8) &7" + body);
    }

    public static Component formatOutgoing(String coloredTo, String body) {
        return LegacyTexts.component("&8(&fpara &7" + coloredTo + "&8) &7" + body);
    }

    public static long cooldownSecondsRemaining(Account account) {
        return Math.max(1L, account.getCooldownRemaining(Constant.TELL_COOLDOWN_KEY) / 1000L);
    }

    private static Optional<Player> findPlayer(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        Optional<Player> exact = Core.getProxy().getPlayer(name);
        if (exact.isPresent()) return exact;
        String lower = name.toLowerCase(Locale.ROOT);
        return Core.getProxy().getAllPlayers().stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(name)
                        || p.getUsername().toLowerCase(Locale.ROOT).startsWith(lower))
                .findFirst();
    }

    private static boolean isBlockedEither(Account a, Account b) {
        return hasBlocked(a, b.getId()) || hasBlocked(b, a.getId());
    }

    private static boolean hasBlocked(Account account, UUID other) {
        if (account == null || other == null) return false;
        return account.getContext().getBlocks().stream()
                .anyMatch(bl -> bl.getId() != null && bl.getId().equals(other));
    }

    private static String sanitize(String message) {
        return message == null ? "" : message.replace('|', ' ');
    }
}
