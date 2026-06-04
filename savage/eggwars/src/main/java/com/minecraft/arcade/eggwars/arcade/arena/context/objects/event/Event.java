package com.minecraft.arcade.eggwars.arcade.arena.context.objects.event;

import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.arcade.arena.context.objects.event.phase.EventPhase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event {

    private EventPhase phase = EventPhase.SUDDEN_DEATH;

    private int time = 60 * 10; // 10 minutos para morte súbita

    public void handle(Arena arena) {
        this.phase.getAction().execute(arena);
    }

    public boolean isNotPhase(EventPhase phase) {
        return !this.phase.equals(phase);
    }
}
