package com.minecraft.game.bukkit.chat;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.vanish.VanishConfig;
import com.minecraft.game.bukkit.vanish.VanishService;
import com.minecraft.game.punishment.PunishmentService;
import com.minecraft.game.api.punishment.Punishment;
import com.minecraft.game.util.ModernText;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class PublicChatHandler {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private PublicChatHandler() {}

    public static void handle(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        Account account = Core.getAccountController().of(sender.getUniqueId());
        if (account == null) return;

        Optional<Punishment> mute = PunishmentService.activeMute(sender.getUniqueId());
        if (mute.isPresent()) {
            account.send(PunishmentService.muteDenyMessage(mute.get()));
            event.setCancelled(true);
            return;
        }

        if (ChatManager.isLocked() && !account.isStaffer()) {
            account.send(ChatManager.lockedMessage());
            event.setCancelled(true);
            return;
        }

        if (VanishConfig.isBlockPublicChat() && VanishService.isVanished(sender)) {
            account.send(ModernText.legacy(VanishConfig.msgChatBlocked()));
            event.setCancelled(true);
            return;
        }

        String plain = LEGACY.serialize(event.message()).trim();
        if (plain.isEmpty()) {
            account.send("§cMensagem vazia.");
            event.setCancelled(true);
            return;
        }

        if (tryStaffRoute(event, sender, account, plain)) return;

        if (plain.length() > GameSettings.CHAT_MAX_LENGTH) {
            account.send("§cMensagem longa demais.");
            event.setCancelled(true);
            return;
        }

        if (!applyRateLimit(account, sender, plain)) {
            event.setCancelled(true);
            return;
        }

        String content = ChatFormatter.formatContent(account, plain);
        content = ChatFormatter.applyMentions(content, new java.util.ArrayList<>(Bukkit.getOnlinePlayers()));
        event.setCancelled(true);

        Set<Player> recipients = new HashSet<>(event.viewers().stream()
                .filter(a -> a instanceof Player).map(a -> (Player) a).toList());
        if (recipients.isEmpty()) recipients = new HashSet<>(Bukkit.getOnlinePlayers());

        for (Player viewer : recipients) {
            if (viewer.equals(sender)) continue;
            Account viewerAcc = Core.getAccountController().of(viewer.getUniqueId());
            if (viewerAcc != null && BlockListService.isBlocked(viewerAcc, sender.getUniqueId())) continue;
            viewer.sendMessage(ModernText.fromLegacy(ChatFormatter.formatLine(account, viewerAcc, content)));
        }
        sender.sendMessage(ModernText.fromLegacy(ChatFormatter.formatLine(account, account, content)));
        Core.getLogger().info("[CHAT] " + account.getNickname() + ": " + plain);
    }

    private static boolean applyRateLimit(Account account, Player sender, String plain) {
        long slowMs = ChatManager.getSlowModeMs();
        if (slowMs > 0 && !account.isStaffer()) {
            if (account.hasCooldown(ChatManager.SLOW_COOLDOWN_KEY)) {
                long left = account.getCooldownRemaining(ChatManager.SLOW_COOLDOWN_KEY);
                account.send("§cAguarde " + Math.max(1, left / 1000L) + "s (slow mode).");
                return false;
            }
            account.setCooldown(ChatManager.SLOW_COOLDOWN_KEY, slowMs);
            return true;
        }

        String normalized = plain.toLowerCase(Locale.ROOT);
        if (!account.isVIP() && !account.isStaffer()) {
            if (account.hasCooldown(Constant.CHAT_COOLDOWN_KEY)) {
                long left = account.getCooldownRemaining(Constant.CHAT_COOLDOWN_KEY);
                account.send("§cAguarde " + Math.max(1, left / 1000L) + "s.");
                return false;
            }
            if (ChatSpamTracker.isSpam(sender.getUniqueId(), normalized)) {
                account.send("§cPare de repetir a mesma mensagem.");
                return false;
            }
            account.setCooldown(Constant.CHAT_COOLDOWN_KEY, Constant.CHAT_COOLDOWN_MS);
        }
        return true;
    }

    private static boolean tryStaffRoute(AsyncChatEvent event, Player sender, Account account, String plain) {
        if (!StaffChatBridge.canUseStaffChat(account, sender)) return false;
        String body = plain;
        boolean prefix = false;
        for (String p : GameSettings.CHAT_STAFF_PREFIXES) {
            if (body.startsWith(p)) {
                body = body.substring(p.length()).trim();
                prefix = true;
                break;
            }
        }
        if (!prefix && !account.getToggle().isStaffChat()) return false;
        if (body.isEmpty()) {
            account.send("§e! <msg> §7ou staff chat em /prefs.");
            event.setCancelled(true);
            return true;
        }
        event.setCancelled(true);
        StaffChatBridge.send(account, body);
        sender.sendMessage(Component.text(GameSettings.STAFFCHAT_PREFIX + "§7(enviado)"));
        return true;
    }
}
