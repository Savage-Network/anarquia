package com.minecraft.eventos.integration;

import com.minecraft.eventos.Constant;
import com.minecraft.eventos.Core;
import com.minecraft.eventos.engine.EventSession;
import com.minecraft.eventos.game.state.GameState;

/** Publica estado live no Redis para lobby/proxy. */
public final class NetworkLivePublisher {

    private NetworkLivePublisher() {}

    public static void publish(EventSession session) {
        if (session == null || Core.getRedis() == null || !Core.getRedis().isEnabled()) return;

        String mode = session.getActiveMode() != null
                ? session.getActiveMode().displayName() : session.getModeId();
        boolean live = session.getState() == GameState.STARTING
                || session.getState() == GameState.PLAYING;

        String payload = String.join("|",
                mode,
                session.getState().name(),
                String.valueOf(session.alives().size()),
                String.valueOf(session.isAutomated()),
                session.getScheduledTaskId() != null ? session.getScheduledTaskId() : "",
                live ? "1" : "0"
        );

        try {
            Core.getRedis().publish(Constant.REDIS_EVENT_LIVE_CHANNEL, payload);
            Core.getRedis().set("eventos:live", payload, 120);
        } catch (Exception ignored) {
        }
    }

    public static void clear() {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) return;
        try {
            Core.getRedis().set("eventos:live", "offline|WAITING|0|0||0", 60);
        } catch (Exception ignored) {
        }
    }
}
