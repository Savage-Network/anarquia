package com.minecraft.game.bukkit.display;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.manager.TagManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Nametags multi-linha acima da cabeca — mesmo layout do lobbyhost.
 */
public final class AboveNameDisplay {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();

    private static final NamespacedKey OWNER_KEY =
            NamespacedKey.minecraft("game_nametag");

    private static final Map<UUID, UUID> displays = new HashMap<>();
    private static final Map<UUID, String> lastSignature = new HashMap<>();

    private AboveNameDisplay() {}

    public static void init(JavaPlugin plugin) {
        Core.getLogger().info("[Nametag] TextDisplay empilhado (passageiro, igual lobby).");
    }

    public static void shutdown() {
        for (UUID displayId : new ArrayList<>(displays.values())) {
            removeEntity(displayId);
        }
        displays.clear();
        lastSignature.clear();
    }

    public static void refresh(Player target) {
        remove(target);
    }

    public static void refreshAllForViewer(Player viewer) {
        if (viewer == null || !viewer.isOnline()) return;
        for (Player target : Bukkit.getOnlinePlayers()) {
            remove(target);
        }
    }

    public static void remove(Player player) {
        if (player == null) return;
        UUID id = displays.remove(player.getUniqueId());
        lastSignature.remove(player.getUniqueId());
        if (id != null) removeEntity(id);
    }

    /**
     * Ordem no texto (topo → base): tag, nick nativo, cla.
     */
    public static List<String> buildLines(Account account) {
        List<String> lines = new ArrayList<>();

        String prefix = TagManager.formatNametagPrefixLine(account);
        if (prefix != null && !prefix.isBlank()) lines.add(prefix);

        String nick = TagManager.formatNametagNickLine(account);
        if (nick != null && !nick.isBlank()) lines.add(nick);

        String clan = TagManager.formatNametagClanLine(account);
        if (clan != null && !clan.isBlank()) lines.add(clan);

        return lines;
    }

    private static TextDisplay spawnDisplay(Player target, Component text) {
        try {
            return target.getWorld().spawn(target.getLocation(), TextDisplay.class, td -> {
                td.text(text);
                td.setBillboard(Display.Billboard.CENTER);
                td.setSeeThrough(NametagSettings.SEE_THROUGH);
                td.setShadowed(NametagSettings.TEXT_SHADOW);
                td.setDefaultBackground(false);
                td.setBackgroundColor(Color.fromARGB(NametagSettings.BACKGROUND_ALPHA, 0, 0, 0));
                td.setAlignment(TextDisplay.TextAlignment.CENTER);
                td.setLineWidth(NametagSettings.LINE_WIDTH);
                td.setPersistent(false);
                td.setVisibleByDefault(true);

                float s = NametagSettings.TEXT_SCALE;
                td.setTransformation(new Transformation(
                        new Vector3f(0f, NametagSettings.MOUNT_Y_OFFSET, 0f),
                        new AxisAngle4f(),
                        new Vector3f(s, s, s),
                        new AxisAngle4f()
                ));

                td.getPersistentDataContainer().set(OWNER_KEY,
                        PersistentDataType.STRING, target.getUniqueId().toString());
            });
        } catch (Throwable t) {
            Core.getLogger().warning("[Nametag] Falha ao criar TextDisplay: " + t.getMessage());
            return null;
        }
    }

    private static void mountOnPlayer(Player target, TextDisplay display) {
        if (!display.isValid()) return;
        if (!target.getPassengers().contains(display)) {
            target.addPassenger(display);
        }
    }

    private static void ensureMounted(Player target, TextDisplay display) {
        if (!NametagSettings.USE_PASSENGER_MOUNT) return;
        mountOnPlayer(target, display);
    }

    private static void removeEntity(UUID id) {
        Entity entity = Bukkit.getEntity(id);
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
    }
}
