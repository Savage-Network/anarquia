package com.minecraft.auth.account;

import com.minecraft.auth.Core;
import com.minecraft.auth.account.context.AccountContext;
import com.minecraft.auth.account.context.objects.medal.Medal;
import com.minecraft.auth.account.context.objects.rank.Rank;
import com.minecraft.auth.account.context.objects.rank.RankType;
import com.minecraft.auth.account.context.objects.tag.Tag;
import com.minecraft.auth.account.context.objects.toggle.Toggle;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Conta do jogador em memoria. Espelha {@code com.minecraft.core.account.Account}
 * do Savage, simplificada para Fase 1 (sem Member/Party/Server arcade).
 *
 * <p>Persistencia delegada a {@link com.minecraft.auth.backend.data.AccountData}.
 */
@Getter
public class Account {

    private final UUID id;
    private String name;

    /** Marcado como dirty quando algum setter altera o contexto. */
    private volatile boolean dirty = false;

    private AccountContext context;

    public Account(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.context = new AccountContext();
        this.context.getRankInfo().setRank(Rank.builder().type(RankType.MEMBER).build());
    }

    /* ----- contexto ----- */

    /** Usado pelo loader para substituir o contexto sem disparar save. */
    public void replaceContext(AccountContext context) {
        this.context = context;
    }

    public void saveContext(AccountContext context) {
        this.context = context;
        markDirty();
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    /* ----- jogador ----- */

    public Player player() {
        return Bukkit.getPlayer(id);
    }

    public boolean isOnline() {
        Player p = player();
        return p != null && p.isOnline();
    }

    public void send(Component message) {
        Player p = player();
        if (p != null) p.sendMessage(message);
    }

    public void sound(Sound sound) {
        Player p = player();
        if (p != null) p.playSound(p.getLocation(), sound, 1f, 1f);
    }

    public void setName(String name) {
        this.name = name;
        markDirty();
    }

    public String getNickname() {
        return context.getFake().isValid() ? context.getFake().getNick() : name;
    }

    public String getColoredName() {
        return getTag().getColor() + getNickname();
    }

    /* ----- rank ----- */

    public Rank getRank() {
        return context.getRankInfo().getRank();
    }

    public RankType getRankType() {
        Rank r = getRank();
        return r != null && r.getType() != null ? r.getType() : RankType.MEMBER;
    }

    public boolean hasRank(RankType type) {
        return getRankType().isAtLeast(type);
    }

    public boolean isVIP() {
        return hasRank(RankType.VIP);
    }

    public boolean isStaffer() {
        return hasRank(RankType.HELPER);
    }

    public void setRank(Rank rank) {
        Rank normalized = normalizeRank(rank);
        context.getRankInfo().setRank(normalized);
        RankType type = normalized.getType() != null ? normalized.getType() : RankType.MEMBER;
        setTag(com.minecraft.auth.account.context.objects.tag.Tag.of(type));
        if (isOnline()) {
            com.minecraft.auth.bukkit.manager.PermissionManager.loadPermissions(player());
        }
    }

    private static Rank normalizeRank(Rank rank) {
        if (rank == null || rank.getType() == null) {
            return Rank.builder().type(RankType.MEMBER).givenAt(System.currentTimeMillis()).build();
        }
        return Rank.builder()
                .type(rank.getType())
                .givenAt(rank.getGivenAt() > 0 ? rank.getGivenAt() : System.currentTimeMillis())
                .expiresAt(rank.getExpiresAt())
                .build();
    }

    public boolean hasPermission(String key) {
        if (key == null || key.isEmpty()) return false;
        refreshPermissions();
        if (getRankType().grants(key)) return true;
        for (Rank rank : getAvailableRanks()) {
            if (rank != null && !rank.hasExpired() && rank.getType() != null && rank.getType().grants(key)) {
                return true;
            }
        }
        return getPermissions().stream()
                .anyMatch(p -> !p.hasExpired() && RankType.matchesPermission(p.getKey(), key));
    }

    public java.util.List<com.minecraft.auth.account.context.objects.permission.Permission> getPermissions() {
        return context.getPermissions();
    }

    public java.util.List<Rank> getAvailableRanks() {
        return context.getRankInfo().getAvailableRanks();
    }

    public void refreshPermissions() {
        var it = getPermissions().iterator();
        boolean changed = false;
        while (it.hasNext()) {
            if (it.next().hasExpired()) {
                it.remove();
                changed = true;
            }
        }
        if (changed) markDirty();
    }

    /* ----- tag/medal ----- */

    public Tag getTag() {
        return context.getTag();
    }

    public void setTag(Tag tag) {
        context.setTag(tag);
        markDirty();
    }

    public Medal getMedal() {
        return context.getMedal();
    }

    public void setMedal(Medal medal) {
        context.setMedal(medal);
        markDirty();
    }

    /* ----- toggle/preferencias ----- */

    public Toggle getToggle() {
        return context.getToggle();
    }

    /* ----- economia ----- */

    public int getGolds() {
        return context.getGolds();
    }

    public void setGolds(int golds) {
        context.setGolds(golds);
        markDirty();
    }

    public void addGolds(int amount) {
        if (amount > 0) setGolds(getGolds() + amount);
    }

    public boolean removeGolds(int amount) {
        if (amount <= 0 || getGolds() < amount) return false;
        setGolds(getGolds() - amount);
        return true;
    }

    public int getLevel() {
        return context.getLevel();
    }

    public void addLevel() {
        context.setLevel(getLevel() + 1);
        markDirty();
    }

    /* ----- cooldowns ----- */

    public void setCooldown(String key, long durationMillis) {
        context.getCooldown().put(key.toLowerCase(), System.currentTimeMillis() + durationMillis);
        markDirty();
    }

    public long getCooldown(String key) {
        return context.getCooldown().getOrDefault(key.toLowerCase(), 0L);
    }

    public boolean hasCooldown(String key) {
        long expiresAt = getCooldown(key);
        if (expiresAt == 0L) return false;
        if (expiresAt > System.currentTimeMillis()) return true;
        context.getCooldown().remove(key.toLowerCase());
        return false;
    }

    /* ----- cosmeticos: subset (expandido na Fase 4) ----- */

    public List<String> getActiveCollectibles() {
        return context.getActiveCollectibles();
    }

    public List<String> getFavoriteCollectibles() {
        return context.getFavoriteCollectibles();
    }

    /* ----- metadados sessao ----- */

    public boolean isPremium() {
        return context.isPremium();
    }

    public void setPremium(boolean premium) {
        context.setPremium(premium);
        markDirty();
    }

    public String getIpAddress() {
        return context.getIpAddress();
    }

    public void setIpAddress(String ip) {
        context.setIpAddress(ip);
        markDirty();
    }

    public void updateLastLogin() {
        context.setLastLogin(System.currentTimeMillis());
        markDirty();
    }

    /** Save async via {@link com.minecraft.auth.backend.data.AccountData}. */
    public void save() {
        Core.getAccountData().saveAsync(this).thenRun(this::clearDirty);
    }
}
