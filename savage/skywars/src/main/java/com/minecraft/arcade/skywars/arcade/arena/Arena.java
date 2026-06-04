package com.minecraft.arcade.skywars.arcade.arena;

import com.minecraft.arcade.skywars.arcade.Arcade;
import com.minecraft.arcade.skywars.user.User;
import com.minecraft.arcade.skywars.arcade.arena.context.phase.ArenaPhaseExecutor;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.arcade.slime.SlimeWorldController;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.util.list.TimeUtil;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.bukkit.user.UserModel;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Arena extends Room {
    private User skyWarsWinner;

    public Arena(int id, Arcade arcade, Map map, Slot slot) {
        super(id, arcade, map, slot);
    }

    public void setWinner(User winner) {
        this.skyWarsWinner = winner;
    }

    public User getSkyWarsWinner() {
        return skyWarsWinner;
    }

    @Override
    public boolean hasWinner() {
        return skyWarsWinner != null;
    }

    public void unload() {
        getUsers().forEach(user -> user.getAccount().redirectToHub());

        if (getPlayers().isEmpty()) {
            Arcade arcade = (Arcade) getArcade();

            SlimeWorldController.unloadWorld(getWorld(), () -> arcade.removeArena(this));
        }
    }

    public List<User> getAllUsers() {
        return UserModel.getList().stream()
                .filter(user -> user instanceof User && isPlayer(user.getAccount().getId()))
                .map(user -> (User) user)
                .collect(Collectors.toList());
    }

    public List<User> getUsers() {
        return UserModel.getList().stream()
                .filter(user -> user instanceof User && user != null && user.isPlayer() && isPlayer(user.getAccount().getId()))
                .map(user -> (User) user)
                .collect(Collectors.toList());
    }

    public List<User> getAliveUsers() {
        return getUsers().stream()
                .filter(user -> user.getContext().isAlive() && user.inState(ArcadeState.ALIVE))
                .collect(Collectors.toList());
    }

    @Override
    public int getTotalPlayers() {
        return getUsers().size();
    }

    @Override
    public void join(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Account account = user.getAccount();

        user.setTag(!user.isPlayer() ? Tag.SPECTATOR : account.getTag());

        getPlayers().add(player);

        player.updateInventory();
        player.getEnderChest().clear();

        Location spawn = getLocation("spawn");
        
        if (spawn == null) {
            Core.getLogger().warning("[SkyWars/" + getIdentifier() + "] Spawn não encontrado no mapa!");
        }

        if (user.isPlayer()) {
            user.setState(ArcadeState.ALIVE);
            user.getContext().setAlive(false);

            if (spawn != null) {
                player.teleport(spawn);
            } else {
                org.bukkit.World world = getWorld();
                if (world != null) {
                    Location worldSpawn = world.getSpawnLocation();
                    player.teleport(worldSpawn);
                    Core.getLogger().warning("[SkyWars/" + getIdentifier() + "] Usando spawn do mundo.");
                }
            }
            
            Tag tag = user.getTag();
            if (!user.getContext().hasReconnect()) {
                send(tag.getColor() + (tag.ordinal() >= Tag.CREATOR.ordinal() ? "§o" : "") + account.getNickname()
                        + "§e entrou na sala. (§b" + getTotalPlayers() + "§e/§b" + getMaxPlayers() + "§e)");
            }
        }

        ((Arcade) getArcade()).load(user);

        hideAndShow(user);
        getAllUsers().forEach(this::hideAndShow);

        account.setRoute(this, "", user.getState(), user.getJoin());
    }

    @Override
    public void quit(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user != null && user.isPlayer() && user.inState(ArcadeState.ALIVE)) {
            if (isPhase(RoomPhase.PLAYING)) {
                ((Arcade) getArcade()).handleDeath(this, user, null, Arcade.DeathCause.NONE);
            }
        }

        getPlayers().remove(player);
    }

    public void handlePulse() {
        if (isPhase(RoomPhase.WAITING) && canBeCount())
            setPhase(RoomPhase.STARTING);

        if (isPhase(RoomPhase.STARTING)) {
            if (canBeCount()) {
                if (isFull() && getTime() > 10)
                    setTime(10);

                setTime(getTime() - 1);

                final int time = getTime();

                if (time == 10) {
                    title("§c§l" + getTime(), "§csegundos!");
                    sound(Sound.CLICK);
                }

                if (time <= 5 && time >= 1) {
                    send("§eA partida inicia em §c" + time + "§e segundo" + (time > 1 ? "s" : "") + "!");
                    title((time <= 3 ? "§a" : "§c") + time, "§ePrepare-se!");
                    sound(Sound.CLICK);
                }

                if (time <= 0)
                    setPhase(RoomPhase.PLAYING);
            } else if (!isCustom())
                setPhase(RoomPhase.WAITING);
        }

        if (isPhase(RoomPhase.PLAYING)) {
            setFullTime(getFullTime() + 1);

            List<User> aliveUsers = getUsers().stream()
                    .filter(u -> u.getContext().isAlive() && u.inState(ArcadeState.ALIVE))
                    .collect(Collectors.toList());

            if (aliveUsers.size() == 1 && !hasWinner()) {
                User winner = aliveUsers.get(0);
                setWinner(winner);
                setPhase(RoomPhase.ENDING);
            } else if (aliveUsers.isEmpty() && !hasWinner()) {
                setPhase(RoomPhase.ENDING);
            }
        }

        if (isPhase(RoomPhase.ENDING)) {
            setTime(getTime() - 1);

            if (getTime() <= 0 || getPlayers().isEmpty())
                setPhase(RoomPhase.RESETTING);
        }

        getAllUsers().forEach(user -> updateSidebarTime(user.getSidebar()));
    }

    public void renderSidebarTime(Sidebar sidebar) {
        String prefix;
        String suffix = "";
        
        if (isPhase(RoomPhase.STARTING)) {
            prefix = "Inicia em §a";
            suffix = TimeUtil.formatTime(getTime());
        } else if (isPhase(RoomPhase.PLAYING)) {
            prefix = "Tempo: §a";
            suffix = TimeUtil.formatTime(getFullTime());
        } else if (isPhase(RoomPhase.ENDING)) {
            prefix = "§cFim de jogo!";
        } else {
            prefix = "§7Aguardando...";
        }

        sidebar.addRow("time", prefix + suffix);
    }

    public void updateSidebarTime(Sidebar sidebar) {
        String prefix;
        String suffix = "";
        
        if (isPhase(RoomPhase.STARTING)) {
            prefix = "Inicia em §a";
            suffix = TimeUtil.formatTime(getTime());
        } else if (isPhase(RoomPhase.PLAYING)) {
            prefix = "Tempo: §a";
            suffix = TimeUtil.formatTime(getFullTime());
        } else if (isPhase(RoomPhase.ENDING)) {
            prefix = "§cFim de jogo!";
        } else {
            prefix = "§7Aguardando...";
        }

        sidebar.updateRow("time", prefix + suffix);
        sidebar.updateRow("players", "Jogadores: §a" + getTotalPlayers() + "/" + getMaxPlayers());
    }

    public void hideAndShow(User user) {
        Player mainPlayer = user.getAccount().player();

        for (Player target : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (target == null || target.equals(mainPlayer)) continue;

            target.hidePlayer(mainPlayer);
            mainPlayer.hidePlayer(target);
        }

        for (Player targetPlayer : getPlayers()) {
            if (targetPlayer == null || mainPlayer.equals(targetPlayer))
                continue;

            User target = (User) User.of(targetPlayer.getUniqueId());

            if (target == null || !target.getArena().equals(user.getArena())) continue;

            if (isPhase(RoomPhase.ENDING)) {
                mainPlayer.showPlayer(targetPlayer);
                targetPlayer.showPlayer(mainPlayer);
                continue;
            }

            if (user.inState(ArcadeState.DEAD)) {
                if (target.inState(ArcadeState.DEAD) || (target.inState(ArcadeState.ALIVE) && target.getContext().isAlive())) {
                    mainPlayer.showPlayer(targetPlayer);
                    targetPlayer.showPlayer(mainPlayer);
                }
            } 
            else if (user.inState(ArcadeState.ALIVE) && user.getContext().isAlive()) {
                if (target.inState(ArcadeState.ALIVE) && target.getContext().isAlive()) {
                    mainPlayer.showPlayer(targetPlayer);
                    targetPlayer.showPlayer(mainPlayer);
                }
            }
            else if (user.inState(ArcadeState.ALIVE) && !user.getContext().isAlive()) {
                if (target.inState(ArcadeState.ALIVE) && !target.getContext().isAlive()) {
                    mainPlayer.showPlayer(targetPlayer);
                    targetPlayer.showPlayer(mainPlayer);
                }
            }
        }
    }

    @Override
    public void setPhase(RoomPhase phase) {
        if (isPhase(phase)) return;

        super.setPhase(phase);
        ArenaPhaseExecutor.execute(this, phase);
    }
}

