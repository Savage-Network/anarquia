package com.minecraft.arcade.skywars.arcade.arena.context.phase;

import com.minecraft.arcade.skywars.arcade.Arcade;
import com.minecraft.arcade.skywars.arcade.arena.Arena;
import com.minecraft.arcade.skywars.user.User;
import com.minecraft.arcade.skywars.arcade.game.SkyWarsGameLogic;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.slime.SlimeWorldController;
import com.minecraft.core.bukkit.api.vanish.Vanish;
import com.minecraft.core.member.list.skywars.SkyMember;
import com.minecraft.core.member.list.skywars.objects.metadata.objects.stats.SkyStats;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ArenaPhaseExecutor {

    public static void execute(Arena arena, RoomPhase phase) {
        List<User> users = arena.getUsers().stream()
                .filter(user -> !Vanish.has(user.getPlayer()))
                .collect(Collectors.toList());

        Arcade arcade = (Arcade) arena.getArcade();

        switch (phase) {
            case WAITING: {
                arena.setTime(0);
                users.forEach(arcade::handleSidebar);
                break;
            }
            case STARTING: {
                arena.setTime(5);
                
                // Inicializar propriedades do jogo (timer de refill) - LÓGICA DO DUELS
                SkyWarsGameLogic.initializeProperties(arena);
                
                users.forEach(arcade::handleSidebar);
                break;
            }
            case PLAYING: {
                arena.setTime(0);
                arena.setFullTime(0);

                       // Teleportar jogadores para suas ilhas
                       users.forEach(user -> {
                           Player player = user.getAccount().player();
                           if (player == null || !player.isOnline()) return;

                    // Determinar ilha baseado na ordem de entrada
                    int playerIndex = users.indexOf(user);
                    Location islandLocation = arena.getLocation("island_" + (playerIndex + 1));
                    
                    if (islandLocation == null) {
                        islandLocation = arena.getLocation("island_spawn_" + (playerIndex + 1));
                    }
                    
                    if (islandLocation == null) {
                        islandLocation = arena.getLocation("spawn");
                    }

                    // Agora sim, teleportar para a ilha e ativar o jogador
                    if (islandLocation != null) {
                        player.teleport(islandLocation);
                    }

                    // Jogador já está em ArcadeState.ALIVE desde que entrou
                    // Agora apenas ativamos ele na partida
                    user.getContext().setAlive(true);
                    user.getContext().setKills(0);

                    arcade.load(user);
                });

                // Preencher baús usando a lógica do Duels
                SkyWarsGameLogic.fillChestsOnStart(arena);

                arena.send("", "§a§lPARTIDA INICIADA!", "§eÚltimo jogador vivo vence!", "");
                arena.sound(Sound.LEVEL_UP);

                // Atualizar visibilidade para todos os jogadores
                arena.getAllUsers().forEach(arena::hideAndShow);

                Core.getLogger().info("[SkyWars/" + arena.getIdentifier() + "/" + arena.getMap().getName() + "] Partida iniciada com " + arena.getTotalPlayers() + " jogadores!");

                break;
            }
            case ENDING: {
                arena.setTime(15);

                // Atualizar visibilidade para todos (no final todos se veem)
                arena.getAllUsers().forEach(arena::hideAndShow);

                // Atualizar estatísticas
                updateStats(arena);

                users.forEach(user -> {
                    Player player = user.getAccount().player();

                    if (player != null && player.isOnline()) {
                        Core.getPlatform().runSync(() -> arcade.load(user));
                    }

                    handleEndMessage(user, arena);
                });

                if (arena.hasWinner()) {
                    User winner = arena.getSkyWarsWinner();
                    String winnerName = winner.getAccount().getRank().getColor() + winner.getAccount().getNickname();
                    arena.send(winnerName + "§e venceu a partida!");
                }

                arena.sound(Sound.ENDERDRAGON_GROWL, 13, 1);
                break;
            }
            case RESETTING: {
                users.forEach(user -> {
                    Player player = user.getPlayer();
                    if (player != null) player.performCommand("playagain");
                });

                // Salvar referência da arena antes de descarregar
                Arena arenaToCopy = arena;

                SlimeWorldController.unloadWorld(arena.getWorld(), () -> {
                    arcade.removeArena(arenaToCopy);
                    
                    // Criar nova arena copiando a antiga
                    Core.getPlatform().runSync(() -> {
                        arcade.copyArena(arenaToCopy);
                    }, 20);
                });

                break;
            }
        }
    }


    private static void updateStats(Arena arena) {
        User winner = arena.hasWinner() ? arena.getSkyWarsWinner() : null;

        List<User> allUsers = arena.getAllUsers();

        for (User user : allUsers) {
            SkyMember member = user.getMember();
            if (member == null) continue;

            ArcadeCategory arcadeCategory = arena.getArcade().getCategory();
            SkyStats stats = member.getStats(arcadeCategory, arena.getType());

            stats.setMatches();

            int gainXp = Core.RANDOM.ints(40, 140).findFirst().orElse(35);

            if (winner != null && winner.equals(user)) {
                stats.setWins();
                stats.setWinStreak();
                member.addLevelXp(gainXp);

                user.setXpGain(gainXp);
                user.getAccount().send("§b+" + gainXp + " XP");
            } else {
                stats.setDefeats();
                stats.resetWinStreak();

                int lostXp = Core.RANDOM.ints(30, 45).findFirst().orElse(25);
                member.removeLevelXp(lostXp);
            }

            // As kills já foram atualizadas durante o jogo, apenas atualizar deaths
            stats.setDeaths();

            member.updateStats(stats);
        }
    }

    private static void handleEndMessage(User user, Arena arena) {
        User winner = arena.hasWinner() ? arena.getSkyWarsWinner() : null;

        Account account = user.getAccount();

        if (winner != null && winner.equals(user)) {
            account.send(TextComponent.fromLegacyText("§a§lVITÓRIA! §f» §eVocê venceu a partida!"));
        } else if (winner != null) {
            account.send(TextComponent.fromLegacyText("§c§lDERROTA! §f» §e" + winner.getAccount().getNickname() + " §e venceu a partida!"));
        } else {
            account.send(TextComponent.fromLegacyText("§e§lEMPATE! §f» §eA partida terminou em empate!"));
        }

        account.send(TextComponent.fromLegacyText("§7Kills: §a" + user.getContext().getKills()));
        account.send(TextComponent.fromLegacyText("§7Mortes: §c1"));
        account.send(TextComponent.fromLegacyText("§7XP Ganhos: §b" + user.getXpGain()));
    }
}

