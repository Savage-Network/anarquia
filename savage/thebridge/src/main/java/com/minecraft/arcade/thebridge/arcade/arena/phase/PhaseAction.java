package com.minecraft.arcade.thebridge.arcade.arena.phase;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.cabin.Cabin;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.member.list.thebridge.TheBridgeMember;
import com.minecraft.core.util.Util;
import com.minecraft.arcade.thebridge.TheBridge;
import com.minecraft.arcade.thebridge.arcade.Arcade;
import com.minecraft.arcade.thebridge.arcade.arena.Arena;
import com.minecraft.arcade.thebridge.user.User;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PhaseAction {

    public static void handle(Arena arena, RoomPhase phase) {
        Set<Player> players = arena.getPlayers();

        Arcade arcade = arena.getArcade();

        switch (phase) {
            case WAITING: {
                players.forEach(player -> {
                    User user = (User) User.of(player.getUniqueId());

                    if (user != null) {
                        // Limpar inventário durante WAITING
                        player.getInventory().clear();
                        player.getInventory().clear();
                        
                        arcade.handleSidebar(user);
                        arcade.handleHotbar(player, arena);
                    }
                });
                break;
            }

            case STARTING: {
                arena.setTime(5);

                players.forEach(player -> {
                    User user = (User) User.of(player.getUniqueId());

                    if (user == null || !user.isPlayer()) return;

                    // Limpar inventário durante STARTING
                    player.getInventory().clear();
                    player.getInventory().clear();

                    Location spawn = user.getTeam().getBase();

                    // Teleportar para a jaula do time (3 blocos acima do spawn)
                    Location cageLocation = spawn.clone().add(0, 3, 0);
                    player.teleport(cageLocation);

                    if (arcade.hasFeature(ArcadeFeature.WITHOUT_MOVING_STARTING)) {
                        user.setTag(Tag.of(user.getTeam().getColor().getName()));

                        BukkitCore.getManager().getHologram().removeClients(player);
                    }

                    // Não dar equipamentos durante STARTING (só quando a partida começar)
                    arcade.handleSidebar(user);
                });
                break;
            }

            case RESTARTING: {
                players.forEach(player -> {
                    User user = (User) User.of(player.getUniqueId());
                    
                    if (user == null || !user.isPlayer()) return;

                    BukkitCore.getManager().getCooldown().resetCooldown(player);

                    player.updateInventory();

                    Location spawn = user.getTeam().getBase();

                    // Teleportar para a jaula do time (3 blocos acima do spawn)
                    Location cageLocation = spawn.clone().add(0, 3, 0);
                    player.teleport(cageLocation);

                    if (arcade instanceof com.minecraft.arcade.thebridge.arcade.list.Solo) {
                        ((com.minecraft.arcade.thebridge.arcade.list.Solo) arcade).updateTeamStyle(arena, user.getSidebar());
                    }
                });

                arena.setTime(4);
                arena.setSuperPhase(RoomPhase.STARTING);
                break;
            }

            case PLAYING: {
                players.forEach(player -> {
                    User user = (User) User.of(player.getUniqueId());

                    if (user == null) return;

                    Account account = user.getAccount();

                    // Resetar jaulas se necessário (não usado no The Bridge, mas mantido para compatibilidade)

                    if (user.isPlayer()) {
                        TeamPreset team = user.getTeam();

                        TheBridgeMember member = user.getMember();

                        ArcadeStats stats = member.getStats(arcade.getCategory());

                        if (!arcade.hasFeature(ArcadeFeature.WITHOUT_MOVING_STARTING)) {
                            Tag tag = Tag.of(team.getColor().getName());

                            user.setTag(tag);
                        }

                        Location spawn = team.getBase();

                        if (!arcade.hasFeature(ArcadeFeature.NOT_TELEPORT_STARTING))
                            player.teleport(spawn);

                        arcade.handleHotbar(player, arena);

                        Core.getPlatform().runSync(() -> player.setWalkSpeed(0.2f));

                        if (!arcade.hasFeature(ArcadeFeature.WITHOUT_MOVING_STARTING))
                            account.title("");

                        if (!arcade.hasFeature(ArcadeFeature.WITHOUT_MOVING_STARTING)) {
                            BukkitCore.getManager().getHologram().removeClients(player);
                        }

                        // Chamar start do modo específico (Solo) se existir
                        if (arcade instanceof com.minecraft.arcade.thebridge.arcade.list.Solo) {
                            ((com.minecraft.arcade.thebridge.arcade.list.Solo) arcade).start(player);
                        }

                        stats.setMatches();
                        member.updateStats(stats);
                    }

                    Core.getPlatform().runSync(() -> {
                        arena.hideAndShow(user);
                        arcade.handleSidebar(user);
                    }, 2L);
                });
                break;
            }

            case ENDING: {
                arena.setTime(10);

                if (arena.hasWinner()) {
                    TeamPreset winner = (TeamPreset) arena.getWinner();

                    List<TeamPreset> losers = arena.getLosers().stream().map(team -> (TeamPreset) team).collect(Collectors.toList());

                    arena.send(winner.getColor() + winner.getName() + "§e venceu a partida.");
                }

                // Ajeitando os jogadores
                players.forEach(player -> {
                    User user = (User) User.of(player.getUniqueId());

                    if (user.isPlayer()) {
                        // Remove all potion effects
                        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                            player.removePotionEffect(effect.getType());
                        }

                        user.setTag(Tag.SPECTATOR);
                        arcade.handleHotbar(player, arena);
                    }

                    arena.hideAndShow(user);
                    arcade.handleSidebar(user);

                    player.setExp(0);
                    player.setLevel(0);

                    if (arcade.hasFeature(ArcadeFeature.CABINS))
                        player.teleport(arena.getWinner().getBase());

                    BukkitCore.getManager().getCooldown().resetCooldown(player);
                });

                break;
            }

            case RESETTING: {
                players.forEach(player -> {
                    Account account = Core.getAccountController().of(player.getUniqueId());

                    if (account != null)
                        account.redirectToHub();
                });

                Core.getPlatform().runSync(() -> {
                    if (arena.getPlayers().isEmpty()) {
                        if (arcade.hasFeature(ArcadeFeature.LIQUID)) {
                            com.minecraft.core.arcade.slime.SlimeWorldController.unloadWorld(arena.getWorld(), () -> {
                                arcade.getRooms().remove(arena);
                                
                                // Recriar arena usando setupRoom
                                com.minecraft.core.arcade.room.map.Map map = arena.getMap();
                                com.minecraft.core.arcade.room.slot.Slot slot = arena.getSlot();
                                com.minecraft.core.arcade.room.type.Type type = arena.getType();
                                
                                arcade.setupRoom(map, slot, type);
                            });
                        } else {
                            java.time.Instant now = java.time.Instant.now();

                            Core.getLogger().info("[" + arcade.getName() + "/" + arena.getIdentifier() + "] Redefinindo...");

                            if (arcade.hasFeature(ArcadeFeature.BUILD)) {
                                try {
                                    arena.getRollbackBlocks().forEach(rollbackBlock -> {
                                        if (rollbackBlock != null && rollbackBlock.getBlock() != null)
                                            rollbackBlock.getBlock().setType(org.bukkit.Material.AIR);
                                    });
                                    arena.getRollbackBlocks().clear();
                                } catch (Exception e) {
                                    Core.getLogger().warning("Não foi possível realizar o rollback...");
                                }
                            }

                            if (arena.hasCabins())
                                arena.resetCabins(TheBridge.getInstance());

                            arena.getTeams().forEach(team -> ((TeamPreset) team).setScore(0));

                            arena.setFullTime(0);
                            arena.setTime(0);

                            arena.setPhase(RoomPhase.WAITING);

                            Core.getLogger().info("[" + arcade.getName() + "/" + arena.getIdentifier() + "/" + Util.formatInstant(now) + "] Arena redefinida com sucesso.");
                        }
                    }
                }, 30);

                break;
            }
        }
    }
}
