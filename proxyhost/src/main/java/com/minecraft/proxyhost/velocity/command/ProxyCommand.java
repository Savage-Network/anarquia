package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import com.minecraft.proxyhost.proxy.MaintenanceMode;
import com.minecraft.proxyhost.proxy.MaintenanceScheduler;
import com.minecraft.proxyhost.proxy.NetworkPublisher;
import com.minecraft.proxyhost.proxy.ProxySettingsManager;
import com.minecraft.proxyhost.network.BroadcastService;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Administracao do proxy: MOTD, manutencao avancada, servidores e alertas.
 */
public final class ProxyCommand implements SimpleCommand {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public void execute(Invocation invocation) {
        CommandSource src = invocation.source();
        String[] args = invocation.arguments();

        if (!canManage(src)) {
            src.sendMessage(legacy("&cSem permissao. Requer rank Admin+ ou proxyhost.proxy."));
            return;
        }

        if (args.length == 0) {
            sendHelp(src);
            return;
        }

        ProxySettingsManager s = ProxySettingsManager.get();
        if (s == null) {
            src.sendMessage(legacy("&cSettings nao inicializado."));
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help", "ajuda" -> sendHelp(src);
            case "status", "info" -> status(src, s);
            case "servers", "servidores" -> servers(src);
            case "reload" -> {
                s.reload();
                src.sendMessage(legacy("&aproxy-settings.yml recarregado."));
            }
            case "motd" -> motd(src, s, args);
            case "maintenance", "manutencao", "maint" -> maintenance(src, s, args);
            case "maxplayers", "slots" -> maxPlayers(src, s, args);
            case "alert", "anuncio" -> alert(src, args);
            case "broadcast", "bc" -> broadcast(src, args);
            case "kicknonstaff" -> {
                s.kickNonStaff(Core.getProxy());
                src.sendMessage(legacy("&eJogadores sem acesso foram desconectados."));
            }
            default -> sendHelp(src);
        }
    }

    private void motd(CommandSource src, ProxySettingsManager s, String[] args) {
        if (args.length < 2) {
            src.sendMessage(legacy("&6MOTD atual:"));
            src.sendMessage(legacy("&7L1: &f" + s.getMotdLine1()));
            src.sendMessage(legacy("&7L2: &f" + s.getMotdLine2()));
            src.sendMessage(legacy("&7Use &f/proxy motd line1 <texto>"));
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "line1", "l1" -> {
                if (args.length < 3) { src.sendMessage(legacy("&cUso: /proxy motd line1 <texto>")); return; }
                s.setMotdLine1(join(args, 2));
                src.sendMessage(legacy("&aLinha 1 do MOTD atualizada."));
            }
            case "line2", "l2" -> {
                if (args.length < 3) { src.sendMessage(legacy("&cUso: /proxy motd line2 <texto>")); return; }
                s.setMotdLine2(join(args, 2));
                src.sendMessage(legacy("&aLinha 2 do MOTD atualizada."));
            }
            case "reset" -> {
                s.resetMotd();
                src.sendMessage(legacy("&aMOTD restaurado ao padrao."));
            }
            case "preview", "ver" -> src.sendMessage(s.buildMotdDescription(Core.getProxy()));
            case "hide" -> {
                s.setHidePlayerSample(true);
                src.sendMessage(legacy("&aLista de jogadores no ping ocultada."));
            }
            case "show" -> {
                s.setHidePlayerSample(false);
                src.sendMessage(legacy("&aLista de jogadores no ping visivel."));
            }
            default -> src.sendMessage(legacy("&cSub: line1|line2|reset|preview|hide|show"));
        }
        publishSettings();
    }

    private void maintenance(CommandSource src, ProxySettingsManager s, String[] args) {
        if (args.length < 2) {
            sendMaintenanceStatus(src, s);
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "on", "enable", "ativar" -> {
                if (args.length >= 3) s.setMaintenanceReason(join(args, 2));
                s.setMaintenanceEnabled(true);
                s.kickNonStaff(Core.getProxy());
                broadcastStaff("&c[Proxy] &7Manutencao &cATIVADA &7(modo &f"
                        + s.getMaintenanceMode() + "&7, rank >= &f" + s.getMaintenanceMinRank() + "&7).");
                broadcastAll(legacy("&c&lMANUTENÇÃO &7— &f" + s.getMaintenanceReason()));
                src.sendMessage(legacy("&cManutencao ativada."));
            }
            case "off", "disable", "desativar" -> {
                s.setMaintenanceEnabled(false);
                MaintenanceScheduler.cancel(Core.getProxy(), false);
                broadcastStaff("&a[Proxy] &7Manutencao &adesativada&7.");
                broadcastAll(legacy("&a&lREDE ONLINE &7— bem-vindo de volta!"));
                src.sendMessage(legacy("&aManutencao desativada."));
            }
            case "toggle" -> {
                s.setMaintenanceEnabled(!s.isMaintenanceEnabled());
                if (s.isMaintenanceEnabled()) {
                    s.kickNonStaff(Core.getProxy());
                    broadcastStaff("&c[Proxy] &7Manutencao &cATIVADA&7.");
                    broadcastAll(legacy("&c&lMANUTENÇÃO &7— &f" + s.getMaintenanceReason()));
                } else {
                    MaintenanceScheduler.cancel(Core.getProxy(), false);
                    broadcastStaff("&a[Proxy] &7Manutencao &adesativada&7.");
                    broadcastAll(legacy("&a&lREDE ONLINE &7— bem-vindo de volta!"));
                }
                src.sendMessage(legacy("&7Manutencao: &f" + (s.isMaintenanceEnabled() ? "&cON" : "&aOFF")));
            }
            case "mode" -> {
                if (args.length < 3) {
                    src.sendMessage(legacy("&cUso: /proxy maintenance mode RANK|WHITELIST|FULL"));
                    return;
                }
                MaintenanceMode mode = MaintenanceMode.parse(args[2]);
                s.setMaintenanceMode(mode);
                src.sendMessage(legacy("&aModo de manutencao: &f" + mode.name()));
            }
            case "rank" -> {
                if (args.length < 3) { src.sendMessage(legacy("&cUso: /proxy maintenance rank <RANK>")); return; }
                try {
                    RankType rank = RankType.valueOf(args[2].toUpperCase(Locale.ROOT));
                    s.setMaintenanceMinRank(rank);
                    src.sendMessage(legacy("&aRank minimo na manutencao: &f" + rank.name()));
                } catch (IllegalArgumentException e) {
                    src.sendMessage(legacy("&cRank invalido."));
                }
            }
            case "reason", "motivo" -> {
                if (args.length < 3) { src.sendMessage(legacy("&cUso: /proxy maintenance reason <texto>")); return; }
                s.setMaintenanceReason(join(args, 2));
                src.sendMessage(legacy("&aMotivo: &f" + s.getMaintenanceReason()));
            }
            case "eta", "previsao" -> {
                if (args.length < 3) { src.sendMessage(legacy("&cUso: /proxy maintenance eta <texto|clear>")); return; }
                String eta = join(args, 2);
                if ("clear".equalsIgnoreCase(eta)) eta = "Em breve";
                s.setMaintenanceEstimatedEnd(eta);
                src.sendMessage(legacy("&aPrevisao: &f" + s.getMaintenanceEstimatedEnd()));
            }
            case "discord" -> {
                if (args.length < 3) { src.sendMessage(legacy("&cUso: /proxy maintenance discord <link>")); return; }
                s.setMaintenanceDiscord(join(args, 2));
                src.sendMessage(legacy("&aDiscord: &f" + s.getMaintenanceDiscord()));
            }
            case "whitelist", "wl" -> maintenanceWhitelist(src, s, args);
            case "msg", "message", "kick" -> {
                if (args.length < 3) { src.sendMessage(legacy("&cUso: /proxy maintenance msg <linha...>")); return; }
                s.setMaintenanceKickMessage(Arrays.asList(join(args, 2).split(";")));
                src.sendMessage(legacy("&aMensagem de kick atualizada (use ; para nova linha)."));
            }
            case "motd1" -> {
                if (args.length < 3) return;
                s.setMaintenanceMotdLine1(join(args, 2));
                src.sendMessage(legacy("&aMOTD manutencao linha 1 atualizada."));
            }
            case "motd2" -> {
                if (args.length < 3) return;
                s.setMaintenanceMotdLine2(join(args, 2));
                src.sendMessage(legacy("&aMOTD manutencao linha 2 atualizada."));
            }
            case "preview", "ver" -> src.sendMessage(s.maintenanceKickComponent());
            case "in", "agendar", "schedule" -> scheduleMaintenance(src, s, args);
            case "cancel", "cancelar" -> {
                if (!MaintenanceScheduler.isScheduled()) {
                    src.sendMessage(legacy("&cNenhuma manutencao agendada."));
                    return;
                }
                MaintenanceScheduler.cancel(Core.getProxy(), true);
                src.sendMessage(legacy("&aAgendamento cancelado."));
            }
            case "actionbar" -> maintenanceActionBar(src, s, args);
            default -> src.sendMessage(legacy("&cSub: on|off|toggle|mode|rank|reason|eta|discord|whitelist|msg|motd1|motd2|preview|in|cancel|actionbar"));
        }
        publishSettings();
        NetworkPublisher.publishStatusNow();
    }

    private void maintenanceWhitelist(CommandSource src, ProxySettingsManager s, String[] args) {
        if (args.length < 3) {
            src.sendMessage(legacy("&7Whitelist manutencao (&f" + s.getMaintenanceWhitelistView().size() + "&7):"));
            if (s.getMaintenanceWhitelistView().isEmpty()) {
                src.sendMessage(legacy("&8(vazia)"));
            } else {
                s.getMaintenanceWhitelistView().forEach(e -> src.sendMessage(legacy("&7- &f" + e)));
            }
            src.sendMessage(legacy("&7/proxy maintenance whitelist add|remove <nick|uuid>"));
            return;
        }
        String action = args[2].toLowerCase(Locale.ROOT);
        if (args.length < 4 && !"list".equals(action)) {
            src.sendMessage(legacy("&cUso: /proxy maintenance whitelist add|remove <nick|uuid>"));
            return;
        }
        switch (action) {
            case "add" -> {
                if (s.addMaintenanceWhitelistEntry(args[3])) {
                    src.sendMessage(legacy("&aAdicionado: &f" + args[3]));
                } else {
                    src.sendMessage(legacy("&cEntrada invalida ou ja existente."));
                }
            }
            case "remove", "rem" -> {
                if (s.removeMaintenanceWhitelistEntry(args[3])) {
                    src.sendMessage(legacy("&aRemovido: &f" + args[3]));
                } else {
                    src.sendMessage(legacy("&cEntrada nao encontrada."));
                }
            }
            case "list" -> maintenanceWhitelist(src, s, new String[]{args[0], args[1]});
            default -> src.sendMessage(legacy("&cSub: add|remove|list"));
        }
    }

    private void maintenanceActionBar(CommandSource src, ProxySettingsManager s, String[] args) {
        if (args.length < 3) {
            src.sendMessage(legacy("&7Action bar countdown: &f"
                    + (s.isCountdownActionBarEnabled() ? "&aON" : "&cOFF")));
            src.sendMessage(legacy("&7Cor: &f" + s.getCountdownActionBarColor()
                    + " &7| Urgente: &f" + s.getCountdownActionBarUrgentColor()
                    + " &7(&f" + s.getCountdownActionBarUrgentSeconds() + "s&7)"));
            src.sendMessage(legacy("&7/proxy maintenance actionbar on|off|color <#hex>|urgent <#hex>"));
            return;
        }
        String action = args[2].toLowerCase(Locale.ROOT);
        switch (action) {
            case "on", "enable" -> {
                s.setCountdownActionBarEnabled(true);
                com.minecraft.proxyhost.proxy.MaintenanceActionBar.broadcast(Core.getProxy());
                src.sendMessage(legacy("&aAction bar de countdown ativada."));
            }
            case "off", "disable" -> {
                s.setCountdownActionBarEnabled(false);
                com.minecraft.proxyhost.proxy.MaintenanceActionBar.clear(Core.getProxy());
                src.sendMessage(legacy("&cAction bar de countdown desativada."));
            }
            case "color", "cor" -> {
                if (args.length < 4) {
                    src.sendMessage(legacy("&cUso: /proxy maintenance actionbar color <#FF8C42>"));
                    return;
                }
                s.setCountdownActionBarColor(args[3]);
                src.sendMessage(legacy("&aCor: &f" + s.getCountdownActionBarColor()));
            }
            case "urgent", "urgente" -> {
                if (args.length < 4) {
                    src.sendMessage(legacy("&cUso: /proxy maintenance actionbar urgent <#FF6B6B> [segundos]"));
                    return;
                }
                s.setCountdownActionBarUrgentColor(args[3]);
                if (args.length >= 5) {
                    try {
                        s.setCountdownActionBarUrgentSeconds(Integer.parseInt(args[4]));
                    } catch (NumberFormatException e) {
                        src.sendMessage(legacy("&cSegundos invalidos."));
                        return;
                    }
                }
                src.sendMessage(legacy("&aCor urgente: &f" + s.getCountdownActionBarUrgentColor()
                        + " &7(<=" + s.getCountdownActionBarUrgentSeconds() + "s)"));
            }
            default -> src.sendMessage(legacy("&cSub: on|off|color|urgent"));
        }
    }

    private void scheduleMaintenance(CommandSource src, ProxySettingsManager s, String[] args) {
        if (args.length < 3) {
            src.sendMessage(legacy("&cUso: /proxy maintenance in <5m|30s|1h> [motivo]"));
            return;
        }
        try {
            long delay = MaintenanceScheduler.parseDuration(args[2]);
            String reason = args.length >= 4 ? join(args, 3) : s.getMaintenanceReason();
            MaintenanceScheduler.schedule(Core.getProxy(), delay, reason, src);
            src.sendMessage(legacy("&eManutencao agendada para &f"
                    + MaintenanceScheduler.formatDuration(delay) + "&7."));
        } catch (IllegalArgumentException e) {
            src.sendMessage(legacy("&c" + e.getMessage()));
        }
    }

    private void sendMaintenanceStatus(CommandSource src, ProxySettingsManager s) {
        src.sendMessage(legacy("&6Manutencao: &f" + (s.isMaintenanceEnabled() ? "&cATIVA" : "&aINATIVA")));
        src.sendMessage(legacy("&7Modo: &f" + s.getMaintenanceMode().name()
                + " &7| Rank min: &f" + s.getMaintenanceMinRank().name()));
        src.sendMessage(legacy("&7Motivo: &f" + s.getMaintenanceReason()));
        src.sendMessage(legacy("&7Previsao: &f" + s.getMaintenanceEstimatedEnd()));
        src.sendMessage(legacy("&7Discord: &f" + s.getMaintenanceDiscord()));
        src.sendMessage(legacy("&7Whitelist: &f" + s.getMaintenanceWhitelistView().size() + " entradas"));
        if (MaintenanceScheduler.isScheduled()) {
            src.sendMessage(legacy("&7Agendada em: &e"
                    + MaintenanceScheduler.formatDuration(MaintenanceScheduler.remainingMs())
                    + " &8(&f" + com.minecraft.proxyhost.proxy.MaintenanceActionBar.formatClock(
                            MaintenanceScheduler.remainingMs()) + "&8)"));
            src.sendMessage(legacy("&7Action bar: &f"
                    + (s.isCountdownActionBarEnabled() ? "&aON" : "&cOFF")));
        }
        src.sendMessage(legacy("&7/proxy maintenance on|off|mode|in 5m|preview"));
    }

    private void maxPlayers(CommandSource src, ProxySettingsManager s, String[] args) {
        if (args.length < 2) {
            src.sendMessage(legacy("&7Max players override: &f" + s.getMaxPlayersOverride()
                    + " &7(-1 = automatico)"));
            return;
        }
        try {
            int n = Integer.parseInt(args[1]);
            s.setMaxPlayersOverride(n);
            src.sendMessage(legacy("&aMax players no ping: &f" + (n < 0 ? "automatico" : n)));
            publishSettings();
        } catch (NumberFormatException e) {
            src.sendMessage(legacy("&cNumero invalido."));
        }
    }

    private void alert(CommandSource src, String[] args) {
        if (args.length < 2) {
            src.sendMessage(legacy("&cUso: /proxy alert <mensagem>"));
            return;
        }
        String msg = join(args, 1);
        String sender = src instanceof Player p ? p.getUsername() : "Console";
        BroadcastService.sendAlert(sender, msg);
        src.sendMessage(legacy("&aAlerta enviado à rede."));
    }

    private void broadcast(CommandSource src, String[] args) {
        if (args.length < 2) {
            src.sendMessage(legacy("&cUso: /proxy broadcast <mensagem>"));
            return;
        }
        String msg = join(args, 1);
        BroadcastService.sendBroadcast(msg);
        src.sendMessage(legacy("&aBroadcast enviado a todos os servidores."));
    }

    private void servers(CommandSource src) {
        src.sendMessage(legacy(" "));
        src.sendMessage(legacy("&6&lSERVIDORES DA REDE"));
        int total = 0;
        for (RegisteredServer server : Core.getProxy().getAllServers()) {
            String name = server.getServerInfo().getName();
            int online = server.getPlayersConnected().size();
            total += online;
            src.sendMessage(legacy("&7" + name + ": &f" + online + " &7online"));
        }
        src.sendMessage(legacy("&7Total no proxy: &f" + Core.getProxy().getPlayerCount()
                + " &8(&7backend sum &f" + total + "&8)"));
        src.sendMessage(legacy(" "));
    }

    private void status(CommandSource src, ProxySettingsManager s) {
        src.sendMessage(legacy(" "));
        src.sendMessage(legacy("&6&lPROXY STATUS"));
        src.sendMessage(legacy("&7Online: &f" + Core.getProxy().getPlayerCount()));
        src.sendMessage(legacy("&7Manutencao: " + (s.isMaintenanceEnabled() ? "&cSIM" : "&aNAO")
                + " &7(&f" + s.getMaintenanceMode() + "&7, rank >= &f" + s.getMaintenanceMinRank() + "&7)"));
        if (MaintenanceScheduler.isScheduled()) {
            src.sendMessage(legacy("&7Agendada: &e"
                    + MaintenanceScheduler.formatDuration(MaintenanceScheduler.remainingMs())));
        }
        src.sendMessage(legacy("&7Motivo: &f" + s.getMaintenanceReason()));
        src.sendMessage(legacy("&7MOTD L1: &f" + s.getMotdLine1()));
        src.sendMessage(legacy("&7MOTD L2: &f" + s.getMotdLine2()));
        src.sendMessage(legacy("&7Max ping: &f" + s.getMaxPlayersOverride()));
        src.sendMessage(legacy(" "));
    }

    private void sendHelp(CommandSource src) {
        src.sendMessage(legacy("&6&l/proxy &7— administracao Velocity"));
        src.sendMessage(legacy("&6/proxy status &7| &6servers &7— resumo e servidores"));
        src.sendMessage(legacy("&6/proxy motd line1|line2|preview &7— MOTD normal"));
        src.sendMessage(legacy("&6/proxy maintenance on|off|toggle &7— liga/desliga"));
        src.sendMessage(legacy("&6/proxy maintenance mode rank|whitelist|full"));
        src.sendMessage(legacy("&6/proxy maintenance reason|eta|discord|whitelist"));
        src.sendMessage(legacy("&6/proxy maintenance in 5m [motivo] &7— agenda com avisos"));
        src.sendMessage(legacy("&6/proxy maintenance preview &7— ver tela de kick"));
        src.sendMessage(legacy("&6/proxy maxplayers &7| &6alert &7| &6broadcast &7| &6reload"));
    }

    private boolean canManage(CommandSource src) {
        if (src.hasPermission("proxyhost.proxy")) return true;
        if (!(src instanceof Player player)) return true;
        Account a = Core.getAccountController().of(player.getUniqueId());
        return a != null && a.getRankType().isAtLeast(RankType.ADMIN);
    }

    private void broadcastStaff(String message) {
        Component c = legacy(message);
        Core.getProxy().getAllPlayers().forEach(p -> {
            Account a = Core.getAccountController().of(p.getUniqueId());
            if (a != null && a.isStaffer()) p.sendMessage(c);
        });
    }

    private void broadcastAll(Component message) {
        Core.getProxy().getAllPlayers().forEach(p -> p.sendMessage(message));
    }

    private void publishSettings() {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) return;
        Core.getRedis().publish(com.minecraft.proxyhost.Constant.REDIS_PROXY_SETTINGS_CHANNEL, "reload");
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
        if (args == null || args.length == 0) {
            return filter(TOP_LEVEL, "");
        }

        String root = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 1) {
            if (isMaintenanceRoot(root)) {
                return filter(MAINTENANCE_SUBS, "");
            }
            if ("motd".equals(root)) {
                return filter(MOTD_SUBS, "");
            }
            return filter(TOP_LEVEL, args[0]);
        }

        if (isMaintenanceRoot(args[0])) {
            if (args.length == 2) {
                return filter(MAINTENANCE_SUBS, args[1]);
            }
            if (args.length == 3) {
                String action = args[1].toLowerCase(Locale.ROOT);
                if ("mode".equals(action)) {
                    return filter(List.of("RANK", "WHITELIST", "FULL"), args[2]);
                }
                if ("rank".equals(action)) {
                    return filter(Stream.of(RankType.values()).map(Enum::name).toList(), args[2]);
                }
                if ("whitelist".equals(action) || "wl".equals(action)) {
                    return filter(List.of("add", "remove", "list"), args[2]);
                }
                if ("in".equals(action) || "agendar".equals(action) || "schedule".equals(action)) {
                    return filter(List.of("5m", "10m", "30m", "1h"), args[2]);
                }
                if ("actionbar".equals(action)) {
                    return filter(List.of("on", "off", "color", "urgent"), args[2]);
                }
            }
        }

        if ("motd".equalsIgnoreCase(args[0]) && args.length == 2) {
            return filter(MOTD_SUBS, args[1]);
        }

        return List.of();
    }

    private static boolean isMaintenanceRoot(String root) {
        return "maintenance".equalsIgnoreCase(root)
                || "manutencao".equalsIgnoreCase(root)
                || "maint".equalsIgnoreCase(root);
    }

    private static final List<String> TOP_LEVEL = List.of(
            "help", "status", "servers", "motd", "maintenance", "maxplayers", "alert", "broadcast", "reload");

    private static final List<String> MOTD_SUBS = List.of(
            "line1", "line2", "reset", "preview", "hide", "show");

    private static final List<String> MAINTENANCE_SUBS = List.of(
            "on", "off", "toggle", "mode", "rank", "reason", "eta", "discord",
            "whitelist", "msg", "motd1", "motd2", "preview", "in", "cancel", "actionbar");

    private static List<String> filter(List<String> options, String partial) {
        String needle = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (needle.isEmpty() || o.toLowerCase(Locale.ROOT).startsWith(needle)) {
                out.add(o);
            }
        }
        return out;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(suggest(invocation));
    }
}
