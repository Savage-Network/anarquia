package com.minecraft.game.bukkit.chat;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import com.minecraft.game.punishment.PunishmentService;
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

    public static void handle(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        Account account = Core.getAccountController().of(sender.getUniqueId());
        if (account == null) return;

        if (PunishmentService.activeMute(sender.getUniqueId()).isPresent()) {
            account.send("§cVocê está mutado e não pode falar no chat.");
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

        String normalized = plain.toLowerCase(Locale.ROOT);
        if (!account.isVIP() && !account.isStaffer()) {
            if (account.hasCooldown(Constant.CHAT_COOLDOWN_KEY)) {
                long left = account.getCooldownRemaining(Constant.CHAT_COOLDOWN_KEY);
                account.send("§cAguarde " + Math.max(1, left / 1000L) + "s.");
                event.setCancelled(true);
                return;
            }
            if (ChatSpamTracker.isSpam(sender.getUniqueId(), normalized)) {
                account.send("§cPare de repetir a mesma mensagem.");
                event.setCancelled(true);
                return;
            }
            account.setCooldown(Constant.CHAT_COOLDOWN_KEY, Constant.CHAT_COOLDOWN_MS);
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
            String line = ChatFormatter.formatLine(account, viewerAcc, GameSettings.GAME_CHAT_PREFIX, content);
            viewer.sendMessage(Component.text(line));
        }
        sender.sendMessage(Component.text(ChatFormatter.formatLine(account, account,
                GameSettings.GAME_CHAT_PREFIX, content)));
        Core.getLogger().info("[CHAT] " + account.getNickname() + ": " + plain);
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
