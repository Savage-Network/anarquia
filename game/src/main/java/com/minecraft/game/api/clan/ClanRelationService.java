package com.minecraft.game.api.clan;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.data.ClanData;
import com.minecraft.game.network.ActivityFeedService;

import java.util.UUID;

public final class ClanRelationService {

    private ClanRelationService() {}

    public static ClanService.Result addAlly(Account actor, String targetQuery) {
        return mutateRelation(actor, targetQuery, true, false);
    }

    public static ClanService.Result addEnemy(Account actor, String targetQuery) {
        return mutateRelation(actor, targetQuery, false, true);
    }

    public static ClanService.Result removeAlly(Account actor, String targetQuery) {
        return removeRelation(actor, targetQuery, true);
    }

    public static ClanService.Result removeEnemy(Account actor, String targetQuery) {
        return removeRelation(actor, targetQuery, false);
    }

    public static boolean areAllies(Clan a, Clan b) {
        if (a == null || b == null || a.getId().equals(b.getId())) return false;
        return a.getStorage().getAllies().contains(b.getId())
                && b.getStorage().getAllies().contains(a.getId());
    }

    public static boolean areEnemies(Clan a, Clan b) {
        if (a == null || b == null || a.getId().equals(b.getId())) return false;
        return a.getStorage().getEnemies().contains(b.getId())
                || b.getStorage().getEnemies().contains(a.getId());
    }

    public static boolean canDamage(Account attacker, Account victim) {
        if (attacker == null || victim == null) return true;
        if (!attacker.hasClan() || !victim.hasClan()) return true;
        Clan a = attacker.getClan();
        Clan b = victim.getClan();
        if (a.getId().equals(b.getId())) return a.isFriendlyFire();
        if (areAllies(a, b)) return false;
        return true;
    }

    private static ClanService.Result mutateRelation(Account actor, String query,
                                                     boolean ally, boolean enemy) {
        if (actor == null || !actor.isClanLeader()) {
            return ClanService.Result.fail("§cApenas o líder pode gerenciar relações.");
        }
        Clan clan = actor.getClan();
        Clan target = ClanService.resolveClan(query);
        if (target == null) return ClanService.Result.fail("§cClã não encontrado.");
        if (target.getId().equals(clan.getId())) {
            return ClanService.Result.fail("§cNão pode aplicar no próprio clã.");
        }

        if (ally) {
            if (clan.getStorage().getEnemies().remove(target.getId())) {
                target.getStorage().getEnemies().remove(clan.getId());
                target.persist();
            }
            clan.getStorage().getAllies().add(target.getId());
            target.getStorage().getAllies().add(clan.getId());
            clan.persist();
            target.persist();
            broadcastBoth(clan, target, "§aAliança formada entre §f" + clan.getName()
                    + " §ae §f" + target.getName());
            ActivityFeedService.publish("clan", actor.getNickname(),
                    "aliou clã " + target.getName(), com.minecraft.game.GameSettings.SERVER_CHAT_ID);
            return ClanService.Result.ok("§aAliança com §f" + target.getName() + "§a!");
        }

        if (enemy) {
            if (areAllies(clan, target)) {
                clan.getStorage().getAllies().remove(target.getId());
                target.getStorage().getAllies().remove(clan.getId());
            }
            clan.getStorage().getEnemies().add(target.getId());
            target.getStorage().getEnemies().add(clan.getId());
            clan.persist();
            target.persist();
            broadcastBoth(clan, target, "§cGuerra declarada: §f" + clan.getName()
                    + " §cvs §f" + target.getName());
            ActivityFeedService.publish("clan", actor.getNickname(),
                    "declarou inimigo " + target.getName(), com.minecraft.game.GameSettings.SERVER_CHAT_ID);
            return ClanService.Result.ok("§cInimizade com §f" + target.getName() + "§c!");
        }
        return ClanService.Result.fail("§cOperação inválida.");
    }

    private static ClanService.Result removeRelation(Account actor, String query, boolean ally) {
        if (actor == null || !actor.isClanLeader()) {
            return ClanService.Result.fail("§cApenas o líder pode gerenciar relações.");
        }
        Clan clan = actor.getClan();
        Clan target = ClanService.resolveClan(query);
        if (target == null) return ClanService.Result.fail("§cClã não encontrado.");

        if (ally) {
            clan.getStorage().getAllies().remove(target.getId());
            target.getStorage().getAllies().remove(clan.getId());
        } else {
            clan.getStorage().getEnemies().remove(target.getId());
            target.getStorage().getEnemies().remove(clan.getId());
        }
        clan.persist();
        target.persist();
        return ClanService.Result.ok("§7Relação removida com §f" + target.getName() + "§7.");
    }

    private static void broadcastBoth(Clan a, Clan b, String message) {
        a.broadcast(message);
        b.broadcast(message);
    }
}
