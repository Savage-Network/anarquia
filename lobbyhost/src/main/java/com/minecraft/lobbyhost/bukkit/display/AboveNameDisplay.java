package com.minecraft.lobbyhost.bukkit.display;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.title.CustomTitle;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Nametags empilhados (titulo + tag + nick) estilo UnlimitedNameTags. */
public final class AboveNameDisplay {

    private static final NamespacedKey OWNER_KEY =
            NamespacedKey.minecraft("lobbyhost_stacked_nametag");

    private static final Map<UUID, List<UUID>> lineDisplays = new HashMap<>();
    private static final Map<UUID, String> lastSignature = new HashMap<>();
    private static BukkitTask followTask;

    private AboveNameDisplay() {}

    public static void startFollowTask(JavaPlugin plugin) {
        if (followTask != null) followTask.cancel();
        followTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                follow(player);
            }
        }, 10L, LobbySettings.NAMETAG_FOLLOW_TICKS);
    }

    public static void shutdown() {
        if (followTask != null) followTask.cancel();
        for (List<UUID> ids : lineDisplays.values()) {
            for (UUID id : ids) removeEntity(id);
        }
        lineDisplays.clear();
        lastSignature.clear();
    }

    public static void refresh(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            remove(player);
            return;
        }

        List<String> lines = buildLines(account);
        String signature = String.join("\u0001", lines);
        if (signature.equals(lastSignature.get(player.getUniqueId()))
                && lineDisplays.containsKey(player.getUniqueId())) {
            follow(player);
            return;
        }

        remove(player);
        if (lines.isEmpty()) return;

        List<UUID> spawned = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            double y = LobbySettings.NAMETAG_BASE_Y + i * LobbySettings.NAMETAG_LINE_SPACING;
            TextDisplay td = spawnLine(player, lines.get(i), y);
            if (td != null) spawned.add(td.getUniqueId());
        }
        if (!spawned.isEmpty()) {
            lineDisplays.put(player.getUniqueId(), spawned);
            lastSignature.put(player.getUniqueId(), signature);
        }
    }

    public static void remove(Player player) {
        List<UUID> ids = lineDisplays.remove(player.getUniqueId());
        lastSignature.remove(player.getUniqueId());
        if (ids != null) {
            for (UUID id : ids) removeEntity(id);
        }
    }

    private static void follow(Player player) {
        List<UUID> ids = lineDisplays.get(player.getUniqueId());
        if (ids == null || ids.isEmpty()) return;

        Location base = player.getLocation();
        for (int i = 0; i < ids.size(); i++) {
            var entity = Bukkit.getEntity(ids.get(i));
            if (!(entity instanceof TextDisplay td) || td.isDead()) {
                refresh(player);
                return;
            }
            double y = LobbySettings.NAMETAG_BASE_Y + i * LobbySettings.NAMETAG_LINE_SPACING;
            Location at = base.clone().add(0, y, 0);
            at.setYaw(base.getYaw());
            at.setPitch(0);
            td.teleport(at);
        }
    }

    private static List<String> buildLines(Account account) {
        List<String> lines = new ArrayList<>();

        String title = titleTextOf(account);
        if (title != null && !title.isBlank()) {
            lines.add(title);
        }

        String prefix = TagManager.formatNametagPrefixLine(account);
        if (prefix != null && !prefix.isBlank()) {
            lines.add(prefix);
        }

        String nick = TagManager.formatNametagNickLine(account);
        if (nick != null && !nick.isBlank()) {
            lines.add(nick);
        }
        return lines;
    }

    private static String titleTextOf(Account account) {
        CustomTitle ct = account.getCustomTitle();
        if (ct != null && ct.getTitle() != null && !ct.getTitle().isEmpty()) {
            String color = ct.getColor() == null || ct.getColor().isEmpty() ? "§d" : ct.getColor();
            return color + ct.getTitle();
        }
        return null;
    }

    private static TextDisplay spawnLine(Player target, String text, double heightOffset) {
        try {
            Location at = target.getLocation().add(0, heightOffset, 0);
            return target.getWorld().spawn(at, TextDisplay.class, td -> {
                td.text(Component.text(text));
                td.setBillboard(Display.Billboard.CENTER);
                td.setSeeThrough(false);
                td.setShadowed(true);
                td.setDefaultBackground(false);
                td.setBackgroundColor(Color.fromARGB(LobbySettings.NAMETAG_BG_ALPHA, 0, 0, 0));
                td.setAlignment(TextDisplay.TextAlignment.CENTER);
                td.setLineWidth(200);
                td.setPersistent(false);
                float s = LobbySettings.NAMETAG_TEXT_SCALE;
                td.setTransformation(new Transformation(
                        new Vector3f(),
                        new AxisAngle4f(),
                        new Vector3f(s, s, s),
                        new AxisAngle4f()
                ));
                td.getPersistentDataContainer().set(OWNER_KEY,
                        PersistentDataType.STRING, target.getUniqueId().toString());
            });
        } catch (Throwable t) {
            Core.getLogger().warning("StackedNametag.spawn falhou: " + t.getMessage());
            return null;
        }
    }

    private static void removeEntity(UUID id) {
        var entity = Bukkit.getEntity(id);
        if (entity != null && !entity.isDead()) entity.remove();
    }
}
