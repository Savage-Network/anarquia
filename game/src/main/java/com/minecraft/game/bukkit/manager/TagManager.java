package com.minecraft.game.bukkit.manager;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.display.GameScoreboard;
import com.minecraft.game.account.context.objects.tag.Tag;
import com.minecraft.game.account.context.objects.toggle.TagPrefix;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.bukkit.fake.FakeConfig;
import com.minecraft.game.bukkit.fake.FakeService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

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
        if (target == null) return;

        Tag targetTag = account.getDisplayTag();
        String prefix = getAdjustedPrefix(account.getTagPrefix(), targetTag, target.getUniqueId(), account);
        String nick = account.getNickname();
        String suffix = truncateSuffix(suffixOf(account));

        Component displayName = legacy(prefix + nick + suffix);
        target.displayName(displayName);
        target.playerListName(displayName);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            applyTagForViewer(viewer, target);
        }
        applyTagForViewer(target, target);
    }

    public static void refresh(Player player) {
        if (player == null) return;
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account != null) updateTag(account);
    }

    /** Linha unica (legado). */
    public static String formatNametagLine(Account account) {
        if (account == null) return "";
        String prefix = formatNametagPrefixLine(account);
        String nick = formatNametagNickLine(account);
        if (prefix.isBlank()) return nick;
        return prefix + nick;
    }

    /** Linha da tag (prefixo colorido). */
    public static String formatNametagPrefixLine(Account account) {
        if (account == null) return "";
        Tag tag = account.getDisplayTag();
        if (tag == null) return "";
        return getAdjustedPrefix(account.getTagPrefix(), tag, account.getId(), account);
    }

    /** Nick para nametag empilhado (sem clã — clã vai no sufixo). */
    public static String formatNametagNickLine(Account account) {
        if (account == null) return "";
        Tag tag = account.getTag();
        String color = "§f";
        if (tag != null) {
            String prefix = tag.getPrefix();
            if (prefix != null && !prefix.isBlank()) {
                for (int i = 0; i < prefix.length() - 1; i++) {
                    if (prefix.charAt(i) == '§') color = prefix.substring(i, i + 2);
                }
            }
        }
        return color + account.getNickname();
    }

    public static String formatNametagClanLine(Account account) {
        if (account == null || account.getToggle() == null || !account.getToggle().isShowClanTag()) {
            return "";
        }
        if (!account.hasClan() || account.getClan() == null) return "";
        return com.minecraft.game.api.clan.ClanTagFormatter.formatBracketed(account.getClan());
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

        Scoreboard board = GameScoreboard.ensure(viewer);

        Tag tag = FakeService.displayTag(targetAcc, viewerAcc);
        String teamName = sanitize("t_" + tag.getOrder() + "_"
                + target.getUniqueId().toString().replace("-", "").substring(0, 12));

        for (Team t : board.getTeams()) {
            if (t.hasEntry(target.getName()) && !t.getName().equals(teamName)) {
                t.removeEntry(target.getName());
            }
        }

        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);

        String prefix = getAdjustedPrefix(viewerAcc.getTagPrefix(), tag, target.getUniqueId(), targetAcc);
        String suffix = truncateSuffix(suffixOf(targetAcc));

        team.prefix(legacy(prefix));
        team.suffix(legacy(suffix));
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

        if (!team.hasEntry(target.getName())) team.addEntry(target.getName());
    }

    public static void removeTag(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard board = viewer.getScoreboard();
            if (board == null) continue;
            for (Team team : board.getTeams()) {
                if (team.hasEntry(target.getName())) {
                    team.removeEntry(target.getName());
                    if (team.getEntries().isEmpty()
                            && (team.getName().startsWith("tag_") || team.getName().startsWith("t_"))) {
                        try { team.unregister(); } catch (Throwable ignored) {}
                    }
                }
            }
        }
    }

    /* ================================================================
     * Prefixo ajustado (animacao / fallback)
     * ============================================================= */

    public static String getAdjustedPrefix(TagPrefix viewerStyle, Tag tag, UUID targetId, Account targetAcc) {
        if (targetAcc == null) {
            targetAcc = Core.getAccountController().of(targetId);
        }
        if (targetAcc != null && !(targetAcc.isFake() && com.minecraft.game.bukkit.fake.FakeConfig.isHideRank())) {
            String emberPrefix = com.minecraft.game.bukkit.cosmetic.EmberPlusService.formatEmberPlusPrefix(targetAcc, tag);
            if (emberPrefix != null) {
                return emberPrefix;
            }
        }

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
        if (account.isFake() && FakeConfig.isHideClanTag()) return "";
        return account.getFormattedClanTagSuffix();
    }

    public static String getAdjustedPrefix(TagPrefix viewerStyle, Tag tag, UUID targetId) {
        return getAdjustedPrefix(viewerStyle, tag, targetId, Core.getAccountController().of(targetId));
    }

    private static String truncateSuffix(String s) {
        return ClanTagFormatter.fitTeamText(s);
    }

    /** Nome valido de team: ≤ 16 chars, sem espacos. */
    private static String sanitize(String raw) {
        String s = raw.replace(' ', '_');
        if (s.length() > 16) s = s.substring(0, 16);
        return s;
    }

    private static Component legacy(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        return LEGACY.deserialize(text);
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

        Component msg = legacy("§aSuas tags: ");
        for (int i = 0; i < available.size(); i++) {
            Tag tag = available.get(i);
            String hover = "§7Previa:\n§8> "
                    + tag.getByPrefix(account.getTagPrefix())
                    + account.getNickname()
                    + "\n\n"
                    + (account.isUsingTag(tag) ? "§cJa selecionada." : "§eClique para escolher.");
            Component entry = legacy(tag.getColoredName())
                    .hoverEvent(HoverEvent.showText(legacy(hover)))
                    .clickEvent(ClickEvent.runCommand("/tag " + tag.name().toLowerCase()));
            msg = msg.append(entry);
            if (i < available.size() - 1) msg = msg.append(legacy("§7, "));
            else msg = msg.append(legacy("§7."));
        }
        account.send(msg);
    }

    public static void sendPrefixTypes(Account account) {
        Component msg = legacy("§aModos de prefixo: ");
        TagPrefix[] all = TagPrefix.values();
        for (int i = 0; i < all.length; i++) {
            TagPrefix p = all[i];
            String preview = "§7Previa:\n§8> "
                    + account.getTag().getByPrefix(p)
                    + account.getNickname()
                    + "\n\n"
                    + (account.isUsingTagPrefix(p) ? "§cJa selecionado." : "§eClique para selecionar.");
            Component entry = legacy("§e" + p.getDisplayName())
                    .hoverEvent(HoverEvent.showText(legacy(preview)))
                    .clickEvent(ClickEvent.runCommand("/prefixtype " + p.name().toLowerCase()));
            msg = msg.append(entry);
            if (i < all.length - 1) msg = msg.append(legacy("§7, "));
            else msg = msg.append(legacy("§7."));
        }
        account.send(msg);
    }
}
