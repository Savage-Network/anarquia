package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.arena.ArenaDefinition;
import com.minecraft.eventos.arena.ArenaGenerator;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.annotation.Completer;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import com.minecraft.eventos.config.EventConfig;
import com.minecraft.eventos.engine.EventEngine;
import com.minecraft.eventos.engine.EventSession;
import com.minecraft.eventos.game.state.GameState;
import com.minecraft.eventos.game.variable.Variable;
import com.minecraft.eventos.message.EventMessages;
import com.minecraft.eventos.util.TabCompleteUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Hub central de eventos: modos, mapas, scheduler, fila. */
public final class EventCommand implements CommandInheritor {

    @Command(name = "evento",
            description = "Gerencia eventos (modos, mapas, automaticos)",
            usage = "/evento <status|mode|start|stop|map|scheduler|join|leave|var> ...",
            aliases = {"event"})
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            sendHelp(ctx);
            return;
        }

        String sub = ctx.getArgs()[0].toLowerCase();
        if (isPlayerSub(sub)) {
            if (!ctx.isPlayer()) {
                ctx.reply(EventMessages.format("command.player-only"));
                return;
            }
            if (!ctx.getSender().hasPermission("eventos.player") && !ctx.getSender().hasPermission("eventos.admin")) {
                ctx.reply(EventMessages.format("command.no-permission"));
                return;
            }
        } else if (!ctx.getSender().hasPermission("eventos.admin")) {
            ctx.reply(EventMessages.format("command.no-permission"));
            return;
        }

        EventEngine engine = EventosPlugin.getInstance().getEventEngine();

        switch (sub) {
            case "status", "info" -> handleStatus(ctx, engine);
            case "mode", "modo" -> handleMode(ctx, engine);
            case "start", "iniciar" -> handleStart(ctx, engine);
            case "stop", "parar" -> handleStop(ctx, engine);
            case "map", "mapa", "arena" -> handleMap(ctx, engine);
            case "scheduler", "agenda", "auto" -> handleScheduler(ctx, engine);
            case "join", "entrar" -> handleJoin(ctx, engine);
            case "leave", "sair" -> handleLeaveQueue(ctx, engine);
            case "var", "variable" -> handleVar(ctx);
            case "help", "ajuda" -> sendHelp(ctx);
            default -> ctx.reply(EventMessages.format("event.command.unknown",
                    Map.of("sub", sub), "§cSubcomando desconhecido."));
        }
    }

    private void handleStatus(BukkitCommandContext ctx, EventEngine engine) {
        EventSession s = engine.getSession();
        ctx.reply(EventMessages.format("event.status.line", Map.of(
                "state", s.getState().name(),
                "mode", s.getActiveMode() != null ? s.getActiveMode().displayName() : s.getModeId(),
                "players", String.valueOf(s.alives().size()),
                "min", String.valueOf(s.minPlayers()),
                "arena", s.getArenaId() != null ? s.getArenaId() : "-",
                "auto", String.valueOf(s.isAutomated())
        )));
    }

    private void handleMode(BukkitCommandContext ctx, EventEngine engine) {
        if (ctx.getArgs().length < 2) {
            String modes = engine.getModes().ids().stream().collect(Collectors.joining(", "));
            ctx.reply(EventMessages.format("event.mode.list", Map.of("modes", modes)));
            return;
        }
        String modeId = ctx.getArgs()[1];
        if (engine.getSession().getState() != GameState.WAITING) {
            ctx.reply(EventMessages.format("event.error.not-waiting"));
            return;
        }
        if (!engine.setMode(modeId)) {
            ctx.reply(EventMessages.format("event.mode.unknown", Map.of("mode", modeId)));
            return;
        }
        ctx.reply(EventMessages.format("event.mode.set", Map.of(
                "mode", engine.getSession().getActiveMode().displayName())));
    }

    private void handleStart(BukkitCommandContext ctx, EventEngine engine) {
        EventSession s = engine.getSession();
        if (!s.canStart()) {
            ctx.reply(EventMessages.format("event.error.cannot-start", Map.of(
                    "state", s.getState().name(),
                    "players", String.valueOf(s.alives().size()),
                    "min", String.valueOf(s.minPlayers()))));
            return;
        }
        int sec = ctx.getArgs().length > 1 ? parseInt(ctx.getArgs()[1], s.startCountdown()) : s.startCountdown();
        engine.startEvent(ctx.player(), sec, false, null);
        ctx.reply(EventMessages.format("event.session.starting", Map.of(
                "seconds", String.valueOf(sec),
                "mode", s.getActiveMode() != null ? s.getActiveMode().displayName() : s.getModeId())));
    }

    private void handleStop(BukkitCommandContext ctx, EventEngine engine) {
        engine.stopEvent("staff");
        ctx.reply(EventMessages.format("event.session.stopped"));
    }

    private void handleMap(BukkitCommandContext ctx, EventEngine engine) {
        if (!(ctx.getSender() instanceof Player player)) {
            ctx.reply(EventMessages.format("command.player-only"));
            return;
        }
        if (ctx.getArgs().length < 2) {
            ctx.reply("§e/evento map generate <modo> [raio] §7| list | tp <id> | clear <id>");
            return;
        }
        String action = ctx.getArgs()[1].toLowerCase();
        switch (action) {
            case "generate", "gerar" -> {
                String mode = ctx.getArgs().length > 2 ? ctx.getArgs()[2] : engine.getSession().getModeId();
                int radius = ctx.getArgs().length > 3 ? parseInt(ctx.getArgs()[3], EventConfig.getAutoMapRadius())
                        : EventConfig.getAutoMapRadius();
                String id = "arena-" + mode + "-" + (System.currentTimeMillis() % 100000);
                ArenaDefinition arena = engine.generateMap(player, id, mode, radius);
                ctx.reply(EventMessages.format("event.map.generated", Map.of(
                        "id", arena.getId(),
                        "mode", mode,
                        "radius", String.valueOf(radius))));
            }
            case "list", "lista" -> {
                StringBuilder sb = new StringBuilder("§6Arenas: ");
                engine.getArenas().all().forEach(a ->
                        sb.append("§e").append(a.getId()).append("§7(").append(a.getModeId()).append(") "));
                ctx.reply(sb.toString());
            }
            case "tp", "teleport" -> {
                if (ctx.getArgs().length < 3) return;
                engine.getArenas().find(ctx.getArgs()[2]).ifPresentOrElse(a -> {
                    if (a.primarySpawn() != null) player.teleport(a.primarySpawn());
                    engine.useArena(a.getId());
                    ctx.reply("§aArena §e" + a.getId());
                }, () -> ctx.reply("§cArena nao encontrada."));
            }
            case "clear", "limpar" -> {
                if (ctx.getArgs().length < 3) return;
                engine.getArenas().find(ctx.getArgs()[2]).ifPresentOrElse(a -> {
                    int n = ArenaGenerator.clearArena(a);
                    ctx.reply(EventMessages.format("event.map.cleared", Map.of(
                            "id", a.getId(), "blocks", String.valueOf(n))));
                }, () -> ctx.reply("§cArena nao encontrada."));
            }
            case "delete", "remover" -> {
                if (ctx.getArgs().length < 3) return;
                engine.getArenas().delete(ctx.getArgs()[2]);
                ctx.reply("§cArena removida.");
            }
            default -> ctx.reply("§cAcao invalida. Use generate, list, tp, clear, delete.");
        }
    }

    private void handleScheduler(BukkitCommandContext ctx, EventEngine engine) {
        if (ctx.getArgs().length < 2) {
            List<String> ids = EventConfig.scheduledTasks(EventosPlugin.getInstance()).stream()
                    .map(EventConfig.ScheduledTaskConfig::id).toList();
            ctx.reply(EventMessages.format("event.scheduler.list", Map.of(
                    "tasks", String.join(", ", ids),
                    "enabled", String.valueOf(EventConfig.isSchedulerEnabled()))));
            return;
        }
        String action = ctx.getArgs()[1].toLowerCase();
        if ("force".equals(action) && ctx.getArgs().length >= 3) {
            engine.getScheduler().forceTask(ctx.getArgs()[2], ctx.player());
            ctx.reply(EventMessages.format("event.scheduler.forced", Map.of("task", ctx.getArgs()[2])));
        } else if ("list".equals(action)) {
            handleScheduler(ctx, engine);
        } else {
            ctx.reply("§e/evento scheduler list §7| §eforce <id>");
        }
    }

    private void handleJoin(BukkitCommandContext ctx, EventEngine engine) {
        if (!ctx.isPlayer()) return;
        if (engine.joinQueue(ctx.player())) {
            ctx.reply(EventMessages.format("event.queue.joined"));
        } else {
            ctx.reply(EventMessages.format("event.queue.fail"));
        }
    }

    private void handleLeaveQueue(BukkitCommandContext ctx, EventEngine engine) {
        if (!ctx.isPlayer()) return;
        if (engine.leaveQueue(ctx.player())) {
            ctx.reply(EventMessages.format("event.queue.left"));
        } else {
            ctx.reply(EventMessages.format("event.queue.not-in"));
        }
    }

    private void handleVar(BukkitCommandContext ctx) {
        if (ctx.getArgs().length < 2) {
            ctx.reply("§e/evento var list|get|set|reset");
            return;
        }
        String action = ctx.getArgs()[1].toLowerCase();
        if ("list".equals(action)) {
            StringBuilder sb = new StringBuilder("§6Variaveis: ");
            for (Variable.Key k : Variable.Key.values()) {
                sb.append("§e").append(k.name()).append("§7=")
                        .append(Variable.get(k)).append(" ");
            }
            ctx.reply(sb.toString());
        } else if ("get".equals(action) && ctx.getArgs().length >= 3) {
            Variable.Key k = Variable.Key.of(ctx.getArgs()[2]);
            ctx.reply(k != null ? k.name() + "=" + Variable.get(k) : "§cChave invalida");
        } else if ("set".equals(action) && ctx.getArgs().length >= 4) {
            Variable.Key k = Variable.Key.of(ctx.getArgs()[2]);
            if (k == null) { ctx.reply("§cChave invalida"); return; }
            String val = ctx.getArgs()[3];
            Object parsed = val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")
                    ? Boolean.parseBoolean(val) : parseInt(val, 0);
            Variable.set(k, parsed);
            ctx.reply("§a" + k.name() + "=" + parsed);
        } else if ("reset".equals(action)) {
            if (ctx.getArgs().length >= 3) {
                Variable.Key k = Variable.Key.of(ctx.getArgs()[2]);
                if (k != null) Variable.reset(k);
            } else Variable.resetAll();
            ctx.reply("§aVariaveis resetadas.");
        }
    }

    private static boolean isPlayerSub(String sub) {
        return "join".equals(sub) || "entrar".equals(sub) || "leave".equals(sub) || "sair".equals(sub);
    }

    private void sendHelp(BukkitCommandContext ctx) {
        List<String> lines = List.of(
                "§6/evento status §7- estado atual",
                "§6/evento mode <id> §7- sumo, spleef, tntrun, blockparty, ffa",
                "§6/evento map generate <modo> [raio] §7- gera arena automatica",
                "§6/evento start [seg] §7- inicia countdown staff",
                "§6/evento stop §7- encerra evento",
                "§6/evento scheduler list|force <id> §7- eventos automaticos",
                "§6/evento join|leave §7- fila do evento auto"
        );
        lines.forEach(ctx::reply);
    }

    @Completer(name = "evento")
    public List<String> tab(BukkitCommandContext ctx) {
        List<String> out = new ArrayList<>();
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.filter(ctx.getArgs()[0],
                    "status", "mode", "start", "stop", "map", "scheduler", "join", "leave", "var", "help");
        }
        if (ctx.getArgs().length == 2) {
            return switch (ctx.getArgs()[0].toLowerCase()) {
                case "mode", "modo" -> TabCompleteUtil.filter(ctx.getArgs()[1],
                        EventosPlugin.getInstance().getEventEngine().getModes().ids().toArray(String[]::new));
                case "map", "mapa" -> TabCompleteUtil.filter(ctx.getArgs()[1],
                        "generate", "list", "tp", "clear", "delete");
                case "scheduler", "auto" -> TabCompleteUtil.filter(ctx.getArgs()[1], "list", "force");
                case "var" -> TabCompleteUtil.filter(ctx.getArgs()[1], "list", "get", "set", "reset");
                default -> out;
            };
        }
        if (ctx.getArgs().length == 3 && "map".equalsIgnoreCase(ctx.getArgs()[0])
                && "generate".equalsIgnoreCase(ctx.getArgs()[1])) {
            return TabCompleteUtil.filter(ctx.getArgs()[2], "sumo", "spleef", "tntrun", "blockparty", "ffa");
        }
        return out;
    }

    private int parseInt(String s, int def) {
        try { return Math.max(1, Integer.parseInt(s)); } catch (Exception e) { return def; }
    }
}
