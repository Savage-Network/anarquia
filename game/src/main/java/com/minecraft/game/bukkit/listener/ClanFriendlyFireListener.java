package com.minecraft.game.bukkit.listener;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanRelationService;
import com.minecraft.game.bukkit.semi.SpawnProtection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/** PvP entre membros do mesmo clã e relações de aliança/inimigo. */
public final class ClanFriendlyFireListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (victim.getUniqueId().equals(attacker.getUniqueId())) return;

        Account vAcc = Core.getAccountController().of(victim.getUniqueId());
        Account aAcc = Core.getAccountController().of(attacker.getUniqueId());
        if (vAcc == null || aAcc == null) return;

        if (vAcc.hasClan() && aAcc.hasClan()) {
            Clan vClan = vAcc.getClan();
            Clan aClan = aAcc.getClan();
            if (vClan != null && aClan != null) {
                if (vClan.getId().equals(aClan.getId())) {
                    if (!vClan.isFriendlyFire()) {
                        event.setCancelled(true);
                        attacker.sendMessage("§cPvP entre membros do clã está desativado.");
                        return;
                    }
                } else if (ClanRelationService.areAllies(vClan, aClan)) {
                    event.setCancelled(true);
                    attacker.sendMessage("§cVocê não pode atacar aliados do clã.");
                    return;
                }
            }
        }

        if (SpawnProtection.isProtected(victim.getLocation())) {
            event.setCancelled(true);
        }
    }
}
