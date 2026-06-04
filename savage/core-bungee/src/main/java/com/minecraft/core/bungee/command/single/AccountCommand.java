package com.minecraft.core.bungee.command.single;

import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.account.context.objects.permission.Permission;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.backend.database.redis.message.types.account.AccountPunishmentsMessage;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.assignment.Assignment;
import com.minecraft.core.account.context.objects.rank.Rank;
import com.minecraft.core.account.context.objects.rank.info.RankInfo;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.geography.GeographyService;
import com.minecraft.core.api.geography.GeographyBuilder;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.core.util.list.TimeUtil;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.cosmetic.CosmeticEntry;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AccountCommand implements CommandInheritor {

    private final List<String> subCommands = Arrays.asList("rank", "golds", "permission", "punishments", "medal", "title");

    @Completer(name = "account", subCommands = {"acc"}, rank = RankType.MODPLUS)
    public List<String> accountCompleter(BungeeCommandContext context) {
        return getPlayerNames(context);
    }

    @Command(name = "account", aliases = {"acc"}, onlyPlayer = false)
    public void account(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        Account staffer = context.getAccount();

        if (args.length == 0) {
            if (!sender.isPlayer()) {
                sender.send("§cUso: /acc <jogador>");
                return;
            }
            
            if (staffer == null) {
                sender.send("§cNão foi possível carregar sua conta.");
                return;
            }
            
            sendInfoMessage(sender, staffer);
            return;
        }

        if (sender.isPlayer() && staffer != null && !staffer.hasRank(RankType.MODPLUS)) {
            sender.send("§cVocê não pode ver a conta de outros jogadores.");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            sender.send(TARGET_NOT_FOUND);
            return;
        }

        if (args.length == 1) {
            sendInfoMessage(sender, target);
            return;
        }

        String label = "/" + context.getLabel();

        String search = args[1].toLowerCase();

        if (subCommands.stream().noneMatch(subCommand -> subCommand.equalsIgnoreCase(search))) {
            sender.send("§cComo usar " + label + ":");

            subCommands.forEach(subCommand -> sender.send("§c* " + label + " [jogador] " + subCommand));
            return;
        }

        switch (search) {
            case "rank": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " rank:",
                            "§c " + label + " [jogador] rank set [tipo] [tempo]",
                            "§c " + label + " [jogador] rank info");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "set": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] rank set (tipo) [tempo].");
                            return;
                        }

                        RankType rankType = RankType.of(args[3]);

                        if (rankType == null) {
                            sender.send("§cO rank solicitado não foi encontrado.");
                            return;
                        }

                        if (sender.isPlayer()) {
                            assert staffer != null;
                            if (staffer.getRankType().equals(rankType) || !staffer.hasRank(rankType)) {
                                sender.send("§cVocê não pode atribuir ranks similares ou iguais ao seu.");
                                return;
                            }
                        }

                        if (sender.isPlayer()) {
                            assert staffer != null;
                            if (!staffer.hasRank(target.getRank().getType())) {
                                sender.send("§cVocê não pode atribuir ranks para jogadores superiores.");
                                return;
                            }
                        }

                        if (target.hasAvailableRank(rankType) || target.hasOnlyRank(rankType)) {
                            sender.send("§cO jogador " + target.getName() + " já possui o rank " + rankType.getName() + ".");
                            return;
                        }

                        long expiresAt = -1L;

                        if (args.length >= 5)
                            expiresAt = TimeUtil.getTime(args[4]);

                        Rank rank = Rank.builder()
                                .type(rankType)
                                .assignment(sender.isPlayer() ? Assignment.STAFF : Assignment.CONSOLE)
                                .author(sender.getId())
                                .expiresAt(expiresAt)
                                .build();

                        target.setRank(rank);

                        sender.send("§aVocê adicionou o rank " + rankType.getName() + " na conta de " + target.getNickname() + ".");

                        log(sender, sender.getName() + " adicionou o rank " + rankType.getName() + " na conta de " + target.getNickname());
                        break;
                    }

                    case "remove": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] rank remove (tipo).");
                            return;
                        }

                        RankType rankType = RankType.of(args[3]);

                        if (rankType == null) {
                            sender.send("§cO rank solicitado não foi encontrado.");
                            return;
                        }

                        if (rankType.equals(RankType.MEMBER)) {
                            sender.send("§cVocê não pode remover o rank " + RankType.MEMBER.getName() + " da conta de " + target.getNickname() + ".");
                            return;
                        }

                        if (!(target.hasOnlyRank(rankType) || target.hasAvailableRank(rankType))) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui o rank " + rankType.getName() + ".");
                            return;
                        }

                        target.removeRank(rankType);

                        sender.send("§eO rank " + rankType.getName() + " de " + target.getNickname() + " foi removido com sucesso.");

                        break;
                    }

                    case "info": {
                        RankInfo info = target.getContext().getRankInfo();

                        Rank rank = info.getRank();
                        List<Rank> rankList = info.getAvailableRanks();

                        /* Criar mensagem de lista dos ranks */
                        StringBuilder builder = new StringBuilder();

                        builder.append("§aLista de Ranks (§f").append(rankList.size()).append("§a): ");

                        int index = 1;
                        boolean end = false;
                        for (Rank extraRank : rankList) {
                            if (index >= rankList.size()) end = true;

                            builder.append(extraRank.getColoredName()).append("§f").append(end ? "." : ", ");

                            index++;
                        }

                        if (rankList.isEmpty())
                            builder.append("§fVázio.");

                        sender.send("§aRank: " + rank.getColoredName(),
                                "§aAtribuído por: §f" + rank.getAuthorName(),
                                " §7Data de atribuição: " + DateUtil.getDate(rank.getAssignedAt()),
                                " §6§o" + rank.getAssignment().getName(),
                                builder.toString());
                        break;
                    }
                }
                break;
            }

            case "permission": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " permission:",
                            "§c* " + label + " [jogador] permission set (chave) [tempo]",
                            "§c* " + label + " [jogador] permission remove (chave)",
                            "§c* " + label + " [jogador] permission list");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "set": {
                        if (args.length <= 4) {
                            sender.send("§cUso: " + label + " [jogador] permission set (chave) [tempo].");
                            return;
                        }

                        String key = args[3];

                        if (target.hasPermission(key)) {
                            sender.send("§cO jogador " + target.getNickname() + " já tem essa permissão.");
                            return;
                        }

                        long expiresAt = TimeUtil.getTime(args[4]);

                        Permission permission = new Permission(key, sender.isPlayer() ? Assignment.STAFF : Assignment.CONSOLE, sender.getId(), expiresAt);

                        target.setPermission(permission);

                        sender.send("§aVocê adicionou a permissão '" + key + "' na conta de " + target.getNickname() + ".");
                        break;
                    }

                    case "remove": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] permission remove (chave).");
                            return;
                        }

                        String key = args[3];

                        if (!target.hasPermission(key)) {
                            sender.send("§cO jogador " + target.getNickname() + " não tem essa permissão.");
                            return;
                        }

                        target.removePermission(key);

                        sender.send("§cVocê removeu a permissão '" + key + "' da conta de " + target.getNickname() + ".");
                        break;
                    }

                    case "list": {
                        List<Permission> list = target.getPermissions();

                        if (list.isEmpty()) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui permissões.");
                            return;
                        }

                        StringBuilder builder = new StringBuilder("§aPermissões: ");


                        int index = 1;
                        boolean end = false;
                        for (Permission permission : list) {
                            if (index >= list.size()) end = true;

                            builder.append("§f").append(permission.getKey()).append(end ? "." : ", ");

                            index++;
                        }

                        sender.send(builder.toString());
                        break;
                    }
                }

                break;
            }

            case "punishments": {
                if (!sender.isPlayer()) {
                    sender.send("§cSomente jogadores podem visualizar o histórico de punições.");
                    return;
                }

                if (args.length == 2) {
                    sender.send("§cUso: " + label + " [jogador] punishments [categoria].");
                    return;
                }

                PunishmentCategory category = PunishmentCategory.of(args[2]);

                if (category == null) {
                    sender.send("§cA categoria '" + args[2] + "' não foi encontrada.");
                    return;
                }

                if (target.getPunishments(category).isEmpty()) {
                    sender.send("§cO jogador " + target.getNickname() + " não possui um histórico de " + category.getName() + "s.");
                    return;
                }

                new AccountPunishmentsMessage(sender.getId(), target.getId(), category).send();
                break;
            }

            case "golds": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " golds:",
                            "§c* " + label + " [jogador] golds add (valor)",
                            "§c* " + label + " [jogador] golds remove (valor)");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "add": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] golds add (valor).");
                            return;
                        }

                        if (!Util.isNumber(args[3])) {
                            sender.send("§cSomente números são válidos.");
                            return;
                        }

                        int value = Integer.parseInt(args[3]);

                        if (value <= 0) {
                            sender.send("§cO valor de ouros não pode ser menor ou igual a zero.");
                            return;
                        }

                        target.addGolds(value);

                        sender.send("§aVocê adicionou " + Util.formatNumber(value) + " ouros na conta de " + target.getNickname() + ".");
                        break;
                    }
                    case "remove": {
                        if (args.length == 3) {
                            sender.send("§cUso: " + label + " [jogador] golds remove (valor).");
                            return;
                        }

                        if (!Util.isNumber(args[3])) {
                            sender.send("§cSomente números são válidos.");
                            return;
                        }

                        int value = Integer.parseInt(args[3]);

                        if (value <= 0) {
                            sender.send("§cO valor de golds não pode ser menor ou igual a zero.");
                            return;
                        }

                        target.removeGolds(value);

                        sender.send("§eVocê removeu " + Util.formatNumber(target.getGolds() - value) + " golds da conta de " + target.getNickname() + ".");
                        break;
                    }
                }

                break;
            }

            case "medal": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " medal:",
                            "§c* " + label + " [jogador] medal add [nome]",
                            "§c* " + label + " [jogador] medal remove [nome]",
                            "§c* " + label + " [jogador] medal set [nome]",
                            "§c* " + label + " [jogador] medal list",
                            "§c* " + label + " [jogador] medal info");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "add": {
                        if (args.length <= 3) {
                            sender.send("§cUso: " + label + " [jogador] medal add [nome completo — pode ter espaços].");
                            return;
                        }

                        String medalQuery = joinArgs(args, 3);
                        Medal medal = Medal.of(medalQuery);

                        if (medal == null) {
                            sender.send("§cA medalha '" + medalQuery + "' não foi encontrada.");
                            return;
                        }

                        if (medal.equals(Medal.NONE)) {
                            sender.send("§cVocê não pode adicionar a medalha " + medal.getColoredName() + ".");
                            return;
                        }

                        if (target.hasMedal(medal)) {
                            sender.send("§cO jogador " + target.getNickname() + " já possui a medalha " + medal.getColoredName() + ".");
                            return;
                        }

                        target.addMedal(medal);

                        sender.send("§aVocê adicionou a medalha " + medal.getColoredName() + " na conta de " + target.getNickname() + ".");
                        log(sender, sender.getName() + " adicionou a medalha " + medal.getName() + " na conta de " + target.getNickname());
                        break;
                    }

                    case "remove": {
                        if (args.length <= 3) {
                            sender.send("§cUso: " + label + " [jogador] medal remove [nome completo — pode ter espaços].");
                            return;
                        }

                        String medalQueryRm = joinArgs(args, 3);
                        Medal medal = Medal.of(medalQueryRm);

                        if (medal == null) {
                            sender.send("§cA medalha '" + medalQueryRm + "' não foi encontrada.");
                            return;
                        }

                        if (medal.equals(Medal.NONE)) {
                            sender.send("§cVocê não pode remover a medalha " + medal.getColoredName() + ".");
                            return;
                        }

                        if (!target.hasMedal(medal)) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui a medalha " + medal.getColoredName() + ".");
                            return;
                        }

                        target.removeMedal(medal);

                        // Se a medalha removida estava sendo usada, definir como NONE
                        if (target.isUsingMedal(medal)) {
                            target.setMedal(Medal.NONE);
                        }

                        sender.send("§eVocê removeu a medalha " + medal.getColoredName() + " da conta de " + target.getNickname() + ".");
                        break;
                    }

                    case "set": {
                        if (args.length <= 3) {
                            sender.send("§cUso: " + label + " [jogador] medal set [nome completo — pode ter espaços].");
                            return;
                        }

                        String medalQuerySet = joinArgs(args, 3);
                        Medal medal = Medal.of(medalQuerySet);

                        if (medal == null) {
                            sender.send("§cA medalha '" + medalQuerySet + "' não foi encontrada.");
                            return;
                        }

                        if (!target.hasMedal(medal)) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui a medalha " + medal.getColoredName() + ".");
                            return;
                        }

                        target.setMedal(medal);

                        sender.send("§aVocê definiu a medalha " + medal.getColoredName() + " como ativa na conta de " + target.getNickname() + ".");
                        break;
                    }

                    case "info": {
                        Medal activeMedal = target.getMedal();
                        if (activeMedal == null || Medal.NONE.equals(activeMedal)) {
                            sender.send("§7Medalha ativa no perfil: §fnenhuma§7.");
                        } else {
                            sender.send("§aMedalha ativa: " + activeMedal.getColoredName() + " §8(" + activeMedal.name() + "§8)");
                        }

                        LinkedHashSet<Medal> ownedSet = new LinkedHashSet<>(target.getMedals());
                        ownedSet.remove(Medal.NONE);
                        if (ownedSet.isEmpty()) {
                            sender.send("§7Medalhas salvas na conta: §fvazio§7.");
                        } else {
                            sender.send("§aMedalhas salvas na conta §8(" + ownedSet.size() + "§8)§a:");
                            for (Medal m : ownedSet) {
                                String prefix = activeMedal != null && m.equals(activeMedal) ? "§e[ATIVA] " : "";
                                sender.send(" §8- " + prefix + m.getColoredName() + " §8(" + m.name() + "§8)");
                            }
                        }

                        if (!target.isStaffer()) {
                            List<Medal> extraMedals = new ArrayList<>();
                            for (Medal m : Medal.values()) {
                                if (m == Medal.NONE) continue;
                                if (target.getMedals().contains(m)) continue;
                                if (target.hasMedal(m)) extraMedals.add(m);
                            }
                            if (!extraMedals.isEmpty()) {
                                sender.send("§eUsáveis agora §8(rank/permissão/VIP)§e mas não na lista salva §8(" + extraMedals.size() + "§8)§e:");
                                int cap = Math.min(extraMedals.size(), 15);
                                for (int i = 0; i < cap; i++) {
                                    Medal m = extraMedals.get(i);
                                    sender.send(" §8- " + m.getColoredName() + " §8(" + m.name() + "§8)");
                                }
                                if (extraMedals.size() > cap)
                                    sender.send(" §8… §7e mais " + (extraMedals.size() - cap) + "§7.");
                            }
                        }
                        break;
                    }

                    case "list": {
                        List<Medal> medals = target.getMedals();
                        Medal activeMedal = target.getMedal();

                        if (medals.isEmpty()) {
                            sender.send("§cO jogador " + target.getNickname() + " não possui medalhas.");
                            return;
                        }

                        StringBuilder builder = new StringBuilder("§aMedalhas: ");

                        int index = 1;
                        boolean end = false;
                        for (Medal medal : medals) {
                            if (index >= medals.size()) end = true;

                            String prefix = medal.equals(activeMedal) ? "§e[ATIVA] " : "";
                            builder.append(prefix).append(medal.getColoredName()).append("§f").append(end ? "." : ", ");

                            index++;
                        }

                        sender.send(builder.toString());
                        if (activeMedal != null && !activeMedal.equals(Medal.NONE)) {
                            sender.send("§aMedalha ativa: " + activeMedal.getColoredName());
                        }
                        break;
                    }

                    default:
                        sender.send("§cComo usar " + label + " medal:",
                                "§c* " + label + " [jogador] medal add [nome]",
                                "§c* " + label + " [jogador] medal remove [nome]",
                                "§c* " + label + " [jogador] medal set [nome]",
                                "§c* " + label + " [jogador] medal list",
                                "§c* " + label + " [jogador] medal info");
                }

                break;
            }

            case "title": {
                if (args.length == 2) {
                    sender.send("§cComo usar " + label + " title:",
                            "§c* " + label + " [jogador] title set [nome ou ID]",
                            "§c* " + label + " [jogador] title remove [nome ou ID]",
                            "§c* " + label + " [jogador] title info");
                    return;
                }

                switch (args[2].toLowerCase()) {
                    case "set": {
                        if (args.length <= 3) {
                            sender.send("§cUso: " + label + " [jogador] title set (nome ou ID completo tipo title:mythical:Nome).");
                            return;
                        }
                        String q = joinArgs(args, 3);
                        String id = resolveTitleIdentifier(q);
                        if (id == null) {
                            sender.send("§cTítulo não encontrado: §f" + q);
                            return;
                        }
                        if (!target.ownsCosmeticUnlock(id)) {
                            sender.send("§cO jogador não possui esse título (sem desbloqueio nem acesso pelo rank).");
                            return;
                        }
                        target.activateCosmeticIdentifier(id);
                        sender.send("§aTítulo §f" + describeTitleIdentifier(id) + "§a definido como ativo para §f" + target.getNickname() + "§a.");
                        log(sender, sender.getName() + " definiu o título ativo " + id + " para " + target.getNickname());
                        break;
                    }
                    case "remove": {
                        if (args.length <= 3) {
                            sender.send("§cUso: " + label + " [jogador] title remove (nome ou ID).");
                            return;
                        }
                        String q = joinArgs(args, 3);
                        String id = resolveTitleIdentifier(q);
                        if (id == null) {
                            sender.send("§cTítulo não encontrado: §f" + q);
                            return;
                        }
                        if (!target.ownsCosmeticUnlock(id)) {
                            sender.send("§cO jogador não tem esse título disponível ou desbloqueado.");
                            return;
                        }
                        boolean hadEntry = target.getCollectibleEntries().stream().anyMatch(e -> e.getIdentifier().equalsIgnoreCase(id));
                        target.revokeCosmetic(id);
                        if (hadEntry) {
                            sender.send("§eTítulo §f" + describeTitleIdentifier(id) + "§e removido da conta de §f" + target.getNickname() + "§e (entrada cosmético + uso ativo).");
                            log(sender, sender.getName() + " revogou o título " + id + " de " + target.getNickname());
                        } else {
                            sender.send("§eTítulo §f" + describeTitleIdentifier(id) + "§e desequipado para §f" + target.getNickname() + "§e §7(disponível pelo rank ou catálogo).");
                            log(sender, sender.getName() + " desequipou o título " + id + " em " + target.getNickname());
                        }
                        break;
                    }
                    case "info": {
                        String prefix = CollectibleCategory.TITLE.name().toLowerCase() + ":";
                        String activeId = target.getActiveCollectibles().stream()
                                .filter(cid -> cid.toLowerCase().startsWith(prefix))
                                .findFirst()
                                .orElse(null);

                        sender.send(activeId == null ? "§7Título acima do nome ativo: §fnenhum§7."
                                : "§aTítulo ativo: §f" + describeTitleIdentifier(activeId) + " §8(" + activeId + "§8)");

                        List<CosmeticEntry> titleEntries = new ArrayList<>();
                        for (CosmeticEntry e : target.getCollectibleEntries()) {
                            if (e.getIdentifier().toLowerCase().startsWith(prefix))
                                titleEntries.add(e);
                        }
                        if (titleEntries.isEmpty()) {
                            sender.send("§7Entradas de título na conta §8(desbloqueio): §fvazio§7.");
                        } else {
                            sender.send("§aTítulos desbloqueados na conta §8(" + titleEntries.size() + "§8)§a:");
                            for (CosmeticEntry e : titleEntries) {
                                sender.send(" §8- §f" + describeTitleIdentifier(e.getIdentifier()) + " §8(" + e.getIdentifier() + "§8)");
                            }
                        }

                        List<Collectible> cat = Core.getCollectibleController().list(CollectibleCategory.TITLE);
                        if (!cat.isEmpty()) {
                            List<String> byRank = new ArrayList<>();
                            for (Collectible c : cat) {
                                if (c.hasAccess(target.getRank().getType())
                                        && target.getCollectibleEntries().stream().noneMatch(e -> e.getIdentifier().equalsIgnoreCase(c.getIdentifier()))) {
                                    byRank.add(c.getName() + " §8(" + c.getIdentifier() + "§8)");
                                }
                            }
                            if (!byRank.isEmpty()) {
                                sender.send("§eDisponível pelo rank (sem entrada própria) §8(" + byRank.size() + "§8)§e:");
                                int cap = Math.min(byRank.size(), 15);
                                for (int i = 0; i < cap; i++) sender.send(" §8- §f" + byRank.get(i));
                                if (byRank.size() > cap)
                                    sender.send(" §8… §7e mais " + (byRank.size() - cap) + "§7.");
                            }
                        } else {
                            sender.send("§7§oCatálogo de títulos não carregado neste proxy. Use IDs completos (ex.: title:mythical:Nome).");
                        }
                        break;
                    }
                    default:
                        sender.send("§cComo usar " + label + " title:",
                                "§c* " + label + " [jogador] title set [nome ou ID]",
                                "§c* " + label + " [jogador] title remove [nome ou ID]",
                                "§c* " + label + " [jogador] title info");
                }
                break;
            }

        }
    }

    private static String joinArgs(String[] args, int fromInclusive) {
        if (args == null || fromInclusive >= args.length) return "";
        return String.join(" ", Arrays.copyOfRange(args, fromInclusive, args.length));
    }

    private static String resolveTitleIdentifier(String query) {
        if (query == null || query.isEmpty()) return null;
        String q = query.trim();
        CollectibleCategory titleCat = CollectibleCategory.TITLE;
        String titlePrefix = titleCat.name().toLowerCase();

        long colons = q.chars().filter(ch -> ch == ':').count();
        if (colons >= 2 && q.toLowerCase().startsWith(titlePrefix + ":")) {
            Collectible byId = Core.getCollectibleController().of(q);
            if (byId != null && byId.getCategory() == titleCat) return byId.getIdentifier();
            return q;
        }

        Collectible named = Core.getCollectibleController().list(titleCat).stream()
                .filter(c -> c.getName().equalsIgnoreCase(q))
                .findFirst()
                .orElse(null);
        if (named != null) return named.getIdentifier();

        for (CollectibleRarity rarity : CollectibleRarity.values()) {
            String id = titlePrefix + ":" + rarity.name().toLowerCase() + ":" + q;
            if (Core.getCollectibleController().contains(id))
                return id;
        }

        return null;
    }

    private static String describeTitleIdentifier(String identifier) {
        if (identifier == null) return "";
        Collectible c = Core.getCollectibleController().of(identifier);
        return c != null ? c.getName() : stripIdentifierForDisplay(identifier);
    }

    private static String stripIdentifierForDisplay(String identifier) {
        int last = identifier.lastIndexOf(':');
        return last >= 0 && last + 1 < identifier.length() ? identifier.substring(last + 1) : identifier;
    }

    protected void sendInfoMessage(CommandSender sender, Account account) {
        List<String> message = new ArrayList<>(Arrays.asList(
                "§aUsuário: §f" + account.getName(),
                "§aTipo: §f" + (account.isPremium() ? "Original" : "Pirata"),
                "§aPrimeira entrada: §f" + DateUtil.getDate(account.getContext().getCreatedAt()),
                "§aÚltima entrada: §f" + DateUtil.getDate(account.getContext().getLastLogin())
        ));

        for (Rank rank : account.getAvailableRanks()) {
            message.addAll(Arrays.asList(
                    "§aRank: " + rank.getColoredName(),
                    " §7Autor: " + rank.getAuthorName(),
                    " §7Atribuído em: " + DateUtil.getDate(rank.getAssignedAt())));
        }

        message.addAll(Arrays.asList(
                "§aRank: " + account.getRank().getColoredName(),
                " §7Autor: " + account.getRank().getAuthorName(),
                " §7Atribuído em: " + DateUtil.getDate(account.getRank().getAssignedAt())
        ));

        CompletableFuture.runAsync(() -> {
            GeographyBuilder geography = GeographyService.getGeography(account.getIpAddress());

            if (geography.isValid())
                message.addAll(Arrays.asList(
                        "§aLocalização:",
                        " §7País: " + geography.getCountry(),
                        " §7Estado: " + geography.getRegion(),
                        " §7Cidade: " + geography.getCity(),
                        " §7ASN: " + geography.getAsn()
                ));
        });

        sender.send(message);
    }
}
