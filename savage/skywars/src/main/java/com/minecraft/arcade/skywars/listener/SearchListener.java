package com.minecraft.arcade.skywars.listener;

import com.minecraft.arcade.skywars.arcade.Arcade;
import com.minecraft.arcade.skywars.arcade.arena.Arena;
import com.minecraft.arcade.skywars.user.User;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.api.reconnect.Reconnect;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.backend.data.list.server.RouteData;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.member.list.skywars.SkyMember;
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

            RouteContext route = routeData.read(id);

            if (route == null || !route.isValidArcade()) {
                Account account = Core.getAccountData().of(id, false);

                if (account == null) {
                    account = Core.getAccountController().of(id);
                }

                if (account != null && account.isStaffer()) {
                    routeData.delete(id);

                    SkyMember member = Core.getSkyWarsData().of(id, true);
                    if (member == null) {
                        member = Core.getSkyWarsData().save(new SkyMember(id, event.getName()));
                    }
                    
                    if (member != null) {
                        Core.getMemberController().save(member);
                        Core.getSkyWarsData().cancelExpiration(member.getId());
                        
                        User user = new User(member, null, Join.VANISH);
                        Core.getLogger().info("[BYPASS] Staff " + account.getNickname() + " entrou sem rota para configurar mapas.");
                        return;
                    }
                }

                routeData.delete(id);
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
                Account account = Core.getAccountData().of(id, false);
                if (account == null) {
                    account = Core.getAccountController().of(id);
                }
                
                if (account != null && account.isStaffer()) {
                    routeData.delete(id);
                    
                    SkyMember member = Core.getSkyWarsData().of(id, true);
                    if (member == null) {
                        member = Core.getSkyWarsData().save(new SkyMember(id, event.getName()));
                    }
                    
                    if (member != null) {
                        Core.getMemberController().save(member);
                        Core.getSkyWarsData().cancelExpiration(member.getId());
                        
                        User user = new User(member, null, Join.VANISH);
                        Core.getLogger().info("[BYPASS] Staff " + account.getNickname() + " entrou sem arena disponível para configurar mapas.");
                        return;
                    }
                }
                
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.NO_ROOM_AVAILABLE_MESSAGE);
                return;
            }

            SkyMember member = Core.getSkyWarsData().of(id, true);

            if (member == null) {
                member = Core.getSkyWarsData().save(new SkyMember(id, event.getName()));

                if (member == null) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE + " (Ex0002)");
                    return;
                }
            }

            Core.getMemberController().save(member);
            Core.getSkyWarsData().cancelExpiration(member.getId());

            User user = new User(member, arena, arcadeRoute.getJoin());

            if (user.getAccount().hasReconnect()) {
                Reconnect reconnect = Core.getReconnectData().of(id);

                if (reconnect != null)
                    user.getContext().setReconnect(reconnect);
            }
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

        if (user != null) {
            if (user.getArena() == null) {
                org.bukkit.World defaultWorld = org.bukkit.Bukkit.getWorld("world");
                if (defaultWorld == null && !org.bukkit.Bukkit.getWorlds().isEmpty()) {
                    defaultWorld = org.bukkit.Bukkit.getWorlds().get(0);
                }
                
                if (defaultWorld != null) {
                    org.bukkit.Location spawn = defaultWorld.getSpawnLocation();
                    if (spawn == null) {
                        spawn = new org.bukkit.Location(defaultWorld, 0.5, 100, 0.5);
                    }
                    player.teleport(spawn);
                    player.sendMessage("§aVocê entrou no modo de configuração.");
                    player.sendMessage("§7Use §e/swprocess §7para processar mapas.");
                }
                
                return;
            }
            
            user.handle();
        }
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

        Core.getSkyWarsData().startExpiration(user.getMember().getId());
    }
}

