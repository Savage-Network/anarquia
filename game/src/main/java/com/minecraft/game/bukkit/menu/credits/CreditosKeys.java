package com.minecraft.game.bukkit.menu.credits;

import com.minecraft.game.GamePlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

final class CreditosKeys {

    static final NamespacedKey CLICK_ID = new NamespacedKey(GamePlugin.getInstance(), "creditos_click");
    static final PersistentDataType<String, String> CLICK_TYPE = PersistentDataType.STRING;

    private CreditosKeys() {}
}
