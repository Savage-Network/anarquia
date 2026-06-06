package com.minecraft.proxyhost.account;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.context.AccountContext;
import com.minecraft.proxyhost.account.context.objects.medal.Medal;
import com.minecraft.proxyhost.account.context.objects.medal.RankMedals;
import com.minecraft.proxyhost.account.context.objects.rank.Rank;
import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import com.minecraft.proxyhost.account.context.objects.tag.Tag;
import com.minecraft.proxyhost.account.context.objects.tag.TagRole;
import com.minecraft.proxyhost.account.context.objects.title.CustomTitle;
import com.minecraft.proxyhost.account.context.objects.toggle.TagPrefix;
import com.minecraft.proxyhost.account.context.objects.punishment.PunishmentHistory;
import com.minecraft.proxyhost.account.context.objects.toggle.Toggle;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Conta em memoria - versao Velocity. Mesmas operacoes do plugin {@code game}, mas
 * usando {@link Player} do Velocity e sem dependencias de Bukkit.
 *
 * <p>O JSON serializado e identico ao do plugin {@code game} - a tabela {@code accounts}
 * e compartilhada entre todos os servidores.
 */
@Getter
public class Account {

    private static final Set<Medal> EXCLUSIVE_MEDALS = EnumSet.of(
            Medal.NONE, Medal.TOWER,
            Medal.CHAMPION, Medal.Y, Medal.PRECIOUS,
            Medal.HALLOWEEN, Medal.ROSA_DOURADA, Medal.PRIMAVERA, Medal.NEVASCA
    );

    private final UUID id;
    private String name;

    private volatile boolean dirty = false;
    private AccountContext context;

    public Account(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.context = new AccountContext();
        this.context.getRankInfo().setRank(Rank.builder().type(RankType.MEMBER).build());
    }

    /* ================================================================
     * Contexto
     * ============================================================= */

    public void replaceContext(AccountContext context) {
        this.context = context;
    }

    public void saveContext(AccountContext context) {
        this.context = context;
        markDirty();
    }

    public void markDirty() { this.dirty = true; }
    public void clearDirty() { this.dirty = false; }

    /* ================================================================
     * Jogador (Velocity)
     * ============================================================= */

    public Optional<Player> player() {
        return Core.getProxy() == null ? Optional.empty() : Core.getProxy().getPlayer(id);
    }

    public boolean isOnline() {
        return player().map(Player::isActive).orElse(false);
    }

    public void send(Component message) {
        player().ifPresent(p -> p.sendMessage(message));
    }

    public void send(String legacy) {
        send(Component.text(legacy));
    }

    public void setName(String name) {
        this.name = name;
        markDirty();
    }

    public boolean isFake() {
        return context.getFake() != null && context.getFake().isValid();
    }

    public String getNickname() {
        return isFake() ? context.getFake().getNick() : name;
    }

    public String getColoredName() {
        return getTag().getColor() + getNickname();
    }

    public String getCurrentServer() {
        return player()
                .flatMap(Player::getCurrentServer)
                .map(s -> s.getServerInfo().getName())
                .orElse("?");
    }

    /* ================================================================
     * Rank
     * ============================================================= */

    public Rank getRank() {
        return context.getRankInfo().getRank();
    }

    public RankType getRankType() {
        RankType best = RankType.MEMBER;
        Rank primary = getRank();
        if (primary != null && primary.getType() != null && !primary.hasExpired()) {
            best = primary.getType();
        }
        for (Rank rank : getAvailableRanks()) {
            if (rank == null || rank.hasExpired() || rank.getType() == null) continue;
            if (rank.getType().ordinal() > best.ordinal()) {
                best = rank.getType();
            }
        }
        return best;
    }

    public boolean hasRank(RankType type) {
        return getRankType().isAtLeast(type);
    }

    public boolean isVIP() { return hasRank(RankType.VIP); }
    public boolean isStaffer() { return getRankType().isStaff(); }

    public boolean hasStaffTag() {
        for (Tag tag : Tag.values()) {
            if (tag.isStaffTag() && hasTag(tag)) return true;
        }
        return false;
    }

    public boolean canUseStaffChat() {
        if (isStaffer() || hasStaffTag()) return true;
        return grantsPermission("game.staffchat")
                || grantsPermission("lobbyhost.staffchat")
                || grantsPermission("proxyhost.staffchat");
    }

    public PunishmentHistory getHistory() {
        return context.getHistory();
    }

    public boolean isAllowColoredChat() {
        return isVIP() && !isFake();
    }

    public void setRank(Rank rank) {
        Rank normalized = normalizeRank(rank);
        context.getRankInfo().setRank(normalized);
        RankType type = normalized.getType() != null ? normalized.getType() : RankType.MEMBER;
        setTag(Tag.of(type));
        markDirty();
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

    /** Permissao logica: rank, ranks extras e permissoes na conta (sem loop no Velocity). */
    public boolean hasPermission(String node) {
        return grantsPermission(node);
    }

    public boolean grantsPermission(String key) {
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

    /** Permissao explicita na conta, sem grants de rank (evita loop com {@link #hasTag}). */
    private boolean hasExplicitPermission(String key) {
        if (key == null || key.isEmpty()) return false;
        refreshPermissions();
        return getPermissions().stream()
                .anyMatch(p -> !p.hasExpired() && RankType.matchesPermission(p.getKey(), key));
    }

    public java.util.List<com.minecraft.proxyhost.account.context.objects.permission.Permission> getPermissions() {
        return context.getPermissions();
    }

    public java.util.List<Rank> getAvailableRanks() {
        return context.getRankInfo().getAvailableRanks();
    }

    public void refreshPermissions() {
        var it = getPermissions().iterator();
        while (it.hasNext()) {
            if (it.next().hasExpired()) {
                it.remove();
            }
        }
    }

    /** Nos Bukkit/Velocity sincronizados a partir do rank ativo e extras. */
    public java.util.Set<String> collectPermissionNodes() {
        java.util.LinkedHashSet<String> nodes = new java.util.LinkedHashSet<>();
        nodes.addAll(getRankType().getPermissionNodes());
        for (Rank rank : getAvailableRanks()) {
            if (rank != null && !rank.hasExpired() && rank.getType() != null) {
                nodes.addAll(rank.getType().getPermissionNodes());
            }
        }
        getPermissions().stream()
                .filter(p -> !p.hasExpired())
                .map(com.minecraft.proxyhost.account.context.objects.permission.Permission::getKey)
                .forEach(nodes::add);
        for (Tag tag : Tag.values()) {
            if (!hasTag(tag)) continue;
            nodes.add(tag.getPermission());
            if (tag.isStaffTag()) {
                nodes.addAll(Tag.STAFF_CHAT_NODES);
            }
        }
        if (hasZwRank()) {
            nodes.add("*");
            nodes.add("game.*");
            nodes.add("lobbyhost.*");
            nodes.add("proxyhost.*");
            nodes.add("auth.*");
            nodes.add("eventos.*");
            nodes.add("tag.*");
            nodes.add("medal.*");
        }
        return nodes;
    }

    private boolean hasZwRank() {
        if (getRankType() == RankType.ZW) return true;
        for (Rank rank : getAvailableRanks()) {
            if (rank != null && !rank.hasExpired() && rank.getType() == RankType.ZW) {
                return true;
            }
        }
        return false;
    }

    /* ================================================================
     * Tag
     * ============================================================= */

    public Tag getTag() {
        Tag t = context.getTag();
        return t == null ? Tag.MEMBER : t;
    }

    public Tag getDefaultTag() { return Tag.of(getRankType()); }

    public void setTag(Tag tag) {
        context.setTag(tag == null ? Tag.MEMBER : tag);
        markDirty();
    }

    public boolean isUsingTag(Tag tag) { return getTag() == tag; }

    public boolean hasTag(Tag tag) {
        if (tag == null) return false;
        RankType rank = getRankType();
        if (tag == Tag.TAG_OWNER) return rank == RankType.OWNER;
        if (tag == Tag.TAG_ZW) return rank == RankType.ZW;

        switch (tag.getRole()) {
            case NONE:
                return tag.getSource() == RankType.MEMBER || rank.isAtLeast(tag.getSource());
            case COLORED:
                if (rank.isAtLeast(RankType.VERMELHO)) return true;
                return rank == RankType.MEMBER;
            case VIP:
                if (rank.isAtLeast(RankType.ADMIN)) return true;
                return rank.isAtLeast(tag.getSource());
            case SPECIAL:
                if (hasPermission(tag.getPermission())) return true;
                if (tag.getSource() != RankType.MEMBER && rank.isAtLeast(tag.getSource())) return true;
                return rank.isAtLeast(RankType.ADMIN);
            case STAFF:
                if (hasExplicitPermission(tag.getPermission())) return true;
                if (rank.isAtLeast(tag.getSource())) return true;
                if (rank.isAtLeast(RankType.ADMIN)) return true;
                if (tag == Tag.VERMELHO) return rank == RankType.VERMELHO;
                return false;
            case ONLY_THESE:
                return tag.getOnlyThese().contains(id);
        }
        return false;
    }

    public List<Tag> getAvailableTags() {
        List<Tag> out = new ArrayList<>();
        for (Tag t : Tag.values()) if (hasTag(t)) out.add(t);
        return out;
    }

    public TagPrefix getTagPrefix() {
        TagPrefix p = getToggle().getTagPrefix();
        return p == null ? TagPrefix.DEFAULT : p;
    }

    public void setTagPrefix(TagPrefix prefix) {
        getToggle().setTagPrefix(prefix == null ? TagPrefix.DEFAULT : prefix);
        markDirty();
    }

    public Toggle getToggle() { return context.getToggle(); }

    /* ================================================================
     * Medal
     * ============================================================= */

    public Medal getMedal() {
        Medal m = context.getMedal();
        return m == null ? Medal.NONE : m;
    }

    public void setMedal(Medal medal) {
        context.setMedal(medal == null ? Medal.NONE : medal);
        markDirty();
    }

    public boolean isUsingMedal(Medal medal) { return getMedal() == medal; }

    public List<Medal> getMedals() { return context.getMedals(); }

    public void addMedal(Medal medal) {
        if (medal == null) return;
        if (!context.getMedals().contains(medal)) {
            context.getMedals().add(medal);
            markDirty();
        }
    }

    public void removeMedal(Medal medal) {
        if (medal == null) return;
        if (context.getMedals().remove(medal)) {
            if (isUsingMedal(medal)) setMedal(Medal.NONE);
            markDirty();
        }
    }

    public boolean hasMedal(Medal medal) {
        if (medal == null) return false;
        RankType rank = getRankType();

        switch (medal) {
            case NONE: return true;
            case CHAMPION:     return hasPermission("tag.champion") || hasTag(Tag.TAG_CHAMPION);
            case Y:            return rank == RankType.VERMELHO;
            case HALLOWEEN:    return hasTag(Tag.TAG_HALLOWEEN) || hasPermission(medal.getPermission());
            case ROSA_DOURADA: return hasTag(Tag.TAG_CARNAVAL) || hasPermission(medal.getPermission());
            case PRIMAVERA:    return hasTag(Tag.TAG_FERIAS)   || hasPermission(medal.getPermission());
            case NEVASCA:      return hasTag(Tag.TAG_NATAL)    || hasPermission(medal.getPermission());
            case PRECIOUS:     return rank == RankType.PRECIOUS || hasPermission(medal.getPermission());
            default: break;
        }

        if (RankMedals.isGrantedByRank(rank, medal)) return true;

        if (isStaffer()) return true;
        if (context.getMedals().contains(medal)) return true;
        if (hasPermission(medal.getPermission())) return true;
        if (EXCLUSIVE_MEDALS.contains(medal)) return false;

        int allowed = vipPoolSize(rank);
        if (allowed <= 0) return false;
        int idx = poolIndexOf(medal);
        return idx >= 0 && idx < allowed;
    }

    private int vipPoolSize(RankType rank) {
        return RankMedals.vipPoolAllowed(rank, poolSize());
    }

    private int poolSize() {
        int n = 0;
        for (Medal m : Medal.values()) if (!EXCLUSIVE_MEDALS.contains(m)) n++;
        return n;
    }

    private int poolIndexOf(Medal medal) {
        int i = 0;
        for (Medal m : Medal.values()) {
            if (EXCLUSIVE_MEDALS.contains(m)) continue;
            if (m == medal) return i;
            i++;
        }
        return -1;
    }

    public List<Medal> getAvailableMedals() {
        return java.util.Arrays.stream(Medal.values())
                .filter(this::hasMedal)
                .collect(Collectors.toList());
    }

    /* ================================================================
     * Custom title (Creator+)
     * ============================================================= */

    public CustomTitle getCustomTitle() { return context.getCustomTitle(); }
    public void setCustomTitle(CustomTitle title) { context.setCustomTitle(title); markDirty(); }
    public boolean hasCustomTitle() {
        CustomTitle t = context.getCustomTitle();
        return t != null && t.getTitle() != null && !t.getTitle().isEmpty();
    }

    /* ================================================================
     * Cor EMBER+
     * ============================================================= */
    public String getEmberPlusColor() {
        String c = context.getEmberPlusColor();
        return (c == null || c.isEmpty()) ? "§5" : c;
    }
    public void setEmberPlusColor(String color) { context.setEmberPlusColor(color); markDirty(); }

    /* ================================================================
     * Economia
     * ============================================================= */
    public int getGolds() { return context.getGolds(); }
    public void setGolds(int golds) { context.setGolds(golds); markDirty(); }
    public void addGolds(int amount) { if (amount > 0) setGolds(getGolds() + amount); }
    public boolean removeGolds(int amount) {
        if (amount <= 0 || getGolds() < amount) return false;
        setGolds(getGolds() - amount);
        return true;
    }
    public int getLevel() { return context.getLevel(); }
    public void addLevel() { context.setLevel(getLevel() + 1); markDirty(); }

    /* ================================================================
     * Cooldowns
     * ============================================================= */
    public void setCooldown(String key, long durationMillis) {
        context.getCooldown().put(key.toLowerCase(), System.currentTimeMillis() + durationMillis);
        markDirty();
    }
    public long getCooldown(String key) {
        return context.getCooldown().getOrDefault(key.toLowerCase(), 0L);
    }
    public long getCooldownRemaining(String key) {
        long expiresAt = getCooldown(key);
        long now = System.currentTimeMillis();
        return expiresAt > now ? expiresAt - now : 0L;
    }
    public boolean hasCooldown(String key) {
        long expiresAt = getCooldown(key);
        if (expiresAt == 0L) return false;
        if (expiresAt > System.currentTimeMillis()) return true;
        context.getCooldown().remove(key.toLowerCase());
        return false;
    }

    /* ================================================================
     * Sessao
     * ============================================================= */
    public boolean isPremium() { return context.isPremium(); }
    public void setPremium(boolean premium) { context.setPremium(premium); markDirty(); }

    public String getIpAddress() { return context.getIpAddress(); }
    public void setIpAddress(String ip) { context.setIpAddress(ip); markDirty(); }

    public void updateLastLogin() {
        context.setLastLogin(System.currentTimeMillis());
        markDirty();
    }

    public void save() {
        Core.getAccountData().saveAsync(this).thenRun(this::clearDirty);
    }
}
