package com.minecraft.lobbyhost.bukkit.vanish;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;

/** Configuração do vanish ({@code plugins/lobbyhost/vanish.yml}). */
public final class VanishConfig {

    private static File file;
    private static FileConfiguration yaml;

    private static boolean enabled = true;
    private static boolean autoFly = true;
    private static boolean nightVision = true;
    private static boolean actionBar = true;
    private static String actionBarText = "";
    private static boolean silentJoin = true;
    private static boolean silentQuit = true;
    private static boolean fakeQuitOnEnable = true;
    private static boolean fakeJoinOnDisable = true;
    private static boolean blockPickup = true;
    private static boolean blockDamage = true;
    private static boolean blockHunger = true;
    private static boolean blockMobTarget = true;
    private static boolean blockPressurePlates = true;
    private static boolean blockPublicChat = true;
    private static boolean staffSeeVanished = true;
    private static String permUse = "lobbyhost.vanish";
    private static String permSee = "lobbyhost.vanish.see";
    private static String msgEnabled = "";
    private static String msgDisabled = "";
    private static String msgChatBlocked = "";
    private static String msgListEmpty = "";
    private static String msgListHeader = "";

    private VanishConfig() {}

    public static void load(LobbyHostPlugin plugin) {
        file = new File(plugin.getDataFolder(), "vanish.yml");
        if (!file.exists()) {
            plugin.saveResource("vanish.yml", false);
        }
        yaml = YamlConfiguration.loadConfiguration(file);
        mergeDefaults(plugin);
        readAll();
    }

    private static void mergeDefaults(LobbyHostPlugin plugin) {
        try (InputStream in = plugin.getResource("vanish.yml")) {
            if (in == null) return;
            yaml.setDefaults(YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)));
            yaml.options().copyDefaults(true);
            yaml.save(file);
        } catch (Exception ignored) {
        }
    }

    private static void readAll() {
        enabled = yaml.getBoolean("enabled", true);
        autoFly = yaml.getBoolean("auto-fly", true);
        nightVision = yaml.getBoolean("night-vision", true);
        actionBar = yaml.getBoolean("action-bar", true);
        actionBarText = yaml.getString("action-bar-text", "");
        silentJoin = yaml.getBoolean("silent-join", true);
        silentQuit = yaml.getBoolean("silent-quit", true);
        fakeQuitOnEnable = yaml.getBoolean("fake-quit-on-enable", true);
        fakeJoinOnDisable = yaml.getBoolean("fake-join-on-disable", true);
        blockPickup = yaml.getBoolean("block-pickup", true);
        blockDamage = yaml.getBoolean("block-damage", true);
        blockHunger = yaml.getBoolean("block-hunger", true);
        blockMobTarget = yaml.getBoolean("block-mob-target", true);
        blockPressurePlates = yaml.getBoolean("block-pressure-plates", true);
        blockPublicChat = yaml.getBoolean("block-public-chat", true);
        staffSeeVanished = yaml.getBoolean("staff-see-vanished", true);
        permUse = yaml.getString("permissions.use", "lobbyhost.vanish");
        permSee = yaml.getString("permissions.see", "lobbyhost.vanish.see");
        msgEnabled = yaml.getString("messages.enabled", "");
        msgDisabled = yaml.getString("messages.disabled", "");
        msgChatBlocked = yaml.getString("messages.chat-blocked", "");
        msgListEmpty = yaml.getString("messages.list-empty", "");
        msgListHeader = yaml.getString("messages.list-header", "");
    }

    public static boolean isEnabled() { return enabled; }
    public static boolean isAutoFly() { return autoFly; }
    public static boolean isNightVision() { return nightVision; }
    public static boolean isActionBar() { return actionBar; }
    public static String getActionBarText() { return actionBarText; }
    public static boolean isSilentJoin() { return silentJoin; }
    public static boolean isSilentQuit() { return silentQuit; }
    public static boolean isFakeQuitOnEnable() { return fakeQuitOnEnable; }
    public static boolean isFakeJoinOnDisable() { return fakeJoinOnDisable; }
    public static boolean isBlockPickup() { return blockPickup; }
    public static boolean isBlockDamage() { return blockDamage; }
    public static boolean isBlockHunger() { return blockHunger; }
    public static boolean isBlockMobTarget() { return blockMobTarget; }
    public static boolean isBlockPressurePlates() { return blockPressurePlates; }
    public static boolean isBlockPublicChat() { return blockPublicChat; }
    public static boolean isStaffSeeVanished() { return staffSeeVanished; }
    public static String permUse() { return permUse; }
    public static String permSee() { return permSee; }
    public static String msgEnabled() { return msgEnabled; }
    public static String msgDisabled() { return msgDisabled; }
    public static String msgChatBlocked() { return msgChatBlocked; }
    public static String msgListEmpty() { return msgListEmpty; }
    public static String msgListHeader() { return msgListHeader; }
}
