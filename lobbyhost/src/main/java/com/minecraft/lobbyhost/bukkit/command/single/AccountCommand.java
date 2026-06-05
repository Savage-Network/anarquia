package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.medal.Medal;
import com.minecraft.lobbyhost.account.context.objects.rank.Rank;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.account.context.objects.tag.Tag;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.bukkit.display.AboveNameDisplay;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyScoreboard;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.bukkit.skin.SkinService;
import com.minecraft.lobbyhost.util.AccountSync;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Alinhado ao {@code game}: perfil + staff ({@code /account setrank ...}).
 * Mesma tabela {@code accounts} (MySQL/SQLite compartilhado).
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
            if (!canStaffAccount(ctx)) {
                ctx.reply("§cSem permissao (lobbyhost.admin).");
                return;
            }
            dispatchStaff(ctx, sub);
            return;
        }

        showProfileByName(ctx, ctx.getArgs()[0]);
    }

    private static boolean isStaffSub(String sub) {
        return switch (sub) {
            case "info", "setrank", "settag", "setmedal", "addmedal", "remmedal", "setgolds", "setcash" -> true;
            default -> false;
        };
    }

    private static boolean canStaffAccount(BukkitCommandContext ctx) {
        if (ctx.getSender().hasPermission("lobbyhost.admin")
                || ctx.getSender().hasPermission("game.admin")) {
            return true;
        }
        if (!ctx.isPlayer()) return false;
        Account account = Core.getAccountController().of(ctx.player().getUniqueId());
        return account != null
                && (account.hasPermission("lobbyhost.admin") || account.hasPermission("game.admin"));
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
            case "addmedal" -> {
                if (args.length < 3) {
                    ctx.reply("§cUso: /account addmedal <jogador> <MEDAL>");
                    return;
                }
                try {
                    Medal medal = Medal.valueOf(args[2].toUpperCase(Locale.ROOT));
                    mutate(ctx, args[1], a -> {
                        a.addMedal(medal);
                        ctx.reply("§aMedalha §6" + medal + " §aadicionada a §f" + a.getName());
                    });
                } catch (Exception e) {
                    ctx.reply("§cMedalha invalida.");
                }
            }
            case "remmedal" -> {
                if (args.length < 3) {
                    ctx.reply("§cUso: /account remmedal <jogador> <MEDAL>");
                    return;
                }
                try {
                    Medal medal = Medal.valueOf(args[2].toUpperCase(Locale.ROOT));
                    mutate(ctx, args[1], a -> {
                        a.removeMedal(medal);
                        ctx.reply("§aMedalha §6" + medal + " §aremovida de §f" + a.getName());
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
                        ctx.reply("§aMoney (game) de §f" + a.getName() + " §a-> §6" + amount);
                    });
                } catch (NumberFormatException e) {
                    ctx.reply("§cQuantidade invalida.");
                }
            }
            case "setcash" -> {
                if (args.length < 3) {
                    ctx.reply("§cUso: /account setcash <jogador> <quantia>");
                    return;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    mutate(ctx, args[1], a -> {
                        a.setCash(amount);
                        ctx.reply("§aCash de §f" + a.getName() + " §a-> §b" + LobbySettings.CASH_SYMBOL + amount);
                    });
                } catch (NumberFormatException e) {
                    ctx.reply("§cQuantidade invalida.");
                }
            }
            default -> ctx.reply("§6/account §7info|setrank|settag|setmedal|setcash|setgolds");
        }
    }

    private void showProfileByName(BukkitCommandContext ctx, String name) {
        Account online = onlineAccount(name);
        if (online != null) {
            printProfile(ctx, online);
            return;
        }
        ctx.reply("§7Carregando conta...");
        LobbyHostPlugin plugin = LobbyHostPlugin.getInstance();
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
            refreshLobbyDisplay(online);
            AccountSync.saveAndBroadcast(online);
            return;
        }

        LobbyHostPlugin plugin = LobbyHostPlugin.getInstance();
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
                    refreshLobbyDisplay(cached);
                }
                Player p = Bukkit.getPlayer(account.getId());
                if (p != null && p.isOnline()) {
                    ctx.reply("§7Display do lobby atualizado.");
                }
            });
        });
    }

    private static void refreshLobbyDisplay(Account account) {
        Player p = Bukkit.getPlayer(account.getId());
        if (p == null || !p.isOnline()) return;
        TagManager.updateTag(account);
        TagManager.updateAllForViewer(p);
        AboveNameDisplay.refresh(p);
        SkinService.refresh(account, p);
        LobbyScoreboard.refreshCash(p);
    }

    private static Account onlineAccount(String name) {
        Player p = Bukkit.getPlayerExact(name);
        if (p == null) return null;
        return Core.getAccountController().of(p.getUniqueId());
    }

    private void printProfile(BukkitCommandContext ctx, Account target) {
        Tag tag = target.getTag() != null ? target.getTag() : Tag.MEMBER;
        Medal medal = target.getMedal() != null ? target.getMedal() : Medal.NONE;

        ctx.reply("§6§l━━━━━ §eConta: §f" + target.getName() + " §6§l━━━━━");
        ctx.reply("§eUUID: §f" + target.getId());
        ctx.reply("§eRank: §f" + target.getRankType());
        ctx.reply("§eTag: " + tag.getColor() + tag.getDisplayName());
        ctx.reply("§eMedalha: §f" + medal.name() + " " + medal.getSymbol());
        ctx.reply("§eCash: §b" + LobbySettings.CASH_SYMBOL + target.getCash()
                + "§7  Money (game): §6" + target.getGolds()
                + "§7  Level: §f" + target.getLevel());
        ctx.reply("§eIP: §f" + target.getIpAddress());
        ctx.reply("§ePremium: §f" + target.isPremium());
        ctx.reply("§eCosmeticos ativos: §f" + target.getActiveCollectibles().size());
        ctx.reply("§eFavoritos: §f" + target.getFavoriteCollectibles().size());
        ctx.reply("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Completer(name = "account", subCommands = {"perfil", "conta", "a"})
    public List<String> complete(BukkitCommandContext ctx) {
        List<String> out = new ArrayList<>();
        if (ctx.getArgs().length == 1) {
            String p = ctx.getArgs()[0].toLowerCase(Locale.ROOT);
            for (String sub : List.of("info", "setrank", "settag", "setmedal", "setgolds", "setcash")) {
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
