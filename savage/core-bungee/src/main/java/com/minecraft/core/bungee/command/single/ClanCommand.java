package com.minecraft.core.bungee.command.single;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.account.context.objects.clan.request.ClanRequest;
import com.minecraft.core.api.clan.Clan;
import com.minecraft.core.api.clan.color.ClanColor;
import com.minecraft.core.api.clan.member.ClanMember;
import com.minecraft.core.api.clan.role.ClanRole;
import com.minecraft.core.backend.data.list.api.ClanData;
import com.minecraft.core.backend.database.redis.message.types.account.AccountClanChangeMessage;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ClanCommand implements CommandInheritor {

    private final ClanData data = Core.getClanData();

    protected final Pattern NAME_VALIDATOR = Pattern.compile("^[a-zA-Z0-9]{3,12}$"),
            NAME_VALIDATOR_OWNER = Pattern.compile("^[a-zA-Z0-9]{1,12}$"),
            TAG_VALIDATOR = Pattern.compile("^[a-zA-Z0-9]{4,8}$"),
            TAG_VALIDATOR_ADMIN = Pattern.compile("^[a-zA-Z0-9]{2,8}$"),
            TAG_VALIDATOR_OWNER = Pattern.compile("^[a-zA-Z0-9]{1,8}$");

    private final String ALREADY_IN_A_CLAN = "§cVocê já faz parte de um clan!",
            NOT_PART_OF_CLAN = "§cVocê não faz parte de um clan!";

    @Completer(name = "clan")
    public List<String> clanCompleter(BungeeCommandContext context) {
        List<String> list = new ArrayList<>(),
                subCommands = context.getSubCommands();

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String command = args[0].toLowerCase();

            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(command))
                    list.add(subCommand);
            }
        } else
            list.addAll(subCommands);

        return list;
    }

    @Command(name = "clan")
    public void clan(BungeeCommandContext context) {
        Account account = context.getAccount();

        String label = "/" + context.getLabel();

        List<String> subCommands = context.getSubCommands();

        String[] args = context.getArgs();

        if (args.length == 0 || !subCommands.contains(args[0].toLowerCase()))
            account.send("§cComo usar " + label + ":",
                    "§c* " + label + " criar (nome) (tag)",
                    "§c* " + label + " deletar",
                    "§c* " + label + " sair",
                    "§c* " + label + " ver (nome/tag)",
                    "§c* " + label + " convidar [jogador]",
                    "§c* " + label + " aceitar [jogador]",
                    "§c* " + label + " recusar [jogador]",
                    "§c* " + label + " transferir [jogador]",
                    "§c* " + label + " tag (cor)",
                    "§c* " + label + " cor (cor)",
                    "§c* /clantag (cor)"
            );
    }

    @Command(name = "clan.criar")
    public void clanCreate(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasClan()) {
            account.send(ALREADY_IN_A_CLAN);
            return;
        }

        if (args.length <= 1) {
            account.send("§cUso: /" + context.getCommandLabel() + " (nome) (tag).");
            return;
        }

        String name = args[0], tag = args[1];

        Pattern nameValidator = account.hasRank(RankType.OWNER) ? NAME_VALIDATOR_OWNER : NAME_VALIDATOR;
        if (!nameValidator.matcher(name).matches()) {
            String minChars = account.hasRank(RankType.OWNER) ? "1" : "3";
            account.send("§cO nome do seu clan não é válido!",
                    "§cSomente letras e números, com caractéres de " + minChars + " até 12!");
            return;
        }

        if (data.getByName(name) != null) {
            account.send("§cOps! Um clan com o nome " + name + " já existe.");
            return;
        }

        Pattern tagValidator;
        String tagMinChars;
        if (account.hasRank(RankType.OWNER)) {
            tagValidator = TAG_VALIDATOR_OWNER;
            tagMinChars = "1";
        } else if (account.hasRank(RankType.ADMIN)) {
            tagValidator = TAG_VALIDATOR_ADMIN;
            tagMinChars = "2";
        } else {
            tagValidator = TAG_VALIDATOR;
            tagMinChars = "4";
        }
        if (!tagValidator.matcher(tag).matches()) {
            account.send("§cA tag do seu clan não é válida!",
                    "§cSomente letras e números, com caractéres de " + tagMinChars + " até 8!");
            return;
        }

        if (data.getByTag(tag) != null) {
            account.send("§cOps! Um clan com a tag " + tag + " já existe.");
            return;
        }

        Clan clan = new Clan(account, UUID.randomUUID(), name, tag);

        account.send("§aO seu clan foi criado com sucesso.");
        account.setClan(clan.getId());

        data.save(clan);

        new AccountClanChangeMessage(account).send();
    }

    @Command(name = "clan.deletar")
    public void clanDelete(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (!account.hasClanRole(ClanRole.LEADER)) {
            account.send("§cVocê não pode deletar o seu clan atual.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (confirmar).");
            return;
        }

        if (args[0].equalsIgnoreCase("confirmar"))
            account.getClan().disband();
    }

    @Command(name = "clan.convidar")
    public void clanInvite(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (!account.hasClanRole(ClanRole.RECRUITER)) {
            account.send("§cVocê não pode convidar jogadores para o clan.");
            return;
        }

        Clan clan = account.getClan();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (!target.getToggle().isAllowClanInvite()) {
            account.send("§cO jogador " + target.getNickname() + " não está recebendo convites de clan.");
            return;
        }

        if (target.hasClan()) {
            account.send("§cO jogador " + target.getNickname() + " já faz parte de um clan!");
            return;
        }

        if (data.hasRequest(clan, target.getId())) {
            account.send("§cO jogador " + target.getNickname() + " já foi convidado para o clan!");
            return;
        }

        account.send("§aVocê convidou " + target.getNickname() + " para o seu clan!");

        TextComponent message = new TextComponent("§6" + account.getNickname() + "§e convidou você para o clan §b" + clan.getName() + "§e!");

        TextComponent acceptButton = new TextComponent("§a§lACEITAR");

        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan aceitar " + account.getNickname()));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para aceitar o convite.")));

        TextComponent declineButton = new TextComponent("§c§lRECUSAR");

        declineButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan recusar " + account.getNickname()));
        declineButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para recusar o convite.")));

        message.addExtra("\n        ");
        message.addExtra(acceptButton);
        message.addExtra("        ");
        message.addExtra(declineButton);

        // Adicionando requisição no Redis
        data.addRequest(new ClanRequest(clan.getId(), account.getId(), target.getId()));

        target.send(message);
    }

    @Command(name = "clan.aceitar")
    public void clanAccept(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasClan()) {
            account.send(ALREADY_IN_A_CLAN);
            return;
        }

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        ClanRequest request = data.getRequest(target.getId(), account.getId());

        if (request == null) {
            account.send("§cNenhum convite foi encontrado de " + target.getNickname() + ".");
            return;
        }

        Clan clan = data.of(request.getId());

        data.removeRequest(request);

        if (clan == null) {
            account.send("§cNão foi possível encontrar o clan.");
            return;
        }

        clan.addMember(account);

        account.send("§aVocê aceitou o convite de " + target.getNickname() + " para entrar no clan §7" + clan.getName() + "§a.");
        target.send("§a" + account.getNickname() + " aceitou o seu convite para entrar no clan.");

        new AccountClanChangeMessage(account).send();
    }

    @Command(name = "clan.sair")
    public void clanLeave(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (account == null) {
            context.getSender().send("§cNão foi possível carregar sua conta.");
            return;
        }

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (account.isClanLeader()) {
            account.send("§cVocê não pode sair do seu clan!");
            return;
        }

        Clan clan = account.getClan();

        if (clan == null) {
            account.send("§cErro: Clan não encontrado.");
            return;
        }

        if (!clan.isMember(account.getId())) {
            account.send("§cVocê não é membro deste clan.");
            // Limpar referência ao clan caso esteja inconsistente
            account.setClan(Constant.DEFAULT_ID);
            return;
        }

        clan.removeMember(account);

        new AccountClanChangeMessage(account).send();
    }

    @Command(name = "clan.transferir")
    public void clanTransfer(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (!account.isClanLeader()) {
            account.send("§cVocê não pode transferir o clan para outros jogadores.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        Clan clan = account.getClan();

        if (!clan.isMember(target.getId())) {
            account.send("§cO jogador " + target.getNickname() + " não é membro do seu clan.");
            return;
        }

        clan.setLeader(target.getId());

        clan.send("§7O clan foi transferido para " + target.getNickname() + ".");
    }

    @Command(name = "clan.recusar")
    public void clanDecline(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasClan()) {
            account.send(ALREADY_IN_A_CLAN);
            return;
        }

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        ClanRequest request = data.getRequest(target.getId(), account.getId());

        if (request == null) {
            account.send("§cNenhum convite foi encontrado de " + target.getNickname() + ".");
            return;
        }

        data.removeRequest(request);

        account.send("§cVocê recusou o convite de " + target.getNickname() + " para entrar no clan.");
        target.send("§c" + account.getNickname() + " recusou o convite para entrar no clan.");
    }

    @Command(name = "clan.tag", aliases = {"clan.cor"})
    public void clanTag(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        Clan clan = account.getClan();
        ClanMember member = clan.getMember(account.getId());
        
        if (member == null) {
            account.send("§cErro: Você não é membro deste clã.");
            return;
        }
        
        ClanRole role = member.getRole();
        if (role != ClanRole.LEADER && role != ClanRole.RECRUITER) {
            account.send("§cApenas o líder e o recrutador podem alterar a cor da tag do clã.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (cor)");
            sendClanColorPreview(account, clan);
            return;
        }

        String colorInput = args[0];
        String colorCode = ClanColor.parse(colorInput);

        if (colorCode == null) {
            account.send("§cCor inválida! Escolha uma cor válida:");
            sendClanColorPreview(account, clan);
            return;
        }

        // Encontrar o ClanColor correspondente
        ClanColor clanColor = ClanColor.getByCode(colorCode);
        
        if (clanColor == null) {
            account.send("§cErro ao processar a cor.");
            return;
        }

        if (!hasAccessToClanColor(account, clanColor)) {
            account.send("§cVocê não pode usar essa cor de clan.",
                    "§cExigência: " + getRequirement(clanColor));
            return;
        }

        // Aplicar a cor — para cores mistas, salvar o id; para cores simples, salvar o code
        String colorToSave = clanColor.hasMixedColorTag() ? clanColor.getId() : colorCode;
        clan.setTagColor(colorToSave);

        String colorName = ClanColor.getName(colorCode) != null ? ClanColor.getName(colorCode) : clanColor.getName();
        account.send("§aA cor da tag do clã foi alterada para " + clanColor.getCode() + clanColor.getName() + "§a.");
        clan.send("§7A cor da tag do clã foi alterada para " + clanColor.getCode() + clanColor.getName() + "§7 por " + account.getNickname() + ".");

        // Atualizar tags de todos os membros online do clã
        clan.getOnlineMembers().forEach(memberAccount -> new AccountClanChangeMessage(memberAccount).send());
    }

    @Command(name = "clantag")
    public void clanTagShortcut(BungeeCommandContext context) {
        clanTag(context);
    }

    private void sendClanColorPreview(Account account, Clan clan) {
        String rawTag = clan.getTagWithoutColors();
        String clanTag = rawTag == null || rawTag.isEmpty() ? "CLAN" : rawTag.toUpperCase();

        TextComponent message = new TextComponent("§aPreview de cores: ");
        boolean first = true;

        for (ClanColor color : ClanColor.values()) {
            if (!hasAccessToClanColor(account, color)) continue;

            if (!first) {
                message.addExtra("§f, ");
            }

            // Para cores mistas (Brasil), aplicar metade amarelo metade verde no nome do clan
            String preview;
            if (color.hasMixedColorTag()) {
                String preview_tag;
                if (color == ClanColor.YINGYANG) {
                    StringBuilder sb = new StringBuilder("§f[");
                    for (int i = 0; i < clanTag.length(); i++) {
                        sb.append(i % 2 == 0 ? "§f" : "§0");
                        sb.append(clanTag.charAt(i));
                    }
                    sb.append("§f]§r");
                    preview_tag = sb.toString();
                } else {
                    // Brasil: [] amarelo
                    int half = (int) Math.ceil(clanTag.length() / 2.0);
                    preview_tag = "§e[§e" + clanTag.substring(0, half) + "§a" + clanTag.substring(half) + "§e]§r";
                }
                preview = preview_tag;
            } else {
                preview = color.getCode() + "[" + clanTag + "]§f";
            }

            TextComponent item = new TextComponent(preview);
            String command = "/clan tag " + color.getId();

            item.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
            item.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                    "§eClique para aplicar esta cor\n§7Comando: §f" + command
            )));

            message.addExtra(item);
            first = false;
        }

        if (first) {
            account.send("§cVocê não possui nenhuma cor de clan disponível.");
            return;
        }

        account.send(message);
    }

    private boolean hasAccessToClanColor(Account account, ClanColor color) {
        switch (color) {
            case BETA:
                return account.hasPermission("tag.beta")
                        || account.hasPermission("rank.beta")
                        || account.hasPermission("clan.color.beta")
                        || account.hasRank(RankType.BETA)
                        || account.hasRank(RankType.PRECIOUS)
                        || account.hasPermission("tag.precious")
                        || account.hasPermission("rank.precious")
                        || account.hasPermission("clan.color.precious");
            case CHAMPION:
                return account.hasPermission(Tag.TAG_CHAMPION.getPermission());
            case MOD:
                return account.hasRank(RankType.MOD);
            case EMBER:
                return account.hasRank(RankType.EMBER);
            case ADMIN:
                return account.hasRank(RankType.ADMIN);
            case VERMELHO:
                return account.hasOnlyRank(RankType.VERMELHO);
            case CREATOR_PLUS:
                return account.hasRank(RankType.CREATOR_PLUS);
            case BRASIL:
                return true;
            case YINGYANG:
                return true;
            default:
                return false;
        }
    }

    private String getRequirement(ClanColor color) {
        switch (color) {
            case BETA:
                return "§1[CLAN] §7- Exclusivo para §1Beta §7ou §5Precious";
            case CHAMPION:
                return "§6[CLAN] §7- Exclusivo para CHAMPION";
            case MOD:
                return "§5[CLAN] §7- Exclusivo para MOD";
            case EMBER:
                return "§c[CLAN] §7- Exclusivo para EMBER";
            case ADMIN:
                return "§4[CLAN] §7- Exclusivo para ADMIN";
            case VERMELHO:
                return "§d[CLAN] §7- Exclusivo para rank VERMELHO";
            case CREATOR_PLUS:
                return "§3[CLAN] §7- Exclusivo para CREATORPLUS";
            case BRASIL:
                return "§e[BR§aASIL§7] §7- Disponível para todos";
            case YINGYANG:
                return "§f[YIN§0YANG§7] §7- Disponível para todos";
            default:
                return "§cSem exigência definida.";
        }
    }
}
