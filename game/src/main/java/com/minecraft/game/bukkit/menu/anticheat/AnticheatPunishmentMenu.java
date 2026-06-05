package com.minecraft.game.bukkit.menu.anticheat;

import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class AnticheatPunishmentMenu extends Menu {

    private static final int[] KICK_OPTIONS = {10, 15, 20, 25, 30, 40, 50};
    private static final int[] BAN_OPTIONS = {20, 30, 40, 50, 60, 80, 100};

    public AnticheatPunishmentMenu(Player player, Menu parent) {
        super(player, "§8§lAC §7| §cPunições", 4, parent);
    }

    @Override
    public void build() {
        clear();

        boolean punishOn = AnticheatConfig.isPunishEnabled();
        set(11, toggle(Material.IRON_DOOR, "§cPunições automáticas", punishOn, () -> {
            AnticheatConfig.setPunishEnabled(!AnticheatConfig.isPunishEnabled());
        }));

        set(13, cycle(Material.BELL, "§eKick em VL total",
                AnticheatConfig.getKickVl(), KICK_OPTIONS, AnticheatConfig::setKickVl));

        set(15, cycle(Material.BARRIER, "§4Ban em VL total",
                AnticheatConfig.getBanVl(), BAN_OPTIONS, AnticheatConfig::setBanVl));

        set(22, toggle(Material.ENDER_PEARL, "§7Resetar VL ao sair",
                AnticheatConfig.isResetVlOnQuit(), () -> {
            AnticheatConfig.setResetVlOnQuit(!AnticheatConfig.isResetVlOnQuit());
        }));

        back(27);
        closeButton(31);
    }

    private MenuItem toggle(Material mat, String name, boolean on, Runnable toggle) {
        MenuItem item = MenuItem.of(mat)
                .name(name)
                .lore("", on ? "§aAtivado" : "§cDesativado", "§eClique para alternar")
                .click(p -> {
                    clickSound();
                    toggle.run();
                    AnticheatConfig.save();
                    reopen();
                });
        return on ? item.glow() : item;
    }

    private MenuItem cycle(Material mat, String name, int current, int[] options,
                           java.util.function.IntConsumer setter) {
        return MenuItem.of(mat)
                .name(name)
                .lore("§7Valor: §f" + current, "§eClique para alterar")
                .click(p -> {
                    clickSound();
                    int idx = 0;
                    for (int i = 0; i < options.length; i++) {
                        if (options[i] == current) {
                            idx = (i + 1) % options.length;
                            break;
                        }
                    }
                    setter.accept(options[idx]);
                    AnticheatConfig.save();
                    reopen();
                });
    }
}
