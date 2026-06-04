package com.minecraft.lobbyhost.architect.list;

import com.minecraft.lobbyhost.architect.Architect;
import com.minecraft.lobbyhost.architect.ServerType;
import com.minecraft.lobbyhost.architect.SignedLocation;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.lobby.HotbarLayout;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyPlayerSetup;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyScoreboard;
import com.minecraft.lobbyhost.bukkit.lobby.PlayerVisibilityService;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import org.bukkit.Bukkit;
import com.minecraft.lobbyhost.display.npc.NpcManager;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Architect do HUB principal. Espelha {@code MainArchitect} do Savage:
 * spawn central, hotbar de navegacao, sidebar institucional.
 */
public final class MainArchitect extends Architect {

    public MainArchitect(ServerType server, List<SignedLocation> locations) {
        super(server, locations);
    }

    @Override
    public void load() {
        NpcManager.spawnForArchitect(this);
    }

    @Override
    public void unload() {
        NpcManager.unloadAll();
    }

    @Override
    public void onJoin(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0f);
        player.setLevel(0);
        player.setFlying(false);
        player.setAllowFlight(false);

        SignedLocation spawn = location("spawn");
        if (spawn != null) {
            Location loc = spawn.toBukkit();
            if (loc.getWorld() != null) player.teleport(loc);
        }

        Account account = Core.getAccountController().of(player.getUniqueId());
        LobbyPlayerSetup.apply(player, account);

        handleSidebar(player);
        if (account != null) {
            TagManager.updateTag(account);
        }

        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;
            player.setFlying(false);
            player.setAllowFlight(account != null && account.getRankType() != com.minecraft.lobbyhost.account.context.objects.rank.RankType.MEMBER);
            handleHotbar(player);
            PlayerVisibilityService.apply(player);
        }, 2L);

        player.sendMessage(Component.text("§eBem-vindo ao §6§lSAVAGE §e- " + server.name() + "!"));
    }

    @Override
    public void onQuit(Player player) {
        // Cleanup pessoais (visibilidade, displays) em proximas fases.
    }

    @Override
    public void handleHotbar(Player player) {
        HotbarLayout.applyDefault(player);
    }

    @Override
    public void handleSidebar(Player player) {
        LobbyScoreboard.applyMain(player, server);
    }
}
