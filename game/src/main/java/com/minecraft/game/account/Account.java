package com.minecraft.game.account;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.account.context.AccountContext;
import com.minecraft.game.account.context.objects.clan.ClanMetadata;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanRole;
import com.minecraft.game.api.clan.ClanSync;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.account.context.objects.medal.Medal;
import com.minecraft.game.account.context.objects.medal.RankMedals;
import com.minecraft.game.account.context.objects.rank.Rank;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.account.context.objects.tag.Tag;
import com.minecraft.game.account.context.objects.tag.TagRole;
import com.minecraft.game.account.context.objects.title.CustomTitle;
import com.minecraft.game.account.context.objects.toggle.TagPrefix;
import com.minecraft.game.account.context.objects.toggle.Toggle;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import com.minecraft.game.util.PunishmentTimeUtil;
import org.bukkit.entity.Player;

import java.util.Comparator;

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
 * <p>Persistencia delegada a {@link com.minecraft.game.backend.data.AccountData}.
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
    /** Estado de sessão: true enquanto o jogador estiver em vanish. Não persistido. */
    private transient boolean vanished = false;

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
    public void setVanished(boolean vanished) { this.vanished = vanished; }

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

    public void sendKey(String path) {
        send(com.minecraft.game.message.Messages.component(path,
                com.minecraft.game.message.LanguageService.of(this)));
    }

    public void sendKey(String path, java.util.Map<String, String> placeholders) {
        send(com.minecraft.game.message.Messages.component(path,
                com.minecraft.game.message.LanguageService.of(this), placeholders));
    }

    public void send(String... lines) {
        for (String line : lines) {
            if (line != null) send(line);
        }
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

    public String getRealName() {
        return name != null ? name : "";
    }

    public String getNickname() {
        if (isFake()) return context.getFake().getNick();
        return getRealName();
    }

    public Tag getDisplayTag() {
        if (isFake() && com.minecraft.game.bukkit.fake.FakeConfig.isHideRank()) {
            return Tag.MEMBER;
        }
        return getTag();
    }

    public RankType getDisplayRankType() {
        if (isFake() && com.minecraft.game.bukkit.fake.FakeConfig.isHideRank()) {
            return RankType.MEMBER;
        }
        return getRankType();
    }

    public void clearFake() {
        context.getFake().setNick("");
        context.getFake().setUpdatedAt(System.currentTimeMillis());
        markDirty();
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

    public String getRankExpiryLabel() {
        Rank rank = getRank();
        if (rank == null || rank.getType() == null) return "";
        if (rank.getExpiresAt() <= 0) {
            return rank.getType() == RankType.MEMBER ? "" : "Permanente";
        }
        if (rank.hasExpired()) return "Expirado";
        return PunishmentTimeUtil.formatRemaining(rank.getExpiresAt());
    }

    public boolean hasRank(RankType type) {
        return getRankType().isAtLeast(type);
    }

    public boolean isVIP() {
        return hasRank(RankType.VIP);
    }

    /** Broadcast de entrada no servidor (VIP+ e preferencia ativa). */
    public boolean shouldBroadcastJoinMessage() {
        return isVIP() && getToggle().isAllowJoinMessage();
    }

    public boolean isStaffer() {
        return getRankType().isStaff();
    }

    /** Possui alguma tag da equipe (Builder, Mod, Admin, etc.). */
    public boolean hasStaffTag() {
        for (Tag tag : Tag.values()) {
            if (tag.isStaffTag() && hasTag(tag)) return true;
        }
        return false;
    }

    public boolean canUseStaffChat() {
        if (isStaffer() || hasStaffTag()) return true;
        return hasPermission("game.staffchat")
                || hasPermission("lobbyhost.staffchat")
                || hasPermission("proxyhost.staffchat");
    }

    public com.minecraft.game.account.context.objects.punishment.PunishmentHistory getHistory() {
        return context.getHistory();
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
        if (isOnline()) {
            com.minecraft.game.bukkit.manager.PermissionManager.loadPermissions(player());
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

    /**
     * Permissao logica: rank ativo, ranks disponiveis e permissoes extras na conta.
     * Comandos usam isto; Bukkit e sincronizado via {@code PermissionManager}.
     */
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

    /** Permissao explicita na conta, sem grants de rank (evita loop com {@link #hasTag}). */
    private boolean hasExplicitPermission(String key) {
        if (key == null || key.isEmpty()) return false;
        refreshPermissions();
        return getPermissions().stream()
                .anyMatch(p -> !p.hasExpired() && RankType.matchesPermission(p.getKey(), key));
    }

    public java.util.List<com.minecraft.game.account.context.objects.permission.Permission> getPermissions() {
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

    /* ================================================================
     * Semi-anarquia (stats, homes)
     * ============================================================= */

    public com.minecraft.game.account.context.objects.stats.GameStats getGameStats() {
        if (context.getGameStats() == null) {
            context.setGameStats(new com.minecraft.game.account.context.objects.stats.GameStats());
        }
        return context.getGameStats();
    }

    public com.minecraft.game.account.context.objects.stats.PlayerProgress getPlayerProgress() {
        if (context.getPlayerProgress() == null) {
            context.setPlayerProgress(new com.minecraft.game.account.context.objects.stats.PlayerProgress());
        }
        return context.getPlayerProgress();
    }

    public void setPlayerProgress(com.minecraft.game.account.context.objects.stats.PlayerProgress progress) {
        context.setPlayerProgress(progress);
    }

    public com.minecraft.game.account.context.objects.home.HomeMetadata getHomes() {
        if (context.getHomes() == null) {
            context.setHomes(new com.minecraft.game.account.context.objects.home.HomeMetadata());
        }
        return context.getHomes();
    }

    public double getKdr() {
        int d = getGameStats().getDeaths();
        return d == 0 ? getGameStats().getKills() : (double) getGameStats().getKills() / d;
    }

    /* ================================================================
     * Skin
     * ============================================================= */

    public com.minecraft.game.account.context.objects.skin.SkinMetadata getSkin() {
        return context.getSkin();
    }

    public void setSkin(com.minecraft.game.account.context.objects.skin.SkinMetadata skin) {
        context.setSkin(skin != null ? skin : new com.minecraft.game.account.context.objects.skin.SkinMetadata());
        markDirty();
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
        return (c == null || c.isEmpty()) ? "purple" : c;
    }

    public void setEmberPlusColor(String color) {
        context.setEmberPlusColor(color);
        markDirty();
    }

    public int getEmberPlusMonths() {
        return Math.max(0, context.getEmberPlusMonths());
    }

    public void setEmberPlusMonths(int months) {
        context.setEmberPlusMonths(Math.max(0, months));
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

    public int getCash() {
        return context.getCash();
    }

    public void setCash(int cash) {
        context.setCash(Math.max(0, cash));
        markDirty();
    }

    public void addCash(int amount) {
        if (amount > 0) setCash(getCash() + amount);
    }

    public boolean removeCash(int amount) {
        if (amount <= 0 || getCash() < amount) return false;
        setCash(getCash() - amount);
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
    public List<com.minecraft.game.account.context.objects.cosmetic.CosmeticEntry> getCollectibles() {
        return context.getCollectibles();
    }

    public com.minecraft.game.account.context.objects.cosmetic.Cape getActiveCape() {
        return getActiveCollectibles().stream()
                .filter(c -> c.startsWith("cape:"))
                .map(c -> com.minecraft.game.account.context.objects.cosmetic.Cape.fromId(c.substring(5)))
                .filter(java.util.Objects::nonNull)
                .findFirst().orElse(null);
    }

    public void setActiveCape(com.minecraft.game.account.context.objects.cosmetic.Cape cape) {
        getActiveCollectibles().removeIf(c -> c.startsWith("cape:"));
        if (cape != null) getActiveCollectibles().add("cape:" + cape.getId());
        com.minecraft.game.bukkit.cosmetic.CapeParticleRegistry.refresh(id, cape);
        markDirty();
    }

    public com.minecraft.game.account.context.objects.cosmetic.ParticleEffect getActiveParticle() {
        return getActiveCollectibles().stream()
                .filter(c -> c.startsWith("particle:"))
                .map(c -> com.minecraft.game.account.context.objects.cosmetic.ParticleEffect.fromId(c.substring(9)))
                .filter(java.util.Objects::nonNull)
                .findFirst().orElse(null);
    }

    public void setActiveParticle(com.minecraft.game.account.context.objects.cosmetic.ParticleEffect particle) {
        getActiveCollectibles().removeIf(c -> c.startsWith("particle:"));
        if (particle != null) getActiveCollectibles().add("particle:" + particle.getId());
        com.minecraft.game.bukkit.cosmetic.CosmeticParticleRegistry.refresh(id, particle);
        markDirty();
    }

    public com.minecraft.game.account.context.objects.cosmetic.CosmeticTitle getActiveCosmeticTitle() {
        return getActiveCollectibles().stream()
                .filter(c -> c.startsWith("title:"))
                .map(c -> com.minecraft.game.account.context.objects.cosmetic.CosmeticTitle.fromId(c.substring(6)))
                .filter(java.util.Objects::nonNull)
                .findFirst().orElse(null);
    }

    public void setActiveCosmeticTitle(com.minecraft.game.account.context.objects.cosmetic.CosmeticTitle title) {
        getActiveCollectibles().removeIf(c -> c.startsWith("title:"));
        if (title != null) getActiveCollectibles().add("title:" + title.getId());
        markDirty();
    }

    public com.minecraft.game.account.context.objects.cosmetic.Gadget getActiveGadget() {
        return getActiveCollectibles().stream()
                .filter(c -> c.startsWith("gadget:"))
                .map(c -> com.minecraft.game.account.context.objects.cosmetic.Gadget.fromId(c.substring(7)))
                .filter(java.util.Objects::nonNull)
                .findFirst().orElse(null);
    }

    public void setActiveGadget(com.minecraft.game.account.context.objects.cosmetic.Gadget gadget) {
        getActiveCollectibles().removeIf(c -> c.startsWith("gadget:"));
        if (gadget != null) getActiveCollectibles().add("gadget:" + gadget.getId());
        markDirty();
    }

    public boolean hasCape(com.minecraft.game.account.context.objects.cosmetic.Cape cape) {
        if (cape == null) return true;
        if (hasRank(cape.getRequiredRank())) return true;
        if (hasPermission(cape.getPermission())) return true;
        return isUnlocked("cape:" + cape.getId());
    }

    public boolean hasParticle(com.minecraft.game.account.context.objects.cosmetic.ParticleEffect particle) {
        if (particle == null) return true;
        if (hasRank(particle.getRequiredRank())) return true;
        if (hasPermission(particle.getPermission())) return true;
        return isUnlocked("particle:" + particle.getId());
    }

    public boolean hasCosmeticTitle(com.minecraft.game.account.context.objects.cosmetic.CosmeticTitle title) {
        if (title == null) return true;
        if (hasRank(title.getRequiredRank())) return true;
        if (hasPermission(title.getPermission())) return true;
        return isUnlocked("title:" + title.getId());
    }

    public boolean hasGadget(com.minecraft.game.account.context.objects.cosmetic.Gadget gadget) {
        if (gadget == null) return true;
        if (hasRank(gadget.getRequiredRank())) return true;
        if (hasPermission(gadget.getPermission())) return true;
        return isUnlocked("gadget:" + gadget.getId());
    }

    public boolean isUnlocked(String identifier) {
        return getCollectibles().stream().anyMatch(e -> e.getIdentifier().equalsIgnoreCase(identifier));
    }

    public void unlockCosmetic(String identifier) {
        if (!isUnlocked(identifier)) {
            getCollectibles().add(new com.minecraft.game.account.context.objects.cosmetic.CosmeticEntry(identifier));
            markDirty();
        }
    }

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

    /* ================================================================
     * Clan
     * ============================================================= */

    public Clan getClan() {
        if (Core.getClanData() == null) return null;
        ClanMetadata meta = context.getClan();
        if (meta != null && meta.getId() != null && !meta.getId().equals(Constant.DEFAULT_ID)) {
            Clan byMeta = Core.getClanData().of(meta.getId());
            if (byMeta != null && byMeta.isMember(id)) return byMeta;
        }
        Clan byMember = Core.getClanData().findByMember(id);
        if (byMember != null) {
            meta.setId(byMember.getId());
            meta.setTag(byMember.getTagWithoutColors());
            meta.setLastUpdate(System.currentTimeMillis());
            markDirty();
            return byMember;
        }
        return null;
    }

    public boolean hasClan() {
        return getClan() != null;
    }

    public boolean isClanLeader() {
        return hasClan() && getClan().isLeader(id);
    }

    public boolean hasClanRole(ClanRole minimum) {
        if (!hasClan()) return false;
        var member = getClan().getMember(id);
        return member != null && member.hasRole(minimum);
    }

    public void joinClan(Clan clan) {
        ClanMetadata meta = context.getClan();
        meta.setId(clan.getId());
        meta.setTag(clan.getTagWithoutColors());
        meta.setLastUpdate(System.currentTimeMillis());
        markDirty();
        ClanSync.refresh(this);
    }

    public void leaveClanSilent() {
        ClanMetadata meta = context.getClan();
        meta.setId(Constant.DEFAULT_ID);
        meta.setTag("");
        meta.setLastUpdate(System.currentTimeMillis());
        markDirty();
        ClanSync.refresh(this);
    }

    public String getFormattedClanTagSuffix() {
        return ClanTagFormatter.formatSuffix(this);
    }
}
