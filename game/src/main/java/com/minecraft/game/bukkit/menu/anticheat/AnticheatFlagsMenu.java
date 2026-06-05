package com.minecraft.game.bukkit.menu.anticheat;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.anticheat.AnticheatFlagRecord;
import com.minecraft.game.anticheat.AnticheatProfile;
import com.minecraft.game.anticheat.AnticheatProfiles;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Menu paginado de flags/VL de um jogador ({@code /acflags}). */
public final class AnticheatFlagsMenu extends Menu {

    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private static final List<AnticheatCheck> ALL_CHECKS = List.of(AnticheatCheck.values());

    private final UUID targetId;
    private final String targetName;
    /** 0 = resumo; 1+ = páginas de checks. */
    private final int page;

    public AnticheatFlagsMenu(Player viewer, Player target, Menu parent, int page) {
        super(viewer, titleFor(target.getName(), page, totalPages()), 6, parent);
        this.targetId = target.getUniqueId();
        this.targetName = target.getName();
        this.page = page;
    }

    private static int totalPages() {
        return 1 + (int) Math.ceil(ALL_CHECKS.size() / (double) CONTENT_SLOTS.length);
    }

    private static String titleFor(String name, int page, int pages) {
        if (page <= 0) {
            return "§8§lAC Flags §7| §f" + name + " §8— §7Resumo";
        }
        return "§8§lAC Flags §7| §f" + name + " §8— §7" + page + "/" + (pages - 1);
    }

    @Override
    public void build() {
        clear();
        Player target = Bukkit.getPlayer(targetId);

        if (page <= 0) {
            buildOverview(target);
        } else {
            buildChecksPage(target, page - 1);
        }

        navigation(target);
    }

    private void buildOverview(Player target) {
        int totalVl = AnticheatEngine.getTotalViolations(targetId);
        boolean sessionExempt = AnticheatConfig.isSessionExempt(targetId);
        boolean monitored = target != null && !AnticheatEngine.isExempt(target);

        List<String> headLore = new ArrayList<>();
        headLore.add("§7VL total: §c" + totalVl);
        headLore.add("§7Checks com VL: §f" + AnticheatEngine.countChecksWithViolations(targetId));
        headLore.add("§7Kick em: §f" + AnticheatConfig.getKickVl() + " VL");
        headLore.add("§7Ban em: §f" + AnticheatConfig.getBanVl() + " VL");
        headLore.add("");
        headLore.add(sessionExempt ? "§6★ Isento (sessão)" : (monitored ? "§aMonitorado agora" : "§eIsento agora"));
        if (target != null) {
            headLore.add("§7Gamemode: §f" + target.getGameMode().name());
            headLore.add("§7Ping: §f" + target.getPing() + "ms");
        } else {
            headLore.add("§cOffline neste servidor");
        }

        MenuItem head = MenuItem.of(Material.PLAYER_HEAD)
                .skullOwner(targetName)
                .name("§f" + targetName)
                .lore(headLore.toArray(String[]::new));
        if (totalVl >= AnticheatConfig.getBanVl()) head.glow();
        else if (totalVl >= AnticheatConfig.getKickVl()) head.glow();
        set(4, head);

        set(10, categorySummary(AnticheatCheck.Category.MOVEMENT));
        set(12, categorySummary(AnticheatCheck.Category.COMBAT));
        set(14, categorySummary(AnticheatCheck.Category.WORLD));
        set(16, categorySummary(AnticheatCheck.Category.PLAYER));

        List<String> exemptLore = new ArrayList<>();
        exemptLore.add("§7Motivos de isenção atuais:");
        if (target != null) {
            List<String> reasons = AnticheatEngine.exemptReasons(target);
            if (reasons.isEmpty()) {
                exemptLore.add("§aNenhum — sujeito a checks");
            } else {
                for (String r : reasons) exemptLore.add("§8▪ §e" + r);
            }
        } else {
            exemptLore.add("§8▪ §7Jogador offline");
        }
        set(22, MenuItem.of(Material.SHIELD)
                .name("§eStatus de monitoramento")
                .lore(exemptLore.toArray(String[]::new)));

        if (target != null) {
            AnticheatProfile profile = AnticheatProfiles.of(target);
            set(30, MenuItem.of(Material.COMPASS)
                    .name("§bPerfil live")
                    .lore(
                            "§7Air ticks: §f" + profile.airTicks,
                            "§7Moves/s: §f" + profile.movesThisSecond,
                            "§7Places/s: §f" + profile.placesThisSecond,
                            "§7CPS L/R: §f" + profile.lastCpsLeft + "/" + profile.lastCpsRight,
                            "§7Alvos (janela): §f" + profile.distinctTargetsInWindow(),
                            "§7Fall dist: §f" + String.format(Locale.US, "%.2f", profile.lastFallDistance)
                    ));
        }

        set(32, MenuItem.of(Material.LAVA_BUCKET)
                .name("§cZerar VL")
                .lore("§7Remove todo VL e histórico", "§7desta sessão.", "", "§eClique para confirmar")
                .click(p -> {
                    clickSound();
                    AnticheatEngine.resetViolations(targetId);
                    p.sendMessage("§aVL e flags de §f" + targetName + " §azerados.");
                    successSound();
                    reopen();
                }));

        set(34, MenuItem.of(Material.GOLDEN_HELMET)
                .name(sessionExempt ? "§6Remover isenção" : "§eIsentar sessão")
                .lore("§7Alterna isenção manual", "§7via painel /ac.", "", "§eClique para alternar")
                .click(p -> {
                    clickSound();
                    AnticheatConfig.toggleSessionExempt(targetId);
                    boolean now = AnticheatConfig.isSessionExempt(targetId);
                    p.sendMessage(now
                            ? "§6" + targetName + " §eisento nesta sessão."
                            : "§f" + targetName + " §avoltou a ser monitorado.");
                    reopen();
                }));
    }

    private MenuItem categorySummary(AnticheatCheck.Category cat) {
        int vl = AnticheatEngine.getCategoryViolations(targetId, cat);
        int checks = 0;
        int enabled = 0;
        int flagged = 0;
        for (AnticheatCheck c : AnticheatCheck.values()) {
            if (c.getCategory() != cat) continue;
            checks++;
            if (AnticheatConfig.isCheckEnabled(c)) enabled++;
            if (AnticheatEngine.getCheckViolations(targetId, c) > 0) flagged++;
        }

        MenuItem item = MenuItem.of(cat.getIcon())
                .name(cat.getDisplay())
                .lore(
                        "§7VL categoria: §c" + vl,
                        "§7Checks flagados: §f" + flagged + "/" + checks,
                        "§7Checks ativos: §f" + enabled + "/" + checks,
                        "",
                        "§8Veja detalhes nas páginas →"
                );
        if (vl > 0) item.glow();
        return item;
    }

    private void buildChecksPage(Player target, int checkPage) {
        int start = checkPage * CONTENT_SLOTS.length;
        int end = Math.min(start + CONTENT_SLOTS.length, ALL_CHECKS.size());

        set(4, MenuItem.of(Material.PAPER)
                .name("§fChecks §7(" + (start + 1) + "-" + end + " de " + ALL_CHECKS.size() + ")")
                .lore(
                        "§7VL total: §c" + AnticheatEngine.getTotalViolations(targetId),
                        "§7Clique num check para ver histórico"
                ));

        for (int i = start; i < end; i++) {
            int slotIdx = i - start;
            AnticheatCheck check = ALL_CHECKS.get(i);
            set(CONTENT_SLOTS[slotIdx], checkItem(check, target));
        }
    }

    private MenuItem checkItem(AnticheatCheck check, Player target) {
        int vl = AnticheatEngine.getCheckViolations(targetId, check);
        boolean globalOn = AnticheatConfig.isCheckEnabled(check);
        AnticheatFlagRecord last = AnticheatEngine.getLastFlag(targetId, check);
        long cdMs = AnticheatEngine.getAlertCooldownRemainingMs(targetId, check);

        List<String> lore = new ArrayList<>();
        lore.add("§7Categoria: " + check.getCategory().getDisplay());
        lore.add("§8" + check.getDescription());
        lore.add("");
        lore.add("§7VL check: §" + (vl > 0 ? "c" : "a") + vl);
        lore.add("§7Check global: " + (globalOn ? "§aAtivo" : "§cDesativado"));
        if (cdMs > 0) {
            lore.add("§7Cooldown alerta: §f" + (cdMs / 1000L) + "s");
        }
        if (last != null) {
            lore.add("");
            lore.add("§7Última flag: §f" + formatAgo(last.atMs()));
            if (!last.detail().isBlank()) {
                lore.add("§8» §7" + truncate(last.detail(), 48));
            }
            lore.add("§8» §7VL na flag: §c" + last.checkVl() + " §8| §7Total: §c" + last.totalVl());
        } else {
            lore.add("");
            lore.add("§7Nunca flagado nesta sessão.");
        }

        List<AnticheatFlagRecord> history = AnticheatEngine.getFlagHistory(targetId, check);
        if (history.size() > 1) {
            lore.add("");
            lore.add("§7Histórico recente:");
            int shown = 0;
            for (AnticheatFlagRecord rec : history) {
                if (shown >= 4) break;
                if (rec == last && shown == 0) {
                    shown++;
                    continue;
                }
                lore.add("§8▪ §7" + formatAgo(rec.atMs()) + " §8— §f" + truncate(rec.detail(), 28));
                shown++;
            }
        }

        lore.add("");
        lore.add("§eClique: detalhes completos");

        MenuItem item = MenuItem.of(check.getIcon())
                .name((vl > 0 ? "§c● " : "§8● ") + "§f" + check.getDisplayName())
                .lore(lore.toArray(String[]::new))
                .click(p -> {
                    clickSound();
                    new AnticheatFlagDetailMenu(p, targetId, targetName, check, this).open();
                });
        if (vl > 0) item.glow();
        return item;
    }

    private void navigation(Player target) {
        int pages = totalPages();

        if (page > 0) {
            set(45, MenuItem.of(Material.ARROW)
                    .name(page == 1 ? "§a← Resumo" : "§a← Página anterior")
                    .click(p -> new AnticheatFlagsMenu(p, requireTarget(p), parent, page - 1).open()));
        }

        if (parent != null) {
            back(48);
        }

        set(49, MenuItem.of(Material.SUNFLOWER)
                .name("§eAtualizar")
                .lore("§7Recarrega dados live")
                .click(p -> {
                    clickSound();
                    reopen();
                }));

        if (page < pages - 1) {
            set(53, MenuItem.of(Material.ARROW)
                    .name(page == 0 ? "§aVer checks →" : "§aPróxima página →")
                    .click(p -> new AnticheatFlagsMenu(p, requireTarget(p), parent, page + 1).open()));
        }

        closeButton(50);
    }

    private Player requireTarget(Player viewer) {
        Player t = Bukkit.getPlayer(targetId);
        if (t != null) return t;
        viewer.sendMessage("§c" + targetName + " saiu — menu fechado.");
        viewer.closeInventory();
        return viewer;
    }

    private static String formatAgo(long atMs) {
        long sec = Math.max(0L, (System.currentTimeMillis() - atMs) / 1000L);
        if (sec < 60) return sec + "s atrás";
        if (sec < 3600) return (sec / 60) + "min atrás";
        return TimeUnit.SECONDS.toHours(sec) + "h atrás";
    }

    private static String truncate(String s, int max) {
        if (s == null || s.isBlank()) return "—";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
