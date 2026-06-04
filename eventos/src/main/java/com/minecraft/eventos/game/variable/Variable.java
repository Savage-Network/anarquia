package com.minecraft.eventos.game.variable;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Espelho de {@code Variable} do modulo Eventos do Savage.
 *
 * <p>Cada {@link Key} tem um valor default; {@code /variable set/reset} altera.
 */
public final class Variable {

    @Getter
    private static final Map<Key, Object> values = new HashMap<>();

    static {
        for (Key k : Key.values()) values.put(k, k.getDefaultValue());
    }

    public static Object get(Key key) {
        return values.getOrDefault(key, key.getDefaultValue());
    }

    public static int getInt(Key key) {
        Object o = get(key);
        return o instanceof Number n ? n.intValue()
                : Integer.parseInt(String.valueOf(o));
    }

    public static boolean getBoolean(Key key) {
        Object o = get(key);
        return o instanceof Boolean b ? b
                : Boolean.parseBoolean(String.valueOf(o));
    }

    public static void set(Key key, Object value) {
        values.put(key, value);
    }

    public static void reset(Key key) {
        values.put(key, key.getDefaultValue());
    }

    public static void resetAll() {
        values.clear();
        for (Key k : Key.values()) values.put(k, k.getDefaultValue());
    }

    @Getter
    public enum Key {
        MIN_PLAYERS(2),
        MAX_PLAYERS(50),
        STARTING_COUNTDOWN(30),
        ENDING_COUNTDOWN(10),
        DIE_Y(50),
        FRIENDLY_FIRE(true),
        ALLOW_SPECTATOR(true);

        private final Object defaultValue;

        Key(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        public static Key of(String raw) {
            try {
                return valueOf(raw.toUpperCase().replace('-', '_'));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
