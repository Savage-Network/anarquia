package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.api.clan.*;
import com.minecraft.game.api.clan.ClanService;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.menu.clan.ClanHubMenu;
import com.minecraft.game.punishment.PunishmentService;
import com.minecraft.game.util.PlayerLookup;
import com.minecraft.game.util.TabCompleteUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public final class ClanCommand implements CommandInheritor {

    private static final String IN_CLAN = "§cVoce ja faz parte de um cla!";
    private static final String NO_CLAN = "§cVoce nao faz parte de um cla!";
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,12}$");
    private static final Pattern TAG_PATTERN = Pattern.compile("^[a-zA-Z0-9]{2,5}$");

    private static final List<String> SUBS = List.of(
            "criar", "deletar", "sair", "ver", "convidar", "aceitar", "recusar",
            "transferir", "expulsar", "promover", "rebaixar", "tag", "cor", "menu", "info",
            "lista", "convites", "motd", "desc", "sethome", "home", "delhome",
            "banco", "depositar", "sacar", "aliado", "inimigo", "mercado"
    );

    @Command(name = "clan", aliases = {"cla"}, playerOnly = true,
            description = "Sistema de clãs", usage = "/clan [subcomando]")
    public void clan(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;

        String[] args = ctx.getArgs();
        if (args.length == 0) {
            new ClanHubMenu(ctx.player(), account).open();
            return;
        }

        String sub = args[0].toLowerCase();
        String[] subArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        BukkitCommandContext subCtx = new BukkitCommandContext(ctx.getSender(), ctx.getLabel(), subArgs);

        switch (sub) {
            case "criar", "create" -> clanCreate(subCtx);
            case "deletar", "delete" -> clanDelete(subCtx);
            case "sair", "leave" -> clanLeave(subCtx);
            case "ver", "view", "info" -> clanView(subCtx);
            case "convidar", "invite" -> clanInvite(subCtx);
            case "aceitar", "accept" -> clanAccept(subCtx);
            case "recusar", "decline", "deny" -> clanDecline(subCtx);
            case "transferir", "transfer" -> clanTransfer(subCtx);
            case "expulsar", "kick" -> clanKick(subCtx);
            case "promover", "promote" -> clanPromote(subCtx);
            case "rebaixar", "demote" -> clanDemote(subCtx);
            case "tag", "cor", "color" -> clanTagColor(subCtx);
            case "menu", "gui" -> new ClanHubMenu(ctx.player(), account).open();
            case "lista", "list" -> new com.minecraft.game.bukkit.menu.clan.ClanListMenu(
                    ctx.player(), account, null, 0).open();
            case "convites", "invites" -> new com.minecraft.game.bukkit.menu.clan.ClanInvitesMenu(
                    ctx.player(), account, null).open();
            case "motd" -> clanMotd(subCtx);
            case "desc", "descricao" -> clanDesc(subCtx);
            case "sethome" -> clanSetHome(subCtx);
            case "home" -> clanHome(subCtx);
            case "delhome" -> clanDelHome(subCtx);
            case "banco", "bank" -> clanBank(subCtx);
            case "depositar", "deposit" -> clanDeposit(subCtx);
            case "sacar", "withdraw" -> clanWithdraw(subCtx);
            case "aliado", "ally" -> clanAlly(subCtx);
            case "inimigo", "enemy" -> clanEnemy(subCtx);
            case "mercado", "market" -> clanMarket(subCtx);
            default -> sendHelp(account, ctx.getLabel());
        }
    }

    @Command(name = "clantag", aliases = {"clancor"}, playerOnly = true,
            description = "Altera cor da clantag", usage = "/clantag <cor>")
    public void clanTagShortcut(BukkitCommandContext ctx) {
        clanTagColor(ctx);
    }

    @Command(name = "cc", aliases = {"c"}, playerOnly = true,
            description = "Chat do cla", usage = "/cc <mensagem>")
    public void clanChat(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (!account.hasClan()) {
            account.send(NO_CLAN);
            return;
        }
        var mute = PunishmentService.activeMute(account.getId());
        if (mute.isPresent()) {
            account.send(PunishmentService.muteDenyMessage(mute.get()));
            return;
        }
        if (ctx.getArgs().length == 0) {
            account.send("§cUso: /cc <mensagem>");
            return;
        }
        Clan clan = account.getClan();
        String tag = ClanTagFormatter.formatChatTag(clan);
        String msg = ctx.joinArgs(0);
        String formatted = Constant.CLAN_CHAT_PREFIX + tag + " §7" + account.getNickname()
                + "§7: §f" + msg;

        for (Account member : clan.getOnlineMembers()) {
            member.send(formatted);
        }
    }

    @Completer(name = "clan", subCommands = {"cla"})
    public List<String> tab(BukkitCommandContext ctx) {
        String[] args = ctx.getArgs();
        if (args.length <= 1) {
            return TabCompleteUtil.sub(ctx, SUBS.toArray(new String[0]));
        }
        String sub = args[0].toLowerCase();
        if (args.length == 2) {
            return switch (sub) {
                case "convidar", "invite", "expulsar", "kick", "promover", "promote",
                     "rebaixar", "demote", "transferir", "transfer", "ver", "view", "info" ->
                        TabCompleteUtil.onlinePlayersExceptSender(ctx);
                case "aceitar", "accept", "recusar", "decline", "deny" -> {
                    if (Core.getClanData() == null) yield List.of();
                    yield Core.getClanData().listAll().stream()
                            .map(Clan::getName)
                            .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                            .toList();
                }
                case "deletar", "delete" -> TabCompleteUtil.filter(args[1], "confirmar");
                case "tag", "cor", "color" -> TabCompleteUtil.filter(args[1], TabCompleteUtil.clanColorIds());
                default -> List.of();
            };
        }
        return List.of();
    }

    @Completer(name = "clantag", subCommands = {"clancor"})
    public List<String> tabClanTag(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.filter(ctx.getArgs()[0], TabCompleteUtil.clanColorIds());
        }
        return List.of();
    }

    private void clanCreate(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;

        if (account.hasClan()) {
            account.send(IN_CLAN);
            return;
        }
        if (ctx.getArgs().length < 2) {
            account.send("§cUso: /clan criar <nome> <tag>");
            return;
        }

        String name = ctx.getArgs()[0];
        String tag = ctx.getArgs()[1].toUpperCase();

        Pattern nameVal = account.hasRank(RankType.OWNER)
                ? Pattern.compile("^[a-zA-Z0-9]{1,12}$") : NAME_PATTERN;
        if (!nameVal.matcher(name).matches()) {
            account.send("§cNome invalido! Use letras/numeros (3-12).");
            return;
        }
        Pattern tagVal = account.hasRank(RankType.ADMIN)
                ? Pattern.compile("^[a-zA-Z0-9]{1,5}$") : TAG_PATTERN;
        if (!tagVal.matcher(tag).matches()) {
            account.send("§cTag invalida! Use letras/numeros (2-5).");
            return;
        }
        if (Core.getClanData().getByName(name) != null) {
            account.send("§cJa existe um cla com esse nome.");
            return;
        }
        if (Core.getClanData().getByTag(tag) != null) {
            account.send("§cJa existe um cla com essa tag.");
            return;
        }

        Clan clan = new Clan(account, UUID.randomUUID(), name, tag);
        Core.getClanData().save(clan);
        account.joinClan(clan);
        com.minecraft.game.progress.AchievementService.onClanCreated(account);

        account.send("§aCla §f" + name + " §acriado com sucesso!",
                "§7Tag: " + ClanTagFormatter.formatBracketed(clan),
                "§7Abra §e/clan §7para o painel.");
        account.sound(org.bukkit.Sound.ENTITY_PLAYER_LEVELUP);
    }

    private void clanDelete(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (!account.hasClan()) { account.send(NO_CLAN); return; }
        if (!account.isClanLeader()) {
            account.send("§cApenas o lider pode desfazer o cla.");
            return;
        }
        if (ctx.getArgs().length == 0 || !ctx.getArgs()[0].equalsIgnoreCase("confirmar")) {
            account.send("§cConfirme com §f/clan deletar confirmar");
            return;
        }
        account.getClan().disband();
        account.send("§7Seu cla foi desfeito.");
    }

    private void clanLeave(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (!account.hasClan()) { account.send(NO_CLAN); return; }
        if (account.isClanLeader()) {
            account.send("§cTransfira a lideranca ou desfaca o cla.");
            return;
        }
        account.getClan().removeMember(account);
        account.send("§7Voce saiu do cla.");
    }

    private void clanView(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (ctx.getArgs().length == 0) {
            if (!account.hasClan()) { account.send(NO_CLAN); return; }
            new com.minecraft.game.bukkit.menu.clan.ClanViewMenu(
                    ctx.player(), account, account.getClan(), null).open();
            return;
        }
        String query = ctx.getArgs()[0];
        Clan clan = Core.getClanData().getByName(query);
        if (clan == null) clan = Core.getClanData().getByTag(query);
        if (clan == null) {
            account.send("§cCla nao encontrado.");
            return;
        }
        new com.minecraft.game.bukkit.menu.clan.ClanViewMenu(ctx.player(), account, clan, null).open();
    }

    private void clanInvite(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (ctx.getArgs().length == 0) {
            if (!account.hasClan()) { account.send(NO_CLAN); return; }
            if (!account.hasClanRole(ClanRole.RECRUITER)) {
                account.send("§cSem permissão.");
                return;
            }
            new com.minecraft.game.bukkit.menu.clan.ClanInviteMenu(
                    ctx.player(), account, account.getClan(), null).open();
            return;
        }

        Account target = PlayerLookup.accountByName(ctx.getArgs()[0]).orElse(null);
        if (target == null) { account.send("§cJogador offline ou não encontrado."); return; }

        ClanService.Result result = ClanService.invite(account, target);
        account.send(result.success() ? "§a" + result.message() : "§c" + result.message());
        if (result.success()) sendInviteMessage(account, target, account.getClan());
    }

    private void sendInviteMessage(Account inviter, Account target, Clan clan) {
        Component invite = Component.text("§6" + inviter.getNickname() + " §econvidou você para §b" + clan.getName() + "§e!")
                .append(Component.newline())
                .append(Component.text("        ")
                        .append(Component.text("§a§lACEITAR")
                                .hoverEvent(HoverEvent.showText(Component.text("§eClique para aceitar")))
                                .clickEvent(ClickEvent.runCommand("/clan aceitar " + clan.getName())))
                        .append(Component.text("        "))
                        .append(Component.text("§c§lRECUSAR")
                                .hoverEvent(HoverEvent.showText(Component.text("§eClique para recusar")))
                                .clickEvent(ClickEvent.runCommand("/clan recusar " + clan.getName()))));
        target.send(invite);
    }

    private void clanAccept(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (account.hasClan()) { account.send(IN_CLAN); return; }

        if (ctx.getArgs().length == 0) {
            new com.minecraft.game.bukkit.menu.clan.ClanInvitesMenu(ctx.player(), account, null).open();
            return;
        }

        Clan clan = ClanService.resolveClan(ctx.getArgs()[0]);
        if (clan == null) {
            Account inviter = PlayerLookup.accountByName(ctx.getArgs()[0]).orElse(null);
            if (inviter != null) {
                ClanRequest request = Core.getClanData().getRequest(inviter.getId(), account.getId());
                if (request != null) clan = Core.getClanData().of(request.getClanId());
            }
        }
        if (clan == null) { account.send("§cConvite não encontrado."); return; }

        ClanService.Result result = ClanService.accept(account, clan.getId());
        account.send(result.success() ? "§a" + result.message() : "§c" + result.message());
    }

    private void clanDecline(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;

        if (ctx.getArgs().length == 0) {
            account.send("§cUso: /clan recusar <clã> ou §e/clan convites");
            return;
        }

        Clan clan = ClanService.resolveClan(ctx.getArgs()[0]);
        if (clan == null) {
            Account inviter = PlayerLookup.accountByName(ctx.getArgs()[0]).orElse(null);
            if (inviter != null) {
                ClanRequest request = Core.getClanData().getRequest(inviter.getId(), account.getId());
                if (request != null) clan = Core.getClanData().of(request.getClanId());
            }
        }
        if (clan == null) { account.send("§cConvite não encontrado."); return; }

        ClanService.Result result = ClanService.decline(account, clan.getId());
        account.send(result.success() ? "§e" + result.message() : "§c" + result.message());
    }

    private void clanTransfer(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (!account.isClanLeader()) {
            account.send("§cApenas o lider pode transferir.");
            return;
        }
        if (ctx.getArgs().length == 0) {
            account.send("§cUso: /clan transferir <jogador>");
            return;
        }
        Account target = PlayerLookup.accountByName(ctx.getArgs()[0]).orElse(null);
        if (target == null || !account.getClan().isMember(target.getId())) {
            account.send("§cMembro nao encontrado.");
            return;
        }
        account.getClan().promote(account, target, ClanRole.LEADER);
    }

    private void clanPromote(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (ctx.getArgs().length == 0) {
            account.send("§cUso: /clan promover <jogador>");
            return;
        }
        Account target = PlayerLookup.accountByName(ctx.getArgs()[0]).orElse(null);
        if (target == null) { account.send("§cMembro não encontrado."); return; }
        ClanService.Result r = ClanService.promote(account, target);
        account.send(r.success() ? "§a" + r.message() : "§c" + r.message());
    }

    private void clanDemote(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (ctx.getArgs().length == 0) {
            account.send("§cUso: /clan rebaixar <jogador>");
            return;
        }
        Account target = PlayerLookup.accountByName(ctx.getArgs()[0]).orElse(null);
        if (target == null) { account.send("§cMembro não encontrado."); return; }
        ClanService.Result r = ClanService.demote(account, target);
        account.send(r.success() ? "§a" + r.message() : "§c" + r.message());
    }

    private void clanKick(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (ctx.getArgs().length == 0) {
            if (account.hasClan() && account.hasClanRole(ClanRole.RECRUITER)) {
                new com.minecraft.game.bukkit.menu.clan.ClanMembersMenu(
                        ctx.player(), account, account.getClan(), null).open();
                return;
            }
            account.send("§cUso: /clan expulsar <jogador>");
            return;
        }
        Account target = PlayerLookup.accountByName(ctx.getArgs()[0]).orElse(null);
        if (target == null) { account.send("§cMembro não encontrado."); return; }
        ClanService.Result r = ClanService.kick(account, target);
        account.send(r.success() ? "§a" + r.message() : "§c" + r.message());
    }

    private void clanMotd(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (!account.isClanLeader()) { account.send("§cApenas o líder."); return; }
        if (ctx.getArgs().length == 0) {
            account.send("§7MOTD: §f" + account.getClan().getMotd());
            account.send("§e/clan motd <mensagem>");
            return;
        }
        String motd = ctx.joinArgs(0);
        if (motd.length() > 64) motd = motd.substring(0, 64);
        account.getClan().setMotd(motd);
        account.getClan().broadcast("§7MOTD atualizado por §f" + account.getNickname() + "§7.");
        account.send("§aMOTD atualizado.");
    }

    private void clanDesc(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (!account.isClanLeader()) { account.send("§cApenas o líder."); return; }
        if (ctx.getArgs().length == 0) {
            account.send("§cUso: /clan desc <texto>");
            return;
        }
        String desc = ctx.joinArgs(0);
        if (desc.length() > 120) desc = desc.substring(0, 120);
        account.getClan().setDescription(desc);
        account.send("§aDescrição atualizada.");
    }

    private void clanSetHome(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        ClanService.Result r = ClanService.setHome(account, ctx.player().getLocation());
        account.send(r.success() ? "§a" + r.message() : "§c" + r.message());
    }

    private void clanHome(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        ClanService.Result r = ClanService.teleportHome(ctx.player(), account);
        account.send(r.success() ? "§a" + r.message() : "§c" + r.message());
    }

    private void clanDelHome(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (!account.isClanLeader()) { account.send("§cApenas o líder."); return; }
        account.getClan().clearHome();
        account.getClan().broadcast("§7Base do clã removida.");
        account.send("§eBase removida.");
    }

    private void clanBank(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null || !account.hasClan()) { if (account != null) account.send(NO_CLAN); return; }
        long bal = account.getClan().getStorage().getBankBalance();
        account.send("§7Banco do clã: §a" + bal);
    }

    private void clanDeposit(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (ctx.getArgs().length == 0) { account.send("§cUso: /clan depositar <valor>"); return; }
        try {
            long amount = Long.parseLong(ctx.getArgs()[0]);
            ClanService.Result r = ClanBankService.deposit(account, amount);
            account.send(r.success() ? r.message() : "§c" + r.message());
        } catch (NumberFormatException e) {
            account.send("§cValor inválido.");
        }
    }

    private void clanWithdraw(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (ctx.getArgs().length == 0) { account.send("§cUso: /clan sacar <valor>"); return; }
        try {
            long amount = Long.parseLong(ctx.getArgs()[0]);
            ClanService.Result r = ClanBankService.withdraw(account, amount);
            account.send(r.success() ? r.message() : "§c" + r.message());
        } catch (NumberFormatException e) {
            account.send("§cValor inválido.");
        }
    }

    private void clanAlly(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (ctx.getArgs().length == 0) { account.send("§cUso: /clan aliado <clã>"); return; }
        ClanService.Result r = ClanRelationService.addAlly(account, ctx.getArgs()[0]);
        account.send(r.success() ? r.message() : "§c" + r.message());
    }

    private void clanEnemy(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (ctx.getArgs().length == 0) { account.send("§cUso: /clan inimigo <clã>"); return; }
        ClanService.Result r = ClanRelationService.addEnemy(account, ctx.getArgs()[0]);
        account.send(r.success() ? r.message() : "§c" + r.message());
    }

    private void clanMarket(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (ctx.getArgs().length == 0) {
            account.send("§cUso: /clan mercado vender <preço> | comprar <id>");
            return;
        }
        String sub = ctx.getArgs()[0].toLowerCase();
        if ("vender".equals(sub) || "sell".equals(sub)) {
            if (ctx.getArgs().length < 2) { account.send("§cUso: /clan mercado vender <preço>"); return; }
            try {
                int price = Integer.parseInt(ctx.getArgs()[1]);
                ClanService.Result r = ClanMarketService.listItem(account, null, price);
                account.send(r.success() ? r.message() : "§c" + r.message());
            } catch (NumberFormatException e) {
                account.send("§cPreço inválido.");
            }
            return;
        }
        if ("comprar".equals(sub) || "buy".equals(sub)) {
            if (ctx.getArgs().length < 2) { account.send("§cUso: /clan mercado comprar <id>"); return; }
            ClanService.Result r = ClanMarketService.buy(account, ctx.getArgs()[1]);
            account.send(r.success() ? r.message() : "§c" + r.message());
            return;
        }
        account.send("§cSubcomando: vender | comprar");
    }

    private void clanTagColor(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (!account.hasClan()) { account.send(NO_CLAN); return; }
        if (!account.hasClanRole(ClanRole.RECRUITER)) {
            account.send("§cApenas Recrutador ou Líder podem mudar a cor.");
            return;
        }

        Clan clan = account.getClan();
        if (ctx.getArgs().length == 0) {
            account.send("§eEscolha uma cor no menu §f/clan §eou use §f/clantag <cor>");
            new com.minecraft.game.bukkit.menu.clan.ClanColorMenu(ctx.player(), account, clan, null).open();
            return;
        }

        ClanColor color = ClanColor.parse(ctx.getArgs()[0]);
        if (color == null) {
            account.send("§cCor inválida. Cores: padrao, aqua, verde, ouro, roxo, brasil, yingyang...");
            return;
        }
        if (!color.isFree() && color.getPermission() != null
                && !account.hasPermission(color.getPermission()) && !account.isStaffer()) {
            account.send("§cVocê não tem acesso à cor §f" + color.getName());
            return;
        }

        clan.applyStyle(color);
        ClanSync.refreshMembers(clan);
        account.send("§aEstilo alterado: " + ClanTagFormatter.formatPreview(color, clan.getTagWithoutColors()));
        clan.broadcast("§7Estilo da tag alterado por " + account.getNickname() + ".");
    }

    private void sendHelp(Account account, String label) {
        account.send("§6§lSistema de Clãs",
                "§e/" + label + " §7- Painel GUI",
                "§e/" + label + " criar <nome> <tag>",
                "§e/" + label + " convites|lista|convidar",
                "§e/" + label + " promover|rebaixar|expulsar",
                "§e/" + label + " sethome|home|motd",
                "§e/cc <msg> §7- Chat do clã");
    }

    private Account require(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) ctx.replyKey("errors.account-not-loaded");
        return account;
    }
}
