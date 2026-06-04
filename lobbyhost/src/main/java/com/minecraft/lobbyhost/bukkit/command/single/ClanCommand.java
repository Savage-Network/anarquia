package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.api.clan.*;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.menu.clan.ClanHubMenu;
import com.minecraft.lobbyhost.util.PlayerLookup;
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
            "transferir", "expulsar", "tag", "cor", "menu", "info"
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
            case "tag", "cor", "color" -> clanTagColor(subCtx);
            case "menu", "gui" -> new ClanHubMenu(ctx.player(), account).open();
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

    @Completer(name = "clan")
    public List<String> tab(BukkitCommandContext ctx) {
        List<String> out = new ArrayList<>();
        if (ctx.getArgs().length <= 1) {
            String partial = ctx.getArgs().length == 1 ? ctx.getArgs()[0].toLowerCase() : "";
            for (String s : SUBS) {
                if (s.startsWith(partial)) out.add(s);
            }
        }
        return out;
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
            new com.minecraft.lobbyhost.bukkit.menu.clan.ClanViewMenu(
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
        new com.minecraft.lobbyhost.bukkit.menu.clan.ClanViewMenu(ctx.player(), account, clan, null).open();
    }

    private void clanInvite(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (!account.hasClan()) { account.send(NO_CLAN); return; }
        if (!account.hasClanRole(ClanRole.RECRUITER)) {
            account.send("§cSem permissao para convidar.");
            return;
        }
        if (ctx.getArgs().length == 0) {
            account.send("§cUso: /clan convidar <jogador>");
            return;
        }

        Account target = PlayerLookup.accountByName(ctx.getArgs()[0]).orElse(null);
        if (target == null) { account.send("§cJogador offline ou nao encontrado."); return; }
        if (target.getId().equals(account.getId())) { account.send("§cVoce nao pode convidar a si."); return; }
        if (!target.getToggle().isAllowClanInvites()) {
            account.send("§c" + target.getNickname() + " nao aceita convites de cla.");
            return;
        }
        if (target.hasClan()) {
            account.send("§cEsse jogador ja tem cla.");
            return;
        }

        Clan clan = account.getClan();
        if (clan.getMembers().size() >= Constant.CLAN_MAX_MEMBERS) {
            account.send("§cO cla esta cheio (" + Constant.CLAN_MAX_MEMBERS + " membros).");
            return;
        }
        if (Core.getClanData().hasRequest(clan, target.getId())) {
            account.send("§cEsse jogador ja foi convidado.");
            return;
        }

        Core.getClanData().addRequest(new ClanRequest(clan.getId(), account.getId(), target.getId()));
        account.send("§aConvite enviado para §f" + target.getNickname() + "§a.");

        Component invite = Component.text("§6" + account.getNickname() + " §econvidou voce para §b" + clan.getName() + "§e!")
                .append(Component.newline())
                .append(Component.text("        ")
                        .append(Component.text("§a§lACEITAR")
                                .hoverEvent(HoverEvent.showText(Component.text("§eClique para aceitar")))
                                .clickEvent(ClickEvent.runCommand("/clan aceitar " + account.getName())))
                        .append(Component.text("        "))
                        .append(Component.text("§c§lRECUSAR")
                                .hoverEvent(HoverEvent.showText(Component.text("§eClique para recusar")))
                                .clickEvent(ClickEvent.runCommand("/clan recusar " + account.getName()))));
        target.send(invite);
    }

    private void clanAccept(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (account.hasClan()) { account.send(IN_CLAN); return; }
        if (ctx.getArgs().length == 0) {
            account.send("§cUso: /clan aceitar <jogador>");
            return;
        }

        Account inviter = PlayerLookup.accountByName(ctx.getArgs()[0]).orElse(null);
        if (inviter == null) { account.send("§cConvite nao encontrado."); return; }

        ClanRequest request = Core.getClanData().getRequest(inviter.getId(), account.getId());
        if (request == null) {
            account.send("§cNenhum convite de " + inviter.getNickname() + ".");
            return;
        }

        Clan clan = Core.getClanData().of(request.getClanId());
        Core.getClanData().removeRequest(request);
        if (clan == null) {
            account.send("§cCla nao encontrado.");
            return;
        }
        if (clan.getMembers().size() >= Constant.CLAN_MAX_MEMBERS) {
            account.send("§cO cla esta cheio.");
            return;
        }

        clan.addMember(account);
        account.send("§aVoce entrou no cla §f" + clan.getName() + "§a!");
        inviter.send("§a" + account.getNickname() + " aceitou seu convite.");
    }

    private void clanDecline(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (ctx.getArgs().length == 0) {
            account.send("§cUso: /clan recusar <jogador>");
            return;
        }
        Account inviter = PlayerLookup.accountByName(ctx.getArgs()[0]).orElse(null);
        if (inviter == null) { account.send("§cConvite nao encontrado."); return; }

        ClanRequest request = Core.getClanData().getRequest(inviter.getId(), account.getId());
        if (request == null) {
            account.send("§cNenhum convite pendente.");
            return;
        }
        Core.getClanData().removeRequest(request);
        account.send("§cConvite recusado.");
        inviter.send("§c" + account.getNickname() + " recusou o convite.");
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

    private void clanKick(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (!account.hasClanRole(ClanRole.RECRUITER)) {
            account.send("§cSem permissao.");
            return;
        }
        if (ctx.getArgs().length == 0) {
            account.send("§cUso: /clan expulsar <jogador>");
            return;
        }
        Account target = PlayerLookup.accountByName(ctx.getArgs()[0]).orElse(null);
        if (target == null || !account.getClan().isMember(target.getId())) {
            account.send("§cMembro nao encontrado.");
            return;
        }
        if (account.getClan().isLeader(target.getId())) {
            account.send("§cNao pode expulsar o lider.");
            return;
        }
        account.getClan().kick(account, target);
    }

    private void clanTagColor(BukkitCommandContext ctx) {
        Account account = require(ctx);
        if (account == null) return;
        if (!account.hasClan()) { account.send(NO_CLAN); return; }
        if (!account.hasClanRole(ClanRole.RECRUITER)) {
            account.send("§cApenas Recrutador ou Lider podem mudar a cor.");
            return;
        }

        Clan clan = account.getClan();
        if (ctx.getArgs().length == 0) {
            account.send("§eEscolha uma cor no menu §f/clan §eou use §f/clantag <cor>");
            new com.minecraft.lobbyhost.bukkit.menu.clan.ClanColorMenu(ctx.player(), account, clan, null).open();
            return;
        }

        ClanColor color = ClanColor.parse(ctx.getArgs()[0]);
        if (color == null) {
            account.send("§cCor invalida. Cores: padrao, aqua, verde, ouro, roxo, brasil, yingyang...");
            return;
        }
        if (!color.isFree() && color.getPermission() != null
                && !account.hasPermission(color.getPermission()) && !account.isStaffer()) {
            account.send("§cVoce nao tem acesso a cor §f" + color.getName());
            return;
        }

        clan.setTagColor(color.hasMixedTag() ? color.getId() : color.getCode());
        clan.persist();
        ClanSync.refreshMembers(clan);
        account.send("§aCor alterada: " + color.getCode() + color.getName());
        clan.broadcast("§7Cor da tag alterada por " + account.getNickname() + ".");
    }

    private void sendHelp(Account account, String label) {
        account.send("§6§lSistema de Clãs",
                "§e/" + label + " §7- Abre o painel",
                "§e/" + label + " criar <nome> <tag>",
                "§e/" + label + " convidar|aceitar|recusar|sair",
                "§e/" + label + " ver <nome|tag>",
                "§e/clantag <cor> §7- Cor da clantag",
                "§e/cc <msg> §7- Chat do cla");
    }

    private Account require(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) ctx.reply("§cConta nao carregada.");
        return account;
    }
}
