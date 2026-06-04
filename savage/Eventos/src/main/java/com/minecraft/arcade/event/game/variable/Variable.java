package com.minecraft.arcade.event.game.variable;

import com.minecraft.core.Core;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.util.Util;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.core.bukkit.BukkitCore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum Variable
{

    /* Game variables */
    GAME_NAME("game_name", String.class, "&b&lEVENTO", value ->
    {
        String title = value.toString();

        BukkitCore.getManager().getSidebar().list().forEach(sidebar -> sidebar.setTitle(Util.color(title)));
    }, true),
    TIME("time", Integer.class, 0, value ->
    {
        int time = (int) value;

        Game game = Event.getGame();

        if (game != null)
            game.setTime(time);
    }),
    MIN_PLAYERS("min_players", Integer.class, 15, value ->
    {
        int max = (int) value;

        Game game = Event.getGame();

        if (game != null)
            game.setMinPlayers(max);
    }),
    MAX_PLAYERS("max_players", Integer.class, 100, value ->
    {
        int max = (int) value;

        Game game = Event.getGame();

        if (game != null)
            game.setMaxPlayers(max);
    }),

    /* Server variables */
    SET_DIE("setdie", Boolean.class, true, value ->
    {
        boolean state = (boolean) value;

        Game game = Event.getGame();

        if (game != null)
            game.setDie(state);
    }),
    TEAM("team", Boolean.class, false, value ->
    {
        boolean state = (boolean) value;

        Game game = Event.getGame();

        if (game != null) {
            game.setTeam(state);

            ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE = !state;
        }
    }),
    TEAM_SELECT("team_select", Boolean.class, false, value ->
    {
        boolean state = (boolean) value;

        Game game = Event.getGame();

        if (game != null)
            game.setTeamSelect(state);
    }),
    PLAYER_HP("player_hp", Double.class, 20.0, value ->
    {
        double hp = (double) value;

        Bukkit.getOnlinePlayers().forEach(player ->
        {
            player.setMaxHealth(hp);
            player.setHealth(hp);
        });
    }),
    LAVA_DAMAGE("lava_damage", Double.class, 1.5, value -> { /* ServerOptions.LAVA_DAMAGE not available */ }),
    JOIN("join", Boolean.class, false),
    DROPS("drops", Boolean.class, false, value -> ServerOptions.DROP_ITEM_ENABLED = (boolean) value),
    CHAT("chat", Boolean.class, false),
    FOOD("food", Boolean.class, false, value -> ServerOptions.FOOD_ENABLED = (boolean) value),
    BUILD("build", Boolean.class, false, value ->
    {
        ServerOptions.BLOCK_PLACE = (boolean) value;
        ServerOptions.BLOCK_BREAK = (boolean) value;
    }),
    DAMAGE_PLAYER("damage_player", Boolean.class, false),
    DAMAGE_NORMAL("damage_normal", Boolean.class, false),
    WATER_KILL("water_kill", Boolean.class, false),
    PHYSICS("physics", Boolean.class, false, value -> { /* ServerOptions.BLOCK_PHYSICS not available */ }),
    BUCKET("bucket", Boolean.class, false, value -> ServerOptions.BUCKET_ENABLED = (boolean) value),
    SPEC("spec", Boolean.class, true, value ->
    {
        Game game = Event.getGame();

        if (game != null)
            game.setSpec((Boolean) value);
    }),
    SUMO("sumo", Boolean.class, false, value ->
    {
        boolean state = (boolean) value;

        Game game = Event.getGame();

        if (game != null) {
            game.setSumo(state);

            if (state)
                com.minecraft.arcade.event.game.sumo.SumoMapGenerator.generate(game);
        }
    }),
    ;

    private final String name;

    private final Class<?> expectedClass;
    private final Object defaultValue;

    private Object value;

    private final VariableExecutor executor;

    private final boolean bigArgs;

    Variable(String name, Class<?> expectedClass, Object defaultValue)
    {
        this(name, expectedClass, defaultValue, defaultValue, value ->
        {
        }, false);
    }

    Variable(String name, Class<?> expectedClass, Object defaultValue, VariableExecutor executor)
    {
        this(name, expectedClass, defaultValue, defaultValue, executor, false);
    }

    Variable(String name, Class<?> expectedClass, Object defaultValue, VariableExecutor executor, boolean bigArgs)
    {
        this(name, expectedClass, defaultValue, defaultValue, executor, bigArgs);
    }

    public interface VariableExecutor
    {
        void execute(Object value);
    }

    public static Set<Variable> find()
    {
        return Stream.of(values())
                .sorted(Comparator
                        .comparing((Variable v) -> getTypePriority(v.getExpectedClass()))
                        .thenComparing(Variable::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static void reset()
    {
        Stream.of(values()).forEach(var -> var.setValue(var.getDefaultValue()));
    }

    private static int getTypePriority(Class<?> clazz)
    {
        return switch (clazz.getSimpleName()) {
            case "String" -> 0;
            case "Integer" -> 1;
            case "Boolean" -> 2;
            default -> 3;
        };
    }

    public static Variable fromName(String name)
    {
        return Stream.of(values())
                .filter(var -> var.name().equalsIgnoreCase(name) || var.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isDefaultValue()
    {
        return defaultValue.equals(value);
    }

    public boolean isValue(Object value)
    {
        if (this.value == null || value == null) return false;

        return switch (expectedClass.getSimpleName()) {
            case "String" -> this.value.toString().equalsIgnoreCase(value.toString());
            case "Integer" -> toInt(this.value) == toInt(value);
            case "Double" -> Double.compare(toDouble(this.value), toDouble(value)) == 0;
            case "Boolean" -> Boolean.parseBoolean(this.value.toString()) == Boolean.parseBoolean(value.toString());
            default -> this.value.equals(value);
        };
    }

    private int toInt(Object obj)
    {
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double toDouble(Object obj)
    {
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public Object getValue()
    {
        return switch (value.getClass().getSimpleName()) {
            case "Integer" -> Util.formatNumber((Integer) value);
            case "Double" -> Util.formatNumber((Double) value);
            case "String" -> Util.color((String) value);
            default -> value;
        };
    }

    @SuppressWarnings("unchecked")
    public <T> T getValueAs(Class<T> clazz)
    {
        if (clazz.isInstance(value)) {
            return (T) value;
        }

        if (value == null) return null;

        String str = value.toString();

        return switch (clazz.getSimpleName()) {
            case "Integer" -> tryParse(() -> (T) Integer.valueOf(str));
            case "Double" -> tryParse(() -> (T) Double.valueOf(str));
            case "Boolean" -> (T) (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")
                    ? Boolean.valueOf(str) : null);
            case "String" -> (T) str;
            default -> null;
        };
    }

    private <T> T tryParse(Supplier<T> supplier)
    {
        try {
            return supplier.get();
        } catch (Exception ignored) {
            return null;
        }
    }

    public Object parseValue(String input)
    {
        try {
            return switch (expectedClass.getSimpleName()) {
                case "Integer" -> Integer.parseInt(input);
                case "Double" -> Double.parseDouble(input);
                case "Boolean" -> {
                    if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false")) {
                        yield Boolean.parseBoolean(input);
                    } else {
                        yield null; // valor inválido, previne bugs
                    }
                }
                case "String" -> input;
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    public void setValue(Object value)
    {
        this.value = value;
        this.executor.execute(value);

        Core.getLogger().info("[Variable] '" + name + "' alterada para: " + getValue());
    }
}
