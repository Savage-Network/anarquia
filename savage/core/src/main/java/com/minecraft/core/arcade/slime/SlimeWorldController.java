package com.minecraft.core.arcade.slime;

import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.util.list.FileUtil;
import com.minecraft.core.util.list.Validator;
import com.minecraft.core.util.list.WorldUtil;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import com.grinderwolf.swm.plugin.config.WorldsConfig;
import lombok.SneakyThrows;
import org.bukkit.*;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SlimeWorldController {

    private static final Map<String, SlimeWorld> templateCache = new ConcurrentHashMap<>();

    public static void cloneWorldFromTemplate(SWMPlugin plugin, String templateWorld, String worldName) {
        cloneWorldFromTemplate(plugin, templateWorld, worldName, () -> {
        });
    }

    public static void cloneWorldFromTemplate(SWMPlugin plugin, String template, String name, SlimeAction afterGenerate) {
        SlimeLoader loader = plugin.getLoader(Constant.SLIME_FILE_LOADER);

        if (loader == null) {
            plugin.getLogger().info("Loader \"" + Constant.SLIME_FILE_LOADER + "\" não encontrado.");
            return;
        }

        WorldsConfig config = ConfigManager.getWorldConfig();

        WorldData worldData = config.getWorlds().get(template);

        if (worldData == null) {
            plugin.getLogger().info("Mundo \"" + template + "\" não encontrado.");
            return;
        }

        if (template.equalsIgnoreCase(name)) {
            plugin.getLogger().info("Os mundos não podem ter os nomes iguais. (" + template + "/" + name + ")");
            return;
        }

        // Gerar mundo
        plugin.getLogger().info("Gerando mundo \"" + name + "\" de \"" + template + "\"...");

        generateWorld(plugin, loader, template, name, afterGenerate, 3); // Tente gerar o mundo até 3 vezes
    }

    private static void generateWorld(SWMPlugin plugin, SlimeLoader loader, String template, String name, SlimeAction afterGenerate, int attempts) {
        if (attempts <= 0) {
            plugin.getLogger().info("Não foi possível gerar o mundo \"" + name + "\" após " + attempts + " tentativas.");
            return;
        }

        try {
            Instant start = Instant.now();

            /* Configurando Mapa */
            SlimeWorld templateWorld = templateCache.get(template);

            if (templateWorld == null) {
                // Configurando propriedades
                SlimePropertyMap properties = new SlimePropertyMap();

                properties.setString(SlimeProperties.WORLD_TYPE, "flat");
                properties.setString(SlimeProperties.DIFFICULTY, "normal");

                properties.setBoolean(SlimeProperties.ALLOW_ANIMALS, false);
                properties.setBoolean(SlimeProperties.ALLOW_MONSTERS, false);

                templateWorld = plugin.loadWorld(loader, template, true, properties);

                if (templateWorld == null) {
                    plugin.getLogger().info("O template \"" + template + "\" não foi encontrado.");
                    return;
                }

                templateCache.put(template, templateWorld);
            }

            SlimeWorld slimeWorld = templateWorld.clone(name, loader);

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    plugin.generateWorld(slimeWorld);

                    World bukkitWorld = Bukkit.getWorld(slimeWorld.getName());

                    if (bukkitWorld != null) {
                        // Verifica se o mundo foi carregado com sucesso
                        if (bukkitWorld.getBlockAt(0, 0, 0) != null) { // Verifica se o bloco (0, 0, 0) existe
                            // Localizações estão consistentes, execute a ação
                            WorldUtil.setup(bukkitWorld);

                            afterGenerate.run();

                            plugin.getLogger().info("Mundo \"" + name + "\" carregado e gerado em "
                                    + Duration.between(start, Instant.now()).toMillis() + "ms.");
                        } else {
                            plugin.getLogger().info("Não foi possível executar a ação do mundo " + name);
                            generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1); // Tente novamente
                        }
                    } else {
                        plugin.getLogger().info("Não foi possível executar a ação do mundo " + name);
                        generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1); // Tente novamente
                    }

                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Não foi possível gerar o mundo \"" + name + "\" (1x)", e);
                    generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1); // Tente novamente
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Não foi possível gerar o mundo \"" + name + "\" (2x)", e);
            generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1); // Tente novamente
        }
    }

    @SneakyThrows
    public static void removeWorldsFromLoader(String loaderName) {
        SlimeLoader loader = SWMPlugin.getInstance().getLoader(loaderName);

        if (loader == null) {
            Core.getLogger().info("Loader não encontrado!");
            return;
        }

        try {
            loader.listWorlds().forEach(world -> {
                try {
                    loader.deleteWorld(world);
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível deletar o mundo \"" + world + "\".", e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static void removeWorldsFromFileLoader() {
        SlimeLoader loader = SWMPlugin.getInstance().getLoader(Constant.SLIME_FILE_LOADER);

        if (loader == null) {
            Core.getLogger().info("Loader não encontrado!");
            return;
        }

        try {
            for (String worldName : loader.listWorlds()) {
                if (!Validator.hasNumberInString(worldName)) continue;

                World world = Bukkit.getWorld(worldName);

                if (world != null) {
                    Bukkit.unloadWorld(world, false);
                    FileUtil.delete(world.getWorldFolder());
                }

                loader.deleteWorld(worldName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unloadWorld(World world) {
        unloadWorld(world, () -> {
        });
    }

    public static void unloadWorld(World world, SlimeAction afterUnloadWorld) {
        SlimeLoader loader = SWMPlugin.getInstance().getLoader(Constant.SLIME_FILE_LOADER);

        if (loader == null) {
            Core.getLogger().log(Level.WARNING, "Não foi possível encontrar o loader \"Hikari\".");
            return;
        }

        if (world == null) return;

        String worldName = world.getName();
        File file = world.getWorldFolder();

        try {
            Bukkit.unloadWorld(worldName, false);
            FileUtil.delete(file);

            loader.deleteWorld(worldName);

        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Ocorreu um erro ao encerrar o mundo \"" + (worldName) + "\"...", e);
        }

        if (Bukkit.getWorld(worldName) == null) {
            Core.getLogger().info("[SlimeWorld] Mundo \"" + worldName + "\" deletado! Executando ação...");

            afterUnloadWorld.run();
        } else {
            Core.getLogger().info("[SlimeWorld] Não foi possível deletar o mundo \"" + worldName + "\".");
        }
    }

    /**
     * Carrega um mundo Slime diretamente (sem clonar)
     * Útil para carregar mundos de lobby que devem permanecer estáticos
     */
    public static void loadSlimeWorld(SWMPlugin plugin, String worldName, SlimeAction afterLoad) {
        loadSlimeWorld(plugin, worldName, afterLoad, 3);
    }

    /**
     * Carrega um mundo Slime diretamente com tentativas
     */
    private static void loadSlimeWorld(SWMPlugin plugin, String worldName, SlimeAction afterLoad, int attempts) {
        if (attempts <= 0) {
            plugin.getLogger().warning("Não foi possível carregar o mundo \"" + worldName + "\" após " + attempts + " tentativas.");
            return;
        }

        try {
            SlimeLoader loader = plugin.getLoader(Constant.SLIME_FILE_LOADER);

            if (loader == null) {
                plugin.getLogger().warning("Loader \"" + Constant.SLIME_FILE_LOADER + "\" não encontrado.");
                return;
            }

            WorldsConfig config = ConfigManager.getWorldConfig();
            WorldData worldData = config.getWorlds().get(worldName);

            if (worldData == null) {
                plugin.getLogger().warning("Mundo \"" + worldName + "\" não encontrado no config do SlimeWorldManager.");
                return;
            }

            Instant start = Instant.now();

            // Configurar propriedades do mundo
            SlimePropertyMap properties = new SlimePropertyMap();
            
            // Usar configurações do worlds.yml
            if (worldData.getSpawn() != null) {
                String[] spawnParts = worldData.getSpawn().split(",");
                if (spawnParts.length == 3) {
                    properties.setInt(SlimeProperties.SPAWN_X, Integer.parseInt(spawnParts[0].trim()));
                    properties.setInt(SlimeProperties.SPAWN_Y, Integer.parseInt(spawnParts[1].trim()));
                    properties.setInt(SlimeProperties.SPAWN_Z, Integer.parseInt(spawnParts[2].trim()));
                }
            }

            properties.setString(SlimeProperties.DIFFICULTY, worldData.getDifficulty());
            properties.setBoolean(SlimeProperties.ALLOW_ANIMALS, worldData.isAllowAnimals());
            properties.setBoolean(SlimeProperties.ALLOW_MONSTERS, worldData.isAllowMonsters());
            properties.setBoolean(SlimeProperties.PVP, worldData.isPvp());
            properties.setString(SlimeProperties.ENVIRONMENT, worldData.getEnvironment());
            properties.setString(SlimeProperties.WORLD_TYPE, worldData.getWorldType());

            // Carregar o mundo
            SlimeWorld slimeWorld = plugin.loadWorld(loader, worldName, true, properties);

            if (slimeWorld == null) {
                plugin.getLogger().warning("Não foi possível carregar o mundo Slime \"" + worldName + "\".");
                loadSlimeWorld(plugin, worldName, afterLoad, attempts - 1);
                return;
            }

            // Gerar o mundo no Bukkit
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    plugin.generateWorld(slimeWorld);

                    World bukkitWorld = Bukkit.getWorld(slimeWorld.getName());

                    if (bukkitWorld != null) {
                        WorldUtil.setup(bukkitWorld);
                        
                        afterLoad.run();

                        plugin.getLogger().info("Mundo Slime \"" + worldName + "\" carregado em "
                                + Duration.between(start, Instant.now()).toMillis() + "ms.");
                    } else {
                        plugin.getLogger().warning("Mundo \"" + worldName + "\" não foi gerado corretamente.");
                        loadSlimeWorld(plugin, worldName, afterLoad, attempts - 1);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Erro ao gerar mundo \"" + worldName + "\"", e);
                    loadSlimeWorld(plugin, worldName, afterLoad, attempts - 1);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao carregar mundo Slime \"" + worldName + "\"", e);
            loadSlimeWorld(plugin, worldName, afterLoad, attempts - 1);
        }
    }

    public interface SlimeAction {
        void run();
    }
}
