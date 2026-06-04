package com.minecraft.arcade.eggwars.arcade.arena.context.objects.event.phase;

import com.minecraft.arcade.eggwars.arcade.arena.context.objects.event.action.EventAction;
import com.minecraft.arcade.eggwars.structure.team.Team;
import com.minecraft.core.Core;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Sound;

import java.util.concurrent.CompletableFuture;

@Getter
@AllArgsConstructor
public enum EventPhase {

    SUDDEN_DEATH("Morte Súbita", arena -> {
        arena.getContext().setEvent(null);

        for (Team team : arena.getTeamList())
            Core.getPlatform().runSync(team::destructEgg);

        arena.getUsers().forEach(user -> CompletableFuture.runAsync(() -> arena.getArcade().handleSidebar(user)));

        arena.send("§5§lMORTE SÚBITA: §cTodos os ovos foram removidos.");
        arena.sound(Sound.ENDERDRAGON_GROWL, 13, 1);
    });

    private final String name;
    private final EventAction action;
}
