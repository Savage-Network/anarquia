package com.minecraft.lobbyhost.architect;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.architect.list.MainArchitect;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Le {@code architect.json} do disco e instancia o {@link Architect} adequado.
 *
 * <p>Espelha o {@code ArchitectLoader} do Savage; o JSON contem o
 * {@link ServerType} e as {@link SignedLocation}s nomeadas.
 */
public final class ArchitectLoader {

    private final JavaPlugin plugin;

    @Getter
    private Architect architect;

    @Getter
    private ServerType server;

    public ArchitectLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        String configFile = plugin.getConfig().getString("architect.config-file",
                "plugins/lobbyhost/architect.json");
        File file = new File(configFile);

        ArchitectConfig cfg;
        if (!file.exists()) {
            plugin.saveResource("architect.json", false);
            file = new File(plugin.getDataFolder(), "architect.json");
        }

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                cfg = Core.GSON.fromJson(reader, ArchitectConfig.class);
            } catch (Throwable t) {
                plugin.getLogger().warning("Falha ao ler architect.json: " + t.getMessage()
                        + " - usando defaults.");
                cfg = fallback(plugin);
            }
        } else {
            cfg = fallback(plugin);
        }

        if (cfg.locations == null) cfg.locations = new ArrayList<>();
        this.server = ServerType.parse(cfg.server);
        this.architect = create(server, cfg.locations);
        plugin.getLogger().info("Architect carregado: " + server
                + " (" + cfg.locations.size() + " locations)");
    }

    public void unload() {
        if (architect != null) architect.unload();
    }

    private Architect create(ServerType type, List<SignedLocation> locations) {
        // Phase 1 traz apenas o MainArchitect.
        // BedWarsArchitect, DuelsArchitect, etc. virao em proximas fases.
        return new MainArchitect(type, locations);
    }

    private static ArchitectConfig fallback(JavaPlugin plugin) {
        ArchitectConfig cfg = new ArchitectConfig();
        cfg.server = plugin.getConfig().getString("server.mode", "HUB");
        SignedLocation spawn = new SignedLocation(
                "spawn",
                plugin.getConfig().getString("architect.default-spawn.world", "world"),
                plugin.getConfig().getDouble("architect.default-spawn.x", 0.5),
                plugin.getConfig().getDouble("architect.default-spawn.y", 100),
                plugin.getConfig().getDouble("architect.default-spawn.z", 0.5),
                (float) plugin.getConfig().getDouble("architect.default-spawn.yaw", 0),
                (float) plugin.getConfig().getDouble("architect.default-spawn.pitch", 0)
        );
        cfg.locations = new ArrayList<>();
        cfg.locations.add(spawn);
        return cfg;
    }

    public static final class ArchitectConfig {
        public String server;
        public String displayName;
        public List<SignedLocation> locations;
    }
}
