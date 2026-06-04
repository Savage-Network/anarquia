package com.minecraft.game.bukkit.listener;

import com.minecraft.game.bukkit.event.AccountMedalEvent;
import com.minecraft.game.bukkit.event.AccountPrefixTypeEvent;
import com.minecraft.game.bukkit.event.AccountTagEvent;
import com.minecraft.game.bukkit.display.AboveNameDisplay;
import com.minecraft.game.bukkit.manager.TagManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Espelho de {@code UpdateListener} do core-bukkit Savage.
 *
 * <p>Reage a mudancas em tag/medal/prefixtype repintando o tab/nametag.
 */
public final class UpdateListener implements Listener {

    @EventHandler
    public void onTag(AccountTagEvent event) {
        TagManager.updateTag(event.getAccount());
    }

    @EventHandler
    public void onMedal(AccountMedalEvent event) {
        // medalha so afeta chat (e nao tab) - mesmo assim chamamos updateTag
        // para garantir consistencia de display name (cor do tag).
        TagManager.updateTag(event.getAccount());
    }

    @EventHandler
    public void onPrefixType(AccountPrefixTypeEvent event) {
        if (event.getAccount().player() != null) {
            TagManager.updateAllForViewer(event.getAccount().player());
            AboveNameDisplay.refresh(event.getAccount().player());
        }
    }
}
