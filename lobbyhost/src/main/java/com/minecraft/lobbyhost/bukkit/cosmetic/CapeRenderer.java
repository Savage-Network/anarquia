package com.minecraft.lobbyhost.bukkit.cosmetic;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.Cape;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Renderiza capas como {@link ItemDisplay} com {@link MapView} customizado.
 * A imagem PNG do resource é desenhada num mapa 128x128 e exibida nas costas do jogador.
 */
public final class CapeRenderer {

    // Escala visual do ItemDisplay (ajuste conforme necessário)
    private static final float SCALE     = 0.7f;
    // Offset Y do centro da capa em relação ao pé do jogador
    private static final float OFFSET_Y  = 1.2f;
    // Offset nas costas do jogador (negativo = para trás)
    private static final float OFFSET_Z  = -0.18f;

    private final Map<Cape, BufferedImage> images       = new HashMap<>();
    private final Map<Cape, MapView>       mapViews     = new HashMap<>();
    private final Map<UUID, UUID>          displays     = new HashMap<>();  // player → entity
    private final Map<UUID, BukkitTask>    trackTasks   = new HashMap<>();  // player → task de posição

    // -------------------------------------------------------------------------
    // Carregamento das imagens
    // -------------------------------------------------------------------------

    public void loadImages() {
        for (Cape cape : Cape.values()) {
            try (InputStream in = LobbyHostPlugin.getInstance().getResource("capas/" + cape.getFileName())) {
                if (in == null) {
                    warn("Recurso nao encontrado: capas/" + cape.getFileName());
                    continue;
                }
                BufferedImage raw = ImageIO.read(in);
                // Redimensiona para 128x128 (tamanho de um mapa do Minecraft)
                BufferedImage scaled = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                var g2 = scaled.createGraphics();
                g2.drawImage(raw, 0, 0, 128, 128, null);
                g2.dispose();
                images.put(cape, scaled);
            } catch (Exception e) {
                warn("Erro ao carregar " + cape.getFileName() + ": " + e.getMessage());
            }
        }
        info("Capas carregadas: " + images.size() + "/" + Cape.values().length);
    }

    // -------------------------------------------------------------------------
    // Spawn / remoção
    // -------------------------------------------------------------------------

    public void spawn(Player player, Cape cape) {
        remove(player); // remove capa anterior se houver

        if (cape == null) return;

        BufferedImage img = images.get(cape);
        if (img == null) {
            warn("Imagem nao carregada para capa: " + cape.name());
            return;
        }

        MapView view = getOrCreateMapView(cape, img);

        // Cria o ItemDisplay nas costas do jogador
        ItemDisplay display = spawnDisplay(player, view);
        if (display == null) return;

        displays.put(player.getUniqueId(), display.getUniqueId());

        // Task que reposiciona o display a cada tick (acompanha o jogador)
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || display.isDead()) {
                    cancel();
                    return;
                }
                // O display é passageiro do jogador — Paper move automaticamente.
                // Esta task só força re-mount caso o passageiro seja ejetado.
                if (!player.getPassengers().contains(display)) {
                    player.addPassenger(display);
                }
            }
        }.runTaskTimer(LobbyHostPlugin.getInstance(), 1L, 10L);

        trackTasks.put(player.getUniqueId(), task);
    }

    public void remove(Player player) {
        if (player == null) return;

        BukkitTask task = trackTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();

        UUID entityId = displays.remove(player.getUniqueId());
        if (entityId != null) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && !entity.isDead()) entity.remove();
        }
    }

    public void removeAll() {
        trackTasks.values().forEach(t -> { if (t != null) t.cancel(); });
        trackTasks.clear();
        displays.values().forEach(id -> {
            Entity e = Bukkit.getEntity(id);
            if (e != null && !e.isDead()) e.remove();
        });
        displays.clear();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Retorna (ou cria) o {@link MapView} com a imagem da capa desenhada. */
    private MapView getOrCreateMapView(Cape cape, BufferedImage img) {
        if (mapViews.containsKey(cape)) return mapViews.get(cape);

        MapView view = Bukkit.createMap(Bukkit.getWorlds().get(0));
        view.setScale(MapView.Scale.FARTHEST);
        view.setTrackingPosition(false);
        view.setUnlimitedTracking(false);

        // Remove o renderer padrão
        view.getRenderers().forEach(view::removeRenderer);

        // Renderer que desenha a imagem
        view.addRenderer(new MapRenderer(true) {
            private boolean rendered = false;

            @Override
            public void render(MapView map, MapCanvas canvas, Player viewer) {
                if (rendered) return;
                canvas.drawImage(0, 0, img);
                rendered = true;
            }
        });

        mapViews.put(cape, view);
        return view;
    }

    private ItemDisplay spawnDisplay(Player player, MapView view) {
        // Item de mapa com o MapView da capa
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setMapView(view);
        mapItem.setItemMeta(meta);

        try {
            Location loc = player.getLocation();
            ItemDisplay display = loc.getWorld().spawn(loc, ItemDisplay.class, d -> {
                d.setItemStack(mapItem);
                d.setBillboard(Display.Billboard.FIXED);
                d.setPersistent(false);
                d.setVisibleByDefault(true);

                // Escala e posição relativa ao pé do jogador
                Transformation transform = new Transformation(
                        new Vector3f(0f, OFFSET_Y, OFFSET_Z),
                        new AxisAngle4f(0f, 1f, 0f, 0f),     // sem rotação extra (herda do jogador)
                        new Vector3f(SCALE, SCALE, SCALE),
                        new AxisAngle4f()
                );
                d.setTransformation(transform);
            });

            // Monta como passageiro: o display segue o jogador sem teleporte por tick
            player.addPassenger(display);
            return display;

        } catch (Exception e) {
            warn("Erro ao criar ItemDisplay: " + e.getMessage());
            return null;
        }
    }

    private static void warn(String msg)  { LobbyHostPlugin.getInstance().getLogger().warning("[CapeRenderer] " + msg); }
    private static void info(String msg)  { LobbyHostPlugin.getInstance().getLogger().info("[CapeRenderer] " + msg); }
}
