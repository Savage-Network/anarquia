package com.minecraft.lobbyhost.bukkit.listener;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.medal.Medal;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

/**
 * Espelho de {@code InfoListener} do core-bukkit Savage.
 *
 * <p>Formato:
 * <pre>
 *   {medal}{tag.getByPrefix(viewer.TagPrefix)}{nick}: {msg}
 * </pre>
 *
 * <p>Cooldown 3s para nao-VIP, mensagem em {@code §7} para nao-VIP e
 * {@code §f} (com {@code &} traduzido) para VIP+.
 */
public final class InfoListener implements Listener {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        Account account = Core.getAccountController().of(sender.getUniqueId());
        if (account == null) return;

        // Cooldown anti-spam (nao-VIP)
        if (!account.isVIP() && !account.isStaffer()) {
            if (account.hasCooldown(Constant.CHAT_COOLDOWN_KEY)) {
                long left = account.getCooldownRemaining(Constant.CHAT_COOLDOWN_KEY);
                account.send("§cAguarde " + Math.max(1, left / 1000L) + "s para falar novamente.");
                event.setCancelled(true);
                return;
            }
            account.setCooldown(Constant.CHAT_COOLDOWN_KEY, Constant.CHAT_COOLDOWN_MS);
        }

        // Conteudo: VIP+ pode colorir via &; demais ficam em cinza puro
        String plainMessage = LEGACY.serialize(event.message());
        String content;
        if (account.isAllowColoredChat()) {
            content = "§f" + colorize(plainMessage);
        } else {
            content = "§7" + LegacyComponentSerializer.legacyAmpersand()
                    .serialize(Component.text(plainMessage))
                    .replace("§", "&"); // remove cores se um nao-VIP usou §
        }

        // Cancela o broadcast nativo (cada viewer recebe a versao com SEU TagPrefix)
        event.setCancelled(true);

        Set<Player> recipients = new HashSet<>(event.viewers().stream()
                .filter(a -> a instanceof Player).map(a -> (Player) a).toList());
        if (recipients.isEmpty()) recipients = new HashSet<>(sender.getServer().getOnlinePlayers());

        for (Player viewer : recipients) {
            Account viewerAcc = Core.getAccountController().of(viewer.getUniqueId());
            String prefix = account.getTag().getByPrefix(
                    viewerAcc != null ? viewerAcc.getTagPrefix() : null);
            String medal = account.getMedal() != Medal.NONE
                    ? account.getMedal().getColoredSymbol() : "";
            String formatted = medal
                    + prefix
                    + account.getNickname()
                    + "§7: " + content;
            viewer.sendMessage(Component.text(formatted));
        }

        // Log no console
        Core.getLogger().info("[CHAT] " + account.getNickname() + ": " + plainMessage);
    }

    private static String colorize(String input) {
        return input.replace('&', '§');
    }
}
