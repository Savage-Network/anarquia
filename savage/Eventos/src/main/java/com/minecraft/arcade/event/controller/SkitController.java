package com.minecraft.arcade.event.controller;

import lombok.Getter;
import com.minecraft.arcade.event.game.skit.Skit;

import java.util.*;

public class SkitController {

    @Getter
    private static final Map<String, Skit> skits = new HashMap<>();

    public static List<Skit> find() {
        return new ArrayList<>(skits.values());
    }

    public static void save(Skit skit) {
        skits.put(skit.getName().toLowerCase(), skit);
    }

    public static Skit of(String name) {
        return skits.get(name.toLowerCase());
    }

    public static boolean has(String name) {
        return skits.containsKey(name.toLowerCase());
    }

    public static void remove(String name) {
        skits.remove(name.toLowerCase());
    }

    public static void clear() {
        skits.clear();
    }
}
