package com.minecraft.arcade.thebridge.listener;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.backend.data.list.server.RouteData;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.member.list.thebridge.TheBridgeMember;
import com.minecraft.arcade.thebridge.arcade.Arcade;
import com.minecraft.arcade.thebridge.arcade.arena.Arena;
import com.minecraft.arcade.thebridge.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.UUID;

public class SearchListener implements Listener {

    private final RouteData routeData;

    public SearchListener() {
        this.routeData = Core.getRouteData();
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) {

            UUID id = event.getUniqueId();

            // Buscando dados da rota
            RouteContext route = routeData.read(id);

            if (route == null || !route.isValidArcade()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.INVALID_ROUTE_MESSAGE);
                return;
            }

            routeData.delete(id);

            ArcadeRouteContext arcadeRoute = route.getArcade();

            Arcade arcade = (Arcade) BukkitCore.getManager().getArcade().read(arcadeRoute.getArcade());

            if (arcade == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ARCADE_NOT_FOUND_MESSAGE);
                return;
            }

            Arena arena = (Arena) arcade.getRoom(arcadeRoute);

            if (arena == null) {
                Core.getLogger().info("[TheBridge] Arena não encontrada pela rota, tentando findBestArena para " + event.getName());
                arena = arcade.findBestArena(id, arcadeRoute);
            }

            if (arena == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.NO_ROOM_AVAILABLE_MESSAGE);
                return;
            }

            TheBridgeMember member = Core.getTheBridgeData().of(id, true);

            if (member == null) {
                member = Core.getTheBridgeData().save(new TheBridgeMember(id, event.getName()));

                if (member == null) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE + " (Ex0002)");
                    return;
                }
            }

            Core.getMemberController().save(member);
            Core.getTheBridgeData().cancelExpiration(member.getId());

            User user = new User(member, arena, arcadeRoute.getJoin());
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        if (user == null)
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE + " (Ex0003)");
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user != null)
            user.handle();
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        exit(event.getPlayer());
    }

    @EventHandler
    public void kick(PlayerKickEvent event) {
        exit(event.getPlayer());
    }

    protected void exit(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user != null && user.getArena() != null)
            user.getArena().quit(player);

        if (user != null && user.getMember() != null) {
            Core.getTheBridgeData().startExpiration(user.getMember().getId());
            Core.getMemberController().remove(player.getUniqueId(), TheBridgeMember.class);
        }
    }
}
