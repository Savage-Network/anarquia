package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.title.CustomTitle;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.util.Util;

public class CustomTitleCommand implements CommandInheritor {

    @Command(name = "customtitle", rank = RankType.CREATOR)
    public void customtitle(BukkitCommandContext context) {
        Account account = context.getAccount();
        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /customtitle <título>");
            account.send("§cExemplo: /customtitle §6§lMEU TITULO");
            account.send("§cUse cores com & (exemplo: &6&lTITULO)");
            account.send("");
            account.send("§7Título atual: " + (account.getCustomTitle() != null ? account.getCustomTitle().getTitle() : "§cNenhum"));
            return;
        }

        // Juntar todos os argumentos em uma string
        String title = String.join(" ", args);
        
        // Converter & para § para cores
        title = Util.color(title);

        // Validar tamanho (máximo 32 caracteres sem contar códigos de cor)
        String stripped = title.replaceAll("§[0-9a-fk-or]", "");
        if (stripped.length() > 32) {
            account.send("§cO título não pode ter mais de 32 caracteres!");
            account.send("§cSeu título tem: §f" + stripped.length() + " caracteres");
            return;
        }

        // Criar título customizado
        CustomTitle customTitle = new CustomTitle(account.getId(), title);
        account.setCustomTitle(customTitle);

        // Salvar
        Core.getPlatform().runAsync(() -> {
            Core.getAccountData().save(account);
        });

        account.send("§a§m------------------------------");
        account.send("§aTítulo customizado definido!");
        account.send("");
        account.send("§eVisualização:");
        account.send(title);
        account.send("");
        account.send("§7Use §f/customtitle remover §7para remover");
        account.send("§a§m------------------------------");
    }

    @Command(name = "customtitle remover")
    public void remove(BukkitCommandContext context) {
        Account account = context.getAccount();

        if (account.getCustomTitle() == null) {
            account.send("§cVocê não possui um título customizado!");
            return;
        }

        account.setCustomTitle(null);

        Core.getPlatform().runAsync(() -> {
            Core.getAccountData().save(account);
        });

        account.send("§aTítulo customizado removido!");
    }
}
