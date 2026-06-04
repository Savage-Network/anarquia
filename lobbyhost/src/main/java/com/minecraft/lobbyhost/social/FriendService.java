package com.minecraft.lobbyhost.social;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.friend.Friend;
import com.minecraft.lobbyhost.account.context.objects.friend.FriendMetadata;
import com.minecraft.lobbyhost.account.context.objects.friend.FriendRequest;
import com.minecraft.lobbyhost.util.AccountSync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FriendService {

    private FriendService() {}

    public static int countFriendsOnlineInLobby(Account account) {
        if (account == null) return 0;
        int n = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId().equals(account.getId())) continue;
            if (isFriend(account, p.getUniqueId())) n++;
        }
        return n;
    }

    public static boolean isFriend(Account account, UUID otherId) {
        FriendMetadata meta = account.getFriendMetadata();
        if (meta == null || meta.getFriends() == null) return false;
        return meta.getFriends().containsKey(otherId);
    }

    public static void sendRequest(Account sender, String targetName, Runnable onDone) {
        if (targetName == null || targetName.isBlank()) {
            sender.send("§cInforme o nick.");
            onDone.run();
            return;
        }
        if (targetName.equalsIgnoreCase(sender.getName())) {
            sender.send("§cVoce nao pode adicionar a si mesmo.");
            onDone.run();
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            var opt = Core.getAccountData().loadByName(targetName);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                    if (opt.isEmpty()) {
                        sender.send("§cJogador nao encontrado.");
                        onDone.run();
                        return;
                    }
                    Account target = opt.get();
                    if (isFriend(sender, target.getId())) {
                        sender.send("§eVoce ja e amigo de §f" + target.getName() + "§e.");
                        onDone.run();
                        return;
                    }
                    Account targetOnline = Core.getAccountController().of(target.getId());
                    Account targetAcc = targetOnline != null ? targetOnline : target;

                    if (!targetAcc.getToggle().isAllowFriendRequests()) {
                        sender.send("§cEsse jogador nao aceita pedidos de amizade.");
                        onDone.run();
                        return;
                    }
                    if (hasIncomingRequest(sender, target.getId())) {
                        acceptRequest(sender, target.getName(), onDone);
                        return;
                    }
                    if (hasPendingRequestTo(targetAcc, sender.getId())) {
                        sender.send("§ePedido ja enviado.");
                        onDone.run();
                        return;
                    }

                    FriendMetadata targetMeta = targetAcc.getFriendMetadata();
                    if (targetMeta.getRequests() == null) targetMeta.setRequests(new ArrayList<>());
                    targetMeta.getRequests().add(new FriendRequest(sender.getId(), System.currentTimeMillis()));
                    targetAcc.markDirty();
                    AccountSync.saveAndBroadcast(targetAcc);

                    sender.send("§aPedido enviado para §f" + target.getName() + "§a.");
                    Player tp = Bukkit.getPlayer(target.getId());
                    if (tp != null) {
                        tp.sendMessage(net.kyori.adventure.text.Component.text(
                                "§e" + sender.getNickname() + " §7enviou um pedido de amizade. §e/amigo aceitar "
                                        + sender.getName()));
                    }
                    onDone.run();
            });
        });
    }

    public static void acceptRequest(Account receiver, String senderName, Runnable onDone) {
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            var opt = Core.getAccountData().loadByName(senderName);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                    if (opt.isEmpty()) {
                        receiver.send("§cJogador nao encontrado.");
                        onDone.run();
                        return;
                    }
                    UUID senderId = opt.get().getId();
                    FriendRequest req = findIncoming(receiver, senderId);
                    if (req == null) {
                        receiver.send("§cNenhum pedido pendente desse jogador.");
                        onDone.run();
                        return;
                    }
                    addMutual(receiver, opt.get());
                    receiver.send("§aVoce agora e amigo de §f" + opt.get().getName() + "§a!");
                    Player sp = Bukkit.getPlayer(senderId);
                    if (sp != null) {
                        sp.sendMessage(net.kyori.adventure.text.Component.text(
                                "§a" + receiver.getNickname() + " §7aceitou seu pedido de amizade!"));
                    }
                    onDone.run();
            });
        });
    }

    public static void removeFriend(Account account, String friendName, Runnable onDone) {
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            var opt = Core.getAccountData().loadByName(friendName);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                    if (opt.isEmpty()) {
                        account.send("§cJogador nao encontrado.");
                        onDone.run();
                        return;
                    }
                    UUID fid = opt.get().getId();
                    FriendMetadata meta = account.getFriendMetadata();
                    if (meta.getFriends() != null) meta.getFriends().remove(fid);
                    if (meta.getRequests() != null) {
                        meta.getRequests().removeIf(r -> r.getSender() != null && r.getSender().equals(fid));
                    }
                    account.markDirty();
                    AccountSync.saveAndBroadcast(account);
                    account.send("§cAmizade removida com §f" + opt.get().getName() + "§c.");
                    onDone.run();
            });
        });
    }

    public static List<Friend> listFriends(Account account) {
        FriendMetadata meta = account.getFriendMetadata();
        if (meta == null || meta.getFriends() == null) return List.of();
        return new ArrayList<>(meta.getFriends().values());
    }

    private static void addMutual(Account a, Account b) {
        long now = System.currentTimeMillis();
        FriendMetadata ma = a.getFriendMetadata();
        FriendMetadata mb = b.getFriendMetadata();
        if (ma.getFriends() == null) ma.setFriends(new java.util.HashMap<>());
        if (mb.getFriends() == null) mb.setFriends(new java.util.HashMap<>());
        ma.getFriends().put(b.getId(), new Friend(b.getId(), b.getName(), now, false));
        mb.getFriends().put(a.getId(), new Friend(a.getId(), a.getName(), now, false));
        if (ma.getRequests() != null) {
            ma.getRequests().removeIf(r -> r.getSender() != null && r.getSender().equals(b.getId()));
        }
        if (mb.getRequests() != null) {
            mb.getRequests().removeIf(r -> r.getSender() != null && r.getSender().equals(a.getId()));
        }
        a.markDirty();
        b.markDirty();
        AccountSync.saveAndBroadcast(a);
        Account onlineB = Core.getAccountController().of(b.getId());
        if (onlineB != null) {
            onlineB.replaceContext(b.getContext());
            AccountSync.saveAndBroadcast(onlineB);
        } else {
            Core.getAccountData().saveAsync(b);
        }
    }

    private static boolean hasPendingRequestTo(Account target, UUID fromSender) {
        return findIncoming(target, fromSender) != null;
    }

    private static boolean hasIncomingRequest(Account receiver, UUID senderId) {
        return findIncoming(receiver, senderId) != null;
    }

    private static FriendRequest findIncoming(Account receiver, UUID senderId) {
        FriendMetadata meta = receiver.getFriendMetadata();
        if (meta.getRequests() == null) return null;
        for (FriendRequest r : meta.getRequests()) {
            if (senderId.equals(r.getSender())) return r;
        }
        return null;
    }
}
