package com.minecraft.proxyhost.routing;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.proxy.NetworkPublisher;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Altera e persiste {@code routing.eventos-access.enabled} em tempo de execução. */
public final class EventosAccessController {

    private EventosAccessController() {}

    public static boolean setEnabled(boolean enabled) {
        if (EventosAccessGuard.isOpen() == enabled) return false;
        EventosAccessGuard.setEnabled(enabled);
        try {
            persistConfig(enabled);
            Core.getPlugin().reloadRoutingConfig();
        } catch (Exception e) {
            Core.getLogger().warn("Falha ao persistir eventos-access: {}", e.getMessage());
            NetworkPublisher.publishStatusNow();
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static void persistConfig(boolean enabled) throws Exception {
        Path file = Core.getPlugin().getDataDirectory().resolve("config.yml");
        Yaml yaml = new Yaml();
        Map<String, Object> root;
        try (InputStream in = Files.newInputStream(file)) {
            root = yaml.load(in);
        }
        if (root == null) root = new LinkedHashMap<>();

        Map<String, Object> routing = (Map<String, Object>) root.computeIfAbsent("routing", k -> new LinkedHashMap<>());
        Map<String, Object> access = (Map<String, Object>) routing.computeIfAbsent("eventos-access", k -> new LinkedHashMap<>());
        access.put("enabled", enabled);

        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            yaml.dump(root, writer);
        }
    }
}
