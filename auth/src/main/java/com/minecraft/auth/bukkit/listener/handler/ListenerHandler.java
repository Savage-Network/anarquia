package com.minecraft.auth.bukkit.listener.handler;

import com.minecraft.auth.Core;
import com.minecraft.auth.util.ClassScanner;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Espelho de {@code ListenerHandler} do Savage. Faz scan de classes
 * que implementam {@link Listener} e as registra automaticamente.
 */
@RequiredArgsConstructor
public final class ListenerHandler {

    private final JavaPlugin plugin;

    public void handle(String packageName) {
        long start = System.currentTimeMillis();
        int registered = 0;

        Core.getLogger().info("Registrando listeners em " + packageName + "...");

        for (Class<?> listenerClass : ClassScanner.getClassesForPackage(plugin, packageName)) {
            if (!Listener.class.isAssignableFrom(listenerClass)) continue;
            if (listenerClass.isInterface()) continue;

            try {
                Listener listener = (Listener) listenerClass.getDeclaredConstructor().newInstance();
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
                registered++;
            } catch (Throwable t) {
                Core.getLogger().log(Level.WARNING,
                        "Falha ao registrar listener " + listenerClass.getSimpleName(), t);
            }
        }

        Core.getLogger().info("Listeners registrados: " + registered
                + " (" + (System.currentTimeMillis() - start) + "ms)");
    }
}
