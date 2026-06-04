package com.minecraft.lobbyhost.bukkit.chat;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.vanish.VanishService;
import com.minecraft.lobbyhost.punishment.PunishmentService;
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

        String plain = LEGACY.serialize(event.message()).trim();
        if (plain.isEmpty()) {
            account.send("§cMensagem vazia.");
            event.setCancelled(true);
            return true;
        }

        if (tryStaffRoute(event, sender, account, plain)) {
            return true;
        }

        if (VanishService.isVanished(sender)) {
            account.send("§cVoce esta em vanish. Use §e!mensagem §cou §e/sc §cpara o staff chat.");
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

        String normalized = plain.toLowerCase(Locale.ROOT);
        if (!account.isVIP() && !account.isStaffer()) {
            if (account.hasCooldown(Constant.CHAT_COOLDOWN_KEY)) {
                long left = account.getCooldownRemaining(Constant.CHAT_COOLDOWN_KEY);
                account.send("§cAguarde " + Math.max(1, left / 1000L) + "s para falar novamente.");
                event.setCancelled(true);
                return true;
            }
            if (ChatSpamTracker.isSpam(sender.getUniqueId(), normalized)) {
                account.send("§cPare de repetir a mesma mensagem.");
                event.setCancelled(true);
                return true;
            }
            account.setCooldown(Constant.CHAT_COOLDOWN_KEY, Constant.CHAT_COOLDOWN_MS);
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
            String line = ChatFormatter.formatLine(account, viewerAcc,
                    LobbySettings.LOBBY_CHAT_PREFIX, content);
            viewer.sendMessage(Component.text(line));
        }

        String selfLine = ChatFormatter.formatLine(account, account,
                LobbySettings.LOBBY_CHAT_PREFIX, content);
        sender.sendMessage(Component.text(selfLine));

        Core.getLogger().info("[CHAT] " + account.getNickname() + ": " + plain);
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
