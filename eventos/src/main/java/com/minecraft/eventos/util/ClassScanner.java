package com.minecraft.eventos.util;

import com.minecraft.eventos.Core;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Localiza classes em um pacote dentro do JAR do plugin.
 * Implementacao baseada em {@code com.minecraft.core.util.list.loader.ClassLoader}
 * do Savage, mas adaptada para Paper 1.21 (sem dependencia de Guava/Reflections).
 */
public final class ClassScanner {

    private ClassScanner() {}

    public static List<Class<?>> getClassesForPackage(JavaPlugin plugin, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        ClassLoader classLoader = plugin.getClass().getClassLoader();

        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("jar".equalsIgnoreCase(resource.getProtocol())) {
                    String jarPath = resource.getPath().substring(5, resource.getPath().indexOf('!'));
                    String decoded = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
                    scanJar(decoded, packagePath, classLoader, classes);
                }
            }
        } catch (IOException e) {
            Core.getLogger().warning("Falha ao escanear pacote " + packageName + ": " + e.getMessage());
        }

        return classes;
    }

    private static void scanJar(String jarPath, String packagePath, ClassLoader cl, List<Class<?>> classes) {
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".class")) continue;
                if (!name.startsWith(packagePath + "/")) continue;
                if (name.contains("$")) continue;

                String className = name.substring(0, name.length() - 6).replace('/', '.');
                try {
                    classes.add(Class.forName(className, false, cl));
                } catch (Throwable t) {
                    Core.getLogger().fine("Pulando classe " + className + ": " + t.getMessage());
                }
            }
        } catch (IOException e) {
            Core.getLogger().warning("Falha ao ler JAR " + jarPath + ": " + e.getMessage());
        }
    }
}
