package com.minecraft.game.bukkit.menu.lang;

import com.minecraft.game.GamePlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

final class LangKeys {

    static final NamespacedKey LANG_ID = new NamespacedKey(GamePlugin.getInstance(), "lang_id");
    static final PersistentDataType<String, String> LANG_TYPE = PersistentDataType.STRING;

    private LangKeys() {}
}
