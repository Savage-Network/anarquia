package com.minecraft.core.bukkit.listener.server;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.Rank;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.payload.ArcadePayload;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.event.ArenaSearchedEvent;
import com.minecraft.core.arcade.room.payload.RoomPayload;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.api.item.book.BookItem;
import com.minecraft.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.api.vanish.Vanish;
import com.minecraft.core.bukkit.event.type.account.AccountClanChangeEvent;
import com.minecraft.core.bukkit.event.type.account.AccountPrefixTypeEvent;
import com.minecraft.core.bukkit.event.type.account.AccountRankUpdateEvent;
import com.minecraft.core.bukkit.event.type.account.AccountRefreshEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerCooldownEndEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerSidebarUpdateEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.bukkit.manager.Manager;
import com.minecraft.core.bukkit.manager.list.PermissionManager;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.TimeUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.*;

public class UpdateListener implements Listener {

    private final Manager manager;
    private int cleanupCounter = 0;

    public UpdateListener() {
        this.manager = BukkitCore.getManager();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSearched(ArenaSearchedEvent event) {
        Account account = event.getSender();

        Room arena = event.getArena();

        Join join = event.getRoute().getJoin();

        /* Executar ação de rota para sender */

        account.redirect(arena, join);
    }

    @EventHandler
    public void onAccountClan(AccountClanChangeEvent event) {
        Account account = event.getAccount();

        if (account != null && Core.getPlatform().isOnlinePlayer(account.getId()))
            TagManager.updateTag(account);
    }

    @EventHandler
    public void onServerUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.TICK)) {
            if (event.getTicks() % 6000 == 0) {
                System.gc();

                Core.message("Limpeza de memória efetuada.");
            }

            /* Cooldown */
            for (UUID uuid : manager.getCooldown().getCooldownMap().keySet()) {
                Player player = Bukkit.getPlayer(uuid);

                if (player != null) {
                    List<Cooldown> list = manager.getCooldown().getCooldownMap().get(uuid);
                    Iterator<Cooldown> iterator = list.iterator();

                    /* Get Cooldown */
                    Cooldown search = null;

                    while (iterator.hasNext()) {
                        Cooldown cooldown = iterator.next();

                        if (!cooldown.expired()) {
                            search = cooldown;
                            continue;
                        }

                        iterator.remove();

                        new PlayerCooldownEndEvent(player, cooldown).call();
                    }

                    /* Display Cooldown */
                    if (search != null && search.isShowBar()) {
                        manager.getCooldown().display(player, search);
                    } else if (list.isEmpty()) {
                        ProtocolHandler.sendBar(player, " ");
                        manager.getCooldown().getCooldownMap().remove(player.getUniqueId());
                    }
                }
            }
        }

        if (event.isType(UpdateType.SECOND)) {
            // Barra de modo admin
            Vanish.getList().forEach(vanish -> {
                Player player = vanish.getPlayer();

                if (player != null)
                    ProtocolHandler.sendBar(player, "§fVocê está no modo §dVANISH");
            });

            // Limpar servidores mortos periodicamente (a cada 10 segundos)
            cleanupCounter++;
            if (cleanupCounter >= 10) {
                cleanupCounter = 0;
                Core.getServerData().cleanupDeadServers();
            }

            Core.getServerData().updatePlayerCounter(Bukkit.getOnlinePlayers().size());

            for (Sidebar sidebar : manager.getSidebar().list()) {
                if (Core.getServerData().local().isArcade() || !sidebar.hasRow("players"))
                    continue;

                sidebar.updateRow("players", "Online: §b" + Util.formatNumber(Core.getServerData().getOnlinePlayers()));
            }

            for (ArcadeHolder arcade : manager.getArcade().getArcades()) {
                if (arcade == null || arcade.getRooms().isEmpty()) continue;

                ArcadePayload arcadePayload = Core.getArcadeData().read(arcade.getCategory());

                if (arcadePayload == null) continue;

                int onlinePlayers = arcade.getRooms().stream().mapToInt(room -> room.getPlayers().size()).sum();

                arcadePayload.setOnlinePlayers(onlinePlayers);

                List<RoomPayload> payloadList = new ArrayList<>(arcadePayload.getRooms());

                Set<Room> rooms = new HashSet<>(arcade.getRooms());

                payloadList.removeIf(payload -> payload == null
                        || rooms.stream().noneMatch(room -> payload.getIdentifier().equalsIgnoreCase(room.getIdentifier())));

                arcadePayload.setRooms(payloadList);

                for (Room room : rooms) {
                    if (room == null) continue;

                    RoomPayload payload = payloadList.stream()
                            .filter(search -> search.getIdentifier().equalsIgnoreCase(room.getIdentifier()))
                            .findFirst()
                            .orElse(null);

                    if (payload == null)
                        payloadList.add(new RoomPayload(room));
                    else {
                        payload.setOnlinePlayers(room.getTotalPlayers());

                        if (room.getPhase() != payload.getPhase())
                            payload.setPhase(room.getPhase());

                        int indexOf = payloadList.indexOf(payload);

                        if (indexOf >= 0)
                            payloadList.set(indexOf, payload);
                    }
                }

                Core.getArcadeData().update(arcadePayload);
            }
        }
    }

    @EventHandler
    public void cooldownEnd(PlayerCooldownEndEvent event) {
        Player player = event.getPlayer();

        player.setExp(0);
        player.setLevel(0);
    }

    @EventHandler
    public void sidebar(PlayerSidebarUpdateEvent event) {
        Player player = event.getPlayer();

        UserModel user = UserModel.of(player.getUniqueId());

        if (user != null) {
            Sidebar sidebar = user.getSidebar();

            sidebar.updateRow(event.getRow());
        }
    }

    @EventHandler
    public void refresh(AccountRefreshEvent event) {
        Account account = event.getAccount();

        if (account == null) return;

        if (Core.getPlatform().isOnlinePlayer(account.getId())) {
            Core.getAccountController().save(account);

            UserModel user = UserModel.of(account.getId());

            if (user == null) return;

            user.setAccount(account);

            if (ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE) {
                if (account.getTag() != user.getTag())
                    user.setTag(account.getTag());
            }
        }
    }

    @EventHandler
    public void rankUpdate(AccountRankUpdateEvent event) {
        Account account = event.getAccount();

        Rank rank = event.getRank();

        Tag tag = Tag.of(rank.getType());

        if (ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE)
            account.setTag(tag);

        Player player = account.player();

        if (player != null) {
            if (rank.isPayment()) {
                TextComponent message = new TextComponent("Sua compra foi processada!\n\n");

                message.addExtra("Você adquiriu o rank: " + rank.getColor() + "Rank " + rank.getName() + "\n\n");
                message.addExtra("Se você estiver com algum problema, contate-nos em:\n");

                TextComponent discordComponent = new TextComponent("§9§lDISCORD");

                discordComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para acessar nosso Discord!")));
                discordComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www." + Constant.SERVER_DISCORD));

                message.addExtra(discordComponent);

                ProtocolHandler.openBook(player, new BookItem().pageComponents(message));
            }

            PermissionManager.loadPermissions(player);
            Core.getPlatform().runSync(() -> TagManager.updateTag(account, tag), 10);

            account.send(rank.isPayment() ?
                    "§aA sua compra foi processada! O rank " + rank.getColoredName() + "§a foi adicionado em sua conta com duração "
                            + (rank.isPermanent() ? "vitalícia" : "de " + TimeUtil.formatTime(rank.getExpiresAt(), TimeUtil.TimeFormat.SHORT)) + "."
                    : "§aO rank " + rank.getColoredName() + "§a foi adicionado a sua conta.");
            account.sound(Sound.ORB_PICKUP);
        }
    }

    @EventHandler
    public void onPrefixType(AccountPrefixTypeEvent event) {
        Account account = event.getAccount();

        Tag tag = UserModel.of(account.getId()).getTag();

        TagManager.updateTag(account, tag);
    }
}
