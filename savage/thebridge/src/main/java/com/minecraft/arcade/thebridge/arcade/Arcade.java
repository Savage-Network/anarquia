package com.minecraft.arcade.thebridge.arcade;

import com.grinderwolf.swm.plugin.SWMPlugin;
import com.minecraft.arcade.thebridge.TheBridge;
import com.minecraft.arcade.thebridge.arcade.arena.Arena;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.redirect.Redirect;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.slime.SlimeWorldController;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.manager.Manager;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.minecraft.arcade.thebridge.user.User;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class Arcade extends ArcadeHolder {

    private final File mapsDirectory;

    @Getter
    private final Manager manager;

    @Getter
    private final List<Material> blocksAllowedToBreak;

    public Arcade(String mapsDirectory, Integer minRooms, Integer maxRooms, ArcadeCategory category) {
        super(mapsDirectory, minRooms, maxRooms, category);

        this.mapsDirectory = new File(TheBridge.getInstance().getDataFolder().getAbsolutePath() + "/maps");

        this.manager = BukkitCore.getManager();

        this.blocksAllowedToBreak = new ArrayList<>();
    }

    @Override
    public boolean load() {
        Core.getLogger().info("[" + getName() + "] Iniciando jogo...");
        Core.getLogger().info("[" + getName() + "] Procurando mapas em: " + mapsDirectory.getAbsolutePath());
        Core.getLogger().info("[" + getName() + "] Nome do arcade (getName()): " + getName());

        if (!loadMaps(mapsDirectory)) {
            Core.getLogger().warning("[" + getName() + "] Falha ao carregar mapas de: " + mapsDirectory.getAbsolutePath());
            return false;
        }

        List<Map> availableMaps = getMaps();

        if (availableMaps.isEmpty()) {
            Core.getLogger().warning("[" + getName() + "] Não há mapas disponíveis para este jogo.");
            Core.getLogger().warning("[" + getName() + "] Diretório esperado: " + new File(mapsDirectory, getName().toLowerCase()).getAbsolutePath());
            return false;
        }

        Core.getLogger().info("[" + getName() + "] Jogo iniciado com sucesso. (Lazy loading ativado - arenas serão criadas sob demanda)");
        Core.getLogger().info("[" + getName() + "] Mapas carregados: " + availableMaps.size());
        return true;
    }

    @Override
    public void unload() {
        Core.getLogger().info("[" + getName() + "] Desligando dados...");

        getRooms().clear();

        Core.getLogger().info("[" + getName() + "] O jogo foi interrompido.");
    }

    @Override
    public Arena setupRoom(Map map, Slot slot, Type type) {
        Instant now = Instant.now();

        int id = (getId().getAndIncrement() + 1);

        Arena arena = new Arena(id, this, map, slot);

        arena.setType(type);

        Core.getLogger().info("[" + getName() + "/" + id + "] Criando arena...");

        String[] mapSplit = map.getName().split(" ");

        String mapName = mapSplit.length > 1 ? mapSplit[0] + "-" + mapSplit[1] : map.getName();

        String templateName = getName().toLowerCase() + "-" + mapName.toLowerCase(),
                worldName = templateName + "-" + id;

        handleArenaData(arena, id, templateName, worldName, now);

        return arena;
    }

    @Override
    public Arena setupRoom(Map map, Slot slot) {
        return setupRoom(map, slot, Type.CASUAL);
    }

    protected void handleArenaData(Arena arena, int id, String templateName, String worldName, Instant now) {
        Slot slot = arena.getSlot();

        SlimeWorldController.cloneWorldFromTemplate(SWMPlugin.getInstance(), templateName, worldName, () -> {
            final int maxPlayers = slot.getMaxPlayers();

            List<TeamPreset> teamList = arena.buildTeamList(ChatColor.RED, ChatColor.BLUE).stream()
                    .map(team -> (TeamPreset) team)
                    .toList();

            teamList.forEach(team -> team.setMaxPlayers(maxPlayers / 2));

            arena.setMinPlayers(maxPlayers);
            arena.setMaxPlayers(maxPlayers);

            arena.getTeams().addAll(teamList);

            handleProperties(arena);

            arena.handleBorder();

            getRooms().add(arena);

            Core.getLogger().info("[" + getName() + "/" + id + "] Arena criada com sucesso. (Tempo médio: " + com.minecraft.core.util.Util.formatInstant(now) + ")");
        });
    }

    public void checkAndGenerateArenas() {
        int totalArenas = getRooms().size();
        int occupiedArenas = (int) getRooms().stream().filter(room -> !room.isAvailable()).count();

        if (occupiedArenas >= (totalArenas / 2)) {
            final int NUM_ARENAS_TO_GENERATE = 3;

            Core.getLogger().info("[" + getName() + "] Gerando " + NUM_ARENAS_TO_GENERATE + " arenas...");

            int loaded = 0;
            for (int i = 0; i < NUM_ARENAS_TO_GENERATE; i++) {
                Map randomMap = getRandomMap();

                if (randomMap != null) {
                    setupRoom(randomMap, randomMap.getArcade().getSlots().get(0), Type.CASUAL);
                    loaded++;
                }
            }
        }
    }

    @Override
    public Arena findBestArena(UUID sender, ArcadeRouteContext route) {
        Account account = Core.getAccountData().of(sender);

        Redirect redirect = Core.getRedirectData().of(sender);

        if (redirect != null) {
            return (Arena) getRooms().stream()
                    .filter(room -> room.isCategory(route.getArcade()) && room.getId() == route.getRoomId() && room.isFiltered(route.getMapId()))
                    .findFirst()
                    .orElse(null);
        }

        Stream<Arena> stream = getRooms().stream().map(room -> (Arena) room)
                .filter(arena -> arena.isAvailable() && arena.getSlot().equals(route.getSlot()))
                .toList().stream();

        if (route.getType() != null)
            stream = stream.filter(arena -> arena.getType().equals(route.getType()));

        if (route.hasMap())
            stream = stream.filter(arena -> arena.isFiltered(route.getMapId()));

        Arena found = stream.filter(arena -> {
            if (account.isPartyOwner() && route.isLinked() && !arena.hasReservations(route.getLink())) {
                arena.setReservations(route.getLink());
            }

            if (account.isPartyOwner() && arena.isReserved() && arena.hasReservation(sender)) {
                Core.getLogger().info("[PT-Route] O dono " + account.getNickname() + " entrou em " + arena.getIdentifier() + ".");
                return true;
            }

            if (arena.isReserved() && arena.hasReservation(sender) && !arena.isFull()) {
                arena.removeReservation(sender);
                return true;
            }

            return arena.isAvailable();
        }).min((a1, a2) -> Integer.compare(a2.getTotalPlayers(), a1.getTotalPlayers())).orElse(null);
        
        if (found == null) {
            Core.getLogger().info("[" + getName() + "] Nenhuma arena disponível. Criando nova arena sob demanda...");
            found = createArenaOnDemand(route);
        }
        
        return found;
    }
    
    private Arena createArenaOnDemand(ArcadeRouteContext route) {
        Map map;
        
        // Se a rota especifica um mapa, usar ele
        if (route.hasMap()) {
            map = getMaps().stream()
                    .filter(m -> m.getId() == route.getMapId())
                    .findFirst()
                    .orElse(null);
        } else {
            // Caso contrário, pegar um mapa aleatório
            map = getRandomMap();
        }
        
        if (map == null) {
            Core.getLogger().warning("[" + getName() + "] Não foi possível criar arena: nenhum mapa disponível.");
            return null;
        }
        
        Slot slot = route.getSlot() != null ? route.getSlot() : getCategory().getSlots().get(0);
        Type type = route.getType() != null ? route.getType() : Type.CASUAL;
        
        Arena arena = setupRoom(map, slot, type);
        
        if (arena != null) {
            Core.getLogger().info("[" + getName() + "] Arena criada sob demanda: " + arena.getIdentifier());
        }
        
        return arena;
    }

    public void handleProperties(Arena arena) {
        // O buildTeam já configura o base automaticamente usando red_spawn/blue_spawn do mapa
        // Não é necessário fazer nada aqui, pois o base é final e já é setado no construtor
    }

    public void join(User user) {
        Player player = user.getAccount().player();

        Arena arena = user.getArena();

        if (arena != null) {
            handleSidebar(user);
            handleHotbar(player, arena);
        }
    }

    public abstract void handleSidebar(User user);

    public abstract void handleHotbar(Player player, Arena arena);

    public void handleDefaultHotbar(Player player) {
        User user = (User) User.of(player.getUniqueId());

        Arena arena = user.getArena();

        org.bukkit.inventory.PlayerInventory inv = player.getInventory();

        player.getInventory().clear();

        if (user.isPlayer() && arena.isPreGame())
            handleWaitingHotbar(inv);

        if (user.inState(com.minecraft.core.arcade.route.state.ArcadeState.DEAD) || arena.isPhase(RoomPhase.ENDING))
            handleDeathHotbar(inv);

        if (!user.inState(com.minecraft.core.arcade.route.state.ArcadeState.RESPAWN))
            inv.setItem(8, com.minecraft.core.api.item.Item.of(org.bukkit.Material.ACACIA_DOOR_ITEM, "§cVoltar").interact(event -> event.getPlayer().performCommand("hub")));
    }

    public void handleWaitingHotbar(org.bukkit.inventory.PlayerInventory inv) {
        // Hotbar durante espera
    }

    public void handleDeathHotbar(org.bukkit.inventory.PlayerInventory inv) {
        inv.setItem(0, com.minecraft.core.api.item.Item.of(org.bukkit.Material.PAPER, "§aJogar Novamente")
                .interact(event -> event.getPlayer().performCommand("playagain")));
    }

    public void handleDeath(User user, User killer, DeathCause cause) {
        Player player = user.getAccount().player();

        Arena arena = user.getArena();
        Arcade arcade = arena.getArcade();

        TeamPreset team = user.getTeam();

        user.setCombat(null);

        // Partida 1v1
        if (killer != null) {
            killer.setKills(killer.getKills() + 1);

            killer.setCombat(null);
        }

        // Quando morre, apenas spawnar no spawn do time (não marcar ponto)
        // O ponto só é marcado quando pula no portal (handlePoint no Solo.java)
        sendDeathMessage(arena, user, killer, cause);

        // Definir estado de morte
        user.setState(com.minecraft.core.arcade.route.state.ArcadeState.DEAD);
        
        // Definir tempo de respawn (3 segundos para The Bridge)
        user.setRespawnTime(3);
        
        // Teleportar para o spawn do time imediatamente
        Location spawn = team.getBase();
        if (spawn != null) {
            player.teleport(spawn);
        }

        BukkitCore.getManager().getCooldown().resetCooldown(player);

        if (player.getHealth() < player.getMaxHealth())
            player.setHealth(player.getMaxHealth());

        if (!cause.equals(DeathCause.VOID) && !arcade.hasFeature(ArcadeFeature.NOT_PULL_BACK))
            arena.pullBack(player);
    }

    public void sendDeathMessage(Arena arena, User user, User killer, DeathCause cause) {
        boolean hasKiller = killer != null;

        if (hasKiller) {
            com.minecraft.core.account.Account account = killer.getAccount();

            account.sound(org.bukkit.Sound.SUCCESSFUL_HIT);
        }

        com.minecraft.core.account.Account account = user.getAccount();

        arena.send(user.getTeam().getColor() + account.getNickname() + " §e" +
                (cause.equals(DeathCause.PLAYER) ? (hasKiller ? "foi morto por " + killer.getTeam().getColor() + killer.getAccount().getNickname() : "morreu")
                        : (hasKiller ? "foi jogado no void por " + killer.getTeam().getColor() + killer.getAccount().getNickname() : "caiu no void"))
                + "§e.");
    }

    public enum DeathCause {
        PLAYER, VOID
    }
}
