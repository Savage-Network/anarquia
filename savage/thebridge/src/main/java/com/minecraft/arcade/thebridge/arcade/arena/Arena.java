package com.minecraft.arcade.thebridge.arcade.arena;

import com.minecraft.arcade.thebridge.TheBridge;
import com.minecraft.arcade.thebridge.arcade.Arcade;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.arcade.slime.SlimeWorldController;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.arcade.thebridge.user.User;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.util.list.DateUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class Arena extends Room {

    public Arena(int id, Arcade arcade, Map map, Slot slot) {
        super(id, arcade, map, slot);

        super.setPhase(RoomPhase.WAITING);
    }

    public List<TeamPreset> getTeamList() {
        return getTeams().stream().map(team -> (TeamPreset) team).collect(Collectors.toList());
    }

    @Override
    public Arcade getArcade() {
        return (Arcade) super.getArcade();
    }

    @Override
    public boolean canBeCount() {
        return getMatchUsers().size() >= getMaxPlayers();
    }

    @Override
    public boolean isFull() {
        return getMatchUsers().size() >= getMaxPlayers();
    }

    @Override
    public void join(Player player) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        Join join = user.getJoin();

        getPlayers().add(player);

        player.setWalkSpeed(0.2f);
        player.updateInventory();

        // Aplicando tag
        user.setGainXp(0);
        Tag tagToSet = !join.equals(Join.PLAYER) ? Tag.SPECTATOR : Tag.MASKED;
        user.setTag(tagToSet);
        
        TagManager.updateTag(account, tagToSet);

        if (!user.getArena().equals(this))
            user.setArena(this);

        /* Entrou como jogador na partida */
        if (join.equals(Join.PLAYER)) {
            // Definindo time
            for (TeamPreset team : getTeamList()) {
                if (team == null || team.isFull()) continue;

                user.setTeam(team);
                team.getMembers().add(player.getUniqueId());
                break;
            }

            // Teleportar para o spawn principal quando entra na sala
            Location mainSpawn = getLocation("spawn");
            if (mainSpawn != null) {
                player.teleport(mainSpawn);
            }

            BukkitCore.getManager().getCooldown().resetCooldown(player);

            send(account.getTag().getColor() + "§k" + account.getNickname() + "§e entrou na partida. §7(" + getMatchUsers().size() + "/" + getMaxPlayers() + ")");
        } else {
            /* Entrou em Vanish ou Spectator na partida */
            handleVanish(player);
        }

        Arcade arcade = getArcade();

        arcade.join(user);

        account.setRoute(this, join);

        hideAndShow(user);

        account.title("");
    }

    @Override
    public void quit(Player player) {
        User user = (User) User.of(player.getUniqueId());

        getPlayers().remove(player);

        if (user.isPlayer()) {
            TeamPreset team = user.getTeam();

            // Remover do time
            if (team != null) {
                team.getMembers().remove(player.getUniqueId());

                if (isPhase(RoomPhase.PLAYING))
                    send(team.getColor() + user.getAccount().getNickname() + "§e saiu da partida. §7(" + getMatchUsers().size() + "/" + getMaxPlayers() + ")");
            }

            BukkitCore.getManager().getHologram().removeClients(player);
        }
    }

    public void timer() {
        Arcade arcade = getArcade();

        // Aguardando jogadores, e partir para o Starting...
        if (isPhase(RoomPhase.WAITING) && isFullTeams())
            setPhase(RoomPhase.STARTING);

        // Iniciando partida
        if (isPhase(RoomPhase.STARTING)) {
            if (isFullTeams()) {
                setTime(getTime() - 1);

                if (getTime() <= 5 && getTime() >= 1) {
                    send("§eA partida inicia em §c" + getTime() + "§e segundo" + (getTime() > 1 ? "s" : "") + "!");
                    title("", (getTime() > 3 ? "§c" : getTime() > 1 ? "§e" : "§a") + getTime());

                    sound(org.bukkit.Sound.NOTE_PLING);
                }

                // Iniciar partida
                if (getTime() <= 0) {
                    setPhase(RoomPhase.PLAYING);

                    sound(org.bukkit.Sound.LEVEL_UP);
                }
            } else {
                // Voltar para Aguardando...
                setPhase(RoomPhase.WAITING);
            }
        }

        // Partida em andamento
        if (isPhase(RoomPhase.PLAYING)) {
            setFullTime(getFullTime() + 1);

            // Lidar com condições da partida
            if (canBeCount()) {
                for (Player player : getPlayers()) {
                    User user = (User) User.of(player.getUniqueId());

                    if (user == null || !user.isPlayer() || user.inState(ArcadeState.ALIVE)) continue;

                    Account account = user.getAccount();

                    int time = user.getRespawnTime();

                    if (time > 0)
                        user.setRespawnTime(time - 1);

                    if (time >= 1 && time <= 5) {
                        account.title("", "§c" + time + "...");

                        account.sound(org.bukkit.Sound.NOTE_PLING);
                    }

                    if (time == 0) {
                        Location spawn = user.getTeam().getBase();

                        if (spawn != null)
                            player.teleport(spawn);

                        account.sound(org.bukkit.Sound.SUCCESSFUL_HIT);

                        user.setState(ArcadeState.ALIVE);

                        hideAndShow(user);

                        arcade.handleHotbar(player, this);

                        account.title("", "§aRenasceu!");
                    }
                }
            } else {
                // Encerrar a partida (Jogadores Insuficientes)
                getTeams().stream().filter(team -> !team.getMembers().isEmpty()).findFirst().ifPresent(this::setWinner);

                send("§4§lAVISO §cNão há jogadores suficientes para continuar!");
                setPhase(RoomPhase.ENDING);
            }
        }

        // Partida encerrando
        if (isPhase(RoomPhase.ENDING)) {
            if (getTime() > 0)
                setTime(getTime() - 1);

            if (getPlayers().isEmpty() || getTime() == 0)
                setPhase(RoomPhase.RESETTING);

            if (hasWinner())
                getWinner().getPlayers().forEach(player -> Core.getPlatform().runSync(() -> 
                    com.minecraft.core.bukkit.api.firework.FireworkApi.random(player.getLocation())));
        }

        updateTimer();
    }

    public void hideAndShow(User user) {
        Player player = user.getAccount().player();

        // Primeiro, esconda todos os jogadores
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == null || target.equals(player)) continue;

            player.hidePlayer(target);
            target.hidePlayer(player);
        }

        // Agora, aplique a lógica personalizada para mostrar os jogadores corretamente
        for (Player target : getPlayers()) {
            if (target == null || target.equals(player)) continue;

            User other = (User) User.of(target.getUniqueId());

            if (user.isVanish()) {
                player.showPlayer(target);

                if (other.isVanish())
                    target.showPlayer(player);

                return;
            }

            if (other.isVanish()) {
                target.showPlayer(player);
                continue;
            }

            if (user.isPlayer() && other.isPlayer()) {
                player.showPlayer(target);
                target.showPlayer(player);
            }
        }
    }

    public void handlePulse() {
        timer();
    }

    public void handleVanish(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user != null) {
            user.setTag(com.minecraft.core.account.context.objects.tag.Tag.SPECTATOR);

            for (Player target : getPlayers()) {
                if (target == null || target.equals(player)) continue;

                player.hidePlayer(target);
                target.hidePlayer(player);
            }
        }
    }

    public void renderSidebarTime(Sidebar sidebar) {
        String prefix = isPhase(RoomPhase.STARTING)
                ? "Inicia em §a"
                : isPhase(RoomPhase.ENDING)
                ? "§cFim de jogo!"
                : "§7Aguardando...";

        String suffix = isPhase(RoomPhase.STARTING)
                ? com.minecraft.core.util.list.TimeUtil.formatTime(getTime())
                : "";

        sidebar.addRow("time", prefix + suffix);
        sidebar.blankRow();
    }

    public void updateSidebarTime(Sidebar sidebar) {
        if (isPhase(RoomPhase.PLAYING)) {
            return;
        }

        String prefix = isPhase(RoomPhase.STARTING)
                ? "Inicia em §a"
                : isPhase(RoomPhase.ENDING)
                ? "§cFim de jogo!"
                : "§7Aguardando...";

        String suffix = isPhase(RoomPhase.STARTING)
                ? com.minecraft.core.util.list.TimeUtil.formatTime(getTime())
                : "";

        if (sidebar.hasRow("time")) {
            sidebar.updateRow("time", prefix + suffix);
        }
        if (sidebar.hasRow("players")) {
            sidebar.updateRow("players", "Jogadores: §a" + getMatchUsers().size() + "/" + getMaxPlayers());
        }
    }

    public void createTimer(Sidebar sidebar) {
        renderSidebarTime(sidebar);
    }

    public void updateTimer() {
        for (Player player : getPlayers()) {
            User user = (User) User.of(player.getUniqueId());

            if (user == null) continue;

            Sidebar sidebar = user.getSidebar();

            sidebar.updateRow("players", "Jogadores: §a" + getMatchUsers().size() + "/" + getMaxPlayers());
            sidebar.updateRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + user.getArena().getIdentifier());

            String prefix = isPhase(RoomPhase.STARTING) ? "Inicia em: " : isPhase(RoomPhase.ENDING) ? "Acaba em: "
                    : isPhase(RoomPhase.WAITING) ? "§7Aguardando..." : "Tempo: ";

            String suffix = isPhase(RoomPhase.WAITING) ? ""
                    : (isPhase(RoomPhase.STARTING) || isPhase(RoomPhase.ENDING)) ? "§a" + getTime() + "s"
                    : "§7" + getTimeFormat();

            sidebar.updateRow("time", prefix + suffix);
        }
    }

    public void pullBack(Player player) {
        Vector pushDirection = player.getLocation().getDirection().setY(0).normalize().multiply(-0.7);
        pushDirection.setY(0.6);

        player.setVelocity(pushDirection);
    }

    public List<User> getMatchUsers() {
        return UserModel.getList().stream()
                .map(model -> (User) model)
                .filter(user -> getPlayers().stream().anyMatch(player -> user.getAccount().getId().equals(player.getUniqueId()) && user.isPlayer()))
                .collect(Collectors.toList());
    }

    public List<User> getAliveUsers(TeamPreset team) {
        return getMatchUsers().stream()
                .filter(user -> user.getTeam().equals(team) && user.inState(ArcadeState.ALIVE))
                .collect(Collectors.toList());
    }

    public boolean isValid(Player player) {
        User user = (User) User.of(player.getUniqueId());

        return isPhase(RoomPhase.PLAYING) && user != null && user.isPlayer();
    }

    public boolean isFullTeams() {
        return getTeamList().stream().allMatch(team -> team.isFull());
    }

    public boolean isPreGame() {
        return isPhase(RoomPhase.WAITING) || isPhase(RoomPhase.STARTING);
    }

    @Override
    public void setPhase(RoomPhase phase) {
        if (isPhase(phase)) return;

        super.setPhase(phase);
        com.minecraft.arcade.thebridge.arcade.arena.phase.PhaseAction.handle(this, phase);
    }

    public void setSuperPhase(RoomPhase phase) {
        super.setPhase(phase);
    }
}
