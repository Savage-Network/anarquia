package com.minecraft.arcade.eggwars.arcade.list.normal;

import com.minecraft.arcade.eggwars.arcade.Arcade;
import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.user.User;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.TimeUtil;

public class Quartet extends Arcade {

    public Quartet(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.EGGWARS_QUARTET);
    }

    @Override
    public void handleSidebar(User user) {
        Arena arena = user.getArena();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle((arena.isType(Type.CASUAL) ? "§6" : "§5") + "§lBED WARS");
        sidebar.blankRow();

        // Renderizar scoreboard quando o jogo iniciou
        if (arena.isPhase(RoomPhase.PLAYING)) {
            arena.renderSidebarTeams(user);

            sidebar.blankRow();
            sidebar.addRow("kills", "Kills: §a" + Util.formatNumber(user.getKills()));
            sidebar.addRow("finalKill", "Kills Finais: §a" + Util.formatNumber(user.getFinalKill()));
            sidebar.addRow("eggDestruction", "Ovos Destruídos: §a" + Util.formatNumber(user.getEggDestruction()));

        } else {
            // Renderizar scoreboard quando o jogo ainda não iniciou

            sidebar.addRow("map", "Mapa: §a" + arena.getMap().getName());
            sidebar.addRow("mode", "Modo: §a" + arena.getArcade().getCategory().getName());

            sidebar.blankRow();
            arena.renderSidebarTime(sidebar);

            sidebar.addRow("players", "Jogadores: §a" + arena.getTotalPlayers() + "/" + arena.getMaxPlayers());
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void updateSidebar(User user) {
        Sidebar sidebar = user.getSidebar();

        sidebar.updateRow("kills", "Kills: §a" + Util.formatNumber(user.getKills()));
        sidebar.updateRow("finalKill", "Kills Finais: §a" + Util.formatNumber(user.getFinalKill()));
        sidebar.updateRow("eggDestruction", "Ovos Destruídos: §a" + Util.formatNumber(user.getEggDestruction()));
    }
}
