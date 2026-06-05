package com.minecraft.game.bukkit.menu.punish;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.api.punishment.Punishment;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.punishment.PunishmentService;
import com.minecraft.game.util.ModernText;
import com.minecraft.game.util.PunishmentTimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class PunishDetailMenu extends Menu {

    private final Account target;
    private final Punishment punishment;

    public PunishDetailMenu(Player player, Account target, Menu parent, Punishment punishment) {
        super(player, "§8§l" + punishment.getId(), 3, parent);
        this.target = target;
        this.punishment = punishment;
    }

    @Override
    public void build() {
        clear();
        String status = punishment.isActive() ? "§aAtiva" : "§7Revogada";
        String duration = punishment.isTemporary()
                ? PunishmentTimeUtil.formatRemaining(punishment.getExpiresAt())
                : "Permanente";

        set(4, MenuItem.of(Material.BOOK)
                .name("§f" + punishment.getId())
                .lore(
                        "§7Jogador: §f" + target.getName(),
                        "§7Tipo: §c" + punishment.getCategory().getDisplayName(),
                        "§7Motivo: §f" + punishment.getReason().getDisplayName(),
                        "§7Causa: §f" + punishment.getCause(),
                        "§7Tempo: §f" + duration,
                        "§7Status: " + status
                ));

        Account staff = Core.getAccountController().of(player.getUniqueId());
        boolean canRevoke = punishment.isActive()
                && staff != null
                && staff.getRankType().isAtLeast(RankType.MOD);

        if (canRevoke) {
            set(11, MenuItem.of(Material.LIME_DYE)
                    .name("§a§lDespunir")
                    .lore("§7Revogar esta punição", "", "§aClique para confirmar")
                    .glow()
                    .click(p -> {
                        clickSound();
                        PunishmentService.revoke(p.getUniqueId(), p.getName(),
                                        punishment.getId(), "Revogado via menu")
                                .thenAccept(r -> Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                                    if (r.success()) {
                                        successSound();
                                        p.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS
                                                + ">Punição revogada: <#F1F5F9>" + punishment.getId()));
                                        p.closeInventory();
                                    } else {
                                        p.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR + ">"
                                                + r.message()));
                                    }
                                }));
                    }));
        }

        set(15, MenuItem.of(Material.BARRIER)
                .name("§cFechar")
                .click(p -> {
                    clickSound();
                    if (parent != null) parent.open();
                    else p.closeInventory();
                }));

        back(18);
    }
}
