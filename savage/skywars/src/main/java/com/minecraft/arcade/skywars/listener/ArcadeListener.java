package com.minecraft.arcade.skywars.listener;

import com.minecraft.arcade.skywars.arcade.Arcade;
import com.minecraft.arcade.skywars.arcade.arena.Arena;
import com.minecraft.arcade.skywars.user.User;
import com.minecraft.arcade.skywars.arcade.game.SkyWarsGameLogic;
import com.minecraft.core.Core;
import com.minecraft.core.api.party.Party;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.room.event.ArenaSearchedEvent;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.event.type.player.PlayerArenaWarpEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.bukkit.manager.Manager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ArcadeListener implements Listener {

    private final Manager manager = BukkitCore.getManager();

    @EventHandler
    public void onSyncUpdate(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (ArcadeHolder holder : manager.getArcade().getArcades()) {
                if (!(holder instanceof Arcade arcade)) continue;

                List<Arena> arenaList = arcade.getRooms().stream().map(room -> (Arena) room).toList();

                arenaList.forEach(arena -> {
                    arena.handlePulse();
                    
                    // Sistema de refill de baús (copiado do Duels)
                    SkyWarsGameLogic.handleRefillTimer(arena);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onArenaSearched(ArenaSearchedEvent event) {
        Arena arena = (Arena) event.getArena();

        User user = (User) User.of(event.getSender().getId());

        if (user == null) return;

        Player player = user.getPlayer();

        if (player != null) {
            event.setCancelled(true);

            ArcadeRouteContext route = ArcadeRouteContext.builder()
                    .arcade(arena.getArcade().getCategory())
                    .roomId(arena.getId())
                    .mapId(arena.getMap().getId())
                    .slot(arena.getSlot())
                    .serverId(Core.getServerId())
                    .arenaIdentifier(arena.getIdentifier())
                    .state(ArcadeState.ALIVE)
                    .maxPlayers(arena.getMaxPlayers())
                    .join(event.getRoute().getJoin())
                    .build();

            Core.getPlatform().runSync(() -> {
                Arena current = user.getArena();

                if (current != null) {
                    current.quit(player);
                }

                user.setArena(arena);

                arena.join(player);
                Party party = user.getAccount().getParty();

                if (party != null && party.isAuthor(player.getUniqueId()))
                    party.redirect(route);
            });
        }
    }

    @EventHandler
    public void onPartyWarp(PlayerArenaWarpEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user != null && user.isPlayer()) {
            Arena current = user.getArena(), arena = (Arena) event.getArena();

            Core.getPlatform().runSync(() -> {
                if (current != null) {
                    current.quit(player);
                }

                user.setArena(arena);

                arena.join(player);
            });
        }
    }

    // Os baús são preenchidos automaticamente no início da partida
    // baseado nas placas configuradas no mapa ([chest_b1], [chest_b2], [chest_feast1], [chest_feast2])
    // Não é necessário preencher dinamicamente quando o jogador abre

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer()) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();
        if (arena == null) {
            event.setCancelled(true);
            return;
        }

        // Cancelar drop no pré-jogo ou se não estiver vivo
        if (!arena.isPhase(RoomPhase.PLAYING) || !user.inState(ArcadeState.ALIVE) || !user.getContext().isAlive()) {
            event.setCancelled(true);
            return;
        }

        ItemStack stack = event.getItemDrop().getItemStack();

        // Não permitir dropar espada de madeira
        if (stack != null && stack.getType() == Material.WOOD_SWORD) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer()) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();
        if (arena == null) {
            event.setCancelled(true);
            return;
        }

        // Cancelar quebra no pré-jogo ou se não estiver vivo
        if (!arena.isPhase(RoomPhase.PLAYING) || !user.inState(ArcadeState.ALIVE) || !user.getContext().isAlive()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer()) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();
        if (arena == null) {
            event.setCancelled(true);
            return;
        }

        // Cancelar colocação no pré-jogo ou se não estiver vivo
        if (!arena.isPhase(RoomPhase.PLAYING) || !user.inState(ArcadeState.ALIVE) || !user.getContext().isAlive()) {
            event.setCancelled(true);
        }
    }

    // Permitir abertura de baús durante o jogo (copiado do Duels)
    @EventHandler
    public void onChestOpen(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!event.hasBlock()) return;

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer()) return;

        Arena arena = user.getArena();
        if (arena == null) return;

        org.bukkit.block.Block block = event.getClickedBlock();

        // Permitir abertura de baús durante o jogo
        if (arena.isPhase(RoomPhase.PLAYING) && 
            (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST)) {
            event.setCancelled(false);
        }
    }
}

