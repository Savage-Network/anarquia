package com.minecraft.game.api.clan;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class Clan {

    private final UUID id;
    private UUID leader;
    private String name;
    private String tag;
    private String tagColor;
    private ClanStorage storage;

    public Clan(Account leader, UUID id, String name, String tag) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.leader = leader.getId();
        this.tagColor = ClanColor.defaultColor().getId();
        this.storage = new ClanStorage();
        this.storage.getMembers().put(leader.getId(), new ClanMember(leader, ClanRole.LEADER));
    }

    public Map<UUID, ClanMember> getMembers() {
        return storage.getMembers();
    }

    public long getCreatedAt() {
        return storage.getCreatedAt();
    }

    public int getPower() {
        return storage.getPower();
    }

    public void setPower(int power) {
        storage.setPower(power);
        persist();
    }

    public String getMotd() {
        return storage.getMotd() != null ? storage.getMotd() : "§7Bem-vindo ao clã!";
    }

    public void setMotd(String motd) {
        storage.setMotd(motd != null && !motd.isBlank() ? motd : "§7Bem-vindo ao clã!");
        persist();
    }

    public String getDescription() {
        return storage.getDescription() != null ? storage.getDescription() : "";
    }

    public void setDescription(String description) {
        storage.setDescription(description != null ? description : "");
        persist();
    }

    public boolean isFriendlyFire() {
        return storage.isFriendlyFire();
    }

    public void setFriendlyFire(boolean friendlyFire) {
        storage.setFriendlyFire(friendlyFire);
        persist();
    }

    public boolean hasHome() {
        return storage.getHomeWorld() != null && !storage.getHomeWorld().isBlank();
    }

    public void setHome(Location location) {
        if (location == null || location.getWorld() == null) return;
        storage.setHomeWorld(location.getWorld().getName());
        storage.setHomeX(location.getX());
        storage.setHomeY(location.getY());
        storage.setHomeZ(location.getZ());
        storage.setHomeYaw(location.getYaw());
        storage.setHomePitch(location.getPitch());
        persist();
    }

    public void clearHome() {
        storage.setHomeWorld(null);
        persist();
    }

    public Location getHome() {
        if (!hasHome()) return null;
        World world = Bukkit.getWorld(storage.getHomeWorld());
        if (world == null) return null;
        return new Location(world, storage.getHomeX(), storage.getHomeY(), storage.getHomeZ(),
                storage.getHomeYaw(), storage.getHomePitch());
    }

    public int getOnlineCount() {
        return getOnlineMembers().size();
    }

    public void persist() {
        if (Core.getClanData() != null) {
            Core.getClanData().save(this);
        }
        com.minecraft.game.backend.sync.NetworkStatsSync.syncClan(this);
    }

    public void disband() {
        Core.message("Desfazendo cla " + name + " (" + tag + ")...");
        for (UUID memberId : new ArrayList<>(getMembers().keySet())) {
            Account account = Core.getAccountController().of(memberId);
            if (account == null) continue;
            account.leaveClanSilent();
        }
        broadcast("§7O cla foi desfeito.");
        Core.getClanData().delete(this);
    }

    public void broadcast(String message) {
        for (Account online : getOnlineMembers()) {
            online.send(Constant.CLAN_CHAT_PREFIX + message);
        }
    }

    public String getTagWithoutColors() {
        if (tag == null) return "";
        return tag.replaceAll("§[0-9a-fA-Fk-oK-Or]", "")
                .replaceAll("&[0-9a-fA-Fk-oK-Or]", "");
    }

    public ClanColor getStyle() {
        return ClanColor.byStored(tagColor);
    }

    public void applyStyle(ClanColor color) {
        if (color == null) return;
        this.tagColor = color.getId();
        persist();
    }

    /** Migra valores legados ({@code §7}) para ids ({@code padrao}). */
    public void normalizeStyle() {
        String normalized = ClanColor.normalizeStored(tagColor);
        if (tagColor == null || !normalized.equals(tagColor)) {
            tagColor = normalized;
        }
    }

    public boolean isLeader(UUID uuid) {
        return leader != null && leader.equals(uuid);
    }

    public boolean isMember(UUID uuid) {
        return getMembers().containsKey(uuid);
    }

    public ClanMember getMember(UUID uuid) {
        return getMembers().get(uuid);
    }

    public List<Account> getOnlineMembers() {
        List<Account> out = new ArrayList<>();
        for (Account account : Core.getAccountController().list()) {
            if (isMember(account.getId())) out.add(account);
        }
        return out;
    }

    public void addMember(Account member) {
        if (isMember(member.getId())) return;
        broadcast("§7" + member.getNickname() + " entrou no cla.");
        getMembers().put(member.getId(), new ClanMember(member, ClanRole.MEMBER));
        member.joinClan(this);
        persist();
    }

    public void removeMember(Account member) {
        if (!isMember(member.getId())) return;
        broadcast("§7" + member.getNickname() + " saiu do cla.");
        getMembers().remove(member.getId());
        member.leaveClanSilent();
        persist();
    }

    public void promote(Account actor, Account target, ClanRole role) {
        ClanMember m = getMember(target.getId());
        if (m == null) return;
        m.setRole(role);
        if (role == ClanRole.LEADER) {
            ClanMember oldLeader = getMember(leader);
            if (oldLeader != null) oldLeader.setRole(ClanRole.RECRUITER);
            leader = target.getId();
        }
        broadcast("§7" + target.getNickname() + " agora é " + role.getColor() + role.getDisplayName() + "§7.");
        persist();
    }

    public void demote(Account actor, Account target) {
        ClanMember m = getMember(target.getId());
        if (m == null || isLeader(target.getId())) return;
        if (m.getRole() == ClanRole.MEMBER) return;
        m.setRole(ClanRole.MEMBER);
        broadcast("§7" + target.getNickname() + " foi rebaixado a " + ClanRole.MEMBER.getColor()
                + ClanRole.MEMBER.getDisplayName() + "§7.");
        persist();
    }

    public void kick(Account actor, Account target) {
        if (!isMember(target.getId())) return;
        if (isLeader(target.getId())) return;
        removeMember(target);
        target.send("§cVoce foi expulso do cla.");
    }
}
