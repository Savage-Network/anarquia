package com.minecraft.eventos.game;

import com.minecraft.eventos.engine.EventSession;

/**
 * Compat legado — delega para {@link EventSession}.
 * @deprecated Use {@link com.minecraft.eventos.engine.EventEngine#getSession()}.
 */
public final class Game extends EventSession {

    public void pulse() {
        var engine = com.minecraft.eventos.EventosPlugin.getInstance().getEventEngine();
        if (engine != null) super.pulse(engine);
    }
}
