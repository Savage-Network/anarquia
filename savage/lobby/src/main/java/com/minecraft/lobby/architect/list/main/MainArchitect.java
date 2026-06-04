package com.minecraft.lobby.architect.list.main;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.server.Server;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.user.User;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

public class MainArchitect extends Architect {
    private static int colorPatternIndex = 0;
    private static final String[] BASE_COLORS = {"§c", "§6", "§e", "§a", "§b", "§9", "§d"};
    private static final String TEXT = "DESTAQUE";

    public MainArchitect() {
        super(ServerType.HUB);
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    public void handleSidebar(User user) {
        Account account = user.getAccount();
        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§2§lSAVAGE");
        sidebar.blankRow();
        sidebar.addRow("rank", "Rank: " + account.getRank().getColoredName());
        Server routeServer = account.getServer();
        if (routeServer == null) {
            routeServer = Core.getServerData().local();
        }
        sidebar.addRow("lobby", routeServer != null ? "Lobby: §7#" + routeServer.getId() : "Lobby: §7-");
        sidebar.blankRow();
        sidebar.addRow("clan", "Clan: " + (account.hasClan() ? account.getClanTag().trim() : "§cNenhum"));
        sidebar.blankRow();
        sidebar.addRow("players", "Jogadores: §a" + Util.formatNumber(Core.getServerData().getOnlinePlayers()));
        sidebar.blankRow();
        sidebar.addWebsiteRow();
        sidebar.display();
        TagManager.updateTag(account);
    }

    @Override
    public void handleEntities() {
        Location npcBw = getLocation("npc_bw");
        Location npcDuels = getLocation("npc_duels");
        Location npcEw = getLocation("npc_ew");
        Location npcSw = getLocation("npc_skywars");
        Location npcTb = getLocation("npc_tb");

        if (npcBw != null) {
            NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(npcBw,
                    "ewogICJ0aW1lc3RhbXAiIDogMTY0MTk0MTE3NDQwOCwKICAicHJvZmlsZUlkIiA6ICJiNjM2OWQ0MzMwNTU0NGIzOWE5OTBhODYyNWY5MmEwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJCb2JpbmhvXyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yOTY5ZWUwNTY5YTI2MzRiZDE2Mjc5YjVhODRkY2VkZDIxNjkzNDc3NTNkZjMwNDUwZmFhMjRiNTQ0N2UyMGUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                    "U0jmE7cZnaUZwFDlQmChWHUkUX6VtQGjxPDD41paZ3tme8haWEQvYoJhWHeNLnW5uThQ5GVyPDv/dOqGLDaBjFe2MVPKjEMWCNtY/xj9Ntqj0U0EezJCE7LsNGpzba6m6oIbvMeEcoHifKB02fTd+P1QwEP7kri+4FcF9x7XqABjQMfRqpLiSUYwxUvC9higlOA1FQT5vFw4fgWcCrDkalsSTsoMiD+MsKWbTQPlMwbEvi697ML4XIh32RNBcOMDmOl3wId5/sIOEMz7pWhi7v0Ck5ljP7sdzW0rH2iRHFJq+9ecqwzo4vG8Y1nqYAXHU0JrUQOLje/D3KOw7ZHkH6ADafwlU86doDRA3fh3Z5nmJrFf8obDPjMMrvvuXowiZCMOb8IjNbE/F9UvuRNXE+jX2PJmrG6eH1nX4nzAr0PRDYMKYN65D+oOdkFncNYl9R280+UIoibr067AddDY65vXU2V5mYp2DH1bYzcZK4Me1rUytAZ8vKeOUccdHsBnumlL0w7sS5IkHsLK38y0PB7T8ocqr/yt9iISqP92O1TFRxIuySlcHMwkFCcRuDDs1Aa8dw7ckXv/pEFH/fzsz7OuHIXncLmVNoLO+9RAvN4ZpvFh2tf/6eRZ4EBMHDQX/iStcqtcwl39YNEmoLy9n0nbAJxZLnX02Sr/Q6aoz3c=");

            npc.setAction((player, action) -> {
                User user = (User) User.of(player.getUniqueId());
                if (user != null)
                    user.getAccount().redirect(ServerType.HUB_BEDWARS);
            });

            npc.setContact(true);
            npc.display();

            HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(ServerType.HUB_BEDWARS.name().toLowerCase(), npcBw);
            int totalPlayers = getTotalPlayersForLobby(ServerType.HUB_BEDWARS);
            hologram.setText(java.util.Arrays.asList(
                    "§b" + ServerType.HUB_BEDWARS.getName(),
                    "§e" + Util.formatNumber(totalPlayers) + " jogando!"
            ));
        }

        if (npcDuels != null) {
            handleServerNpc(ServerType.HUB_DUELS, npcDuels,
                "ewogICJ0aW1lc3RhbXAiIDogMTc2NTY3MDczNjQ3MCwKICAicHJvZmlsZUlkIiA6ICJlZmI1ZWQ2YjVjOTU0ODBlYWFmMjAyZDIxOWVmNjBjNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaWtlSHdhazAwMSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNDQyYTk4Njk0YTRlZmE4ZDlmNTg1ZmZhNDBjYjJlZmZiYWNkZDk5NmEzOWFjNjI5OWNmYmZiNzdiNzVlYjY0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                "sCzgHQTvwYklsTxnPwJmwkJ/9b+yeVJ3DC8D7T8DIv7I2eNUZof0DGI7bY2IRePMiQEdDGI/nNUqBr0c/F4CnksBSyUy9b2s/6GKWElHKSjm/SBlsyhFIUee4nQIg+tVmULWgepaqIybKp969zPwIpKTuEiPOcDZtXXG9gumeG6BnmOje+xxv3mb3TeJXz3vrNnaaNs8oDffSbuZVQ1lSgwsN/StqEy5z7F9tIDDyrSTJj6LCxyZLOjC+n2cEinbfELu9bH7NHEsgsXt/tR3FxWXqeZSUGnmaB326QhLcOjjChKD4LUJwJSBChRdD9qv5rGyMxeU05xI7lCayZzqSE47vxJf8587lS7InsuZQSleFJzG7tSvBYbxg2oYIcsS5EKEr8ozi21wf77YEGaMnE7AraNUDIOlF0t42G1JnDAt/cKjTL/Eh0jwWJPfSuhFuE7g378f3AIhkQVn9uOkyzXEWcv4n78mfVTl/nxFvcCE34LLs/Y5wd7FhgiOjka39JUOfvQBI/LKovakui98fZ2FlQiNxrtni6JzHfC4beOHUqlhSyp3bDzZ9gTllZ6LROnijNw7Tg+SMCcFt/43ljWSYOH5DUyYUV82CG5unn1rjbXyP/ZLmM/weoFEols7rKKFC/81TAdUQxv+7ib7htn9urwnhyfFtJThc+Nt6OY=");
        }

        if (npcEw != null) {
            handleServerNpc(ServerType.HUB_EGGWARS, npcEw,
                "ewogICJ0aW1lc3RhbXAiIDogMTc2NTM3NTA5ODUyOCwKICAicHJvZmlsZUlkIiA6ICI5MjI1NDBlMTRjZjI0NDI3YmExYmZjOTk3NWVmNGQ0YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfRmxhbnR5XyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mOTg3MmMwNzVjZmE0MzM0MTJjODAwY2NhZWQxZmViNDUxOTUzNDM0MmM5YmVhNzIzOTRmM2Y5ZmI0NmEwNDg2IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                "JTI+aL3K89LAGfTtka6o1fTZAeSlVGrvGeFsu25nY3DmmrCLg17r8yCXWD+HgRj+roSrgom9T/EycPoh157c31jHK0tZoBN7VoMfhSyth5cQOL6+DJoUlDn6UTTExpwy2bi7RGhFnld35K3d9JECMvC8WgLeuIPhi+Da8ISSWUMdWC0w3SiWEJ20qwRQmGEmBZR3w3DDVUFG5eMpKd9a5SWB0mNbwcFNlsJhgakuQVSJHsRSt5kf+0zoZBHek3LMKVGdkL8Ix2YESCtcESzQdosVBsjMbXLSKR+wbl2tMPaUSyKbU+1sgxg9eHiaw99oljOwHYJNZXq2zcIHAESULpbhgktggbdTk93pt1p2/mpXKUrXUtMyzmlsIN16PH3AIFNNaQkuPq97XrGKEUrwS9LAkdR3IoehQZFkanWfkClmIU7ZtsQb2DmyJS1MPGED17JSEXGC3FbyLPc9RJH4SlmKIaXmllKGCGrgUKI/Bf3bx5mbIhDgJiNGkgI933UQy2WbseSA97th/uNGo6U6uR1pJYhQFVONwu+k2fJzhgHBKHDBIDVR3vVSVDdZ01R4FPk8fb2h24VBiJySuIXCBZtmdGtObRVUaXiLkDhgl7I/8WXFL1j1XkM/PoUc8+eP1zjWvy7NVzqe8VrK2YMxVnlsuDrDuM2oDPWGgkqGa7k=");
        }

        if (npcSw != null) {
            handleServerNpc(ServerType.HUB_SKYWARS, npcSw,
                    "ewogICJ0aW1lc3RhbXAiIDogMTY4OTgxMzk5MjgwMSwKICAicHJvZmlsZUlkIiA6ICJkNzU2OTc4MWUyYjY0OWIyYjVlMjVlYTJhNDZkOGQxOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEckthcGRvciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMDQ2Yzk1NmVmOThhOTkyZmZmN2ZlNDkxZGQ4MWNlMTBmNTM5YTk1YjVlNzkzMWQ4ZjMzZDAwYjdkOTYzNjdjIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                    "BmD50CTXr6FS78VcYs1fC5KW5Sh5m7I7WSYdfLdGLLihj923Jz3ZQSEruphISEPd6/ovNwPK1lzVQlHTKR6TCJnDVwUkS/pLx30HZFjA/vy7qebCu8GeEkEA8XGTbkj1noIMsKhFMs1rhuNzh6ZeAeMzxqodDBQVzuaSns/JXin8OuHxc6OAEPYCTIJ4cMKOkXTApR1BIHFL6HDa/xjbS9SbUDyNXioo5fIkwwHNgD+uvfz5YDBonbBTaWwxkcNjdrO69sTI9yudcpcm3ouDAaYKgMtVuAn08wgwKGvg6lNZrQWOSiO8l3YgHhpHTA8vSwdMetiy2jurRJVKBAF6EmmnBRtyYRdhVBccRk0TLG/A+XC5/PIk1Ix+N2f+xdIF6MsdXTynhWse+ACdxxocdLELy/5hQFf9bUw/nWwf+uCVjGPNUQ8tmrTGvic/pyU6a3fgUZFCYfa3jhYrI/86uhF85xNgYLXjnP+OwTLbJsrTXEJa+3hqXtmSWmJC5JdZdJrhHGKtBqShOx0mEzpbZr8ho6Etktzo95haGLs9aDyNkO5LUH+RuAQTOfwO5ZfeoP0cymxxx7wZtYOvMNTtw3BRVJwzB+1NKJjL3aflolePHn2AVoZ4hCE/Oy7dBZ3n/vj66gI7F91p7Q8nxiK0WrQq/pjAQt3wmwY+whbELc0=");
        }

        if (npcTb != null) {
            handleServerNpc(ServerType.HUB_THE_BRIDGE, npcTb,
                    "ewogICJ0aW1lc3RhbXAiIDogMTczNjA4Mjk4OTI1OCwKICAicHJvZmlsZUlkIiA6ICJhYzY1NDYwOWVkZjM0ODhmOTM0ZWNhMDRmNjlkNGIwMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGFjZUd1cmxTa3kiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTI0NjQ2MTM0ZTljMmQzYTIwNDM5OGE2Mjk5ZjM0OTRiOGE4ZjI5Y2YwNTA1NjI5NGE0OGM0MGVmN2VlYTZlYyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                    "MgZhdUdFDIYYTs6Yd6vQtkiJwuGwgJ9y0QgzurSd4Zr6eDj+RugyU/tnuPhI4vh6oqQNndbMKJ8i2yAKFXnofuB5d/azVqZJqjkLk2RnvqLp7QiQEAw3B1AhKeDKQMsfChthRDAWx/xkRwVQT6VhdBFLmeNcTkJUbiHH/N5F37MuBFopFeu+ccqLP+U2Y+VkMgAY54fvteLMWRAWNYnpPuWCZLHYC5F6kY4h4C9TKFb8bGE4hxAcabDFJr8QgpBiel8vLN4/DKaRWsDi0HicVj9lrpQCbawOaVOC1rGgaLMXMLt+9urgj6fzfz43lshDOIxNxMZOWxm0beCHv2DrOFngvqCJA5lNpjcyeWar2Knt1SPU/Tt1j42UZFhlZN3uex9x62ZShH+vpPxL6ZTw0biGiRdXb1OSrtdOtMJAMjvLxyR7NQMlhIuwkHZ4ihYzR2eZQXbG19daHjyokBSTWevrGQq67qMbIzwpxeIuRj8YKdF09Db7HH4vXTJ6kDF0JtYZWFsx2NA10dO/USXjH5S4aIm5Q3WVDJDn0W19rI64YDgVE+OcNB0tsUEjZoO0vO7yBEKobPZaBQUqJh/Wr8GXFmTjori+pFDlX2lLKUb0RmMoX4IsDtLvWgn6LDWfWYtzxu4wKf56pB7+G6eMY6GNJCrBEDhLaqMjy29C4cY=");
        }
    }

    @Override
    public void chat(Player player, String message) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        Core.getAccountController().list().forEach(target -> {
            StringBuilder builder = new StringBuilder();
            
            // Adicionar medalha se não for NONE (antes do prefixo da tag)
            if (account.getMedal() != null && !account.getMedal().equals(Medal.NONE)) {
                builder.append(account.getMedal().getColoredSymbol()).append(" ");
            }
            
            builder.append(account.getTag().getByPrefix(target.getTagPrefix()))
                   .append(account.getNickname())
                   .append(": ");

            TextComponent text = new TextComponent(builder.toString());
            text.addExtra(account.isAllowColoredChat() ? "§f" + Util.color(message) : "§7" + message);

            target.send(text);
        });
    }

    @Override
    public Vector getSlimeJump(Vector direction) {
        return direction.multiply(2.1).setY(0.6);
    }

    private int getTotalPlayersForLobby(ServerType server) {
        int onlinePlayers = Core.getServerData().getOnlinePlayers(server);
        ServerType realServer = server.getRealServerOfLobby();
        if (realServer != null)
            onlinePlayers += Core.getServerData().getOnlinePlayers(realServer);
        return onlinePlayers;
    }

    @EventHandler
    public void onAsyncCounterUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (ServerType server : ServerType.list(ServerType::isLobby)) {
                if (server.equals(ServerType.HUB)) continue;

                HologramServer hologram = BukkitCore.getManager().getHologram().getServer(server.name().toLowerCase());

                if (hologram != null) {
                    int totalPlayers = getTotalPlayersForLobby(server);
                    hologram.setText(1, "§e" + Util.formatNumber(totalPlayers) + " jogando!");
                }
            }
        }
    }


    public static String generateColoredText() {
        StringBuilder result = new StringBuilder();
        int letterIndex = 0;

        for (int i = 0; i < TEXT.length(); i++) {
            char character = TEXT.charAt(i);
            if (character == ' ') {
                result.append(character);
            } else {
                int colorIndex = (colorPatternIndex + letterIndex) % BASE_COLORS.length;
                result.append(BASE_COLORS[colorIndex]).append("§l").append(character);
                letterIndex++;
            }
        }
        return result.toString();
    }
}
