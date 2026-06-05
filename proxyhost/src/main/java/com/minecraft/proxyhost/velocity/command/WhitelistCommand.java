package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import com.minecraft.proxyhost.whitelist.WhitelistManager;
import com.minecraft.proxyhost.whitelist.WhitelistRule;
import com.minecraft.proxyhost.whitelist.WhitelistTarget;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Whitelist por alvo: {@code /wl <proxy|lobby|game|eventos> <on|off|rank|jogador> ...}
 */
public final class WhitelistCommand implements SimpleCommand {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public void execute(Invocation invocation) {
        CommandSource src = invocation.source();
        String[] args = invocation.arguments();

        if (!canManage(src)) {
            src.sendMessage(legacy("&cSem permissão. Requer rank Admin+ ou proxyhost.whitelist."));
            return;
        }

        WhitelistManager wl = WhitelistManager.get();
        if (wl == null) {
            src.sendMessage(legacy("&cWhitelist não inicializada."));
            return;
        }

        if (args.length == 0) {
            sendStatus(src, wl, null);
            return;
        }

        WhitelistTarget target = WhitelistTarget.parse(args[0]);
        if (target == null) {
            src.sendMessage(legacy("&cAlvo inválido. Use: &fproxy&7, &flobby&7, &fgame &7ou &feventos"));
            return;
        }

        if (args.length == 1) {
            sendStatus(src, wl, target);
            return;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        WhitelistRule rule = wl.rule(target);

        switch (action) {
            case "on", "ativar", "enable" -> {
                rule.setEnabled(true);
                wl.save();
                publish();
                src.sendMessage(legacy("&aWhitelist &f" + target.displayName() + " &aativada."));
            }
            case "off", "desativar", "disable" -> {
                rule.setEnabled(false);
                wl.save();
                publish();
                src.sendMessage(legacy("&eWhitelist &f" + target.displayName() + " &edesativada."));
            }
            case "rank" -> handleRank(src, wl, target, rule, args);
            case "jogador", "player", "pl" -> handlePlayer(src, wl, target, rule, args);
            case "reload" -> {
                wl.reload();
                src.sendMessage(legacy("&awhitelist.yml recarregado."));
            }
            default -> sendHelp(src);
        }
    }

    private void handleRank(CommandSource src, WhitelistManager wl, WhitelistTarget target,
                            WhitelistRule rule, String[] args) {
        if (args.length == 2) {
            src.sendMessage(legacy("&7Rank mínimo: &f" + rule.getMinRank().name()));
            if (!rule.getExtraRanks().isEmpty()) {
                src.sendMessage(legacy("&7Ranks extras: &f" + joinRanks(rule)));
            }
            src.sendMessage(legacy("&7Uso: &f/wl " + targetKey(target) + " rank <RANK>"));
            src.sendMessage(legacy("&7     &f/wl " + targetKey(target) + " rank add|remove <RANK>"));
            return;
        }

        String sub = args[2].toLowerCase(Locale.ROOT);
        if (sub.equals("add") || sub.equals("remove") || sub.equals("rem")) {
            if (args.length < 4) {
                src.sendMessage(legacy("&cUso: /wl " + targetKey(target) + " rank add|remove <RANK>"));
                return;
            }
            RankType rank = parseRank(args[3]);
            if (rank == null) {
                src.sendMessage(legacy("&cRank inválido: &f" + args[3]));
                return;
            }
            if (sub.equals("add")) {
                rule.getExtraRanks().add(rank);
                wl.save();
                publish();
                src.sendMessage(legacy("&aRank &f" + rank.name() + " &aadicionado à whitelist de &f"
                        + target.displayName() + "&a."));
            } else {
                rule.getExtraRanks().remove(rank);
                wl.save();
                publish();
                src.sendMessage(legacy("&eRank &f" + rank.name() + " &eremovido da whitelist de &f"
                        + target.displayName() + "&e."));
            }
            return;
        }

        RankType rank = parseRank(args[2]);
        if (rank == null) {
            src.sendMessage(legacy("&cRank inválido: &f" + args[2]));
            return;
        }
        rule.setMinRank(rank);
        wl.save();
        publish();
        src.sendMessage(legacy("&aRank mínimo de &f" + target.displayName() + " &adefinido para &f"
                + rank.name() + "&a."));
    }

    private void handlePlayer(CommandSource src, WhitelistManager wl, WhitelistTarget target,
                              WhitelistRule rule, String[] args) {
        if (args.length == 2) {
            src.sendMessage(legacy("&7Jogadores com bypass (&f" + rule.getBypassPlayers().size() + "&7):"));
            if (rule.getBypassPlayers().isEmpty()) {
                src.sendMessage(legacy("&8(nenhum)"));
            } else {
                rule.getBypassPlayers().forEach((uuid, name) ->
                        src.sendMessage(legacy("&8- &f" + name + " &7(" + uuid + ")")));
            }
            src.sendMessage(legacy("&7Uso: &f/wl " + targetKey(target) + " jogador add <nick>"));
            src.sendMessage(legacy("&7     &f/wl " + targetKey(target) + " jogador remove <nick>"));
            return;
        }

        String sub = args[2].toLowerCase(Locale.ROOT);
        boolean remove = sub.equals("remove") || sub.equals("rem") || sub.equals("del");
        String nick;
        if (remove || sub.equals("add")) {
            if (args.length < 4) {
                src.sendMessage(legacy("&cUso: /wl " + targetKey(target) + " jogador add|remove <nick>"));
                return;
            }
            nick = args[3];
        } else {
            nick = args[2];
            remove = false;
        }

        boolean removing = remove;
        resolveUuid(nick).thenAccept(opt -> {
            if (opt.isEmpty()) {
                src.sendMessage(legacy("&cJogador não encontrado: &f" + nick));
                return;
            }
            UUID uuid = opt.get();
            String resolvedName = resolveName(uuid, nick);

            if (removing) {
                rule.getBypassPlayers().remove(uuid);
                wl.save();
                publish();
                src.sendMessage(legacy("&eJogador &f" + resolvedName + " &eremovido do bypass de &f"
                        + target.displayName() + "&e."));
            } else {
                rule.getBypassPlayers().put(uuid, resolvedName);
                wl.save();
                publish();
                src.sendMessage(legacy("&aJogador &f" + resolvedName + " &aadicionado ao bypass de &f"
                        + target.displayName() + "&a."));
            }
        });
    }

    private CompletableFuture<Optional<UUID>> resolveUuid(String nick) {
        Optional<Player> online = Core.getProxy().getPlayer(nick);
        if (online.isPresent()) {
            return CompletableFuture.completedFuture(Optional.of(online.get().getUniqueId()));
        }
        return CompletableFuture.supplyAsync(() ->
                Core.getAccountData().loadByName(nick).map(Account::getId));
    }

    private String resolveName(UUID uuid, String fallback) {
        return Core.getProxy().getPlayer(uuid)
                .map(Player::getUsername)
                .orElseGet(() -> Core.getAccountController().of(uuid) != null
                        ? Core.getAccountController().of(uuid).getName()
                        : fallback);
    }

    private void sendStatus(CommandSource src, WhitelistManager wl, WhitelistTarget only) {
        src.sendMessage(legacy(" "));
        src.sendMessage(legacy("&6&lWHITELIST"));
        Iterable<WhitelistTarget> targets = only != null
                ? List.of(only)
                : List.of(WhitelistTarget.values());

        for (WhitelistTarget target : targets) {
            WhitelistRule rule = wl.rule(target);
            String state = rule.isEnabled() ? "&cATIVA" : "&aINATIVA";
            src.sendMessage(legacy("&7" + target.displayName() + ": " + state
                    + " &8| &7rank >= &f" + rule.getMinRank().name()
                    + " &8| &7bypass: &f" + rule.getBypassPlayers().size()
                    + " &8| &7extras: &f" + rule.getExtraRanks().size()));
        }
        src.sendMessage(legacy(" "));
        sendHelp(src);
    }

    private void sendHelp(CommandSource src) {
        src.sendMessage(legacy("&6/wl &7- status geral"));
        src.sendMessage(legacy("&6/wl <proxy|lobby|game|eventos> &7- status do alvo"));
        src.sendMessage(legacy("&6/wl <alvo> on|off &7- ativar/desativar"));
        src.sendMessage(legacy("&6/wl <alvo> rank <RANK> &7- rank mínimo"));
        src.sendMessage(legacy("&6/wl <alvo> rank add|remove <RANK> &7- ranks extras"));
        src.sendMessage(legacy("&6/wl <alvo> jogador add|remove <nick> &7- bypass de jogador"));
    }

    private boolean canManage(CommandSource src) {
        if (src.hasPermission("proxyhost.whitelist")) return true;
        if (!(src instanceof Player player)) return true;
        Account a = Core.getAccountController().of(player.getUniqueId());
        return a != null && a.getRankType().isAtLeast(RankType.ADMIN);
    }

    private void publish() {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) return;
        Core.getRedis().publish(com.minecraft.proxyhost.Constant.REDIS_PROXY_SETTINGS_CHANNEL, "reload");
    }

    private static RankType parseRank(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return RankType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return null;
        }
    }

    private static String joinRanks(WhitelistRule rule) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (RankType rank : rule.getExtraRanks()) {
            if (i++ > 0) sb.append(", ");
            sb.append(rank.name());
        }
        return sb.toString();
    }

    private static String targetKey(WhitelistTarget target) {
        return switch (target) {
            case PROXY -> "proxy";
            case LOBBY -> "lobby";
            case GAME -> "game";
            case EVENTOS -> "eventos";
        };
    }

    private static String join(String[] args, int from) {
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }

    private Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            return filter(List.of("proxy", "lobby", "game", "eventos"), "");
        }
        if (args.length == 1) {
            return filter(List.of("proxy", "lobby", "game", "eventos"), args[0]);
        }
        if (args.length == 2) {
            return filter(List.of("on", "off", "rank", "jogador", "reload"), args[1]);
        }
        if (args.length == 3 && "rank".equalsIgnoreCase(args[1])) {
            List<String> opts = new ArrayList<>();
            opts.add("add");
            opts.add("remove");
            Stream.of(RankType.values()).map(Enum::name).forEach(opts::add);
            return filter(opts, args[2]);
        }
        if (args.length == 3 && isPlayerAction(args[1])) {
            return filter(List.of("add", "remove"), args[2]);
        }
        if (args.length == 4 && "rank".equalsIgnoreCase(args[1])) {
            return filter(Arrays.stream(RankType.values()).map(Enum::name).toList(), args[3]);
        }
        if (args.length >= 3 && isPlayerAction(args[1])) {
            String partial = args.length >= 4 ? args[3].toLowerCase(Locale.ROOT) : "";
            return Core.getProxy().getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(partial))
                    .toList();
        }
        return List.of();
    }

    private static boolean isPlayerAction(String action) {
        String a = action.toLowerCase(Locale.ROOT);
        return a.equals("jogador") || a.equals("player") || a.equals("pl");
    }

    private static List<String> filter(List<String> options, String partial) {
        String p = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(p)).toList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
