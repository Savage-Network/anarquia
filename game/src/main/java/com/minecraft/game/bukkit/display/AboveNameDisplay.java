package com.minecraft.game.bukkit.display;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.title.CustomTitle;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Espelho de {@code AboveNameManager} do Savage, usando <b>TextDisplay 1.21</b>
 * em vez de ArmorStand invisivel. Cada jogador com {@link CustomTitle}
 * recebe um TextDisplay flutuando 2.3 blocos acima da cabeca.
 *
 * <p>Follow task atualiza posicao a cada tick (montar diretamente no player
 * via {@code addPassenger} causaria conflito com a tag nativa - mantemos
 * teleport manual por simplicidade).
 */
public final class AboveNameDisplay {

    private static final NamespacedKey OWNER_KEY =
            NamespacedKey.minecraft("game_abovename_owner");

    private static final Map<UUID, UUID> displays = new HashMap<>();
    private static BukkitTask followTask;

    private AboveNameDisplay() {}

    public static void startFollowTask(JavaPlugin plugin) {
        if (followTask != null) followTask.cancel();
        followTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                refresh(p);
            }
        }, 20L, 5L);
    }

    public static void shutdown() {
        if (followTask != null) followTask.cancel();
        for (UUID displayId : displays.values()) removeEntity(displayId);
        displays.clear();
    }

    public static void refresh(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) { remove(player); return; }

        String text = titleTextOf(account);
        if (text == null || text.isEmpty()) { remove(player); return; }

        UUID displayId = displays.get(player.getUniqueId());
        TextDisplay td = displayId == null ? null
                : (Bukkit.getEntity(displayId) instanceof TextDisplay t ? t : null);

        if (td == null || td.isDead()) {
            td = spawn(player, text);
            if (td != null) displays.put(player.getUniqueId(), td.getUniqueId());
        } else {
            // atualiza texto e teleporta
            td.text(Component.text(text));
            td.teleport(player.getLocation().add(0, 2.3, 0));
        }
    }

    public static void remove(Player player) {
        UUID id = displays.remove(player.getUniqueId());
        if (id != null) removeEntity(id);
    }

    private static String titleTextOf(Account account) {
        CustomTitle ct = account.getCustomTitle();
        if (ct != null && ct.getTitle() != null && !ct.getTitle().isEmpty()) {
            String color = ct.getColor() == null || ct.getColor().isEmpty() ? "§d" : ct.getColor();
            return color + ct.getTitle();
        }
        // futuramente: cosmetic TITLE ativo
        return null;
    }

    private static TextDisplay spawn(Player target, String text) {
        try {
            return target.getWorld().spawn(target.getLocation().add(0, 2.3, 0),
                    TextDisplay.class, td -> {
                        td.text(Component.text(text));
                        td.setBillboard(Display.Billboard.CENTER);
                        td.setSeeThrough(true);
                        td.setShadowed(true);
                        td.setPersistent(false);
                        td.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
                        td.setTransformation(new Transformation(
                                new Vector3f(),
                                new AxisAngle4f(),
                                new Vector3f(1f, 1f, 1f),
                                new AxisAngle4f()
                        ));
                        td.getPersistentDataContainer().set(OWNER_KEY,
                                PersistentDataType.STRING, target.getUniqueId().toString());
                    });
        } catch (Throwable t) {
            Core.getLogger().warning("AboveNameDisplay.spawn falhou: " + t.getMessage());
            return null;
        }
    }

    private static void removeEntity(UUID id) {
        var entity = Bukkit.getEntity(id);
        if (entity != null && !entity.isDead()) entity.remove();
    }
}
