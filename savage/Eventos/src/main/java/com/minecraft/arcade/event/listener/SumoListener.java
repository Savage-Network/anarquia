package com.minecraft.arcade.event.listener;

import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.game.state.GameState;
import com.minecraft.arcade.event.user.User;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class SumoListener implements Listener {

    // -------------------------------------------------------
    // Sem dano direto — cancela todo dano de entidade
    // -------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Game game = Event.getGame();
        if (game == null || !game.isSumo()) return;
        if (!game.isState(GameState.PLAYING)) return;

        User user = (User) User.of(event.getEntity().getUniqueId());
        if (user == null || user.isSpectator()) return;

        // Cancela o dano mas aplica knockback se for hit de jogador
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

            if (e.getDamager() instanceof Player) {
                Player attacker = (Player) e.getDamager();
                Player victim = (Player) event.getEntity();

                // Knockback manual na direção do atacante → vítima
                Vector kb = victim.getLocation().toVector()
                        .subtract(attacker.getLocation().toVector())
                        .normalize()
                        .multiply(1.2)
                        .setY(0.4);

                event.setCancelled(true);
                victim.setVelocity(kb);
                return;
            }
        }

        event.setCancelled(true);
    }

    // -------------------------------------------------------
    // Cair na água ou no void = eliminado
    // -------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Game game = Event.getGame();
        if (game == null || !game.isSumo()) return;
        if (!game.isState(GameState.PLAYING)) return;

        User user = (User) User.of(player.getUniqueId());
        if (user == null || user.isSpectator()) return;

        double y = event.getTo().getY();
        Material block = event.getTo().getBlock().getType();

        boolean fellInWater = block == Material.WATER || block == Material.STATIONARY_WATER;
        boolean fellInVoid = y < 0;

        if (!fellInWater && !fellInVoid) return;

        eliminate(game, user, player);
    }

    // -------------------------------------------------------
    // Lógica de eliminação
    // -------------------------------------------------------
    private void eliminate(Game game, User user, Player player) {
        game.send("§c" + user.getAccount().getColoredName() + " §cfoi eliminado! §7(§e"
                + (game.getUsers().size() - 1) + "§7 restantes)");

        // Verifica vitória
        if (game.getUsers().size() - 1 <= 1) {
            game.getUsers().stream()
                    .filter(u -> !u.getAccount().getId().equals(user.getAccount().getId()))
                    .findFirst()
                    .ifPresent(winner -> {
                        game.setWinner(winner.getAccount().getId());
                        game.setState(GameState.ENDING);
                        game.getAllUsers().forEach(game::handleSidebar);
                    });
        }

        if (game.isSpec()) {
            user.setState(ArcadeState.DEAD);
            player.teleport(game.getSpawn());
        } else {
            if (user.getAccount().isStaffer()) {
                user.setJoin(Join.VANISH);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.teleport(game.getSpawn());
            } else {
                user.getAccount().redirect(ServerType.HUB);
            }
        }
    }
}
