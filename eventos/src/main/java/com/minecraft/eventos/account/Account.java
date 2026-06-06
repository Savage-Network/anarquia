package com.minecraft.eventos.account;

import com.minecraft.eventos.Core;
import com.minecraft.eventos.account.context.AccountContext;
import com.minecraft.eventos.account.context.objects.medal.Medal;
import com.minecraft.eventos.account.context.objects.medal.RankMedals;
import com.minecraft.eventos.account.context.objects.rank.Rank;
import com.minecraft.eventos.account.context.objects.rank.RankType;
import com.minecraft.eventos.account.context.objects.tag.Tag;
import com.minecraft.eventos.account.context.objects.tag.TagRole;
import com.minecraft.eventos.account.context.objects.title.CustomTitle;
import com.minecraft.eventos.account.context.objects.toggle.TagPrefix;
import com.minecraft.eventos.account.context.objects.toggle.Toggle;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Conta do jogador em memoria. Espelha {@code com.minecraft.core.account.Account}
 * do Savage (modulos {@code core} + {@code core-bukkit}).
 *
 * <p>Persistencia delegada a {@link com.minecraft.eventos.backend.data.AccountData}.
 * Toda mutacao chama {@link #markDirty()} para o auto-save async.
 */
@Getter
public class Account {

    /* ===== medalhas que NUNCA entram no pool VIP automatico ===== */
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
     * Jogador / mensagens
     * ============================================================= */

    public Player player() { return Bukkit.getPlayer(id); }

    public boolean isOnline() {
        Player p = player();
        return p != null && p.isOnline();
    }

    public void send(Component message) {
        Player p = player();
        if (p != null) p.sendMessage(message);
    }

    public void send(String legacy) {
        send(Component.text(legacy));
    }

    public void sound(Sound sound) {
        Player p = player();
        if (p != null) p.playSound(p.getLocation(), sound, 1f, 1f);
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

    /* ================================================================
     * Rank
     * ============================================================= */

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

    public boolean shouldBroadcastJoinMessage() {
        return isVIP() && getToggle().isAllowJoinMessage();
    }

    public boolean isStaffer() {
        return getRankType().isStaff();
    }

    /** Permite chat colorido (rank >= VIP e nao-fake). */
    public boolean isAllowColoredChat() {
        return isVIP() && !isFake();
    }

    public void setRank(Rank rank) {
        Rank normalized = normalizeRank(rank);
        context.getRankInfo().setRank(normalized);
        RankType type = normalized.getType() != null ? normalized.getType() : RankType.MEMBER;
        setTag(Tag.of(type));
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

    /** Permission gate; aceita {@code null}. Em Paper 1.21 consulta o player online. */
    public boolean hasPermission(String node) {
        if (node == null || node.isEmpty()) return false;
        if (getRankType().grants(node)) return true;
        Player p = player();
        return p != null && p.hasPermission(node);
    }

    /* ================================================================
     * Tag
     * ============================================================= */

    public Tag getTag() {
        Tag t = context.getTag();
        return t == null ? Tag.MEMBER : t;
    }

    /** Tag default deduzida do rank (Savage: {@code Tag.of(rank.getType())}). */
    public Tag getDefaultTag() {
        return Tag.of(getRankType());
    }

    public void setTag(Tag tag) {
        context.setTag(tag == null ? Tag.MEMBER : tag);
        markDirty();
    }

    public boolean isUsingTag(Tag tag) {
        return getTag() == tag;
    }

    /**
     * Espelha {@code Account.hasTag(Tag)} do Savage. Determina se a tag esta
     * disponivel para este jogador conforme regras de {@link TagRole}.
     */
    public boolean hasTag(Tag tag) {
        if (tag == null) return false;
        RankType rank = getRankType();

        // Owner: precisa do rank Owner explicito.
        if (tag == Tag.TAG_OWNER) {
            return rank == RankType.OWNER;
        }
        if (tag == Tag.TAG_ZW) {
            return rank == RankType.ZW;
        }

        switch (tag.getRole()) {
            case NONE:
                return tag.getSource() == RankType.MEMBER
                        || rank.isAtLeast(tag.getSource());

            case COLORED:
                // Tags de cor sao para MEMBER puro; staff alto pode usar (espelha Savage).
                if (rank.isAtLeast(RankType.VERMELHO)) return true;
                return rank == RankType.MEMBER;

            case VIP:
                // Privilegio staff alto: Vermelho/Admin/Diretor podem usar VIP.
                if (rank.isAtLeast(RankType.ADMIN)) return true;
                return rank.isAtLeast(tag.getSource());

            case SPECIAL:
                if (hasPermission(tag.getPermission())) return true;
                if (tag.getSource() != RankType.MEMBER && rank.isAtLeast(tag.getSource())) return true;
                // staff alto pode usar todas SPECIAL
                return rank.isAtLeast(RankType.ADMIN);

            case STAFF:
                if (rank == tag.getSource()) return true;
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

    /* ================================================================
     * Tag prefix (modo visual)
     * ============================================================= */

    public Toggle getToggle() { return context.getToggle(); }

    public TagPrefix getTagPrefix() {
        TagPrefix p = getToggle().getTagPrefix();
        return p == null ? TagPrefix.DEFAULT : p;
    }

    public void setTagPrefix(TagPrefix prefix) {
        getToggle().setTagPrefix(prefix == null ? TagPrefix.DEFAULT : prefix);
        markDirty();
    }

    public boolean isUsingTagPrefix(TagPrefix prefix) {
        return getTagPrefix() == prefix;
    }

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

    public boolean isUsingMedal(Medal medal) {
        return getMedal() == medal;
    }

    /** Lista persistida (medalhas concedidas/staff add). */
    public List<Medal> getMedals() {
        return context.getMedals();
    }

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

    /**
     * Espelha {@code Account.hasMedal(Medal)} do Savage. Ordem:
     *
     * <ol>
     *   <li>Medalhas exclusivas por rank/tag</li>
     *   <li>Staff -> todas</li>
     *   <li>Lista persistida ou permissao {@code medal.xxx}</li>
     *   <li>Pool VIP proporcional ao rank</li>
     * </ol>
     */
    public boolean hasMedal(Medal medal) {
        if (medal == null) return false;
        RankType rank = getRankType();

        // 1) Exclusivas
        switch (medal) {
            case NONE:
                return true;
            case CHAMPION:
                return hasPermission("tag.champion") || hasTag(Tag.TAG_CHAMPION);
            case Y:
                return rank == RankType.VERMELHO;
            case HALLOWEEN:
                return hasTag(Tag.TAG_HALLOWEEN) || hasPermission(medal.getPermission());
            case ROSA_DOURADA:
                return hasTag(Tag.TAG_CARNAVAL) || hasPermission(medal.getPermission());
            case PRIMAVERA:
                return hasTag(Tag.TAG_FERIAS) || hasPermission(medal.getPermission());
            case NEVASCA:
                return hasTag(Tag.TAG_NATAL) || hasPermission(medal.getPermission());
            case PRECIOUS:
                return rank == RankType.PRECIOUS || hasPermission(medal.getPermission());
            default:
                break;
        }

        if (RankMedals.isGrantedByRank(rank, medal)) return true;

        // 2) Staff
        if (isStaffer()) return true;

        // 3) Lista ou permissao
        if (context.getMedals().contains(medal)) return true;
        if (hasPermission(medal.getPermission())) return true;

        // 4) Pool VIP - so para nao-exclusivas
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

    public CustomTitle getCustomTitle() {
        return context.getCustomTitle();
    }

    public void setCustomTitle(CustomTitle title) {
        context.setCustomTitle(title);
        markDirty();
    }

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

    public void setEmberPlusColor(String color) {
        context.setEmberPlusColor(color);
        markDirty();
    }

    /* ================================================================
     * Economia / level
     * ============================================================= */

    public int getGolds() { return context.getGolds(); }

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

    public int getLevel() { return context.getLevel(); }

    public void addLevel() {
        context.setLevel(getLevel() + 1);
        markDirty();
    }

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
     * Cosmeticos / sessao
     * ============================================================= */

    public List<String> getActiveCollectibles() { return context.getActiveCollectibles(); }
    public List<String> getFavoriteCollectibles() { return context.getFavoriteCollectibles(); }

    public boolean isPremium() { return context.isPremium(); }
    public void setPremium(boolean premium) { context.setPremium(premium); markDirty(); }

    public String getIpAddress() { return context.getIpAddress(); }
    public void setIpAddress(String ip) { context.setIpAddress(ip); markDirty(); }

    public com.minecraft.eventos.account.context.objects.skin.SkinMetadata getSkin() {
        return context.getSkin();
    }
    public void setSkin(com.minecraft.eventos.account.context.objects.skin.SkinMetadata skin) {
        context.setSkin(skin);
        markDirty();
    }

    public void updateLastLogin() {
        context.setLastLogin(System.currentTimeMillis());
        markDirty();
    }

    public void save() {
        Core.getAccountData().saveAsync(this).thenRun(this::clearDirty);
    }
}
