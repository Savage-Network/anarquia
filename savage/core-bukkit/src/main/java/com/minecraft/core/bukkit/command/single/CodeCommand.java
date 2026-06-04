package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.code.RedeemCode;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;

import java.util.concurrent.TimeUnit;

public class CodeCommand implements CommandInheritor {

    @Command(name = "code", rank = RankType.ADMIN)
    public void code(BukkitCommandContext context) {
        Account account = context.getAccount();
        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /code <vip> <tempo>");
            account.send("§cExemplo: /code ELITE 30d");
            account.send("§cOu use: /code list - para ver códigos disponíveis");
            return;
        }

        // Subcomando list
        if (args[0].equalsIgnoreCase("list")) {
            Core.getPlatform().runAsync(() -> {
                java.util.List<RedeemCode> codes = Core.getRedeemCodeData().listUnused();

                Core.getPlatform().runSync(() -> {
                    if (codes.isEmpty()) {
                        account.send("§cNão há códigos disponíveis.");
                        return;
                    }

                    account.send("§a§m------------------------------");
                    account.send("§aCódigos Disponíveis §7(" + codes.size() + ")");
                    account.send("");

                    for (RedeemCode code : codes) {
                        Account creator = Core.getAccountData().of(code.getCreator());
                        String creatorName = creator != null ? creator.getName() : "Desconhecido";

                        account.send("§e" + code.getCode() + " §7- " + 
                                code.getRankType().getColor() + code.getRankType().name() + 
                                " §7(" + code.getFormattedDuration() + ")");
                        account.send("  §7Criado por: §f" + creatorName);
                    }

                    account.send("§a§m------------------------------");
                });
            });
            return;
        }

        if (args.length < 2) {
            account.send("§cUso: /code <vip> <tempo>");
            account.send("§cExemplo: /code ELITE 30d");
            account.send("§cTempo: 1h, 1d, 30d, etc.");
            return;
        }

        String rankName = args[0].toUpperCase();
        String timeStr = args[1].toLowerCase();

        // Validar rank
        RankType rankType;
        try {
            rankType = RankType.valueOf(rankName);
        } catch (IllegalArgumentException e) {
            account.send("§cRank inválido! Ranks disponíveis:");
            account.send("§cVIP, NATIVE, EMBER, EMBER_PLUS, SAVAGE");
            account.send("§cBETA, PRECIOUS, CARNAVAL, FERIAS, HALLOWEEN, NATAL");
            return;
        }

        // Lista de ranks VIP permitidos
        java.util.List<RankType> allowedVips = java.util.Arrays.asList(
            RankType.VIP,
            RankType.NATIVE,
            RankType.EMBER,
            RankType.EMBER_PLUS,
            RankType.SAVAGE,
            RankType.BETA,
            RankType.PRECIOUS,
            RankType.CARNAVAL,
            RankType.FERIAS,
            RankType.HALLOWEEN,
            RankType.NATAL
        );

        // Validar se é um dos ranks VIP permitidos
        if (!allowedVips.contains(rankType)) {
            account.send("§cApenas estes ranks VIP podem ser usados:");
            account.send("§cVIP, NATIVE, EMBER, EMBER_PLUS, SAVAGE");
            account.send("§cBETA, PRECIOUS, CARNAVAL, FERIAS, HALLOWEEN, NATAL");
            return;
        }

        // Parsear tempo
        long duration;
        try {
            duration = parseTime(timeStr);
        } catch (IllegalArgumentException e) {
            account.send("§cTempo inválido! Use: 1h, 1d, 30d, etc.");
            return;
        }

        // Gerar código único
        String code = generateUniqueCode();

        // Criar código
        RedeemCode redeemCode = new RedeemCode(code, rankType, duration, account.getId());

        // Salvar no banco
        Core.getPlatform().runAsync(() -> {
            Core.getRedeemCodeData().save(redeemCode);

            Core.getPlatform().runSync(() -> {
                account.send("§a§m------------------------------");
                account.send("§aCódigo criado com sucesso!");
                account.send("");
                account.send("§eCódigo: §f" + code);
                account.send("§eRank: " + rankType.getColor() + rankType.name());
                account.send("§eDuração: §f" + redeemCode.getFormattedDuration());
                account.send("");
                account.send("§7Use §f/resgatar " + code + " §7para resgatar");
                account.send("§a§m------------------------------");
            });
        });
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = com.minecraft.core.util.list.StringUtil.generateLetterCode(8).toUpperCase();
        } while (Core.getRedeemCodeData().of(code) != null);
        return code;
    }

    private long parseTime(String timeStr) throws IllegalArgumentException {
        if (timeStr.length() < 2) {
            throw new IllegalArgumentException("Invalid time format");
        }

        char unit = timeStr.charAt(timeStr.length() - 1);
        String numberStr = timeStr.substring(0, timeStr.length() - 1);

        int amount;
        try {
            amount = Integer.parseInt(numberStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number");
        }

        switch (unit) {
            case 'h':
                return TimeUnit.HOURS.toMillis(amount);
            case 'd':
                return TimeUnit.DAYS.toMillis(amount);
            case 'm':
                return TimeUnit.MINUTES.toMillis(amount);
            default:
                throw new IllegalArgumentException("Invalid time unit");
        }
    }
}
