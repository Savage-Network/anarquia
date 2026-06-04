package com.minecraft.eventos.bukkit.command.annotation;

import com.minecraft.eventos.account.context.objects.rank.RankType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacao para metodos de comando (estilo {@code @Command} do Savage).
 *
 * <p>Exemplo:
 * <pre>{@code
 * @Command(name = "tag", aliases = {"tags"}, rank = RankType.MEMBER)
 * public void executar(BukkitCommandContext ctx) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    String name();

    String[] aliases() default {};

    RankType rank() default RankType.MEMBER;

    boolean playerOnly() default false;

    String permission() default "";

    String description() default "";

    String usage() default "";
}
