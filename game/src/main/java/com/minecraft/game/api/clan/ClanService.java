package com.minecraft.game.api.clan;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.data.ClanData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/** Lógica central do sistema de clãs (convites, cargos, base). */
public final class ClanService {

    private ClanService() {}

    public record Result(boolean success, String message) {
        public static Result ok(String msg) { return new Result(true, msg); }
        public static Result fail(String msg) { return new Result(false, msg); }
    }

    public static Result invite(Account actor, Account target) {
        if (actor == null || target == null) return Result.fail("Jogador inválido.");
        if (!actor.hasClan()) return Result.fail("§cVocê não faz parte de um clã.");
        if (!actor.hasClanRole(ClanRole.RECRUITER)) return Result.fail("§cSem permissão para convidar.");
        if (target.getId().equals(actor.getId())) return Result.fail("§cVocê não pode convidar a si.");
        if (!target.getToggle().isAllowClanInvites()) {
            return Result.fail("§c" + target.getNickname() + " não aceita convites de clã.");
        }
        if (target.hasClan()) return Result.fail("§cEsse jogador já tem clã.");

        Clan clan = actor.getClan();
        ClanData data = Core.getClanData();
        if (data == null) return Result.fail("§cSistema indisponível.");
        if (clan.getMembers().size() >= Constant.CLAN_MAX_MEMBERS) {
            return Result.fail("§cO clã está cheio (" + Constant.CLAN_MAX_MEMBERS + " membros).");
        }
        if (data.hasRequest(clan, target.getId())) {
            return Result.fail("§cEsse jogador já foi convidado.");
        }

        data.addRequest(new ClanRequest(clan.getId(), actor.getId(), target.getId()));
        return Result.ok("§aConvite enviado para §f" + target.getNickname() + "§a.");
    }

    public static Result accept(Account target, UUID clanId) {
        if (target == null) return Result.fail("Jogador inválido.");
        if (target.hasClan()) return Result.fail("§cVocê já faz parte de um clã.");

        ClanData data = Core.getClanData();
        if (data == null) return Result.fail("§cSistema indisponível.");

        ClanRequest request = data.getRequestByClan(target.getId(), clanId);
        if (request == null) return Result.fail("§cConvite não encontrado.");

        Clan clan = data.of(clanId);
        data.removeRequest(request);
        if (clan == null) return Result.fail("§cClã não encontrado.");
        if (clan.getMembers().size() >= Constant.CLAN_MAX_MEMBERS) {
            return Result.fail("§cO clã está cheio.");
        }

        clan.addMember(target);
        Account inviter = Core.getAccountController().of(request.getAuthor());
        if (inviter != null) {
            inviter.send("§a" + target.getNickname() + " §aentrou no clã!");
        }
        return Result.ok("§aVocê entrou no clã §f" + clan.getName() + "§a!");
    }

    public static Result decline(Account target, UUID clanId) {
        if (target == null) return Result.fail("Jogador inválido.");
        ClanData data = Core.getClanData();
        if (data == null) return Result.fail("§cSistema indisponível.");

        ClanRequest request = data.getRequestByClan(target.getId(), clanId);
        if (request == null) return Result.fail("§cConvite não encontrado.");

        data.removeRequest(request);
        Clan clan = data.of(clanId);
        Account inviter = Core.getAccountController().of(request.getAuthor());
        if (inviter != null) {
            inviter.send("§c" + target.getNickname() + " §crecusou o convite.");
        }
        return Result.ok("§cConvite recusado" + (clan != null ? " de §f" + clan.getName() : "") + ".");
    }

    public static Result kick(Account actor, Account target) {
        if (actor == null || target == null) return Result.fail("Jogador inválido.");
        if (!actor.hasClan()) return Result.fail("§cVocê não faz parte de um clã.");
        if (!actor.hasClanRole(ClanRole.RECRUITER)) return Result.fail("§cSem permissão.");
        Clan clan = actor.getClan();
        if (!clan.isMember(target.getId())) return Result.fail("§cMembro não encontrado.");
        if (clan.isLeader(target.getId())) return Result.fail("§cNão pode expulsar o líder.");
        if (target.getId().equals(actor.getId())) return Result.fail("§cUse /clan sair.");

        clan.kick(actor, target);
        return Result.ok("§e" + target.getNickname() + " §efoi expulso do clã.");
    }

    public static Result promote(Account actor, Account target) {
        if (actor == null || target == null) return Result.fail("Jogador inválido.");
        if (!actor.isClanLeader()) return Result.fail("§cApenas o líder pode promover.");
        Clan clan = actor.getClan();
        if (!clan.isMember(target.getId())) return Result.fail("§cMembro não encontrado.");
        if (clan.isLeader(target.getId())) return Result.fail("§cEsse jogador já é líder.");

        ClanMember member = clan.getMember(target.getId());
        if (member.getRole() == ClanRole.RECRUITER) {
            return Result.fail("§cEsse membro já é Recrutador.");
        }
        clan.promote(actor, target, ClanRole.RECRUITER);
        return Result.ok("§a" + target.getNickname() + " §apromovido a Recrutador.");
    }

    public static Result demote(Account actor, Account target) {
        if (actor == null || target == null) return Result.fail("Jogador inválido.");
        if (!actor.isClanLeader()) return Result.fail("§cApenas o líder pode rebaixar.");
        Clan clan = actor.getClan();
        if (!clan.isMember(target.getId())) return Result.fail("§cMembro não encontrado.");
        if (clan.isLeader(target.getId())) return Result.fail("§cNão pode rebaixar o líder.");

        clan.demote(actor, target);
        return Result.ok("§e" + target.getNickname() + " §erebaixado a Membro.");
    }

    public static Result transferLeadership(Account actor, Account target) {
        if (actor == null || target == null) return Result.fail("Jogador inválido.");
        if (!actor.isClanLeader()) return Result.fail("§cApenas o líder pode transferir.");
        Clan clan = actor.getClan();
        if (!clan.isMember(target.getId())) return Result.fail("§cMembro não encontrado.");

        clan.promote(actor, target, ClanRole.LEADER);
        return Result.ok("§aLiderança transferida para §f" + target.getNickname() + "§a.");
    }

    public static Result setHome(Account actor, Location location) {
        if (actor == null || !actor.hasClan()) return Result.fail("§cVocê não faz parte de um clã.");
        if (!actor.hasClanRole(ClanRole.RECRUITER)) return Result.fail("§cSem permissão.");
        if (location == null || location.getWorld() == null) return Result.fail("§cLocal inválido.");

        actor.getClan().setHome(location.clone());
        actor.getClan().broadcast("§7Base do clã definida por §f" + actor.getNickname() + "§7.");
        return Result.ok("§aBase do clã definida neste local.");
    }

    public static Result teleportHome(Player player, Account account) {
        if (account == null || !account.hasClan()) return Result.fail("§cVocê não faz parte de um clã.");
        Clan clan = account.getClan();
        if (!clan.hasHome()) return Result.fail("§cO clã não tem base. Líder: §e/clan sethome");

        Location home = clan.getHome();
        if (home == null || home.getWorld() == null) {
            return Result.fail("§cMundo da base não está carregado.");
        }
        home.getChunk().load(true);
        if (!player.teleport(home)) {
            return Result.fail("§cFalha ao teleportar para a base.");
        }
        return Result.ok("§aTeleportado para a base do clã.");
    }

    public static Result toggleFriendlyFire(Account actor) {
        if (actor == null || !actor.isClanLeader()) return Result.fail("§cApenas o líder pode alterar.");
        Clan clan = actor.getClan();
        clan.setFriendlyFire(!clan.isFriendlyFire());
        String state = clan.isFriendlyFire() ? "§aativado" : "§cdesativado";
        clan.broadcast("§7PvP entre membros " + state + " §7por §f" + actor.getNickname() + "§7.");
        return Result.ok("§7PvP entre membros: " + state);
    }

    /** Resolve clã por nome, tag ou UUID string para aceitar convites via comando. */
    public static Clan resolveClan(String query) {
        if (query == null || query.isBlank()) return null;
        ClanData data = Core.getClanData();
        if (data == null) return null;

        Clan byName = data.getByName(query);
        if (byName != null) return byName;
        Clan byTag = data.getByTag(query);
        if (byTag != null) return byTag;

        try {
            return data.of(UUID.fromString(query));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
