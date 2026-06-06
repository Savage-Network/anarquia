package com.minecraft.game.api.clan;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.network.ActivityFeedService;
import com.minecraft.game.network.NotificationService;
import org.bukkit.Bukkit;

import java.util.UUID;

public final class ClanBankService {

    private ClanBankService() {}

    public static ClanService.Result deposit(Account actor, long amount) {
        if (actor == null || amount <= 0) return ClanService.Result.fail("§cValor inválido.");
        if (!actor.hasClan()) return ClanService.Result.fail("§cVocê não faz parte de um clã.");
        if (actor.getGolds() < amount) return ClanService.Result.fail("§cSaldo insuficiente.");

        Clan clan = actor.getClan();
        actor.removeGolds((int) Math.min(amount, Integer.MAX_VALUE));
        clan.getStorage().setBankBalance(clan.getStorage().getBankBalance() + amount);
        clan.persist();

        String msg = "§aDepositou §f" + amount + " §ano banco do clã.";
        clan.broadcast("§7" + actor.getNickname() + " depositou §a" + amount + " §7no banco.");
        ActivityFeedService.publish("clan", actor.getNickname(),
                "depositou " + amount + " no clã " + clan.getName(),
                com.minecraft.game.GameSettings.SERVER_CHAT_ID);
        notifyMembers(clan, actor.getNickname() + " depositou " + amount + " no banco.");
        return ClanService.Result.ok(msg);
    }

    public static ClanService.Result withdraw(Account actor, long amount) {
        if (actor == null || amount <= 0) return ClanService.Result.fail("§cValor inválido.");
        if (!actor.hasClanRole(ClanRole.RECRUITER)) {
            return ClanService.Result.fail("§cSem permissão para sacar do banco.");
        }
        Clan clan = actor.getClan();
        if (clan.getStorage().getBankBalance() < amount) {
            return ClanService.Result.fail("§cBanco do clã insuficiente.");
        }

        clan.getStorage().setBankBalance(clan.getStorage().getBankBalance() - amount);
        clan.persist();
        actor.addGolds((int) Math.min(amount, Integer.MAX_VALUE));
        clan.broadcast("§7" + actor.getNickname() + " sacou §a" + amount + " §7do banco.");
        return ClanService.Result.ok("§aSacou §f" + amount + " §ado banco do clã.");
    }

    private static void notifyMembers(Clan clan, String message) {
        for (ClanMember member : clan.getMembers().values()) {
            Account acc = Core.getAccountController().of(member.getId());
            if (acc != null) NotificationService.notifyClanActivity(acc, message);
        }
    }
}
