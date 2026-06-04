package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import com.minecraft.proxyhost.proxy.ProxySettingsManager;
import com.minecraft.proxyhost.util.LegacyTexts;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Administracao do proxy: MOTD, manutencao por rank, alertas e status.
 *
 * <p>Permissao {@code proxyhost.proxy} ou rank {@link RankType#ADMIN}+.
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
            case "reload" -> {
                s.reload();
                src.sendMessage(legacy("&aproxy-settings.yml recarregado."));
            }
            case "motd" -> motd(src, s, args);
            case "maintenance", "manutencao", "maint" -> maintenance(src, s, args);
            case "maxplayers", "slots" -> maxPlayers(src, s, args);
            case "alert", "broadcast", "anuncio" -> alert(src, args);
            case "kicknonstaff" -> {
                s.kickNonStaff(Core.getProxy());
                src.sendMessage(legacy("&eJogadores sem rank minimo foram desconectados."));
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
            case "preview", "ver" -> {
                src.sendMessage(s.buildMotdDescription(Core.getProxy()));
            }
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
            src.sendMessage(legacy("&6Manutencao: &f" + (s.isMaintenanceEnabled() ? "&cATIVA" : "&aINATIVA")));
            src.sendMessage(legacy("&7Rank minimo: &f" + s.getMaintenanceMinRank().name()));
            src.sendMessage(legacy("&7/proxy maintenance on|off|toggle|rank <RANK>|msg <texto>"));
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "on", "enable", "ativar" -> {
                s.setMaintenanceEnabled(true);
                s.kickNonStaff(Core.getProxy());
                broadcastStaff("&c[Proxy] &7Manutencao &cATIVADA &7(rank >= &f"
                        + s.getMaintenanceMinRank() + "&7).");
                src.sendMessage(legacy("&cManutencao ativada."));
            }
            case "off", "disable", "desativar" -> {
                s.setMaintenanceEnabled(false);
                broadcastStaff("&a[Proxy] &7Manutencao &adesativada&7.");
                src.sendMessage(legacy("&aManutencao desativada."));
            }
            case "toggle" -> {
                s.setMaintenanceEnabled(!s.isMaintenanceEnabled());
                if (s.isMaintenanceEnabled()) s.kickNonStaff(Core.getProxy());
                src.sendMessage(legacy("&7Manutencao: &f" + (s.isMaintenanceEnabled() ? "&cON" : "&aOFF")));
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
            default -> src.sendMessage(legacy("&cSub: on|off|toggle|rank|msg|motd1|motd2"));
        }
        publishSettings();
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
        Component c = legacy("&6&l" + Core.getPlugin().getConfig().getString("server.name", "Savage")
                + " &8» &f" + msg);
        Core.getProxy().getAllPlayers().forEach(p -> p.sendMessage(c));
        Core.getProxy().getConsoleCommandSource().sendMessage(c);
        src.sendMessage(legacy("&aAnuncio enviado."));
    }

    private void status(CommandSource src, ProxySettingsManager s) {
        src.sendMessage(legacy(" "));
        src.sendMessage(legacy("&6&lPROXY STATUS"));
        src.sendMessage(legacy("&7Online: &f" + Core.getProxy().getPlayerCount()));
        src.sendMessage(legacy("&7Manutencao: " + (s.isMaintenanceEnabled() ? "&cSIM" : "&aNAO")
                + " &7(rank >= &f" + s.getMaintenanceMinRank() + "&7)"));
        src.sendMessage(legacy("&7MOTD L1: &f" + s.getMotdLine1()));
        src.sendMessage(legacy("&7MOTD L2: &f" + s.getMotdLine2()));
        src.sendMessage(legacy("&7Max ping: &f" + s.getMaxPlayersOverride()));
        src.sendMessage(legacy(" "));
    }

    private void sendHelp(CommandSource src) {
        src.sendMessage(legacy("&6/proxy status &7- resumo"));
        src.sendMessage(legacy("&6/proxy motd line1|line2 &7- editar MOTD"));
        src.sendMessage(legacy("&6/proxy maintenance on|off|rank &7- manutencao por rank"));
        src.sendMessage(legacy("&6/proxy maxplayers <n|-1> &7- slots no ping"));
        src.sendMessage(legacy("&6/proxy alert <msg> &7- anuncio global"));
        src.sendMessage(legacy("&6/proxy reload &7- recarregar proxy-settings.yml"));
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
        if (args.length <= 1) {
            return filter(List.of("help", "status", "motd", "maintenance", "maxplayers", "alert", "reload"), args);
        }
        if (args[0].equalsIgnoreCase("motd") && args.length == 2) {
            return filter(List.of("line1", "line2", "reset", "preview", "hide", "show"), args);
        }
        if (args[0].equalsIgnoreCase("maintenance") && args.length == 2) {
            return filter(List.of("on", "off", "toggle", "rank", "msg", "motd1", "motd2"), args);
        }
        if (args[0].equalsIgnoreCase("maintenance") && args.length == 3 && args[1].equalsIgnoreCase("rank")) {
            return filter(Stream.of(RankType.values()).map(Enum::name).toList(), args);
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String[] args) {
        String partial = args[args.length - 1].toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase(Locale.ROOT).startsWith(partial)) out.add(o);
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
