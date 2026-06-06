package com.minecraft.game.bukkit.menu.prefs;

import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.toggle.Toggle;
import com.minecraft.game.bukkit.event.AccountTagEvent;
import com.minecraft.game.util.AccountSync;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.function.Function;
import java.util.function.BiConsumer;

public enum PrefsToggle {

    MESSAGES(Category.SOCIAL, "Mensagens privadas", Material.PAPER,
            t -> t.isAllowPrivateMessages(), (t, v) -> t.setAllowPrivateMessages(v)),
    FRIENDS(Category.SOCIAL, "Pedidos de amizade", Material.PLAYER_HEAD,
            t -> t.isAllowFriendRequests(), (t, v) -> t.setAllowFriendRequests(v)),
    PARTY(Category.SOCIAL, "Convites de party", Material.EMERALD,
            t -> t.isAllowPartyInvites(), (t, v) -> t.setAllowPartyInvites(v)),
    CLAN(Category.SOCIAL, "Convites de clã", Material.SHIELD,
            t -> t.isAllowClanInvites(), (t, v) -> t.setAllowClanInvites(v)),
    CLANTAG(Category.VISIBILITY, "Tag do clã visível", Material.NAME_TAG,
            t -> t.isShowClanTag(), (t, v) -> t.setShowClanTag(v), true),
    STATS(Category.VISIBILITY, "Estatísticas visíveis", Material.BOOK,
            t -> t.isShowStats(), (t, v) -> t.setShowStats(v)),
    JOINMSG(Category.VISIBILITY, "Mensagem ao entrar", Material.OAK_DOOR,
            t -> t.isAllowJoinMessage(), (t, v) -> t.setAllowJoinMessage(v)),
    COSMETIC_PARTICLES(Category.VISIBILITY, "Partículas cosméticas", Material.BLAZE_POWDER,
            t -> t.isShowCosmeticParticles(), (t, v) -> t.setShowCosmeticParticles(v)),
    OTHER_CAPES(Category.VISIBILITY, "Capas de outros jogadores", Material.LEATHER_CHESTPLATE,
            t -> t.isShowOtherCapes(), (t, v) -> t.setShowOtherCapes(v)),
    COMPACT_CHAT(Category.CHAT, "Chat compacto", Material.WRITABLE_BOOK,
            t -> t.isCompactChat(), (t, v) -> t.setCompactChat(v)),
    HIDE_ANNOUNCE(Category.CHAT, "Ocultar anúncios", Material.BELL,
            t -> t.isHideGlobalAnnouncements(), (t, v) -> t.setHideGlobalAnnouncements(v)),
    CHATLOBBY(Category.CHAT, "Chat do lobby", Material.BOOK,
            t -> t.isAllowLobbyChat(), (t, v) -> t.setAllowLobbyChat(v)),
    NOTIFY_FRIENDS(Category.NOTIFICATIONS, "Amigo online", Material.EMERALD,
            t -> t.isNotifyFriendOnline(), (t, v) -> t.setNotifyFriendOnline(v)),
    NOTIFY_CLAN(Category.NOTIFICATIONS, "Atividade do clã", Material.SHIELD,
            t -> t.isNotifyClanActivity(), (t, v) -> t.setNotifyClanActivity(v)),
    NOTIFY_BOUNTY(Category.NOTIFICATIONS, "Bounty na cabeça", Material.GOLD_NUGGET,
            t -> t.isNotifyBountyOnMe(), (t, v) -> t.setNotifyBountyOnMe(v)),
    NOTIFY_STREAK(Category.NOTIFICATIONS, "Killstreak global", Material.IRON_SWORD,
            t -> t.isNotifyKillStreakGlobal(), (t, v) -> t.setNotifyKillStreakGlobal(v)),
    NOTIFY_FEED(Category.NOTIFICATIONS, "Feed da rede", Material.BOOK,
            t -> t.isNotifyFeed(), (t, v) -> t.setNotifyFeed(v)),
    AC_COMBAT_TAG(Category.ANTICHEAT, "Tag de combate (AC)", Material.IRON_SWORD,
            t -> t.isAcShowCombatTag(), (t, v) -> t.setAcShowCombatTag(v)),
    AC_NOTIFY(Category.ANTICHEAT, "Aviso se flagado (AC)", Material.BELL,
            t -> t.isAcNotifyOnFlag(), (t, v) -> t.setAcNotifyOnFlag(v)),
    STAFFCHAT(Category.STAFF, "Modo staff chat", Material.COMMAND_BLOCK,
            t -> t.isStaffChat(), (t, v) -> t.setStaffChat(v), false, true),
    AC_STAFF_ALERTS(Category.STAFF, "Alertas anticheat", Material.TNT,
            t -> t.isAcStaffAlerts(), (t, v) -> t.setAcStaffAlerts(v), false, true),
    AC_STAFF_VERBOSE(Category.STAFF, "AC modo detalhado", Material.SPYGLASS,
            t -> t.isAcStaffVerbose(), (t, v) -> t.setAcStaffVerbose(v), false, true),
    AUTO_VANISH(Category.STAFF, "Vanish ao entrar", Material.ENDER_EYE,
            t -> t.isAutoVanish(), (t, v) -> t.setAutoVanish(v), false, true);

    public enum Category {
        SOCIAL("§b§lSocial", Material.CYAN_STAINED_GLASS_PANE, "§7Mensagens · amigos · clã"),
        VISIBILITY("§d§lVisibilidade", Material.MAGENTA_STAINED_GLASS_PANE, "§7Tag · stats · cosméticos"),
        CHAT("§e§lChat", Material.YELLOW_STAINED_GLASS_PANE, "§7Compacto · anúncios · lobby"),
        NOTIFICATIONS("§a§lNotificações", Material.LIME_STAINED_GLASS_PANE, "§7Amigos · clã · feed"),
        ANTICHEAT("§c§lAnticheat", Material.RED_STAINED_GLASS_PANE, "§7Feedback do anti-cheat"),
        STAFF("§6§lStaff", Material.ORANGE_STAINED_GLASS_PANE, "§7Ferramentas da equipe");

        private final String title;
        private final Material pane;
        private final String subtitle;

        Category(String title, Material pane, String subtitle) {
            this.title = title;
            this.pane = pane;
            this.subtitle = subtitle;
        }

        public String title() { return title; }
        public Material pane() { return pane; }
        public String subtitle() { return subtitle; }
    }

    private static final Category[] LAYOUT_ORDER = {
            Category.SOCIAL,
            Category.VISIBILITY,
            Category.CHAT,
            Category.NOTIFICATIONS,
            Category.ANTICHEAT,
            Category.STAFF
    };

    private static final int[] ITEM_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final Category category;
    private final String label;
    private final Material icon;
    private final Function<Toggle, Boolean> getter;
    private final BiConsumer<Toggle, Boolean> setter;
    private final boolean refreshTag;
    private final boolean staffOnly;

    PrefsToggle(Category category, String label, Material icon,
                Function<Toggle, Boolean> getter, BiConsumer<Toggle, Boolean> setter) {
        this(category, label, icon, getter, setter, false, false);
    }

    PrefsToggle(Category category, String label, Material icon,
                Function<Toggle, Boolean> getter, BiConsumer<Toggle, Boolean> setter,
                boolean refreshTag) {
        this(category, label, icon, getter, setter, refreshTag, false);
    }

    PrefsToggle(Category category, String label, Material icon,
                Function<Toggle, Boolean> getter, BiConsumer<Toggle, Boolean> setter,
                boolean refreshTag, boolean staffOnly) {
        this.category = category;
        this.label = label;
        this.icon = icon;
        this.getter = getter;
        this.setter = setter;
        this.refreshTag = refreshTag;
        this.staffOnly = staffOnly;
    }

    public Category category() { return category; }
    public String label() { return label; }
    public Material icon() { return icon; }

    public static Category[] layoutOrder() {
        return LAYOUT_ORDER;
    }

    public static int[] itemSlots() {
        return ITEM_SLOTS;
    }

    public static PrefsToggle[] byCategory(Category category) {
        return java.util.Arrays.stream(values())
                .filter(t -> t.category == category)
                .toArray(PrefsToggle[]::new);
    }

    public boolean get(Toggle t) {
        return getter.apply(t);
    }

    public void flip(Account account) {
        Toggle t = account.getToggle();
        boolean next = !getter.apply(t);
        setter.accept(t, next);
        account.markDirty();
        AccountSync.saveAndBroadcast(account);
        if (refreshTag) {
            Bukkit.getPluginManager().callEvent(new AccountTagEvent(account, account.getTag()));
        }
    }

    public static boolean visible(PrefsToggle toggle, Account account) {
        if (!toggle.staffOnly) return true;
        return account != null && account.isStaffer();
    }
}
