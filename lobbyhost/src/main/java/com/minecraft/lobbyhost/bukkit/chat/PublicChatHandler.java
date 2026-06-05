package com.minecraft.lobbyhost.bukkit.chat;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.vanish.VanishConfig;
import com.minecraft.lobbyhost.bukkit.vanish.VanishService;
import com.minecraft.lobbyhost.punishment.PunishmentService;
import com.minecraft.lobbyhost.util.ModernText;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class PublicChatHandler {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private PublicChatHandler() {}

    public static boolean handle(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        Account account = Core.getAccountController().of(sender.getUniqueId());
        if (account == null) return false;

        if (PunishmentService.activeMute(sender.getUniqueId()).isPresent()) {
            account.send("§cVocê está mutado e não pode falar no chat.");
            event.setCancelled(true);
            return true;
        }

        if (ChatManager.isLocked() && !account.isStaffer()) {
            account.send(ChatManager.lockedMessage());
            event.setCancelled(true);
            return true;
        }

        String plain = LEGACY.serialize(event.message()).trim();
        if (plain.isEmpty()) {
            account.send("§cMensagem vazia.");
            event.setCancelled(true);
            return true;
        }

        if (tryStaffRoute(event, sender, account, plain)) {
            return true;
        }

        if (VanishConfig.isBlockPublicChat() && VanishService.isVanished(sender)) {
            account.send(ModernText.legacy(VanishConfig.msgChatBlocked()));
            event.setCancelled(true);
            return true;
        }

        if (!account.getToggle().isAllowLobbyChat()) {
            account.send("§cChat do lobby desativado. §e/prefs §cpara reativar.");
            event.setCancelled(true);
            return true;
        }

        if (plain.length() > LobbySettings.CHAT_MAX_LENGTH) {
            account.send("§cMensagem longa demais (max " + LobbySettings.CHAT_MAX_LENGTH + ").");
            event.setCancelled(true);
            return true;
        }

        if (!applyRateLimit(account, sender, plain)) {
            event.setCancelled(true);
            return true;
        }

        String content = ChatFormatter.formatContent(account, plain);
        content = ChatFormatter.applyMentions(content, new java.util.ArrayList<>(Bukkit.getOnlinePlayers()));

        event.setCancelled(true);
        Set<Player> recipients = new HashSet<>(event.viewers().stream()
                .filter(a -> a instanceof Player).map(a -> (Player) a).toList());
        if (recipients.isEmpty()) {
            recipients = new HashSet<>(sender.getServer().getOnlinePlayers());
        }

        for (Player viewer : recipients) {
            if (viewer.equals(sender)) continue;
            Account viewerAcc = Core.getAccountController().of(viewer.getUniqueId());
            if (viewerAcc != null && BlockListService.isBlocked(viewerAcc, sender.getUniqueId())) {
                continue;
            }
            if (viewerAcc != null && !viewerAcc.getToggle().isAllowLobbyChat()) {
                continue;
            }
            viewer.sendMessage(Component.text(ChatFormatter.formatLine(account, viewerAcc, content)));
        }

        sender.sendMessage(Component.text(ChatFormatter.formatLine(account, account, content)));
        Core.getLogger().info("[CHAT] " + account.getNickname() + ": " + plain);
        return true;
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
                account.send("§cAguarde " + Math.max(1, left / 1000L) + "s para falar novamente.");
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
        boolean staffPrefix = false;
        for (String prefix : LobbySettings.CHAT_STAFF_PREFIXES) {
            if (body.startsWith(prefix)) {
                body = body.substring(prefix.length()).trim();
                staffPrefix = true;
                break;
            }
        }

        if (!staffPrefix && !account.getToggle().isStaffChat()) {
            return false;
        }

        if (body.isEmpty()) {
            account.send("§eUso: §f! <mensagem> §eou ative §fStaff chat §eem /prefs.");
            event.setCancelled(true);
            return true;
        }

        event.setCancelled(true);
        StaffChatBridge.send(account, body);
        sender.sendMessage(Component.text(LobbySettings.STAFFCHAT_PREFIX + "§7(enviado ao staff)"));
        return true;
    }
}
