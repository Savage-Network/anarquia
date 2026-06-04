package com.minecraft.arcade.event.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.game.state.GameState;
import com.minecraft.arcade.event.game.team.Team;
import com.minecraft.arcade.event.game.team.menu.TeamSelectorMenu;
import com.minecraft.arcade.event.user.User;
import com.minecraft.core.arcade.room.team.preset.TeamPresetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Stream;

public class TeamCommand implements CommandInheritor
{

    @Command(
            name = "team",
            rank = RankType.MOD
    )
    public void team(BukkitCommandContext context)
    {

        Account account = context.getAccount();

        String[] args = context.getArgs();

        Game game = Event.getGame();

        if (!game.isState(com.minecraft.arcade.event.game.state.GameState.WAITING) && !account.hasRank(RankType.MODPLUS)) {
            account.send("§cOs times não podem ser manipulados no momento.");
            return;
        }

        if (!game.isTeam()) {
            account.send("§cOs times não foram ativados neste evento.");
            return;
        }

        if (!account.hasRank(RankType.MODPLUS)) {

            if (game.isTeamSelect())
                new TeamSelectorMenu(context.getPlayer()).handle();

            return;
        } else if (args.length == 0) {
            new TeamSelectorMenu(context.getPlayer()).handle();
            return;
        }

        TeamArgument argument = TeamArgument.search(args[0]);

        argument.handle(game, context);
    }

    @Getter
    @AllArgsConstructor
    public enum TeamArgument
    {

        HELP("list", null, (game, context) ->
        {

            CommandSender sender = context.getSender();

            String label = "/" + context.getLabel();

            List<TeamArgument> arguments = Stream.of(TeamArgument.values())
                    .filter(arg -> !arg.name().equalsIgnoreCase("HELP") && arg.getUsage() != null)
                    .toList();

            sender.send("§cUso do " + label + ":");

            arguments.forEach(arg -> sender.send("§c* %s %s %s".formatted(label, arg.getName(), arg.getUsage())));

        }),
        CREATE("criar", "<cor>", (game, context) ->
        {

            CommandSender sender = context.getSender();

            String[] args = context.getArgs();

            if (args.length == 1) {
                sender.send("§cUso: /" + context.getLabel() + " criar <cor>.");
                return;
            }

            TeamPresetType type = java.util.Arrays.stream(TeamPresetType.values())
                    .filter(t -> t.name().equalsIgnoreCase(args[1]) || t.getName().equalsIgnoreCase(args[1]))
                    .findFirst().orElse(null);

            if (type == null) {
                sender.send("§cO time informado não está disponível nas cores de times.");
                return;
            }

            Team team = game.getTeam(type);

            if (team.isUnlocked()) {
                sender.send("§cO Time " + team.getColoredName() + "§c já está desbloqueado.");
                return;
            }

            team.setUnlocked(true);

            sender.send("§aVocê desbloqueou o time " + team.getColoredName() + "§a no evento.");
        }),
        DELETE("remover", "<cor>", (game, context) ->
        {

            CommandSender sender = context.getSender();

            String[] args = context.getArgs();

            if (args.length == 1) {
                sender.send("§cUso: /" + context.getLabel() + " remover <cor>.");
                return;
            }

            TeamPresetType type = java.util.Arrays.stream(TeamPresetType.values())
                    .filter(t -> t.name().equalsIgnoreCase(args[1]) || t.getName().equalsIgnoreCase(args[1]))
                    .findFirst().orElse(null);

            if (type == null) {
                sender.send("§cO time informado não está disponível nas cores de times.");
                return;
            }

            Team team = game.getTeam(type);

            if (!team.isUnlocked()) {
                sender.send("§cO Time " + team.getColoredName() + "§c não está desbloqueado.");
                return;
            }

            team.setUnlocked(false);

            sender.send("§eVocê bloqueou o time " + team.getColoredName() + "§e no evento.");
        }),
        OUT("sair", "<jogador>", (game, context) ->
        {

            Account account = context.getAccount();

            String[] args = context.getArgs();

            if (!game.isTeam()) {
                account.send("§cO sistema de times ainda não foi ativado.");
                return;
            }

            if (args.length == 1) {
                account.send("§cUso: /" + context.getLabel() + " sair <jogador>.");
                return;
            }

            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                account.send("§cJogador não encontrado.");
                return;
            }
            User target = (User) User.of(targetPlayer.getUniqueId());

            if (target == null) {
                account.send(TARGET_NOT_FOUND);
                return;
            }

            Team current = target.getEventTeam();

            if (current == null) {
                account.send("§cO jogador %s não faz parte de nenhum time.".formatted(target.getAccount().getNickname()));
                return;
            }

            if (game.isState(GameState.PLAYING))
                target.getAccount().player().getInventory().setArmorContents(null);

            current.getMembers().remove(target.getAccount().getId());

            account.send("§cVocê removeu o jogador %s do time %s§c!".formatted(target.getAccount().getNickname(), current.getColoredName()));
        }),
        TELEPORT("teleportar", "<time>", (game, context) ->
        {

            Player player = context.getPlayer();

            String[] args = context.getArgs();

            if (!game.isTeam()) {
                player.sendMessage("§cO sistema de times ainda não foi ativado.");
                return;
            }

            if (args.length == 1) {
                player.sendMessage("§cUso: /" + context.getLabel() + " teleportar <time>.");
                return;
            }

            TeamPresetType type = java.util.Arrays.stream(TeamPresetType.values())
                    .filter(t -> t.name().equalsIgnoreCase(args[1]) || t.getName().equalsIgnoreCase(args[1]))
                    .findFirst().orElse(null);

            if (type == null) {
                player.sendMessage("§cO time informado não está disponível nas cores de times.");
                return;
            }

            Team team = game.getTeam(type);

            if (!team.isUnlocked()) {
                player.sendMessage("§cO Time " + team.getColoredName() + "§c não foi liberado ainda.");
                return;
            }

            Set<UUID> members = team.getMembers();

            if (members.isEmpty()) {
                player.sendMessage("§cO Time " + team.getColoredName() + "§c está vázio!");
                return;
            }

            Set<Player> players = new HashSet<>();

            for (UUID uuid : members) {

                Player target = Bukkit.getPlayer(uuid);

                if (target != null)
                    players.add(target);
            }

            if (players.isEmpty()) {
                player.sendMessage("§cNão há jogadores para teletransportar.");
                return;
            }

            Location destination = player.getLocation();
            players.forEach(p -> p.teleport(destination));

            player.sendMessage("§aVocê teletransportou §e%s§a jogadores do time %s§a até você!".formatted(players.size(), team.getColoredName()));
        }),
        MOVE("mover", "<jogador|todos> <time>", (game, context) ->
        {

            Account account = context.getAccount();

            String[] args = context.getArgs();

            if (!game.isTeam()) {
                account.send("§cO sistema de times ainda não foi ativado.");
                return;
            }

            if (args.length <= 2) {
                account.send("§cUso: /" + context.getLabel() + " mover <jogador|todos> <time>.");
                return;
            }

            List<User> targets = new ArrayList<>();

            if (args[1].equalsIgnoreCase("todos"))
                targets.addAll(game.getUsers());
            else {
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    account.send("§cJogador não encontrado.");
                    return;
                }
                User target = (User) User.of(targetPlayer.getUniqueId());

                if (target == null) {
                    account.send(TARGET_NOT_FOUND);
                    return;
                }

                targets.add(target);
            }

            TeamPresetType type = java.util.Arrays.stream(TeamPresetType.values())
                    .filter(t -> t.name().equalsIgnoreCase(args[2]) || t.getName().equalsIgnoreCase(args[2]))
                    .findFirst().orElse(null);

            if (type == null) {
                account.send("§cO time informado não está disponível nas cores de times.");
                return;
            }

            Team team = game.getTeam(type);

            if (!team.isUnlocked()) {
                account.send("§cO Time " + team.getColoredName() + "§c não foi liberado ainda.");
                return;
            }

            int changes = targets.size();

            if (changes == 0) {
                account.send("§cNão há jogadores para mover de time.");
                return;
            }

            targets.forEach(target ->
            {

                Team current = target.getEventTeam();

                if (current != null)
                    current.getMembers().remove(target.getAccount().getId());

                team.getMembers().add(target.getAccount().getId());

                if (game.isState(GameState.PLAYING))
                    target.loadTeam();
            });

            account.send("§aVocê moveu §e%s§a jogadores para o time %s§a!".formatted(changes, team.getColoredName()));

        });

        private final String name, usage;
        private final ArgumentExecutor executor;

        public static TeamArgument search(String name)
        {
            return Stream.of(values())
                    .filter(arg -> arg.name().equalsIgnoreCase(name) || arg.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(HELP);
        }

        public void handle(Game game, BukkitCommandContext context)
        {
            this.executor.handle(game, context);
        }

        interface ArgumentExecutor
        {
            void handle(Game game, BukkitCommandContext context);
        }

    }
}
