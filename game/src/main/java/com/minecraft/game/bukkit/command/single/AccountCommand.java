package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.medal.Medal;
import com.minecraft.game.account.context.objects.rank.Rank;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.account.context.objects.tag.Tag;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.display.AboveNameDisplay;
import com.minecraft.game.bukkit.manager.TagManager;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import com.minecraft.game.bukkit.skin.SkinService;
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
            usage = "/account [jogador] | /account setrank <nick> <RANK>")
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
            if (!ctx.getSender().hasPermission("game.admin")
                    && !ctx.getSender().hasPermission("lobbyhost.admin")) {
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
            case "info", "setrank", "settag", "setmedal", "addmedal", "remmedal", "setgolds" -> true;
            default -> false;
        };
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
            case "setrank" -> {
                if (args.length < 3) {
                    ctx.reply("§cUso: /account setrank <jogador> <RANK>");
                    ctx.reply("§7Ex.: /account setrank zrwfaWASD OWNER");
                    return;
                }
                RankType type;
                try {
                    type = RankType.valueOf(args[2].toUpperCase(Locale.ROOT));
                } catch (Exception e) {
                    ctx.reply("§cRank invalido: §e" + args[2]);
                    return;
                }
                mutate(ctx, args[1], a -> {
                    a.setRank(Rank.builder().type(type).build());
                    ctx.reply("§aRank de §f" + a.getName() + " §adefinido para §6" + type + "§a.");
                });
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
            default -> ctx.reply("§6/account §7info|setrank|settag|setmedal|setgolds");
        }
    }

    private void showProfileByName(BukkitCommandContext ctx, String name) {
        Account online = onlineAccount(name);
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
        Account online = onlineAccount(playerName);
        if (online != null) {
            action.accept(online);
            refreshDisplay(online);
            AccountSync.saveAndBroadcast(online);
            return;
        }

        GamePlugin plugin = GamePlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Optional<Account> opt = Core.getAccountData().loadByName(playerName);
            Account account = opt.orElseGet(() -> {
                OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
                return new Account(op.getUniqueId(), playerName);
            });
            action.accept(account);
            Core.getAccountData().save(account);
            AccountSync.broadcastUpdate(account);
            Bukkit.getScheduler().runTask(plugin, () -> {
                Account cached = Core.getAccountController().of(account.getId());
                if (cached != null) {
                    cached.replaceContext(account.getContext());
                    cached.clearDirty();
                    refreshDisplay(cached);
                }
                Player p = Bukkit.getPlayer(account.getId());
                if (p != null && p.isOnline()) {
                    ctx.reply("§7Display atualizado no servidor.");
                }
            });
        });
    }

    private static Account onlineAccount(String name) {
        Player p = Bukkit.getPlayerExact(name);
        if (p == null) return null;
        return Core.getAccountController().of(p.getUniqueId());
    }

    private static void refreshDisplay(Account account) {
        Player p = Bukkit.getPlayer(account.getId());
        if (p == null || !p.isOnline()) return;
        TagManager.updateTag(account);
        TagManager.updateAllForViewer(p);
        AboveNameDisplay.refresh(p);
        SkinService.refresh(account, p);
    }

    private void printProfile(BukkitCommandContext ctx, Account target) {
        Tag tag = target.getTag() != null ? target.getTag() : Tag.MEMBER;
        Medal medal = target.getMedal() != null ? target.getMedal() : Medal.NONE;

        ctx.reply("§6§l━━━━━ §eConta: §f" + target.getName() + " §6§l━━━━━");
        ctx.reply("§eUUID: §f" + target.getId());
        ctx.reply("§eRank: §f" + target.getRankType());
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

    @Completer(name = "account")
    public List<String> complete(BukkitCommandContext ctx) {
        List<String> out = new ArrayList<>();
        if (ctx.getArgs().length == 1) {
            String p = ctx.getArgs()[0].toLowerCase(Locale.ROOT);
            for (String sub : List.of("info", "setrank", "settag", "setmedal", "setgolds")) {
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
        }
        return out;
    }
}
