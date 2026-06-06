package com.minecraft.lobbyhost.bukkit.menu.punish;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.punishment.PunishmentCategory;
import com.minecraft.lobbyhost.api.punishment.PunishmentReason;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.punishment.PunishContext;
import com.minecraft.lobbyhost.punishment.PunishmentRules;
import com.minecraft.lobbyhost.punishment.PunishmentService;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PunishConfirmMenu extends Menu {

    private final Account target;

    public PunishConfirmMenu(Player player, Account target, Menu parent) {
        super(player, "§8§lConfirmar §7| §f" + target.getName(), 3, parent);
        this.target = target;
    }

    @Override
    public void build() {
        clear();
        PunishContext ctx = PunishContext.of(player.getUniqueId());
        boolean kick = ctx != null && ctx.kickMode();

        String action = kick ? "Kick" : (ctx != null && ctx.category() != null
                ? ctx.category().getDisplayName() : "Punição");
        String reason = ctx != null && ctx.reason() != null ? ctx.reason().getDisplayName() : "—";
        String time = ctx != null ? PunishmentRules.formatTime(ctx.time()) : "—";
        String cause = ctx != null ? ctx.cause() : "Não informado";
        int offense = ctx != null ? ctx.offenseNumber() : 1;
        boolean silent = ctx != null && ctx.silent();

        set(4, MenuItem.of(Material.PLAYER_HEAD)
                .skullOwner(target.getName())
                .name("§f" + target.getName())
                .lore(
                        "§7Ação: §f" + action,
                        kick ? "" : "§7Motivo: §f" + reason,
                        kick ? "" : "§7Infração: §f#" + offense,
                        kick ? "" : "§7Tempo: §f" + time,
                        "§7Causa: §f" + cause,
                        silent ? "§7Modo: §aSilencioso" : "§7Modo: §fNormal",
                        "",
                        "§8Confirme abaixo"
                ));

        set(11, MenuItem.of(Material.LIME_WOOL)
                .name("§a§lConfirmar")
                .lore("§7Aplicar punição", "", "§aClique para executar")
                .glow()
                .click(p -> {
                    clickSound();
                    apply(p, ctx, kick);
                }));

        set(15, MenuItem.of(Material.RED_WOOL)
                .name("§c§lCancelar")
                .lore("§7Voltar sem aplicar")
                .click(p -> {
                    clickSound();
                    if (parent != null) parent.open();
                    else p.closeInventory();
                }));

        back(18);
        closeButton(22);
    }

    private void apply(Player staff, PunishContext ctx, boolean kick) {
        if (ctx == null) {
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR + ">Sessão expirada."));
            staff.closeInventory();
            return;
        }

        UUID authorId = staff.getUniqueId();
        String authorName = staff.getName();
        String targetName = target.getName();

        if (kick) {
            PunishmentService.kick(authorId, authorName, targetName, ctx.cause())
                    .thenAccept(r -> Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                        if (r.success()) {
                            successSound();
                            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS
                                    + "><#F1F5F9>" + r.targetName() + " <" + ModernText.SUCCESS + ">expulso."));
                            PunishContext.close(staff.getUniqueId());
                            staff.closeInventory();
                        } else {
                            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR + ">"
                                    + r.message()));
                            reopen();
                        }
                    }));
            return;
        }

        PunishmentCategory category = ctx.category();
        PunishmentReason reason = ctx.reason();
        if (category == null || reason == null) {
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR + ">Dados incompletos."));
            return;
        }

        PunishmentService.apply(authorId, authorName, targetName, category, reason,
                        ctx.time(), ctx.cause(), ctx.ruleId(), ctx.silent())
                .thenAccept(r -> Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                    if (r.success()) {
                        successSound();
                        staff.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS
                                + ">Punição aplicada em <#F1F5F9>" + r.targetName()
                                + " <#64748B>ID <#F1F5F9>" + r.punishment().getId()));
                        PunishContext.close(staff.getUniqueId());
                        staff.closeInventory();
                    } else {
                        staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR + ">"
                                + r.message()));
                        reopen();
                    }
                }));
    }
}
