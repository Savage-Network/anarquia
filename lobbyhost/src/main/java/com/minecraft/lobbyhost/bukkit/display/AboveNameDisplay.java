package com.minecraft.lobbyhost.bukkit.display;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.title.CustomTitle;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.bukkit.vanish.VanishService;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Nametags multi-linha acima da cabeca (estilo UnlimitedNameTags).
 * Um {@link TextDisplay} montado no jogador — sem config, sem teleporte por tick.
 */
public final class AboveNameDisplay {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();

    private static final NamespacedKey OWNER_KEY =
            NamespacedKey.minecraft("lobbyhost_nametag");

    private static final Map<UUID, UUID> displays = new HashMap<>();
    private static final Map<UUID, String> lastSignature = new HashMap<>();

    private AboveNameDisplay() {}

    public static void init(JavaPlugin plugin) {
        Core.getLogger().info("[Nametag] TextDisplay empilhado ativo (passageiro, sem config).");
    }

    public static void shutdown() {
        for (UUID displayId : new ArrayList<>(displays.values())) {
            removeEntity(displayId);
        }
        displays.clear();
        lastSignature.clear();
    }

    public static void refresh(Player target) {
        if (target == null || !target.isOnline()) return;

        if (VanishService.isVanished(target)) {
            remove(target);
            return;
        }

        Account account = Core.getAccountController().of(target.getUniqueId());
        if (account == null) {
            remove(target);
            return;
        }

        List<String> lines = buildLines(account);
        if (lines.isEmpty()) {
            remove(target);
            return;
        }

        String signature = String.join("\u0001", lines);
        Component text = buildComponent(lines);

        UUID existingId = displays.get(target.getUniqueId());
        if (signature.equals(lastSignature.get(target.getUniqueId())) && existingId != null) {
            Entity entity = Bukkit.getEntity(existingId);
            if (entity instanceof TextDisplay td && !td.isDead()) {
                td.text(text);
                ensureMounted(target, td);
                return;
            }
        }

        remove(target);

        TextDisplay display = spawnDisplay(target, text);
        if (display == null) return;

        if (NametagSettings.USE_PASSENGER_MOUNT) {
            mountOnPlayer(target, display);
        }

        displays.put(target.getUniqueId(), display.getUniqueId());
        lastSignature.put(target.getUniqueId(), signature);
    }

    /** Com entidades reais, novos viewers ja enxergam o display — nada a fazer. */
    public static void refreshAllForViewer(Player viewer) {
        if (viewer == null || !viewer.isOnline()) return;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(viewer)) continue;
            refresh(target);
        }
    }

    public static void remove(Player player) {
        if (player == null) return;
        UUID id = displays.remove(player.getUniqueId());
        lastSignature.remove(player.getUniqueId());
        if (id != null) removeEntity(id);
    }

    /**
     * Ordem no mundo (baixo → cima): nick, tag, cla, titulo.
     * No TextDisplay a primeira linha fica no topo — invertemos na hora de montar o texto.
     */
    public static List<String> buildLines(Account account) {
        List<String> lines = new ArrayList<>();

        String nick = TagManager.formatNametagNickLine(account);
        if (nick != null && !nick.isBlank()) lines.add(nick);

        String prefix = TagManager.formatNametagPrefixLine(account);
        if (prefix != null && !prefix.isBlank()) lines.add(prefix);

        String clan = TagManager.formatNametagClanLine(account);
        if (clan != null && !clan.isBlank()) lines.add(clan);

        String title = titleTextOf(account);
        if (title != null && !title.isBlank()) lines.add(title);

        return lines;
    }

    private static Component buildComponent(List<String> bottomToTop) {
        List<String> topFirst = new ArrayList<>(bottomToTop);
        Collections.reverse(topFirst);
        String legacy = String.join(NametagSettings.LINE_SEPARATOR, topFirst);
        return LEGACY.deserialize(legacy);
    }

    private static String titleTextOf(Account account) {
        var ct = account.getCustomTitle();
        if (ct != null && ct.getTitle() != null && !ct.getTitle().isEmpty()) {
            String color = ct.getColor() == null || ct.getColor().isEmpty() ? "§d" : ct.getColor();
            return color + ct.getTitle();
        }
        return null;
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
                Transformation transform = new Transformation(
                        new Vector3f(0f, NametagSettings.MOUNT_Y_OFFSET, 0f),
                        new AxisAngle4f(),
                        new Vector3f(s, s, s),
                        new AxisAngle4f()
                );
                td.setTransformation(transform);

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
