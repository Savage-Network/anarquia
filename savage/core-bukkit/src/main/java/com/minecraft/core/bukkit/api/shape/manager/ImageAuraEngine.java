package com.minecraft.core.bukkit.api.shape.manager;

import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.shape.ImageShapeBuilder;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ImageAuraEngine {

    private static final int MAX_IMAGE_SIZE = 96;
    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int READ_TIMEOUT_MS = 12000;

    @Getter
    private static final ImageAuraEngine instance = new ImageAuraEngine();

    private final Map<UUID, AuraSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, CachedShape> urlCache = new ConcurrentHashMap<>();

    public void startFromResource(Player player, String resourcePath, float ratio, int scanQuality) {
        BufferedImage image = loadFromResources(resourcePath);
        if (image == null) {
            player.sendMessage("§cImagem da aura não encontrada no servidor.");
            return;
        }
        start(player, buildShape(image, scanQuality, ratio));
    }

    public void startFromUrl(Player player, String rawUrl, float ratio, int scanQuality) {
        String url = ImgurUtil.normalize(rawUrl);
        CachedShape cached = urlCache.get(url);
        if (cached != null) {
            start(player, cached);
            return;
        }

        player.sendMessage("§eCarregando aura...");

        Bukkit.getScheduler().runTaskAsynchronously(BukkitCore.getInstance(), () -> {
            BufferedImage image = loadFromUrl(url);
            if (image == null) {
                Bukkit.getScheduler().runTask(BukkitCore.getInstance(), () ->
                        player.sendMessage("§cNão foi possível baixar a imagem. Use um link direto do Imgur (i.imgur.com/...png)."));
                return;
            }

            CachedShape shape = buildShape(image, scanQuality, ratio);
            urlCache.put(url, shape);

            Bukkit.getScheduler().runTask(BukkitCore.getInstance(), () -> {
                if (player.isOnline())
                    start(player, shape);
            });
        });
    }

    public void stop(Player player) {
        AuraSession session = sessions.remove(player.getUniqueId());
        if (session != null && session.task != null)
            session.task.cancel();
    }

    public boolean isActive(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    private void start(Player player, CachedShape shape) {
        stop(player);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(BukkitCore.getInstance(), () -> {
            if (!player.isOnline()) {
                stop(player);
                return;
            }
            renderAboveHead(player, shape);
        }, 0L, 2L);

        sessions.put(player.getUniqueId(), new AuraSession(shape, task));
    }

    private void renderAboveHead(Player player, CachedShape shape) {
        Location anchor = player.getLocation().clone().add(0, 2.35, 0);
        float yaw = player.getLocation().getYaw();

        Map<Location, Color> particleMap = shape.builder.getParticles(anchor, 0, yaw);

        for (Map.Entry<Location, Color> entry : particleMap.entrySet()) {
            Location spot = entry.getKey();
            Color color = entry.getValue();

            if (color.getRed() + color.getGreen() + color.getBlue() < 15)
                color = Color.fromRGB(12, 12, 12);

            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                    EnumParticle.REDSTONE,
                    true,
                    (float) spot.getX(),
                    (float) spot.getY(),
                    (float) spot.getZ(),
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    1.0F,
                    0
            );

            sendToViewers(player, packet);
        }
    }

    private void sendToViewers(Player owner, Packet<?> packet) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.canSee(owner))
                ((CraftPlayer) target).getHandle().playerConnection.sendPacket(packet);
        }
    }

    private CachedShape buildShape(BufferedImage source, int scanQuality, float ratio) {
        BufferedImage resized = resize(source, MAX_IMAGE_SIZE);
        ImageShapeBuilder builder = new ImageShapeBuilder(resized, Math.max(1, scanQuality));
        builder.setAnchor(resized.getWidth() / 2, resized.getHeight() / 2);
        builder.setDisplayRatio(ratio);
        return new CachedShape(builder);
    }

    private BufferedImage loadFromResources(String path) {
        String resourcePath = path.endsWith(".png") ? path : path + ".png";
        try (InputStream in = BukkitCore.getInstance().getResource(resourcePath)) {
            if (in == null)
                return null;
            return resize(ImageIO.read(in), MAX_IMAGE_SIZE);
        } catch (Exception e) {
            Core.getLogger().warning("Erro ao carregar aura " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    private BufferedImage loadFromUrl(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (SavageMC ImageAura)");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);

            try (InputStream in = connection.getInputStream()) {
                BufferedImage image = ImageIO.read(in);
                if (image == null)
                    return null;
                return resize(image, MAX_IMAGE_SIZE);
            }
        } catch (Exception e) {
            Core.getLogger().warning("Erro ao baixar aura " + url + ": " + e.getMessage());
            return null;
        }
    }

    private BufferedImage resize(BufferedImage source, int maxSize) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= maxSize && height <= maxSize)
            return toArgb(source);

        double scale = Math.min((double) maxSize / width, (double) maxSize / height);
        int newW = Math.max(1, (int) (width * scale));
        int newH = Math.max(1, (int) (height * scale));

        BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, newW, newH, null);
        g.dispose();
        return scaled;
    }

    private BufferedImage toArgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_ARGB)
            return source;
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    private static class CachedShape {
        final ImageShapeBuilder builder;

        CachedShape(ImageShapeBuilder builder) {
            this.builder = builder;
        }
    }

    private static class AuraSession {
        final CachedShape shape;
        final BukkitTask task;

        AuraSession(CachedShape shape, BukkitTask task) {
            this.shape = shape;
            this.task = task;
        }
    }
}
