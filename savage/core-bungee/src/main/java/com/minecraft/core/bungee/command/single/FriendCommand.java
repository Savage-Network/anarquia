package com.minecraft.core.bungee.command.single;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.friend.Friend;
import com.minecraft.core.backend.database.redis.message.types.account.AccountFriendMessage;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.server.type.ServerType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class FriendCommand implements CommandInheritor {

    private final String NOT_FRIEND_MESSAGE = "§cVocê não possui amizade com esse jogador.";

    @Completer(name = "amigo", subCommands = {"friend"})
    public List<String> friendCompleter(BungeeCommandContext context) {
        List<String> list = new ArrayList<>(),
                subCommands = context.getSubCommands(),
                playerList = context.getServerPlayerList();

        String[] args = context.getArgs();

        if (args.length == 1) {
            String command = args[0].toLowerCase();

            if (command.isEmpty())
                list.addAll(subCommands);
            else {
                for (String subCommand : subCommands) {
                    if (subCommand.toLowerCase().startsWith(command))
                        list.add(subCommand);
                }
            }

        } else if (args.length == 2) {
            String playerName = args[1].toLowerCase();

            if (playerName.isEmpty())
                list.addAll(playerList);
            else {
                for (String name : playerList) {
                    if (name.toLowerCase().startsWith(playerName))
                        list.add(name);
                }
            }
        }

        return list;
    }

    @Command(name = "amigo", aliases = {"friend"})
    public void friend(BungeeCommandContext context) {
        String label = "/" + context.getLabel();

        if (context.getArgs().length == 0)
            context.getSender().send("§cComandos do " + label + ":",
                    "§c" + label + " lista - Visualizar lista de amizades.",
                    "§c" + label + " add [jogador] - Enviar um pedido de amizade.",
                    "§c" + label + " remove [jogador] - Encerrar amizade com um jogador.",
                    "§c" + label + " pedidos - Visualizar lista de pedidos.",
                    "§c" + label + " recusar [jogador] - Recusar um pedido de amizade.",
                    "§c" + label + " aceitar [jogador] - Aceitar um pedido de amizade.");
    }

    @Command(name = "amigo.lista", aliases = {"amigos", "friend.lista"})
    public void friendList(BungeeCommandContext context) {
        new AccountFriendMessage(AccountFriendMessage.FriendMenuType.LIST, context.getSender().getId(), null).send();
    }

    @Command(name = "amigo.pedidos", aliases = {"friend.pedidos"})
    public void friendRequest(BungeeCommandContext context) {
        new AccountFriendMessage(AccountFriendMessage.FriendMenuType.REQUEST, context.getSender().getId(), null).send();
    }

    @Command(name = "amigo.ver", aliases = {"friend.ver"})
    public void friendSee(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null || !target.isOnline()) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (!account.isFriend(target)) {
            account.send(NOT_FRIEND_MESSAGE);
            return;
        }

        Friend friend = account.getFriend(target.getId());

        new AccountFriendMessage(AccountFriendMessage.FriendMenuType.INFO, account.getId(), friend).send();
    }

    @Command(name = "amigo.add", aliases = {"friend.add"})
    public void friendAdd(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null || !target.isOnline() || target.inServer(ServerType.AUTH)) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (account.isFriend(target)) {
            account.send("§cEsse jogador já é seu amigo.");
            return;
        }

        if (!target.getToggle().isAllowFriendRequests()) {
            account.send("§cO jogador " + target.getColoredName() + " §cnão está recebendo pedidos de amizade!");
            return;
        }

        if (account.hasFriendRequest(target)) {
            account.send("§cVocê já enviou uma solicitação de amizade para " + target.getColoredName() + "§c.");
            return;
        }

        account.send("§aVocê enviou uma solicitação de amizade para " + target.getColoredName() + "§a.");
        target.addFriendRequest(account);

        TextComponent preview = new TextComponent("\n" + account.getColoredName() + "§e enviou uma solicitação de amizade!");

        TextComponent acceptButton = new TextComponent("§a§lCLIQUE AQUI");

        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para aceitar a solicitação.")));
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/amigo aceitar " + account.getName()));

        TextComponent refuseButton = new TextComponent("§4§lCLIQUE AQUI");

        refuseButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para recusar a solicitação.")));
        refuseButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/amigo recusar " + account.getName()));

        preview.addExtra("\n");
        preview.addExtra(acceptButton);
        preview.addExtra(" §epara aceitar ou ");
        preview.addExtra(refuseButton);
        preview.addExtra("§e para recusar.");
        preview.addExtra("\n");

        target.send(preview);
    }

    @Command(name = "amigo.remove", aliases = {"friend.remove"})
    public void friendRemove(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null || !target.isOnline()) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (!account.isFriend(target)) {
            account.send(NOT_FRIEND_MESSAGE);
            return;
        }

        account.removeFriend(target);
        target.removeFriend(account);

        account.send("§cVocê desfez a amizade com " + target.getColoredName() + "§c.");
        target.send("§eO jogador " + account.getColoredName() + "§e desfez a amizade com você.");
    }

    @Command(name = "amigo.aceitar", aliases = {"friend.aceitar"})
    public void friendAccept(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (!account.hasFriendRequest(target)) {
            account.send("§cNão há solicitação de amizade pendente.");
            return;
        }

        account.addFriend(target);
        target.addFriend(account);

        account.send("§aVocê aceitou a solicitação de amizade de " + target.getColoredName() + "§a.");
        target.send("§a" + account.getColoredName() + "§a aceitou a sua solicitação de amizade.");

        account.removeFriendRequest(target.getId());
    }

    @Command(name = "amigo.recusar", aliases = {"friend.recusar"})
    public void friendRefuse(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (!account.hasFriendRequest(target)) {
            account.send("§cNão há solicitação de amizade pendente.");
            return;
        }

        account.send("§cVocê recusou a solicitação de amizade de " + target.getColoredName() + "§c.");
        target.send("§c" + account.getColoredName() + "§c recusou a sua solicitação de amizade.");

        account.removeFriendRequest(target.getId());
    }
}
