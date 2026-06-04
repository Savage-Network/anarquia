package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.account.context.objects.medal.Medal;
import com.minecraft.proxyhost.account.context.objects.rank.Rank;
import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import com.minecraft.proxyhost.account.context.objects.tag.Tag;
import com.minecraft.proxyhost.account.context.objects.title.CustomTitle;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Comando staff de visualizacao/edicao da {@link Account}.
 *
 * <ul>
 *   <li>{@code /account info <player>}</li>
 *   <li>{@code /account setrank <player> <rank>}</li>
 *   <li>{@code /account settag <player> <tag>}</li>
 *   <li>{@code /account setmedal <player> <medal>}</li>
 *   <li>{@code /account addmedal <player> <medal>}</li>
 *   <li>{@code /account remmedal <player> <medal>}</li>
 *   <li>{@code /account settitle <player> <title...>}</li>
 *   <li>{@code /account setgolds <player> <amount>}</li>
 * </ul>
 */
public final class AccountCommand implements SimpleCommand {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();

    @Override
    public void execute(Invocation invocation) {
        CommandSource src = invocation.source();
        String[] args = invocation.arguments();

        if (!src.hasPermission("proxyhost.account")) {
            src.sendMessage(legacy("&cVoce nao tem permissao para usar este comando."));
            return;
        }

        if (args.length == 0) {
            usage(src);
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "info"      -> info(src, args);
            case "setrank"   -> setRank(src, args);
            case "settag"    -> setTag(src, args);
            case "setmedal"  -> setMedal(src, args);
            case "addmedal"  -> addMedal(src, args);
            case "remmedal"  -> remMedal(src, args);
            case "settitle"  -> setTitle(src, args);
            case "setgolds"  -> setGolds(src, args);
            default          -> usage(src);
        }
    }

    private void usage(CommandSource src) {
        src.sendMessage(legacy("&6/account &7info|setrank|settag|setmedal|addmedal|remmedal|settitle|setgolds &7..."));
    }

    /* ---------------- Subcomandos ---------------- */

    private void info(CommandSource src, String[] args) {
        if (args.length < 2) { src.sendMessage(legacy("&cUso: /account info <player>")); return; }
        resolve(args[1]).thenAccept(opt -> {
            if (opt.isEmpty()) { src.sendMessage(legacy("&cConta nao encontrada.")); return; }
            Account a = opt.get();
            src.sendMessage(legacy(" "));
            src.sendMessage(legacy("&6&l" + a.getName().toUpperCase()));
            src.sendMessage(legacy("&7Rank: &f" + a.getRankType()));
            src.sendMessage(legacy("&7Tag: &f" + a.getTag()));
            src.sendMessage(legacy("&7Medal: &f" + a.getMedal()));
            src.sendMessage(legacy("&7Golds: &6" + a.getGolds()));
            src.sendMessage(legacy("&7Server: &f" + a.getCurrentServer()));
            src.sendMessage(legacy("&7Premium: &f" + a.isPremium()));
            CustomTitle ct = a.getCustomTitle();
            if (ct != null && ct.getTitle() != null && !ct.getTitle().isEmpty()) {
                src.sendMessage(legacy("&7Title: &f" + ct.getTitle()));
            }
            src.sendMessage(legacy(" "));
        });
    }

    private void setRank(CommandSource src, String[] args) {
        if (args.length < 3) { src.sendMessage(legacy("&cUso: /account setrank <player> <rank>")); return; }
        RankType type;
        try {
            type = RankType.valueOf(args[2].toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            src.sendMessage(legacy("&cRank invalido: &f" + args[2]));
            return;
        }
        mutate(src, args[1], a -> {
            a.setRank(Rank.builder().type(type).build());
            src.sendMessage(legacy("&aRank de &f" + a.getName() + " &adefinido para &6" + type + "&a."));
        });
    }

    private void setTag(CommandSource src, String[] args) {
        if (args.length < 3) { src.sendMessage(legacy("&cUso: /account settag <player> <tag>")); return; }
        Tag tag;
        try { tag = Tag.valueOf(args[2].toUpperCase(Locale.ROOT)); }
        catch (Exception e) { src.sendMessage(legacy("&cTag invalida: &f" + args[2])); return; }
        mutate(src, args[1], a -> {
            a.setTag(tag);
            src.sendMessage(legacy("&aTag de &f" + a.getName() + " &adefinida para " + tag.getColor() + tag.name() + "&a."));
        });
    }

    private void setMedal(CommandSource src, String[] args) {
        if (args.length < 3) { src.sendMessage(legacy("&cUso: /account setmedal <player> <medal>")); return; }
        Medal m;
        try { m = Medal.valueOf(args[2].toUpperCase(Locale.ROOT)); }
        catch (Exception e) { src.sendMessage(legacy("&cMedalha invalida: &f" + args[2])); return; }
        mutate(src, args[1], a -> {
            a.setMedal(m);
            src.sendMessage(legacy("&aMedalha de &f" + a.getName() + " &adefinida para &6" + m + "&a."));
        });
    }

    private void addMedal(CommandSource src, String[] args) {
        if (args.length < 3) { src.sendMessage(legacy("&cUso: /account addmedal <player> <medal>")); return; }
        Medal m;
        try { m = Medal.valueOf(args[2].toUpperCase(Locale.ROOT)); }
        catch (Exception e) { src.sendMessage(legacy("&cMedalha invalida: &f" + args[2])); return; }
        mutate(src, args[1], a -> {
            a.addMedal(m);
            src.sendMessage(legacy("&aMedalha &6" + m + " &aadicionada a &f" + a.getName() + "&a."));
        });
    }

    private void remMedal(CommandSource src, String[] args) {
        if (args.length < 3) { src.sendMessage(legacy("&cUso: /account remmedal <player> <medal>")); return; }
        Medal m;
        try { m = Medal.valueOf(args[2].toUpperCase(Locale.ROOT)); }
        catch (Exception e) { src.sendMessage(legacy("&cMedalha invalida: &f" + args[2])); return; }
        mutate(src, args[1], a -> {
            a.removeMedal(m);
            src.sendMessage(legacy("&aMedalha &6" + m + " &aremovida de &f" + a.getName() + "&a."));
        });
    }

    private void setTitle(CommandSource src, String[] args) {
        if (args.length < 3) { src.sendMessage(legacy("&cUso: /account settitle <player> <titulo...>")); return; }
        String title = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        mutate(src, args[1], a -> {
            CustomTitle ct = a.getCustomTitle() == null ? new CustomTitle() : a.getCustomTitle();
            ct.setTitle(title);
            a.setCustomTitle(ct);
            src.sendMessage(legacy("&aCustom title de &f" + a.getName() + " &adefinido."));
        });
    }

    private void setGolds(CommandSource src, String[] args) {
        if (args.length < 3) { src.sendMessage(legacy("&cUso: /account setgolds <player> <amount>")); return; }
        int amount;
        try { amount = Integer.parseInt(args[2]); }
        catch (Exception e) { src.sendMessage(legacy("&cQuantidade invalida.")); return; }
        mutate(src, args[1], a -> {
            a.setGolds(amount);
            src.sendMessage(legacy("&aGolds de &f" + a.getName() + " &adefinidos para &6" + amount + "&a."));
        });
    }

    /* ---------------- helpers ---------------- */

    private void mutate(CommandSource src, String name, java.util.function.Consumer<Account> action) {
        resolve(name).thenAccept(opt -> {
            if (opt.isEmpty()) { src.sendMessage(legacy("&cConta nao encontrada.")); return; }
            Account a = opt.get();
            action.accept(a);
            Core.getAccountData().saveAsync(a).thenRun(a::clearDirty);
        });
    }

    private CompletableFuture<Optional<Account>> resolve(String name) {
        Account online = Core.getAccountController().of(name);
        if (online != null) return CompletableFuture.completedFuture(Optional.of(online));
        return CompletableFuture.supplyAsync(() -> Core.getAccountData().loadByName(name));
    }

    private Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            return Arrays.asList("info", "setrank", "settag", "setmedal", "addmedal", "remmedal", "settitle", "setgolds");
        }
        if (args.length == 2) {
            List<String> names = new ArrayList<>();
            Core.getProxy().getAllPlayers().forEach(p -> names.add(p.getUsername()));
            return names;
        }
        if (args.length == 3) {
            return switch (args[0].toLowerCase(Locale.ROOT)) {
                case "setrank" -> Arrays.stream(RankType.values()).map(Enum::name).toList();
                case "settag"  -> Arrays.stream(Tag.values()).map(Enum::name).toList();
                case "setmedal", "addmedal", "remmedal" -> Arrays.stream(Medal.values()).map(Enum::name).toList();
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }
}
