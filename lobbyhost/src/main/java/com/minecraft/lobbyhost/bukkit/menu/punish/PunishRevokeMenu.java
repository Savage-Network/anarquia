package com.minecraft.lobbyhost.bukkit.menu.punish;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.punishment.Punishment;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.punishment.PunishmentService;
import com.minecraft.lobbyhost.util.ModernText;
import com.minecraft.lobbyhost.util.PunishmentTimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PunishRevokeMenu extends Menu {

    private final Account target;
    private final Punishment punishment;

    public PunishRevokeMenu(Player player, Account target, Menu parent, Punishment punishment) {
        super(player, "§8§lRevogar §7| §f" + target.getName(), 3, parent);
        this.target = target;
        this.punishment = punishment;
    }

    @Override
    public void build() {
        clear();
        String duration = punishment.isTemporary()
                ? PunishmentTimeUtil.formatRemaining(punishment.getExpiresAt())
                : "Permanente";

        set(4, MenuItem.of(Material.PLAYER_HEAD)
                .skullOwner(target.getName())
                .name("§f" + punishment.getId())
                .lore(
                        "§7Tipo: §f" + punishment.getCategory().getDisplayName(),
                        "§7Causa: §f" + punishment.getCause(),
                        "§7Tempo: §f" + duration,
                        "",
                        "§8Confirme a revogação"
                ));

        set(11, MenuItem.of(Material.LIME_WOOL)
                .name("§a§lConfirmar revogação")
                .glow()
                .click(p -> revoke(p, "Revisão staff")));

        set(15, MenuItem.of(Material.RED_WOOL)
                .name("§c§lCancelar")
                .click(p -> {
                    clickSound();
                    if (parent != null) parent.open();
                    else p.closeInventory();
                }));

        back(18);
        closeButton(22);
    }

    private void revoke(Player staff, String reason) {
        UUID authorId = staff.getUniqueId();
        PunishmentService.revoke(authorId, staff.getName(), punishment.getId(), reason)
                .thenAccept(r -> Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                    if (r.success()) {
                        successSound();
                        staff.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS
                                + ">Punição revogada: <#F1F5F9>" + punishment.getId()));
                        staff.closeInventory();
                        new PunishHubMenu(staff, target).open();
                    } else {
                        staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR + ">"
                                + r.message()));
                        reopen();
                    }
                }));
    }
}
