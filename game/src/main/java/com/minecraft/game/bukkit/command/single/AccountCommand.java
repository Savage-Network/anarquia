package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.medal.Medal;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.account.context.objects.tag.Tag;
import com.minecraft.game.account.package_.AccountPackage;
import com.minecraft.game.account.package_.AccountPackages;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.package_.AccountPackageService;
import com.minecraft.game.bukkit.rank.RankGrantService;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import com.minecraft.game.util.AccountSync;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * {@code /account [jogador]} — perfil da conta.
 * Staff ({@code game.admin}): {@code /account setrank <nick> <RANK>}, etc.
 */
public final class AccountCommand implements CommandInheritor {

    @Command(name = "account", aliases = {"perfil", "conta", "a"},
            description = "Perfil ou edicao staff da conta",
            usage = "/account [jogador] | /account setpackage <nick> <pacote> | /account rempackage <nick> <pacote>")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            if (!ctx.isPlayer()) {
                ctx.reply("§cUso: /account <jogador>  ou  /account setrank <nick> <RANK>");
                return;
            }
            Account self = Core.getAccountController().of(ctx.player().getUniqueId());
            if (self == null) {
                ctx.reply("§cSua conta ainda nao foi carregada.");
                return;
            }
            printProfile(ctx, self);
            return;
        }

        String sub = ctx.getArgs()[0].toLowerCase(Locale.ROOT);
        if (isStaffSub(sub)) {
            if (!canStaffAccount(ctx)) {
                ctx.reply("§cSem permissao (game.admin).");
                return;
            }
            dispatchStaff(ctx, sub);
            return;
        }

        showProfileByName(ctx, ctx.getArgs()[0]);
    }

    private static boolean isStaffSub(String sub) {
        return switch (sub) {
            case "info", "help", "ajuda",
                    "setrank", "remrank", "removerrank", "delrank",
                    "setpackage", "rempackage", "packages",
                    "settag", "remtag", "removertag", "deltag",
                    "setmedal", "addmedal", "remmedal", "setgolds" -> true;
            default -> false;
        };
    }

    private static boolean canStaffAccount(BukkitCommandContext ctx) {
        if (ctx.getSender().hasPermission("game.admin")
                || ctx.getSender().hasPermission("lobbyhost.admin")) {
            return true;
        }
        if (!ctx.isPlayer()) return false;
        Account account = Core.getAccountController().of(ctx.player().getUniqueId());
        return account != null
                && (account.hasPermission("game.admin") || account.hasPermission("lobbyhost.admin"));
    }

    private void dispatchStaff(BukkitCommandContext ctx, String sub) {
        String[] args = ctx.getArgs();
        switch (sub) {
            case "info" -> {
                if (args.length < 2) {
                    ctx.reply("§cUso: /account info <jogador>");
                    return;
                }
                mutate(ctx, args[1], a -> printProfile(ctx, a));
            }
            case "help", "ajuda" -> sendStaffHelp(ctx);
            case "setrank" -> {
                if (args.length < 3) {
                    ctx.reply("§cUso: /account setrank <jogador> <RANK> [TEMPO]");
                    ctx.reply("§7Ex.: /account setrank zrwfaWASD VIP 30d");
                    ctx.reply("§7Sem TEMPO = permanente | Tempo: 7d, 30d, 12h, perm");
                    return;
                }
                var typeOpt = RankGrantService.parseRank(args[2]);
                if (typeOpt.isEmpty()) {
                    ctx.reply("§cRank invalido: §e" + args[2]);
                    return;
                }
                RankType type = typeOpt.get();
                String tempo = args.length >= 4 ? args[3] : null;
                mutate(ctx, args[1], a -> {
                    if (tempo == null || tempo.isBlank()) {
                        RankGrantService.grantPermanent(a, type);
                        ctx.reply("§aRank de §f" + a.getName() + " §adefinido para §6" + type + " §7(permanente)§a.");
                    } else {
                        try {
                            long durationMs = RankGrantService.parseDurationMs(tempo);
                            RankGrantService.grantTemporary(a, type, durationMs);
                            ctx.reply("§aRank temporario §6" + type + " §apor §f"
                                    + RankGrantService.formatDuration(durationMs)
                                    + " §adado a §f" + a.getName() + "§a.");
                        } catch (Exception e) {
                            ctx.reply("§cTempo invalido: §e" + tempo);
                            return;
                        }
                    }
                }, type);
            }
            case "remrank", "removerrank", "delrank" -> {
                if (args.length < 2) {
                    ctx.reply("§cUso: /account remrank <jogador> [RANK]");
                    ctx.reply("§7Sem RANK = remove todos e volta para MEMBER");
                    ctx.reply("§7Ex.: /account remrank zrwfaWASD VIP");
                    return;
                }
                RankType removeType = null;
                if (args.length >= 3) {
                    var typeOpt = RankGrantService.parseRank(args[2]);
                    if (typeOpt.isEmpty()) {
                        ctx.reply("§cRank invalido: §e" + args[2]);
                        return;
                    }
                    removeType = typeOpt.get();
                }
                RankType finalRemoveType = removeType;
                mutate(ctx, args[1], a -> {
                    var result = RankGrantService.removeRank(a, finalRemoveType);
                    if (!result.success()) {
                        ctx.reply("§c" + result.message());
                        return;
                    }
                    ctx.reply("§a" + result.message());
                    RankGrantService.notifyRankRemoved(a, result.removedType());
                });
            }
            case "setpackage" -> runPackage(ctx, args, true);
            case "rempackage" -> runPackage(ctx, args, false);
            case "packages" -> {
                ctx.reply("§6§lPacotes disponiveis:");
                for (AccountPackage pkg : AccountPackages.all()) {
                    ctx.reply("§e" + pkg.getId() + " §7- §f" + pkg.getDisplayName()
                            + " §8| " + pkg.describeContents());
                }
            }
            case "settag" -> {
                if (args.length < 3) {
                    ctx.reply("§cUso: /account settag <jogador> <TAG>");
                    return;
                }
                try {
                    Tag tag = Tag.valueOf(args[2].toUpperCase(Locale.ROOT));
                    mutate(ctx, args[1], a -> {
                        a.setTag(tag);
                        ctx.reply("§aTag de §f" + a.getName() + " §a-> " + tag.getColor() + tag.name());
                    });
                } catch (Exception e) {
                    ctx.reply("§cTag invalida.");
                }
            }
            case "remtag", "removertag", "deltag" -> {
                if (args.length < 2) {
                    ctx.reply("§cUso: /account remtag <jogador> [TAG]");
                    ctx.reply("§7Sem TAG = volta para a tag padrao do rank atual");
                    return;
                }
                Tag removeTag = null;
                if (args.length >= 3) {
                    removeTag = Tag.of(args[2]);
                    if (removeTag == null) {
                        try {
                            removeTag = Tag.valueOf(args[2].toUpperCase(Locale.ROOT));
                        } catch (Exception e) {
                            ctx.reply("§cTag invalida: §e" + args[2]);
                            return;
                        }
                    }
                }
                Tag finalRemoveTag = removeTag;
                mutate(ctx, args[1], a -> {
                    Tag current = a.getTag() != null ? a.getTag() : Tag.MEMBER;
                    if (finalRemoveTag != null && current != finalRemoveTag) {
                        ctx.reply("§c" + a.getName() + " nao possui a tag "
                                + finalRemoveTag.getColor() + finalRemoveTag.name() + "§c.");
                        return;
                    }
                    Tag defaultTag = Tag.of(a.getRankType());
                    a.setTag(defaultTag);
                    ctx.reply("§aTag de §f" + a.getName() + " §aremovida. Nova tag: "
                            + defaultTag.getColor() + defaultTag.getDisplayName());
                });
            }
            case "setmedal" -> {
                if (args.length < 3) {
                    ctx.reply("§cUso: /account setmedal <jogador> <MEDAL>");
                    return;
                }
                try {
                    Medal medal = Medal.valueOf(args[2].toUpperCase(Locale.ROOT));
                    mutate(ctx, args[1], a -> {
                        a.setMedal(medal);
                        ctx.reply("§aMedalha de §f" + a.getName() + " §a-> §6" + medal);
                    });
                } catch (Exception e) {
                    ctx.reply("§cMedalha invalida.");
                }
            }
            case "setgolds" -> {
                if (args.length < 3) {
                    ctx.reply("§cUso: /account setgolds <jogador> <quantia>");
                    return;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    mutate(ctx, args[1], a -> {
                        a.setGolds(amount);
                        ctx.reply("§aGolds de §f" + a.getName() + " §a-> §6" + amount);
                    });
                } catch (NumberFormatException e) {
                    ctx.reply("§cQuantidade invalida.");
                }
            }
            default -> ctx.reply("§6/account §7info|setrank|remrank|setpackage|rempackage|packages|settag|remtag|setmedal|setgolds");
        }
    }

    private void runPackage(BukkitCommandContext ctx, String[] args, boolean apply) {
        String usage = apply ? "setpackage" : "rempackage";
        if (args.length < 3) {
            ctx.reply("§cUso: /account " + usage + " <jogador> <pacote>");
            ctx.reply("§7Pacotes: §e" + AccountPackages.idsJoined());
            ctx.reply("§7Use §e/account packages §7para ver detalhes.");
            return;
        }
        var pkgOpt = AccountPackages.find(args[2]);
        if (pkgOpt.isEmpty()) {
            ctx.reply("§cPacote invalido: §e" + args[2]);
            ctx.reply("§7Pacotes: §e" + AccountPackages.idsJoined());
            return;
        }
        AccountPackage pkg = pkgOpt.get();
        mutate(ctx, args[1], a -> {
            var result = apply
                    ? AccountPackageService.apply(a, pkg)
                    : AccountPackageService.remove(a, pkg);
            if (!result.success()) {
                ctx.reply("§c" + result.message());
                return;
            }
            ctx.reply("§a" + result.message());
            if (apply) {
                AccountPackageService.notifyApplied(a, pkg);
            } else {
                AccountPackageService.notifyRemoved(a, pkg);
            }
        });
    }

    private void showProfileByName(BukkitCommandContext ctx, String name) {
        Account online = resolveOnlineAccount(name);
        if (online != null) {
            printProfile(ctx, online);
            return;
        }
        ctx.reply("§7Carregando conta...");
        GamePlugin plugin = GamePlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Optional<Account> opt = Core.getAccountData().loadByName(name);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (opt.isEmpty()) {
                    ctx.reply("§cConta §e" + name + " §cnao encontrada no banco.");
                    ctx.reply("§7O jogador precisa entrar no servidor pelo menos uma vez.");
                    return;
                }
                printProfile(ctx, opt.get());
            });
        });
    }

    private void mutate(BukkitCommandContext ctx, String playerName, Consumer<Account> action) {
        mutate(ctx, playerName, action, null);
    }

    private void mutate(BukkitCommandContext ctx, String playerName, Consumer<Account> action, RankType notifyRank) {
        Account online = resolveOnlineAccount(playerName);
        if (online != null) {
            action.accept(online);
            RankGrantService.refreshOnline(online);
            AccountSync.saveAndBroadcast(online);
            if (notifyRank != null) {
                RankGrantService.notifyRankReceived(online, notifyRank);
            }
            return;
        }

        GamePlugin plugin = GamePlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Optional<Account> opt = Core.getAccountData().loadByName(playerName);
            Account account = opt.orElseGet(() -> {
                OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
                return new Account(op.getUniqueId(), playerName);
            });
            Bukkit.getScheduler().runTask(plugin, () -> {
                action.accept(account);
                Account cached = Core.getAccountController().of(account.getId());
                if (cached != null) {
                    cached.replaceContext(account.getContext());
                    cached.clearDirty();
                    RankGrantService.refreshOnline(cached);
                    if (notifyRank != null) {
                        RankGrantService.notifyRankReceived(cached, notifyRank);
                    }
                } else if (notifyRank != null) {
                    Player p = Bukkit.getPlayer(account.getId());
                    if (p != null && p.isOnline()) {
                        Account live = Core.getAccountController().of(p.getUniqueId());
                        if (live != null) {
                            RankGrantService.refreshOnline(live);
                            RankGrantService.notifyRankReceived(live, notifyRank);
                        }
                    }
                }
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                        AccountSync.saveAndBroadcast(account));
            });
        });
    }

    private static void sendStaffHelp(BukkitCommandContext ctx) {
        ctx.reply("§6§l/account §7(staff)");
        ctx.reply("§f/account info <nick> §7- perfil");
        ctx.reply("§f/account setrank <nick> <RANK> [tempo] §7- rank permanente/temporario");
        ctx.reply("§f/account remrank <nick> [RANK] §7- remove rank");
        ctx.reply("§f/account setpackage|rempackage <nick> <pacote> §7- pacotes");
        ctx.reply("§f/account packages §7- lista pacotes");
        ctx.reply("§f/account settag|remtag <nick> [TAG] §7- tag");
        ctx.reply("§f/account setmedal|setgolds §7- medalha / money");
        ctx.reply("§7Pacotes: §e" + AccountPackages.idsJoined());
    }

    private static Account resolveOnlineAccount(String name) {
        Player exact = Bukkit.getPlayerExact(name);
        if (exact != null) {
            return Core.getAccountController().of(exact.getUniqueId());
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return Core.getAccountController().of(player.getUniqueId());
            }
        }
        return null;
    }

    private void printProfile(BukkitCommandContext ctx, Account target) {
        Tag tag = target.getTag() != null ? target.getTag() : Tag.MEMBER;
        Medal medal = target.getMedal() != null ? target.getMedal() : Medal.NONE;

        ctx.reply("§6§l━━━━━ §eConta: §f" + target.getName() + " §6§l━━━━━");
        ctx.reply("§eUUID: §f" + target.getId());
        String expiry = target.getRankExpiryLabel();
        ctx.reply("§eRank: §f" + target.getRankType()
                + (expiry.isBlank() ? "" : " §7(" + expiry + ")"));
        ctx.reply("§eTag: " + tag.getColor() + tag.getDisplayName());
        ctx.reply("§eMedalha: §f" + medal.name() + " " + medal.getSymbol());
        ctx.reply("§eGolds: §6" + SemiAnarchyConfig.currencySymbol() + target.getGolds()
                + "§7  Level: §f" + target.getLevel());
        var stats = target.getGameStats();
        ctx.reply("§ePvP: §a" + stats.getKills() + " kills §7| §c" + stats.getDeaths()
                + " deaths §7| §e" + String.format("%.2f", target.getKdr()) + " KDR");
        ctx.reply("§eStreak: §f" + stats.getKillStreak() + " §7(recorde §f"
                + stats.getBestKillStreak() + "§7)");
        if (stats.getBounty() > 0) {
            ctx.reply("§eBounty: §a" + SemiAnarchyConfig.currencySymbol() + stats.getBounty());
        }
        ctx.reply("§eHomes: §f" + target.getHomes().getHomes().size());
        ctx.reply("§eIP: §f" + target.getIpAddress());
        ctx.reply("§ePremium: §f" + target.isPremium());
        ctx.reply("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Completer(name = "account", subCommands = {"perfil", "conta", "a"})
    public List<String> complete(BukkitCommandContext ctx) {
        List<String> out = new ArrayList<>();
        if (ctx.getArgs().length == 1) {
            String p = ctx.getArgs()[0].toLowerCase(Locale.ROOT);
            for (String sub : List.of("info", "help", "setrank", "remrank", "setpackage", "rempackage", "packages", "settag", "remtag", "setmedal", "setgolds")) {
                if (sub.startsWith(p)) out.add(sub);
            }
            for (Account account : Core.getAccountController().list()) {
                if (account.getName().toLowerCase(Locale.ROOT).startsWith(p)) {
                    out.add(account.getName());
                }
            }
        } else if (ctx.getArgs().length == 2 && "setrank".equalsIgnoreCase(ctx.getArgs()[0])) {
            String p = ctx.getArgs()[1].toLowerCase(Locale.ROOT);
            for (Account account : Core.getAccountController().list()) {
                if (account.getName().toLowerCase(Locale.ROOT).startsWith(p)) {
                    out.add(account.getName());
                }
            }
        } else if (ctx.getArgs().length == 3 && "setrank".equalsIgnoreCase(ctx.getArgs()[0])) {
            String p = ctx.getArgs()[2].toUpperCase(Locale.ROOT);
            for (RankType rt : RankType.values()) {
                if (rt.name().startsWith(p)) out.add(rt.name());
            }
        } else if (ctx.getArgs().length == 4 && "setrank".equalsIgnoreCase(ctx.getArgs()[0])) {
            out.addAll(com.minecraft.game.util.TabCompleteUtil.filter(
                    ctx.getArgs()[3], com.minecraft.game.util.TabCompleteUtil.punishmentTimes()));
        } else if (ctx.getArgs().length == 2 && isRemRankSub(ctx.getArgs()[0])) {
            String p = ctx.getArgs()[1].toLowerCase(Locale.ROOT);
            for (Account account : Core.getAccountController().list()) {
                if (account.getName().toLowerCase(Locale.ROOT).startsWith(p)) {
                    out.add(account.getName());
                }
            }
        } else if (ctx.getArgs().length == 3 && isRemRankSub(ctx.getArgs()[0])) {
            String p = ctx.getArgs()[2].toUpperCase(Locale.ROOT);
            for (RankType rt : RankType.values()) {
                if (rt.name().startsWith(p)) out.add(rt.name());
            }
        } else if (ctx.getArgs().length == 2 && isPackageSub(ctx.getArgs()[0])) {
            String p = ctx.getArgs()[1].toLowerCase(Locale.ROOT);
            for (Account account : Core.getAccountController().list()) {
                if (account.getName().toLowerCase(Locale.ROOT).startsWith(p)) {
                    out.add(account.getName());
                }
            }
        } else if (ctx.getArgs().length == 3 && isPackageSub(ctx.getArgs()[0])) {
            String p = ctx.getArgs()[2].toLowerCase(Locale.ROOT);
            for (AccountPackage pkg : AccountPackages.all()) {
                if (pkg.getId().startsWith(p)) out.add(pkg.getId());
            }
        } else if (ctx.getArgs().length == 2 && isRemTagSub(ctx.getArgs()[0])) {
            String p = ctx.getArgs()[1].toLowerCase(Locale.ROOT);
            for (Account account : Core.getAccountController().list()) {
                if (account.getName().toLowerCase(Locale.ROOT).startsWith(p)) {
                    out.add(account.getName());
                }
            }
        } else if (ctx.getArgs().length == 3 && isRemTagSub(ctx.getArgs()[0])) {
            String p = ctx.getArgs()[2].toUpperCase(Locale.ROOT);
            for (Tag tag : Tag.values()) {
                if (tag.name().startsWith(p)) out.add(tag.name());
            }
        }
        return out;
    }

    private static boolean isPackageSub(String sub) {
        return "setpackage".equalsIgnoreCase(sub) || "rempackage".equalsIgnoreCase(sub);
    }

    private static boolean isRemRankSub(String sub) {
        return "remrank".equalsIgnoreCase(sub)
                || "removerrank".equalsIgnoreCase(sub)
                || "delrank".equalsIgnoreCase(sub);
    }

    private static boolean isRemTagSub(String sub) {
        return "remtag".equalsIgnoreCase(sub)
                || "removertag".equalsIgnoreCase(sub)
                || "deltag".equalsIgnoreCase(sub);
    }
}
