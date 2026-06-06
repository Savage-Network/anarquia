package com.minecraft.game.bukkit.menu.anticheat;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class AnticheatHubMenu extends Menu {

    public AnticheatHubMenu(Player player) {
        super(player, "§c§lANTICHEAT §8| §7SavageMC", 6, null);
    }

    @Override
    public void build() {
        clear();

        boolean global = AnticheatConfig.isEnabled();
        set(4, MenuItem.of(Material.NETHERITE_SWORD)
                .name("§c§lSavageMC Anticheat")
                .lore(
                        "§7Estado: " + (global ? "§aAtivo" : "§cDesativado"),
                        "§7Alertas staff: " + (AnticheatConfig.isAlertStaff() ? "§aSim" : "§cNão"),
                        "§7Lag comp: " + (AnticheatConfig.isLagCompensation() ? "§aSim" : "§cNão"),
                        "§7Setback: " + (AnticheatConfig.isSetbackEnabled() ? "§aSim" : "§cNão"),
                        "§7Punições: " + (AnticheatConfig.isPunishEnabled() ? "§aSim" : "§cNão"),
                        "",
                        "§8Owner only"));

        set(10, globalToggle(Material.LIME_DYE, Material.GRAY_DYE,
                "§aAnticheat global", AnticheatConfig.isEnabled(), () ->
                        AnticheatConfig.setEnabled(!AnticheatConfig.isEnabled())));

        set(12, globalToggle(Material.BELL, Material.BELL,
                "§eAlertas para staff", AnticheatConfig.isAlertStaff(), () ->
                        AnticheatConfig.setAlertStaff(!AnticheatConfig.isAlertStaff())));

        set(14, categoryButton(AnticheatCheck.Category.MOVEMENT));
        set(15, categoryButton(AnticheatCheck.Category.COMBAT));
        set(16, categoryButton(AnticheatCheck.Category.WORLD));
        set(17, categoryButton(AnticheatCheck.Category.PLAYER));

        set(24, MenuItem.of(Material.ANVIL)
                .name("§cPunições automáticas")
                .lore("§7Kick VL: §f" + AnticheatConfig.getKickVl(),
                        "§7Ban VL: §f" + AnticheatConfig.getBanVl(),
                        "§eClique para configurar")
                .click(p -> {
                    clickSound();
                    new AnticheatPunishmentMenu(p, this).open();
                }));

        set(26, MenuItem.of(Material.PLAYER_HEAD)
                .name("§fJogadores online")
                .lore("§7VL, isenção e reset",
                        "§7Online: §f" + Bukkit.getOnlinePlayers().size(),
                        "§eClique para abrir")
                .click(p -> {
                    clickSound();
                    new AnticheatPlayersMenu(p, this).open();
                }));

        set(28, globalToggle(Material.ENDER_PEARL, Material.ENDER_EYE,
                "§bSetback (movimento)", AnticheatConfig.isSetbackEnabled(), () ->
                        AnticheatConfig.setSetbackEnabled(!AnticheatConfig.isSetbackEnabled())));

        set(29, globalToggle(Material.CLOCK, Material.CLOCK,
                "§bCompensação de lag", AnticheatConfig.isLagCompensation(), () ->
                        AnticheatConfig.setLagCompensation(!AnticheatConfig.isLagCompensation())));

        set(30, MenuItem.of(Material.BOOK)
                .name("§bEstatísticas")
                .lore(
                        "§7Checks ativos: §f" + countEnabledChecks(),
                        "§7Total checks: §f" + AnticheatCheck.values().length,
                        "§7Seu VL monitor: §c" + AnticheatEngine.getTotalViolations(player.getUniqueId()),
                        "",
                        "§7Flags: §e/acflags <nick>"));

        set(32, MenuItem.of(Material.EMERALD)
                .name("§aSalvar configuração")
                .lore("§7Grava em §fanticheat.yml")
                .click(p -> {
                    clickSound();
                    AnticheatConfig.save();
                    successSound();
                    p.sendMessage("§a[AC] Configuração salva.");
                })
                .glow());

        set(34, MenuItem.of(Material.COMPARATOR)
                .name("§eRecarregar")
                .lore("§7Relê §fanticheat.yml §7do disco")
                .click(p -> {
                    clickSound();
                    AnticheatConfig.load(com.minecraft.game.Core.getJavaPlugin());
                    AnticheatEngine.reload();
                    p.sendMessage("§a[AC] Configuração recarregada.");
                    reopen();
                }));

        closeButton(49);
    }

    private MenuItem globalToggle(Material onMat, Material offMat, String name, boolean on, Runnable toggle) {
        MenuItem item = MenuItem.of(on ? onMat : offMat)
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

    private MenuItem categoryButton(AnticheatCheck.Category cat) {
        int enabled = 0;
        int total = 0;
        for (AnticheatCheck c : AnticheatCheck.values()) {
            if (c.getCategory() != cat) continue;
            total++;
            if (AnticheatConfig.isCheckEnabled(c)) enabled++;
        }
        return MenuItem.of(cat.getIcon())
                .name(cat.getDisplay())
                .lore("§7" + enabled + "/" + total + " checks ativos",
                        "§eClique para gerenciar")
                .click(p -> {
                    clickSound();
                    new AnticheatCategoryMenu(p, cat, this).open();
                });
    }

    private int countEnabledChecks() {
        int n = 0;
        for (AnticheatCheck c : AnticheatCheck.values()) {
            if (AnticheatConfig.isCheckEnabled(c)) n++;
        }
        return n;
    }
}
