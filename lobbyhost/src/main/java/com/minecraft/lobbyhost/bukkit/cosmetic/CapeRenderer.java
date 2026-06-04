package com.minecraft.lobbyhost.bukkit.cosmetic;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.Cape;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.Transformation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CapeRenderer {

    private final Map<Cape, BufferedImage> images = new HashMap<>();
    private final Map<UUID, ItemDisplay> activeCapes = new HashMap<>();

    public void loadImages() {
        for (Cape cape : Cape.values()) {
            try (InputStream in = LobbyHostPlugin.getInstance().getResource("capas/" + cape.getFileName())) {
                if (in != null) {
                    BufferedImage img = ImageIO.read(in);
                    // Redimensiona para 128x128 (padrão de mapas do Minecraft)
                    BufferedImage scaled = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g2 = scaled.createGraphics();
                    g2.drawImage(img, 0, 0, 128, 128, null);
                    g2.dispose();
                    images.put(cape, scaled);
                } else {
                    LobbyHostPlugin.getInstance().getLogger().warning("Capa nao encontrada nos recursos: " + cape.getFileName());
                }
            } catch (Exception e) {
                LobbyHostPlugin.getInstance().getLogger().severe("Erro ao carregar capa " + cape.name() + ": " + e.getMessage());
            }
        }
    }

    public void spawn(Player player, Cape cape) {
        remove(player);

        if (cape == null) return;

        BufferedImage img = images.get(cape);
        if (img == null) return;

        MapView view = Bukkit.createMap(player.getWorld());
        view.getRenderers().clear();
        view.addRenderer(new MapRenderer() {
            private boolean rendered = false;
            @Override
            public void render(MapView map, MapCanvas canvas, Player p) {
                if (rendered) return;
                canvas.drawImage(0, 0, img);
                rendered = true;
            }
        });

        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setMapView(view);
        mapItem.setItemMeta(meta);

        Location loc = player.getLocation();
        ItemDisplay display = player.getWorld().spawn(loc, ItemDisplay.class, ent -> {
            ent.setItemStack(mapItem);
            ent.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            
            Transformation trans = ent.getTransformation();
            trans.getScale().set(0.6f, 0.9f, 0.05f);
            trans.getTranslation().set(0.0f, -0.7f, -0.25f);
            trans.getLeftRotation().set(0.0f, 1.0f, 0.0f, 0.0f); // 180 graus no eixo Y
            ent.setTransformation(trans);
        });

        player.addPassenger(display);
        activeCapes.put(player.getUniqueId(), display);
    }

    public void remove(Player player) {
        ItemDisplay display = activeCapes.remove(player.getUniqueId());
        if (display != null && display.isValid()) {
            display.remove();
        }
    }

    public void removeAll() {
        for (ItemDisplay display : activeCapes.values()) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        activeCapes.clear();
    }
}
