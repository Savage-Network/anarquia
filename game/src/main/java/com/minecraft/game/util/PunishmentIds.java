package com.minecraft.game.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class PunishmentIds {

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

    private PunishmentIds() {}

    public static String newId() {
        String letters = ThreadLocalRandom.current()
                .ints(4, 0, LETTERS.length())
                .mapToObj(i -> String.valueOf(LETTERS.charAt(i)))
                .collect(Collectors.joining());
        return "#" + letters + ThreadLocalRandom.current().nextInt(10);
    }
}
