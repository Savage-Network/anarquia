package com.minecraft.lobbyhost.api.clan;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import lombok.Getter;
import lombok.Setter;

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
        this.tagColor = ClanColor.DEFAULT.getId();
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

    public void persist() {
        if (Core.getClanData() != null) {
            Core.getClanData().save(this);
        }
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
        broadcast("§7" + target.getNickname() + " agora e " + role.getColor() + role.getDisplayName() + "§7.");
        persist();
    }

    public void kick(Account actor, Account target) {
        if (!isMember(target.getId())) return;
        if (isLeader(target.getId())) return;
        removeMember(target);
        target.send("§cVoce foi expulso do cla.");
    }
}
