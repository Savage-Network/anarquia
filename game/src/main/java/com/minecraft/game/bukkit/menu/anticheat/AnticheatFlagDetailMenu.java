package com.minecraft.game.bukkit.menu.anticheat;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.anticheat.AnticheatFlagRecord;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Detalhe completo de um check/flag ({@code /acflags} → clique). */
public final class AnticheatFlagDetailMenu extends Menu {

    private final UUID targetId;
    private final String targetName;
    private final AnticheatCheck check;

    public AnticheatFlagDetailMenu(Player viewer, UUID targetId, String targetName,
                                   AnticheatCheck check, Menu parent) {
        super(viewer, "§8§lAC §7| §f" + check.getDisplayName(), 6, parent);
        this.targetId = targetId;
        this.targetName = targetName;
        this.check = check;
    }

    @Override
    public void build() {
        clear();

        int vl = AnticheatEngine.getCheckViolations(targetId, check);
        boolean globalOn = AnticheatConfig.isCheckEnabled(check);
        List<AnticheatFlagRecord> history = AnticheatEngine.getFlagHistory(targetId, check);

        set(4, MenuItem.of(check.getIcon())
                .name("§f" + check.getDisplayName() + " §8— §7" + targetName)
                .lore(
                        "§7Categoria: " + check.getCategory().getDisplay(),
                        "§8" + check.getDescription(),
                        "",
                        "§7VL check: §" + (vl > 0 ? "c" : "a") + vl,
                        "§7VL total jogador: §c" + AnticheatEngine.getTotalViolations(targetId),
                        "§7Check global: " + (globalOn ? "§aAtivo" : "§cDesativado"),
                        "§7Config key: §8" + check.getConfigKey()
                ));

        if (history.isEmpty()) {
            set(22, MenuItem.of(Material.BARRIER)
                    .name("§7Sem flags registradas")
                    .lore("§7Este jogador não foi flagado", "§7por §f" + check.getDisplayName(),
                            "§7nesta sessão."));
        } else {
            int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
            for (int i = 0; i < history.size() && i < slots.length; i++) {
                AnticheatFlagRecord rec = history.get(i);
                set(slots[i], flagEntry(i + 1, rec));
            }
        }

        set(40, MenuItem.of(Material.LAVA_BUCKET)
                .name("§cZerar VL deste check")
                .lore("§7Zera apenas §f" + check.getDisplayName())
                .click(p -> {
                    clickSound();
                    AnticheatEngine.resetCheckViolations(targetId, check);
                    p.sendMessage("§aVL de §f" + check.getDisplayName()
                            + " §ade §f" + targetName + " §azerado.");
                    successSound();
                    reopen();
                }));

        back(45);
        closeButton(49);
    }

    private MenuItem flagEntry(int index, AnticheatFlagRecord rec) {
        long sec = Math.max(0L, (System.currentTimeMillis() - rec.atMs()) / 1000L);
        String when = sec < 60 ? sec + "s" : (sec < 3600 ? (sec / 60) + "min" : TimeUnit.SECONDS.toHours(sec) + "h");

        List<String> lore = new ArrayList<>();
        lore.add("§7Há §f" + when);
        lore.add("§7VL check: §c" + rec.checkVl());
        lore.add("§7VL total: §c" + rec.totalVl());
        lore.add("");
        if (rec.detail().isBlank()) {
            lore.add("§8(sem detalhe)");
        } else {
            for (String line : wrap(rec.detail(), 38)) {
                lore.add("§7" + line);
            }
        }

        MenuItem item = MenuItem.of(Material.PAPER)
                .name("§e#" + index + " §fFlag")
                .lore(lore.toArray(String[]::new));
        if (index == 1) item.glow();
        return item;
    }

    private static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            if (line.length() + w.length() + 1 > width) {
                lines.add(line.toString().trim());
                line = new StringBuilder();
            }
            if (!line.isEmpty()) line.append(' ');
            line.append(w);
        }
        if (!line.isEmpty()) lines.add(line.toString().trim());
        if (lines.size() > 6) return lines.subList(0, 6);
        return lines;
    }
}
