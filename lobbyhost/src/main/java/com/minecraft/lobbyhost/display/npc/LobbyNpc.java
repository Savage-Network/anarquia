package com.minecraft.lobbyhost.display.npc;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * NPC de lobby 1.21: {@link Interaction} (clique) + {@link ItemDisplay} (cabeca)
 * + linhas {@link TextDisplay} (holograma).
 */
@Getter
public final class LobbyNpc {

    public static final NamespacedKey NPC_ID_KEY =
            NamespacedKey.minecraft("lobbyhost_npc_id");

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();

    private final NpcDefinition definition;
    private final UUID interactionId;
    private final List<UUID> entityIds = new ArrayList<>();

    public LobbyNpc(NpcDefinition definition, Location base) {
        this.definition = definition;
        this.interactionId = spawnBundle(base);
    }

    private UUID spawnBundle(Location base) {
        if (base.getWorld() == null) throw new IllegalStateException("Mundo invalido para NPC " + definition.getId());

        float yaw = base.getYaw();
        Location headLoc = base.clone().add(0, 1.35, 0);
        headLoc.setYaw(yaw);

        // Zona de clique (2 blocos de altura)
        Interaction interaction = base.getWorld().spawn(base, Interaction.class, ent -> {
            ent.setInteractionWidth(0.9f);
            ent.setInteractionHeight(2.0f);
            ent.setResponsive(true);
            ent.setPersistent(false);
            ent.getPersistentDataContainer().set(NPC_ID_KEY, PersistentDataType.STRING, definition.getId());
        });

        // Cabeca (ItemDisplay)
        ItemStack head = (definition.getSkinTexture() != null && !definition.getSkinTexture().isBlank())
                ? SkullProfiles.createHeadFromTexture(definition.getSkinTexture(), definition.getSkinSignature())
                : SkullProfiles.createHead(definition.getSkinPlayer() != null ? definition.getSkinPlayer() : "Steve");

        ItemDisplay headDisplay = base.getWorld().spawn(headLoc, ItemDisplay.class, id -> {
            id.setItemStack(head);
            id.setBillboard(Display.Billboard.CENTER);
            id.setPersistent(false);
            id.getPersistentDataContainer().set(NPC_ID_KEY, PersistentDataType.STRING, definition.getId());
        });
        entityIds.add(headDisplay.getUniqueId());

        // Holograma
        List<String> lines = definition.getHologramLines();
        double lineHeight = 0.28;
        double startY = 2.05;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isEmpty()) continue;
            Location holo = base.clone().add(0, startY + (lines.size() - 1 - i) * lineHeight, 0);
            TextDisplay td = base.getWorld().spawn(holo, TextDisplay.class, text -> {
                text.text(legacy(line));
                text.setBillboard(Display.Billboard.CENTER);
                text.setSeeThrough(true);
                text.setShadowed(true);
                text.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
                text.setPersistent(false);
                text.getPersistentDataContainer().set(NPC_ID_KEY, PersistentDataType.STRING, definition.getId());
            });
            entityIds.add(td.getUniqueId());
        }

        entityIds.add(interaction.getUniqueId());
        return interaction.getUniqueId();
    }

    public void remove() {
        for (UUID id : new ArrayList<>(entityIds)) {
            Entity e = org.bukkit.Bukkit.getEntity(id);
            if (e != null && !e.isDead()) e.remove();
        }
        entityIds.clear();
    }

    public static Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }
}
