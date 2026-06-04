package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.code.RedeemCode;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;

public class RedeemCommand implements CommandInheritor {

    @Command(name = "resgatar")
    public void resgatar(BukkitCommandContext context) {
        Account account = context.getAccount();
        String[] args = context.getArgs();

        if (args.length < 1) {
            account.send("§cUso: /resgatar <código>");
            account.send("§cExemplo: /resgatar ABC123XY");
            return;
        }

        String codeStr = args[0].toUpperCase();

        Core.getPlatform().runAsync(() -> {
            RedeemCode code = Core.getRedeemCodeData().of(codeStr);

            Core.getPlatform().runSync(() -> {
                if (code == null) {
                    account.send("§cCódigo inválido!");
                    return;
                }

                if (code.isUsed()) {
                    account.send("§cEste código já foi utilizado!");
                    return;
                }

                // Verificar se o jogador já tem um rank VIP ativo
                if (account.hasRank(code.getRankType())) {
                    account.send("§cVocê já possui o rank " + code.getRankType().getColor() + code.getRankType().name() + "§c!");
                    return;
                }

                // Verificar se o jogador tem um rank VIP superior
                if (account.getRank().getType().ordinal() > code.getRankType().ordinal() && 
                    account.getRank().getType().getRole() == com.minecraft.core.account.context.objects.rank.type.role.RankRole.VIP) {
                    account.send("§cVocê já possui um rank superior!");
                    return;
                }

                // Marcar código como usado
                code.markAsUsed(account.getId());

                // Salvar código atualizado
                Core.getPlatform().runAsync(() -> {
                    Core.getRedeemCodeData().save(code);
                });

                // Aplicar rank ao jogador
                long expiresAt = System.currentTimeMillis() + code.getDuration();
                com.minecraft.core.account.context.objects.rank.Rank newRank = com.minecraft.core.account.context.objects.rank.Rank.builder()
                        .type(code.getRankType())
                        .expiresAt(expiresAt)
                        .build();
                
                account.setRank(newRank);

                // Salvar conta
                Core.getPlatform().runAsync(() -> {
                    Core.getAccountData().save(account);
                });

                // Mensagem de sucesso
                account.send("§a§m------------------------------");
                account.send("§aCódigo resgatado com sucesso!");
                account.send("");
                account.send("§eVocê recebeu o rank " + code.getRankType().getColor() + code.getRankType().name());
                account.send("§eDuração: §f" + code.getFormattedDuration());
                account.send("");
                account.send("§7Aproveite seus benefícios!");
                account.send("§a§m------------------------------");

                // Notificar todos os jogadores online
                Core.getAccountController().list().forEach(target -> {
                    if (target.getId().equals(account.getId())) return;
                    
                    target.send("§e" + account.getName() + " §aresgatou o rank " + 
                            code.getRankType().getColor() + code.getRankType().name() + "§a!");
                });
            });
        });
    }
}
