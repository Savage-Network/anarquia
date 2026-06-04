package com.minecraft.arcade.event.game;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.StringUtil;
import com.minecraft.core.util.list.TimeUtil;
import com.minecraft.core.util.list.WorldUtil;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.arcade.event.controller.SkitController;
import com.minecraft.arcade.event.game.state.GameState;
import com.minecraft.arcade.event.game.team.Team;
import com.minecraft.arcade.event.menu.PlayersAlivesMenu;
import com.minecraft.arcade.event.user.User;
import com.minecraft.arcade.event.game.variable.Variable;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.arcade.room.team.preset.TeamPresetType;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.github.paperspigot.Title;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
public class Game
{

    private String id;

    private GameState state;

    private Location spawn;

    private UUID winner;

    private int time, minPlayers, maxPlayers;

    private boolean spec = true, die = true, team = false, teamSelect = false, sumo = false;

    private final List<Team> teams = new ArrayList<>();

    private Location dieSpawnPoint = null;

    public Game()
    {
        this.loadWorld();

        WorldUtil.setup(getWorld());
    }

    private Team buildTeam(TeamPresetType preset)
    {
        return new Team(preset);
    }

    private void loadTeams()
    {
        this.teams.clear();
        this.teams.addAll(Arrays.stream(TeamPresetType.values()).map(this::buildTeam).toList());
    }

    public Team getTeam(TeamPresetType type)
    {
        return teams.stream().filter(team -> team != null && team.getColor() == type.getColor()).findFirst().orElse(null);
    }

    public boolean isUnlockedTeam(TeamPresetType type)
    {
        Team team = getTeam(type);

        return team != null && team.isUnlocked();
    }

    public World getWorld()
    {
        return Bukkit.getWorlds().get(0);
    }

    public void handlePulse()
    {
        if (isState(GameState.WAITING) && canBeCount())
            setState(GameState.STARTING);

        if (isState(GameState.STARTING)) {

            if (!canBeCount()) {
                setState(GameState.WAITING);
                return;
            }

            time--;

            if (time > 0 && time % 15 == 0) {
                send("§cO evento irá iniciar em %s segundos.".formatted(time));
                sound(Sound.CLICK);
            }

            if (time >= 1 && time <= 5) {
                send("§cO evento irá iniciar em %s segundo%s.".formatted(time, time > 1 ? "s" : ""));
                sound(Sound.CLICK);

                if (time <= 3)
                    title("", (time == 3 ? "§c" : time == 2 ? "§e" : "§a") + time);
            }

            if (time <= 0) {
                send("§aO evento iniciou! Boa sorte a todos :)");
                sound(Sound.LEVEL_UP);

                title("");

                setState(GameState.PLAYING);

                if (team) {
                    List<Team> unlocked = teams.stream().filter(Team::isUnlocked).toList();

                    if (!unlocked.isEmpty()) {
                        distributePlayersEqually(unlocked);

                        unlocked.forEach(t ->
                                t.getMembers().forEach(uuid ->
                                {
                                    User user = (User) User.of(uuid);

                                    if (user != null)
                                        user.loadTeam();
                                }));
                    }
                }

                getAllUsers().forEach(this::handleSidebar);
            }
        }

        if (isState(GameState.PLAYING)) {
            time++;
        }

        if (isState(GameState.ENDING)) {
            time--;

            if (time >= 1 && time <= 5) {
                send("§cO evento acabará em %s segundo%s.".formatted(time, time > 1 ? "s" : ""));
                sound(Sound.WOOD_CLICK);
            }

            if (time <= 0) {
                send("§cO evento acabou.");
                reset();
            } else if (hasWinner()) {
                User winnerUser = (User) User.of(this.winner);

                if (winnerUser != null) {
                    send(winnerUser.getAccount().getColoredName() + "§e ganhou o evento!");
                }
            }
        }

        getAllUsers().forEach(this::updateSidebar);
    }

    public void distributePlayersEqually(List<Team> teams)
    {
        List<Team> unlockedTeams = teams.stream()
                .filter(Team::isUnlocked)
                .toList();

        if (unlockedTeams.isEmpty()) return;

        List<User> users = new ArrayList<>(getUsers().stream().filter(user -> user.getEventTeam() == null).toList());

        if (users.isEmpty()) return;

        Collections.shuffle(users);

        int teamIndex = 0;

        for (User user : users) {
            Team t = unlockedTeams.get(teamIndex);

            t.getMembers().add(user.getAccount().getId());

            teamIndex++;
            if (teamIndex >= unlockedTeams.size()) {
                teamIndex = 0;
            }
        }
    }

    private void loadWorld()
    {
        this.id = StringUtil.generateExclusiveCode(3) + "cm";
        this.winner = null;

        this.state = GameState.WAITING;
        this.spawn = new Location(getWorld(), 0, 100, 0);

        this.die = true;
        this.spec = true;

        this.team = false;
        this.teamSelect = false;
        this.sumo = false;

        this.loadTeams();

        this.minPlayers = (int) Variable.MIN_PLAYERS.getDefaultValue();
        this.maxPlayers = (int) Variable.MAX_PLAYERS.getDefaultValue();

        SkitController.clear();
        Variable.reset();
    }

    public void reset()
    {
        getAllUsers().forEach(user -> user.getAccount().redirectToHub());

        this.loadWorld();
    }

    public void removeUser(User user)
    {
        User.remove(user.getAccount().getId());
    }

    public void setState(GameState state)
    {
        this.state = state;
        this.time = state.getDefaultTime();
    }

    public boolean isState(GameState state)
    {
        return this.state.equals(state);
    }

    public boolean canBeCount()
    {
        return getOnlineCount() >= getMinPlayers();
    }

    public boolean isFull()
    {
        return getOnlineCount() >= getMaxPlayers();
    }

    public void handleSidebar(User user)
    {
        Account account = user.getAccount();

        Sidebar sidebar = user.getSidebar();
        if (sidebar == null) return;

        sidebar.clear();
        sidebar.addRow("id", "§8" + id);

        sidebar.blankRow();

        if (!isState(GameState.PLAYING)) {

            String text = isState(GameState.WAITING)
                    ? "Aguardando..."
                    : isState(GameState.STARTING)
                    ? "Inicia em §a" + TimeUtil.time(time)
                    : "Acaba em §c" + TimeUtil.time(time);

            sidebar.addRow("time", text);
            sidebar.blankRow();
        } else if (!user.isSpectator()) {
            sidebar.addRow("kills", "Kills: §a" + Util.formatNumber(user.getKills()));

            if (team && user.getEventTeam() != null)
                sidebar.addRow("team", "Time: " + user.getEventTeam().getColoredName());

            sidebar.blankRow();
        }

        sidebar.addRow("players", "Players: §a" + getOnlineCount() + "/" + maxPlayers);

        if (sumo) {
            sidebar.blankRow();
            sidebar.addRow("mode", "§6Modo: §eSumô");
        }

        if (user.isSpectator()) {
            sidebar.blankRow();
            sidebar.addRow("spectator", "§cMODO ESPECTADOR");
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(account);
    }

    public void updateSidebar(User user)
    {
        Sidebar sidebar = user.getSidebar();
        if (sidebar == null) return;

        if (!isState(GameState.PLAYING)) {
            String text = isState(GameState.WAITING)
                    ? "Aguardando..."
                    : isState(GameState.STARTING)
                    ? "Inicia em §a" + TimeUtil.time(time)
                    : "Acaba em §c" + TimeUtil.time(time);

            sidebar.updateRow("time", text);
        } else if (!user.isSpectator()) {
            sidebar.updateRow("kills", "Kills: §a" + Util.formatNumber(user.getKills()));

            if (team && user.getEventTeam() != null)
                sidebar.updateRow("team", "Time: " + user.getEventTeam().getColoredName());
        }

        sidebar.updateRow("players", "Players: §a" + getOnlineCount() + "/" + maxPlayers);
    }

    public void handleHotbar(Player player)
    {
        User user = (User) User.of(player.getUniqueId());

        player.getInventory().clear();

        if (user == null) return;

        if (user.isSpectator())
            player.getInventory().setItem(0, Item.of(Material.COMPASS, "§aJogadores vivos")
                    .click(event -> new PlayersAlivesMenu((Player) event.getWhoClicked()).handle()));
        // No modo sumô o inventário fica vazio (só punho) — nada a adicionar
    }

    public void hideAndShow(Player player)
    {
        User user = (User) User.of(player.getUniqueId());
        if (user == null) return;

        Bukkit.getOnlinePlayers().forEach(onlinePlayer ->
        {
            if (onlinePlayer.equals(player)) return;

            onlinePlayer.hidePlayer(player);
            player.hidePlayer(onlinePlayer);

            User target = (User) User.of(onlinePlayer.getUniqueId());

            if (target == null) return;

            if (user.isVanish()) {
                player.showPlayer(onlinePlayer);

                if (target.isVanish())
                    onlinePlayer.showPlayer(player);
            }

            if (user.isSpectator()) {
                if (target.isSpectator() || target.isPlayer())
                    player.showPlayer(onlinePlayer);

                if (!target.isPlayer())
                    onlinePlayer.showPlayer(player);
            }

            if (user.isPlayer()) {
                if (target.isPlayer())
                    player.showPlayer(onlinePlayer);

                onlinePlayer.showPlayer(player);
            }
        });
    }

    public boolean hasWorld()
    {
        return getWorld() != null;
    }

    public Set<User> getAllUsers()
    {
        return User.getList().stream()
                .map(user -> (User) user)
                .collect(Collectors.toSet());
    }

    public Set<User> getUsers()
    {
        return getAllUsers().stream().filter(user -> !user.isSpectator()).collect(Collectors.toSet());
    }

    public Set<User> getUsers(Predicate<User> filter)
    {
        return getUsers().stream().filter(filter).collect(Collectors.toSet());
    }

    public Set<Player> getOnlinePlayers()
    {
        return getAllUsers().stream()
                .map(user -> user.getAccount().player())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public int getOnlineCount()
    {
        return getOnlinePlayers().size();
    }

    public void send(String message)
    {
        getWorld().getPlayers().forEach(player -> player.sendMessage(message));
    }

    public void send(BaseComponent message)
    {
        getWorld().getPlayers().forEach(player -> player.sendMessage(message));
    }

    public void sound(Sound sound)
    {
        getWorld().getPlayers().forEach(player -> player.playSound(player.getLocation(), sound, 1f, 1f));
    }

    public void title(String title, String subTitle)
    {
        getWorld().getPlayers().forEach(player -> player.sendTitle(new Title(title, subTitle, 5, 60, 80)));
    }

    public void title(String title)
    {
        this.title(title, "");
    }

    public boolean hasWinner()
    {
        return winner != null && Bukkit.getPlayer(winner) != null;
    }
}
