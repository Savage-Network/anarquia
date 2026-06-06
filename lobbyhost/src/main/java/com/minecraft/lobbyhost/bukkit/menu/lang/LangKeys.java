package com.minecraft.lobbyhost.bukkit.menu.lang;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

final class LangKeys {

    static final NamespacedKey LANG_ID = new NamespacedKey(LobbyHostPlugin.getInstance(), "lang_id");
    static final PersistentDataType<String, String> LANG_TYPE = PersistentDataType.STRING;

    private LangKeys() {}
}
