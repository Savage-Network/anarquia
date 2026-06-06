package com.minecraft.eventos.engine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Registro de modos disponiveis. */
public final class EventModeRegistry {

    private final Map<String, EventMode> modes = new LinkedHashMap<>();

    public void register(EventMode mode) {
        if (mode == null) return;
        modes.put(mode.id().toLowerCase(), mode);
    }

    public Optional<EventMode> find(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return Optional.ofNullable(modes.get(id.toLowerCase()));
    }

    public EventMode require(String id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("Modo desconhecido: " + id));
    }

    public Collection<EventMode> all() {
        return modes.values();
    }

    public Collection<String> ids() {
        return modes.keySet();
    }
}
