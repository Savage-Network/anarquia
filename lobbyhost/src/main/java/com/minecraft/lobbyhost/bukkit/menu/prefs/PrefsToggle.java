package com.minecraft.lobbyhost.bukkit.menu.prefs;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.toggle.Toggle;
import com.minecraft.lobbyhost.bukkit.event.AccountTagEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.function.Function;
import java.util.function.BiConsumer;

public enum PrefsToggle {

    MESSAGES(Category.SOCIAL, "Mensagens privadas", Material.PAPER,
            t -> t.isAllowPrivateMessages(), (t, v) -> t.setAllowPrivateMessages(v)),
    FRIENDS(Category.SOCIAL, "Pedidos de amizade", Material.PLAYER_HEAD,
            t -> t.isAllowFriendRequests(), (t, v) -> t.setAllowFriendRequests(v)),
    CLAN(Category.SOCIAL, "Convites de clã", Material.SHIELD,
            t -> t.isAllowClanInvites(), (t, v) -> t.setAllowClanInvites(v)),
    CLANTAG(Category.VISIBILITY, "Tag do clã visível", Material.NAME_TAG,
            t -> t.isShowClanTag(), (t, v) -> t.setShowClanTag(v), true),
    STATS(Category.VISIBILITY, "Estatísticas visíveis", Material.BOOK,
            t -> t.isShowStats(), (t, v) -> t.setShowStats(v)),
    JOINMSG(Category.VISIBILITY, "Mensagem ao entrar", Material.OAK_DOOR,
            t -> t.isAllowJoinMessage(), (t, v) -> t.setAllowJoinMessage(v)),
    CHATLOBBY(Category.CHAT, "Chat do lobby", Material.WRITABLE_BOOK,
            t -> t.isAllowLobbyChat(), (t, v) -> t.setAllowLobbyChat(v)),
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
        SOCIAL(
                "<!italic><gradient:#38BDF8:#818CF8><bold>Social</bold></gradient>",
                Material.CYAN_STAINED_GLASS_PANE,
                "<!italic><#94A3B8>Mensagens · amigos · clã"),
        VISIBILITY(
                "<!italic><gradient:#C084FC:#F472B6><bold>Visibilidade</bold></gradient>",
                Material.MAGENTA_STAINED_GLASS_PANE,
                "<!italic><#94A3B8>Tag · stats · entrada"),
        CHAT(
                "<!italic><gradient:#FBBF24:#F59E0B><bold>Chat</bold></gradient>",
                Material.YELLOW_STAINED_GLASS_PANE,
                "<!italic><#94A3B8>Canal público do lobby"),
        ANTICHEAT(
                "<!italic><gradient:#F87171:#EF4444><bold>Anticheat</bold></gradient>",
                Material.RED_STAINED_GLASS_PANE,
                "<!italic><#94A3B8>Feedback do anti-cheat"),
        STAFF(
                "<!italic><gradient:#FF8C42:#FFD166><bold>Staff</bold></gradient>",
                Material.ORANGE_STAINED_GLASS_PANE,
                "<!italic><#94A3B8>Ferramentas da equipe");

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
    public boolean staffOnly() { return staffOnly; }

    public static PrefsToggle[] byCategory(Category category) {
        return java.util.Arrays.stream(values())
                .filter(t -> t.category == category)
                .toArray(PrefsToggle[]::new);
    }

    public boolean get(Toggle t) { return getter.apply(t); }

    public void flip(Account account) {
        Toggle t = account.getToggle();
        boolean next = !getter.apply(t);
        setter.accept(t, next);
        account.markDirty();
        com.minecraft.lobbyhost.util.AccountSync.saveAndBroadcast(account);
        if (refreshTag) {
            Bukkit.getPluginManager().callEvent(new AccountTagEvent(account, account.getTag()));
        }
    }

    public static boolean visible(PrefsToggle toggle, Account account) {
        if (!toggle.staffOnly) return true;
        return account != null && account.isStaffer();
    }
}
