package com.minecraft.lobbyhost.bukkit.manager;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.tag.Tag;
import com.minecraft.lobbyhost.account.context.objects.toggle.TagPrefix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Espelho de {@code com.minecraft.core.bukkit.manager.TagManager} do Savage.
 *
 * <p>Gerencia <b>tab list</b>, <b>display name</b> e <b>nametag</b> (teams de
 * scoreboard). Cada viewer recebe um {@link Scoreboard} personalizado para
 * que o {@link TagPrefix} do proprio jogador valha ao ver os demais.
 *
 * <p>Tags animadas (Owner) recebem um pulse a cada 40 ticks (~2s).
 */
public final class TagManager {

    private static final Map<UUID, Integer> animationState = new HashMap<>();
    private static BukkitTask animationTask;

    private TagManager() {}

    /* ================================================================
     * Animacao
     * ============================================================= */

    public static void startAnimationTask(JavaPlugin plugin) {
        if (animationTask != null) animationTask.cancel();
        animationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            boolean anyAnimated = false;
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                Account vAcc = Core.getAccountController().of(viewer.getUniqueId());
                if (vAcc == null) continue;
                if (vAcc.getTag().isAnimated()) {
                    anyAnimated = true;
                    animationState.merge(viewer.getUniqueId(), 1, Integer::sum);
                }
                // tambem precisamos animar quando outros viewers veem
                for (Player target : Bukkit.getOnlinePlayers()) {
                    Account tAcc = Core.getAccountController().of(target.getUniqueId());
                    if (tAcc != null && tAcc.getTag().isAnimated()) {
                        anyAnimated = true;
                        animationState.merge(target.getUniqueId(), 1, Integer::sum);
                    }
                }
                if (anyAnimated) updateTag(vAcc);
            }
        }, 40L, 40L);
    }

    public static void stopAnimationTask() {
        if (animationTask != null) { animationTask.cancel(); animationTask = null; }
        animationState.clear();
    }

    /* ================================================================
     * Update API
     * ============================================================= */

    /**
     * Atualiza o tab/nametag do {@code account} para TODOS os viewers online.
     * Se a tag mudou, chame este metodo a partir do command/listener.
     */
    public static void updateTag(Account account) {
        if (account == null || !account.isOnline()) return;
        Player target = account.player();
        Tag targetTag = account.getTag();

        Component displayName = Component.text(getAdjustedPrefix(account.getTagPrefix(), targetTag, target.getUniqueId())
                + account.getNickname() + suffixOf(account));
        target.displayName(displayName);
        target.playerListName(displayName);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            applyTagForViewer(viewer, target);
        }
    }

    public static void updateTag(Account account, Tag newTag) {
        account.setTag(newTag);
        updateTag(account);
    }

    /**
     * Atualiza apenas para um viewer especifico (usado quando o viewer muda
     * {@link TagPrefix} via {@code /prefixtype}: re-renderiza todos os demais
     * para ele).
     */
    public static void updateAllForViewer(Player viewer) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            applyTagForViewer(viewer, target);
        }
    }

    /* ================================================================
     * Aplicacao por viewer (Scoreboard team)
     * ============================================================= */

    public static void applyTagForViewer(Player viewer, Player target) {
        Account viewerAcc = Core.getAccountController().of(viewer.getUniqueId());
        Account targetAcc = Core.getAccountController().of(target.getUniqueId());
        if (viewerAcc == null || targetAcc == null) return;

        Scoreboard board = com.minecraft.lobbyhost.bukkit.lobby.LobbyScoreboard.ensure(viewer);

        Tag tag = targetAcc.getTag();
        String teamName = sanitize("tag_" + tag.getOrder() + "_" + target.getEntityId());
        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);

        String prefix = getAdjustedPrefix(viewerAcc.getTagPrefix(), tag, target.getUniqueId());
        String suffix = truncateSuffix(suffixOf(targetAcc));

        team.prefix(Component.text(prefix));
        team.suffix(Component.text(suffix));

        if (!team.hasEntry(target.getName())) team.addEntry(target.getName());
    }

    public static void removeTag(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard board = viewer.getScoreboard();
            if (board == null) continue;
            for (Team team : board.getTeams()) {
                if (team.hasEntry(target.getName())) {
                    team.removeEntry(target.getName());
                    if (team.getEntries().isEmpty() && team.getName().startsWith("tag_")) {
                        try { team.unregister(); } catch (Throwable ignored) {}
                    }
                }
            }
        }
    }

    /* ================================================================
     * Prefixo ajustado (animacao / fallback)
     * ============================================================= */

    public static String getAdjustedPrefix(TagPrefix viewerStyle, Tag tag, UUID targetId) {
        if (tag.isAnimated() && !tag.getAnimationFrames().isEmpty()) {
            int state = animationState.getOrDefault(targetId, 0);
            int idx = Math.floorMod(state, tag.getAnimationFrames().size());
            return tag.getAnimationFrames().get(idx);
        }
        if (tag.hasAlternates() && viewerStyle == TagPrefix.DEFAULT) {
            int state = animationState.getOrDefault(targetId, 0);
            String[] frames = new String[]{
                    tag.getPrefix(),
                    tag.getAlternatePrefix() != null ? tag.getAlternatePrefix() : tag.getPrefix(),
                    tag.getThirdPrefix() != null ? tag.getThirdPrefix() : tag.getPrefix()
            };
            return frames[Math.floorMod(state, frames.length)];
        }
        return tag.getByPrefix(viewerStyle);
    }

    private static String suffixOf(Account account) {
        if (account.getToggle() == null || !account.getToggle().isShowClanTag()) return "";
        String clanTag = account.getContext().getClan() != null
                ? account.getContext().getClan().getTag() : null;
        if (clanTag == null || clanTag.isEmpty()) return "";
        return " §7[" + clanTag + "§7]";
    }

    private static String truncateSuffix(String s) {
        if (s == null) return "";
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    /** Nome valido de team: ≤ 16 chars, sem espacos. */
    private static String sanitize(String raw) {
        String s = raw.replace(' ', '_');
        if (s.length() > 16) s = s.substring(0, 16);
        return s;
    }

    /* ================================================================
     * Listas interativas no chat
     * ============================================================= */

    public static void sendTags(Account account) {
        List<Tag> available = account.getAvailableTags();
        if (available.isEmpty()) {
            account.send("§cVoce nao possui nenhuma tag.");
            return;
        }

        Component msg = Component.text("§aSuas tags: ");
        for (int i = 0; i < available.size(); i++) {
            Tag tag = available.get(i);
            String hover = "§7Previa:\n§8> "
                    + tag.getByPrefix(account.getTagPrefix())
                    + account.getNickname()
                    + "\n\n"
                    + (account.isUsingTag(tag) ? "§cJa selecionada." : "§eClique para escolher.");
            Component entry = Component.text(tag.getColoredName())
                    .hoverEvent(HoverEvent.showText(Component.text(hover)))
                    .clickEvent(ClickEvent.runCommand("/tag " + tag.name().toLowerCase()));
            msg = msg.append(entry);
            if (i < available.size() - 1) msg = msg.append(Component.text("§7, "));
            else msg = msg.append(Component.text("§7."));
        }
        account.send(msg);
    }

    public static void sendPrefixTypes(Account account) {
        Component msg = Component.text("§aModos de prefixo: ");
        TagPrefix[] all = TagPrefix.values();
        for (int i = 0; i < all.length; i++) {
            TagPrefix p = all[i];
            String preview = "§7Previa:\n§8> "
                    + account.getTag().getByPrefix(p)
                    + account.getNickname()
                    + "\n\n"
                    + (account.isUsingTagPrefix(p) ? "§cJa selecionado." : "§eClique para selecionar.");
            Component entry = Component.text("§e" + p.getDisplayName())
                    .hoverEvent(HoverEvent.showText(Component.text(preview)))
                    .clickEvent(ClickEvent.runCommand("/prefixtype " + p.name().toLowerCase()));
            msg = msg.append(entry);
            if (i < all.length - 1) msg = msg.append(Component.text("§7, "));
            else msg = msg.append(Component.text("§7."));
        }
        account.send(msg);
    }
}
