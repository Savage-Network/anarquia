package com.minecraft.lobbyhost.architect;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Espelho do {@code Architect} abstract do Savage.
 *
 * <p>Cada subclasse implementa o comportamento de um tipo especifico de lobby
 * (HUB principal, HUB_BEDWARS, etc.): NPCs, hotbar, sidebar, chat, hologramas.
 */
public abstract class Architect {

    protected final ServerType server;
    protected final List<SignedLocation> locations;

    protected Architect(ServerType server, List<SignedLocation> locations) {
        this.server = server;
        this.locations = locations;
    }

    public abstract void load();

    public abstract void unload();

    public abstract void onJoin(Player player);

    public abstract void onQuit(Player player);

    public abstract void handleHotbar(Player player);

    public abstract void handleSidebar(Player player);

    public SignedLocation location(String name) {
        return locations.stream()
                .filter(l -> l.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public ServerType getServer() {
        return server;
    }

    public List<SignedLocation> getLocations() {
        return locations;
    }
}
