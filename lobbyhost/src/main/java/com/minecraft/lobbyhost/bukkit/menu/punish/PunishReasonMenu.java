package com.minecraft.lobbyhost.bukkit.menu.punish;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.punishment.PunishmentCategory;
import com.minecraft.lobbyhost.api.punishment.PunishmentReason;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.punishment.PunishContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class PunishReasonMenu extends Menu {

    private final Account target;

    public PunishReasonMenu(Player player, Account target, Menu parent) {
        super(player, "§8§lMotivo §7| §f" + target.getName(), 3, parent);
        this.target = target;
    }

    @Override
    public void build() {
        clear();
        PunishContext ctx = PunishContext.of(player.getUniqueId());
        PunishmentCategory cat = ctx != null ? ctx.category() : PunishmentCategory.BAN;
        String catName = cat.getDisplayName();

        set(11, MenuItem.of(Material.ANVIL)
                .name("§4§lCheating / Trapaça")
                .lore("§7Uso de hacks, x-ray, killaura...", "", "§eClique para ver presets")
                .click(p -> {
                    clickSound();
                    if (ctx != null) ctx.setReason(PunishmentReason.CHEATING);
                    new PunishPresetMenu(p, target, this, cat, PunishmentReason.CHEATING).open();
                }));

        set(15, MenuItem.of(Material.WRITABLE_BOOK)
                .name("§6§lComunidade / Regras")
                .lore("§7Assédio, spam, skin inapropriada...", "", "§eClique para ver presets")
                .click(p -> {
                    clickSound();
                    if (ctx != null) ctx.setReason(PunishmentReason.COMMUNITY);
                    new PunishPresetMenu(p, target, this, cat, PunishmentReason.COMMUNITY).open();
                }));

        set(13, MenuItem.of(Material.NAME_TAG)
                .name("§7Tipo: §f" + catName)
                .lore("§8Escolha o motivo da punição"));

        back(18);
        closeButton(22);
    }
}
