package com.minecraft.lobbyhost.bukkit.menu.punish;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.punishment.Punishment;
import com.minecraft.lobbyhost.api.punishment.PunishmentCategory;
import com.minecraft.lobbyhost.backend.data.PunishmentData;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.punishment.PunishContext;
import com.minecraft.lobbyhost.punishment.PunishmentRules;
import com.minecraft.lobbyhost.util.PunishmentTimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class PunishHubMenu extends Menu {

    private final Account target;
    private final PunishContext ctx;

    public PunishHubMenu(Player player, Account target) {
        super(player, "§8§lPunições §7| §f" + target.getName(), 6, null);
        this.target = target;
        this.ctx = PunishContext.of(player.getUniqueId());
    }

    @Override
    public void build() {
        clear();
        PunishmentData data = Core.getPunishmentData();

        String banStatus = "§aNenhum";
        String muteStatus = "§aNenhum";
        java.util.Optional<Punishment> activeBan = data != null
                ? data.findActive(target.getId(), PunishmentCategory.BAN) : java.util.Optional.empty();
        java.util.Optional<Punishment> activeMute = data != null
                ? data.findActive(target.getId(), PunishmentCategory.MUTE) : java.util.Optional.empty();

        if (activeBan.isPresent()) {
            var p = activeBan.get();
            banStatus = "§cAtivo §8(" + (p.isTemporary()
                    ? PunishmentTimeUtil.formatRemaining(p.getExpiresAt()) : "Permanente") + ")";
        }
        if (activeMute.isPresent()) {
            var p = activeMute.get();
            muteStatus = "§eAtivo §8(" + (p.isTemporary()
                    ? PunishmentTimeUtil.formatRemaining(p.getExpiresAt()) : "Permanente") + ")";
        }

        int warnCount = 0;
        if (data != null) {
            for (var p : data.listByPlayer(target.getId(), 100)) {
                if (p.getCategory() == PunishmentCategory.WARN && p.isActive()) warnCount++;
            }
        }

        set(4, MenuItem.of(Material.PLAYER_HEAD)
                .skullOwner(target.getName())
                .name("§f" + target.getName())
                .lore(
                        "§7Rank: " + target.getRankType().getColor() + target.getRankType().name(),
                        "",
                        "§7Ban: " + banStatus,
                        "§7Mute: " + muteStatus,
                        "§7Advertências ativas: §f" + warnCount,
                        "",
                        "§8Selecione uma ação abaixo"
                ));

        set(20, MenuItem.of(Material.RED_BANNER)
                .name("§c§lBanimento")
                .lore("§7Banir o jogador", "", "§eClique para escolher a regra")
                .click(p -> openRules(p, PunishmentCategory.BAN)));

        set(22, MenuItem.of(Material.PAPER)
                .name("§e§lMute")
                .lore("§7Silenciar no chat", "", "§eClique para escolher a regra")
                .click(p -> openRules(p, PunishmentCategory.MUTE)));

        set(24, MenuItem.of(Material.IRON_DOOR)
                .name("§6§lKick")
                .lore("§7Expulsar sem registrar ban", "", "§eClique para continuar")
                .click(p -> {
                    clickSound();
                    if (ctx != null) ctx.setKickMode(true);
                    new PunishKickMenu(p, target, this).open();
                }));

        set(26, MenuItem.of(Material.OAK_SIGN)
                .name("§6§lAdvertência")
                .lore("§7Aviso registrado no histórico", "", "§eClique para escolher a regra")
                .click(p -> openRules(p, PunishmentCategory.WARN)));

        set(30, MenuItem.of(Material.BOOK)
                .name("§b§lHistórico")
                .lore("§7Ver punições anteriores", "", "§eClique para abrir")
                .click(p -> {
                    clickSound();
                    new PunishHistoryMenu(p, target, this, 0).open();
                }));

        boolean silent = ctx != null && ctx.silent();
        MenuItem silentItem = MenuItem.of(silent ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(silent ? "§a§lModo silencioso §8(ON)" : "§7§lModo silencioso §8(OFF)")
                .lore(
                        "§7Staff não recebe broadcast",
                        "",
                        silent ? "§aClique para desativar" : "§eClique para ativar"
                )
                .click(p -> {
                    clickSound();
                    if (ctx != null) ctx.toggleSilent();
                    reopen();
                });
        if (silent) silentItem.glow();
        set(32, silentItem);

        if (activeBan.isPresent()) {
            set(38, MenuItem.of(Material.LIME_BANNER)
                    .name("§a§lDesbanir")
                    .lore("§7Revogar banimento ativo", "§8" + activeBan.get().getId(), "", "§eClique para confirmar")
                    .click(p -> {
                        clickSound();
                        new PunishRevokeMenu(p, target, this, activeBan.get()).open();
                    }));
        }

        if (activeMute.isPresent()) {
            set(40, MenuItem.of(Material.LIME_DYE)
                    .name("§a§lDesmutar")
                    .lore("§7Revogar mute ativo", "§8" + activeMute.get().getId(), "", "§eClique para confirmar")
                    .click(p -> {
                        clickSound();
                        new PunishRevokeMenu(p, target, this, activeMute.get()).open();
                    }));
        }

        closeButton(49);
    }

    private void openRules(Player p, PunishmentCategory category) {
        clickSound();
        if (ctx != null) {
            ctx.setKickMode(false);
            ctx.setCategory(category);
        }
        new PunishRulesMenu(p, target, this, category).open();
    }
}
