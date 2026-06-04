package com.minecraft.game.bukkit.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um metodo como tab-completer de um comando {@link Command}.
 *
 * <p>O metodo deve receber {@code BukkitCommandContext} e retornar {@code List<String>}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Completer {

    String name();

    String[] subCommands() default {};
}
