package com.minecraft.eventos.bukkit.command.structure;

import com.minecraft.eventos.Core;
import com.minecraft.eventos.account.Account;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;

/**
 * Contexto passado para todo metodo {@code @Command} no {@link BukkitCommandHandler}.
 *
 * <p>Combina o {@link CommandSender} do Bukkit com a {@link Account} carregada
 * (quando o sender e um Player).
 */
@Getter
@RequiredArgsConstructor
public final class BukkitCommandContext {

    private final CommandSender sender;
    private final String label;
    private final String[] args;

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public boolean isConsole() {
        return sender instanceof ConsoleCommandSender;
    }

    public Player player() {
        return isPlayer() ? (Player) sender : null;
    }

    public Optional<Account> account() {
        if (!isPlayer()) return Optional.empty();
        return Optional.ofNullable(Core.getAccountController().of(player().getUniqueId()));
    }

    public void reply(Component message) {
        sender.sendMessage(message);
    }

    public void reply(String legacy) {
        sender.sendMessage(legacy);
    }

    public String joinArgs(int from) {
        if (args.length <= from) return "";
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }
}
