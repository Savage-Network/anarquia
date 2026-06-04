package com.minecraft.game.bukkit.command.structure;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.util.ClassScanner;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Espelho de {@code BukkitCommandHandler} do Savage adaptado a Paper 1.21.
 * Faz scan reflection em um pacote e registra todos os metodos
 * anotados com {@link Command}/{@link Completer}.
 */
@RequiredArgsConstructor
public final class BukkitCommandHandler {

    private final JavaPlugin plugin;

    public void handle(String packageName) {
        long start = System.currentTimeMillis();
        int registered = 0;

        Core.getLogger().info("Registrando comandos em " + packageName + "...");

        for (Class<?> cmdClass : ClassScanner.getClassesForPackage(plugin, packageName)) {
            if (!CommandInheritor.class.isAssignableFrom(cmdClass)) continue;
            if (cmdClass.isInterface()) continue;

            try {
                CommandInheritor inheritor = (CommandInheritor) cmdClass.getDeclaredConstructor().newInstance();
                registered += registerInheritor(inheritor);
            } catch (Throwable t) {
                Core.getLogger().log(Level.WARNING,
                        "Falha ao registrar comando " + cmdClass.getSimpleName(), t);
            }
        }

        Core.getLogger().info("Comandos registrados: " + registered
                + " (" + (System.currentTimeMillis() - start) + "ms)");
    }

    private int registerInheritor(CommandInheritor inheritor) {
        int count = 0;
        Map<String, TabExecutor> labels = new HashMap<>();

        for (Method method : inheritor.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Command.class)) continue;
            if (method.getParameterCount() != 1) continue;
            if (!BukkitCommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) continue;

            Command annotation = method.getAnnotation(Command.class);
            ReflectExecutor executor = new ReflectExecutor(inheritor, method, annotation);

            registerToBukkit(annotation.name(), executor, annotation);
            labels.put(annotation.name().toLowerCase(), executor);

            for (String alias : annotation.aliases()) {
                registerToBukkit(alias, executor, annotation);
                labels.put(alias.toLowerCase(), executor);
            }
            count++;
        }

        for (Method method : inheritor.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Completer.class)) continue;
            if (method.getParameterCount() != 1) continue;
            if (!BukkitCommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) continue;
            if (!List.class.isAssignableFrom(method.getReturnType())) continue;

            Completer completer = method.getAnnotation(Completer.class);
            TabExecutor executor = labels.get(completer.name().toLowerCase());
            if (executor instanceof ReflectExecutor reflect) {
                reflect.setCompleter(method, inheritor);
            }
        }

        return count;
    }

    private void registerToBukkit(String label, TabExecutor executor, Command annotation) {
        try {
            CommandMap commandMap = Bukkit.getCommandMap();
            String cmdLabel = label.toLowerCase();

            org.bukkit.command.Command existing = commandMap.getCommand(cmdLabel);
            if (existing != null) {
                // Sobrescrever apenas se for nosso (PluginCommand) ou DynamicCommand antigo
                if (existing instanceof org.bukkit.command.PluginCommand pluginCommand) {
                    pluginCommand.setExecutor((sender, command, lbl, args) ->
                            executor.onCommand(sender, command, lbl, args));
                    pluginCommand.setTabCompleter((sender, command, lbl, args) ->
                            executor.onTabComplete(sender, command, lbl, args));
                    return;
                }
            }

            DynamicCommand dynamic = new DynamicCommand(cmdLabel, executor, annotation);
            commandMap.register(plugin.getName().toLowerCase(), dynamic);
        } catch (Throwable t) {
            Core.getLogger().log(Level.WARNING, "Falha ao registrar /" + label, t);
        }
    }

    /* -------------------- helpers -------------------- */

    private static final class DynamicCommand extends org.bukkit.command.Command {

        private final TabExecutor executor;

        DynamicCommand(String label, TabExecutor executor, Command annotation) {
            super(label);
            this.executor = executor;
            this.setDescription(annotation.description());
            this.setUsage(annotation.usage().isEmpty() ? "/" + label : annotation.usage());
            this.setAliases(new ArrayList<>(java.util.Arrays.asList(annotation.aliases())));
            if (!annotation.permission().isEmpty()) {
                this.setPermission(annotation.permission());
            }
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            return executor.onCommand(sender, this, label, args);
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            List<String> out = executor.onTabComplete(sender, this, alias, args);
            return out == null ? Collections.emptyList() : out;
        }
    }

    private static final class ReflectExecutor implements TabExecutor {

        private final CommandInheritor inheritor;
        private final Method commandMethod;
        private final Command annotation;
        private Method completerMethod;
        private CommandInheritor completerInheritor;

        ReflectExecutor(CommandInheritor inheritor, Method commandMethod, Command annotation) {
            this.inheritor = inheritor;
            this.commandMethod = commandMethod;
            this.annotation = annotation;
            this.commandMethod.setAccessible(true);
        }

        void setCompleter(Method method, CommandInheritor owner) {
            method.setAccessible(true);
            this.completerMethod = method;
            this.completerInheritor = owner;
        }

        @Override
        public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
            BukkitCommandContext ctx = new BukkitCommandContext(sender, label, args);

            if (annotation.playerOnly() && !ctx.isPlayer()) {
                sender.sendMessage("§cApenas jogadores podem usar este comando.");
                return true;
            }

            if (!annotation.permission().isEmpty() && !sender.hasPermission(annotation.permission())) {
                sender.sendMessage("§cVoce nao tem permissao para usar este comando.");
                return true;
            }

            if (ctx.isPlayer() && annotation.rank() != RankType.MEMBER) {
                Account account = ctx.account().orElse(null);
                if (account == null || !account.hasRank(annotation.rank())) {
                    sender.sendMessage("§cRank necessario: §e" + annotation.rank().name());
                    return true;
                }
            }

            try {
                commandMethod.invoke(inheritor, ctx);
            } catch (Throwable t) {
                Throwable root = t.getCause() != null ? t.getCause() : t;
                Core.getLogger().log(Level.WARNING, "Erro ao executar /" + label, root);
                sender.sendMessage("§cErro interno ao executar o comando.");
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
            if (completerMethod == null) return Collections.emptyList();
            BukkitCommandContext ctx = new BukkitCommandContext(sender, alias, args);
            try {
                Object res = completerMethod.invoke(completerInheritor, ctx);
                if (res instanceof List<?> list) {
                    List<String> casted = new ArrayList<>();
                    for (Object o : list) if (o instanceof String s) casted.add(s);
                    return casted;
                }
            } catch (Throwable t) {
                Core.getLogger().log(Level.FINE, "Tab-complete falhou", t);
            }
            return Collections.emptyList();
        }
    }

    // Mantido para compatibilidade reflection caso plugins externos
    // queiram acessar a tabela via reflection (igual Savage)
    @SuppressWarnings("unused")
    private Map<String, org.bukkit.command.Command> knownCommands() {
        try {
            CommandMap map = Bukkit.getCommandMap();
            Field field = map.getClass().getDeclaredField("knownCommands");
            field.setAccessible(true);
            //noinspection unchecked
            return (Map<String, org.bukkit.command.Command>) field.get(map);
        } catch (Throwable t) {
            return new HashMap<>();
        }
    }
}
