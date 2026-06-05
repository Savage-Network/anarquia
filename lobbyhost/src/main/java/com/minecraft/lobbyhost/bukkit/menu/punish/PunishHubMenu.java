package com.minecraft.lobbyhost.bukkit.menu.punish;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.punishment.PunishmentCategory;
import com.minecraft.lobbyhost.backend.data.PunishmentData;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.punishment.PunishContext;
import com.minecraft.lobbyhost.util.PunishmentTimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class PunishHubMenu extends Menu {

    private final Account target;
    private final PunishContext ctx;

    public PunishHubMenu(Player player, Account target) {
        super(player, "§8§lPunições §7| §f" + target.getName(), 5, null);
        this.target = target;
        this.ctx = PunishContext.of(player.getUniqueId());
    }

    @Override
    public void build() {
        clear();
        PunishmentData data = Core.getPunishmentData();

        String banStatus = "§aNenhum";
        String muteStatus = "§aNenhum";
        if (data != null) {
            var ban = data.findActive(target.getId(), PunishmentCategory.BAN);
            if (ban.isPresent()) {
                var p = ban.get();
                banStatus = "§cAtivo §8(" + (p.isTemporary()
                        ? PunishmentTimeUtil.formatRemaining(p.getExpiresAt()) : "Permanente") + ")";
            }
            var mute = data.findActive(target.getId(), PunishmentCategory.MUTE);
            if (mute.isPresent()) {
                var p = mute.get();
                muteStatus = "§eAtivo §8(" + (p.isTemporary()
                        ? PunishmentTimeUtil.formatRemaining(p.getExpiresAt()) : "Permanente") + ")";
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
                        "",
                        "§8Selecione uma ação abaixo"
                ));

        set(20, MenuItem.of(Material.RED_BANNER)
                .name("§c§lBanimento")
                .lore("§7Banir o jogador do servidor", "", "§eClique para continuar")
                .click(p -> {
                    clickSound();
                    if (ctx != null) {
                        ctx.setKickMode(false);
                        ctx.setCategory(PunishmentCategory.BAN);
                    }
                    new PunishReasonMenu(p, target, this).open();
                }));

        set(22, MenuItem.of(Material.PAPER)
                .name("§e§lMute")
                .lore("§7Silenciar no chat", "", "§eClique para continuar")
                .click(p -> {
                    clickSound();
                    if (ctx != null) {
                        ctx.setKickMode(false);
                        ctx.setCategory(PunishmentCategory.MUTE);
                    }
                    new PunishReasonMenu(p, target, this).open();
                }));

        set(24, MenuItem.of(Material.IRON_DOOR)
                .name("§6§lKick")
                .lore("§7Expulsar sem registrar ban", "", "§eClique para continuar")
                .click(p -> {
                    clickSound();
                    if (ctx != null) ctx.setKickMode(true);
                    new PunishKickMenu(p, target, this).open();
                }));

        set(30, MenuItem.of(Material.BOOK)
                .name("§b§lHistórico")
                .lore("§7Ver punições anteriores", "", "§eClique para abrir")
                .click(p -> {
                    clickSound();
                    new PunishHistoryMenu(p, target, this, 0).open();
                }));

        closeButton(40);
    }
}
