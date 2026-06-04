package com.minecraft.lobbyhost.bukkit.listener;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.display.npc.LobbyNpc;
import com.minecraft.lobbyhost.display.npc.NpcDefinition;
import com.minecraft.lobbyhost.display.npc.NpcManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;

/**
 * Clique em NPCs ({@link Interaction} com PDC {@link LobbyNpc#NPC_ID_KEY}).
 */
public final class NpcInteractionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof Interaction)) return;

        String npcId = clicked.getPersistentDataContainer()
                .get(LobbyNpc.NPC_ID_KEY, PersistentDataType.STRING);
        if (npcId == null || npcId.isEmpty()) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        NpcDefinition def = NpcManager.getDefinition(npcId);
        if (def == null) return;

        Account account = com.minecraft.lobbyhost.Core.getAccountController().of(player.getUniqueId());
        if (account != null && account.hasCooldown("npc-click-" + npcId)) {
            return;
        }
        if (account != null) {
            account.setCooldown("npc-click-" + npcId, NpcManager.getClickCooldownMs());
        }

        NpcManager.handleClick(player, def);
    }
}
