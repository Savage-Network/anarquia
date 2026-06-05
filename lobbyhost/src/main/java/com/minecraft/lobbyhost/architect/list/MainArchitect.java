package com.minecraft.lobbyhost.architect.list;

import com.minecraft.lobbyhost.architect.Architect;
import com.minecraft.lobbyhost.architect.ServerType;
import com.minecraft.lobbyhost.architect.SignedLocation;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.lobby.HotbarLayout;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyPlayerSetup;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyScoreboard;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyTabList;
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
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0f);
        player.setLevel(0);

        SignedLocation spawn = location("spawn");
        if (spawn != null) {
            Location loc = spawn.toBukkit();
            if (loc.getWorld() != null) player.teleport(loc);
        }

        Account account = Core.getAccountController().of(player.getUniqueId());
        LobbyPlayerSetup.apply(player, account);

        handleSidebar(player);
        LobbyTabList.apply(player);
        if (account != null) {
            TagManager.updateTag(account);
        }

        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;
            Account acc = Core.getAccountController().of(player.getUniqueId());
            boolean canFly = acc != null && (acc.isVIP() || player.hasPermission("lobbyhost.fly"));
            player.setAllowFlight(canFly);
            if (canFly) player.setFlying(true);
            handleHotbar(player);
            PlayerVisibilityService.apply(player);
        }, 5L);

        player.sendMessage(com.minecraft.lobbyhost.util.ModernText.parse(
                "<!italic>Bem-vindo ao " + com.minecraft.lobbyhost.util.ModernText.brandGradient("SAVAGE")
                + " <#94A3B8>— <#F1F5F9>" + server.name()));
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
