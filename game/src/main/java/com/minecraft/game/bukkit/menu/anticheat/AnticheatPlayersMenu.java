package com.minecraft.game.bukkit.menu.anticheat;

import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.bukkit.menu.anticheat.AnticheatFlagsMenu;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AnticheatPlayersMenu extends Menu {

    public AnticheatPlayersMenu(Player player, Menu parent) {
        super(player, "§8§lAC §7| §fJogadores", 6, parent);
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(Material.PLAYER_HEAD)
                .name("§fJogadores online")
                .lore("§7Clique: isentar sessão",
                        "§7Shift+Clique: zerar VL"));

        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        for (int i = 0; i < online.size() && i < slots.length; i++) {
            Player target = online.get(i);
            int vl = AnticheatEngine.getTotalViolations(target.getUniqueId());
            boolean exempt = AnticheatConfig.isSessionExempt(target.getUniqueId());

            MenuItem item = MenuItem.of(Material.PLAYER_HEAD)
                    .skullOwner(target.getName())
                    .name((exempt ? "§6★ " : "§f") + target.getName())
                    .lore(
                            "§7VL total: §c" + vl,
                            exempt ? "§6Isento (sessão)" : "§7Monitorado",
                            "",
                            "§eClique: ver flags (/acflags)",
                            "§eShift+Clique: alternar isenção")
                    .click(p -> {
                        clickSound();
                        if (p.isSneaking()) {
                            AnticheatConfig.toggleSessionExempt(target.getUniqueId());
                            boolean now = AnticheatConfig.isSessionExempt(target.getUniqueId());
                            p.sendMessage(now
                                    ? "§6" + target.getName() + " §eisento nesta sessão."
                                    : "§f" + target.getName() + " §avoltou a ser monitorado.");
                            reopen();
                        } else {
                            new AnticheatFlagsMenu(p, target, this, 0).open();
                        }
                    });
            if (vl > 0) item.glow();
            set(slots[i], item);
        }

        back(45);
        closeButton(49);
    }
}
