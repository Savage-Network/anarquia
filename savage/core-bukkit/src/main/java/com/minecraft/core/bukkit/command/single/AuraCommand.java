package com.minecraft.core.bukkit.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.aura.image.ImageAuraCollectible;
import com.minecraft.core.api.collectible.type.aura.image.ImageAuraList;
import com.minecraft.core.bukkit.api.shape.manager.ImgurUtil;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class AuraCommand implements CommandInheritor {

    @Command(name = "aura", aliases = {"minhaaura"}, onlyPlayer = true)
    public void aura(BukkitCommandContext context) {
        Account account = context.getAccount();
        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§6§m-------------- §eAura §6§m--------------");
            account.send("§f/aura link <url> §7— imagem do Imgur acima da cabeça");
            account.send("§f/aura usar <nome> §7— ativa preset (ex: Yin-Yang)");
            account.send("§f/aura remover §7— desativa a aura");
            account.send("");
            if (account.getImageAuraMetadata().hasImage()) {
                account.send("§7Sua imagem: §f" + account.getImageAuraMetadata().getImageUrl());
            } else {
                account.send("§7Nenhuma imagem personalizada definida.");
            }
            CollectibleOperator operator = CollectibleOperator.of(context.getPlayer(), CollectibleCategory.AURA);
            if (operator != null && operator.getCollectible() != null) {
                account.send("§7Aura ativa: §a" + operator.getCollectible().getName());
            }
            account.send("§6§m--------------------------------");
            return;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("remover") || sub.equals("remove")) {
            remove(context);
            return;
        }

        if (sub.equals("link") || sub.equals("url") || sub.equals("imagem")) {
            link(context, args);
            return;
        }

        if (sub.equals("usar") || sub.equals("use") || sub.equals("set")) {
            use(context, args);
            return;
        }

        // Atalho: /aura <url> direto
        if (ImgurUtil.looksLikeImageUrl(args[0])) {
            applyLink(context.getPlayer(), account, String.join(" ", args), "Personalizada");
            return;
        }

        account.send("§cSubcomando inválido. Use §f/aura§c para ajuda.");
    }

    @Command(name = "aura link", onlyPlayer = true)
    public void link(BukkitCommandContext context, String[] args) {
        Account account = context.getAccount();

        if (args.length < 2) {
            account.send("§cUso: /aura link <url do Imgur>");
            account.send("§7Exemplo: §f/aura link https://i.imgur.com/abc123.png");
            account.send("§7Também aceita: §fhttps://imgur.com/abc123");
            return;
        }

        String url = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        applyLink(context.getPlayer(), account, url, "Personalizada");
    }

    @Command(name = "aura usar", onlyPlayer = true)
    public void use(BukkitCommandContext context, String[] args) {
        Account account = context.getAccount();
        Player player = context.getPlayer();

        if (args.length < 2) {
            account.send("§cUso: /aura usar <nome>");
            account.send("§7Disponíveis: §fYin-Yang§7, §fPersonalizada");
            return;
        }

        String name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        ImageAuraCollectible collectible = null;

        for (ImageAuraList preset : ImageAuraList.values()) {
            if (preset.getCosmetic().getName().equalsIgnoreCase(name)) {
                collectible = preset.getCosmetic();
                break;
            }
        }

        if (collectible == null) {
            account.send("§cAura não encontrada: §f" + name);
            return;
        }

        if (!collectible.hasAccess(account.getRank().getType())) {
            account.send("§cVocê não tem acesso a esta aura.");
            return;
        }

        if (collectible.isUsesAccountUrl() && !account.getImageAuraMetadata().hasImage()) {
            account.send("§cConfigure antes com §f/aura link <url>§c.");
            return;
        }

        activate(player, account, collectible);
        account.send("§bVocê selecionou §e" + collectible.getName() + " §bcomo sua aura.");
    }

    @Command(name = "aura remover", onlyPlayer = true)
    public void remove(BukkitCommandContext context) {
        Account account = context.getAccount();
        Player player = context.getPlayer();

        CollectibleOperator.unload(player, CollectibleCategory.AURA);
        account.send("§eSua aura foi removida.");
    }

    private void applyLink(Player player, Account account, String rawUrl, String displayName) {
        if (!ImgurUtil.looksLikeImageUrl(rawUrl)) {
            account.send("§cURL inválida. Use um link do Imgur ou imagem direta (.png/.jpg).");
            return;
        }

        String normalized = ImgurUtil.normalize(rawUrl);
        account.setImageAuraUrl(normalized, displayName);

        Core.getPlatform().runAsync(() -> Core.getAccountData().save(account));

        ImageAuraCollectible personalizada = ImageAuraList.PERSONALIZADA.getCosmetic();
        activate(player, account, personalizada);
        account.send("§bVocê selecionou §e" + displayName + " §bcomo sua aura.");
        account.send("§7Carregando imagem... (pode levar alguns segundos)");
    }

    private void activate(Player player, Account account, ImageAuraCollectible collectible) {
        CollectibleOperator.unloadConflicts(player, CollectibleCategory.AURA);
        CollectibleOperator.unload(player, CollectibleCategory.AURA);
        CollectibleOperator.start(player, collectible);
    }
}
