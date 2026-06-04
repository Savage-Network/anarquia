package com.minecraft.core.bukkit.api.abovename;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.title.CustomTitle;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AboveNameManager {

    private final Map<UUID, ArmorStand> playerHolograms = new HashMap<>();
    private final Map<UUID, String> playerTitles = new HashMap<>();
    /** Índice do quadro animado atual (por jogador). */
    private final Map<UUID, Integer> titleAnimationFrameIndex = new HashMap<>();
    /** Passos até avançar quadro; ver {@link TitleCollectible#getTitleAnimationStepsPerFrame()}. */
    private final Map<UUID, Integer> titleAnimationCooldown = new HashMap<>();
    private final Map<UUID, String> titleAnimationLastCollectibleId = new HashMap<>();
    private final Plugin plugin;

    public AboveNameManager(Plugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateAboveName(player);
            }
        }, 0L, 10L); // Atualiza a cada 0.5 segundos (10 ticks)
    }

    public void updateAboveName(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            removeAboveName(player);
            return;
        }

        String titleText = getTitleText(account, player);
        
        // Se não tem título, remover hologram
        if (titleText == null || titleText.isEmpty()) {
            removeAboveName(player);
            return;
        }

        String currentTitle = playerTitles.get(player.getUniqueId());
        ArmorStand hologram = playerHolograms.get(player.getUniqueId());

        // Criar hologram se não existir
        if (hologram == null || hologram.isDead()) {
            hologram = createHologram(player, titleText);
            playerHolograms.put(player.getUniqueId(), hologram);
            playerTitles.put(player.getUniqueId(), titleText);
        } else {
            // Atualizar posição (mais rápido)
            Location loc = player.getLocation().add(0, 2.3, 0);
            hologram.teleport(loc);

            // Atualizar texto apenas se mudou (evita operações desnecessárias)
            if (!titleText.equals(currentTitle)) {
                hologram.setCustomName(titleText);
                playerTitles.put(player.getUniqueId(), titleText);
            }
        }
    }

    private ArmorStand createHologram(Player player, String text) {
        Location loc = player.getLocation().add(0, 2.3, 0);
        ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        armorStand.setCustomName(text);
        armorStand.setCustomNameVisible(true);
        armorStand.setSmall(true);
        armorStand.setMarker(true);
        
        return armorStand;
    }

    private String pickAnimatedTitleFrame(UUID playerId, TitleCollectible title, List<String> frames) {
        String collectibleId = title.getIdentifier();
        String lastId = titleAnimationLastCollectibleId.get(playerId);
        if (lastId == null || !lastId.equals(collectibleId)) {
            titleAnimationFrameIndex.put(playerId, 0);
            titleAnimationCooldown.put(playerId, 0);
            titleAnimationLastCollectibleId.put(playerId, collectibleId);
        }

        int stepsPer = Math.max(1, title.getTitleAnimationStepsPerFrame());
        int idx = titleAnimationFrameIndex.getOrDefault(playerId, 0);
        int cd = titleAnimationCooldown.getOrDefault(playerId, 0) + 1;
        if (cd >= stepsPer) {
            cd = 0;
            idx = (idx + 1) % frames.size();
        }
        titleAnimationCooldown.put(playerId, cd);
        titleAnimationFrameIndex.put(playerId, idx);
        return frames.get(idx % frames.size());
    }

    private String getTitleText(Account account, Player player) {
        if (account.hasCustomTitle()) {
            clearTitleAnimationState(player.getUniqueId());
            CustomTitle customTitle = account.getCustomTitle();
            return customTitle.getTitle();
        }

        for (String activeId : account.getActiveCollectibles()) {
            if (activeId.startsWith("title:")) {
                Collectible collectible = Core.getCollectibleController().of(activeId);

                if (collectible instanceof TitleCollectible) {
                    TitleCollectible titleCollectible = (TitleCollectible) collectible;
                    List<String> frames = titleCollectible.getTitleAnimationFrames();
                    if (frames != null && !frames.isEmpty()) {
                        return pickAnimatedTitleFrame(player.getUniqueId(), titleCollectible, frames);
                    }
                    clearTitleAnimationState(player.getUniqueId());
                    return getTitleForDisplay(titleCollectible, account, player);
                }
            }
        }

        clearTitleAnimationState(player.getUniqueId());
        return null;
    }

    private void clearTitleAnimationState(UUID playerId) {
        titleAnimationFrameIndex.remove(playerId);
        titleAnimationCooldown.remove(playerId);
        titleAnimationLastCollectibleId.remove(playerId);
    }

    private String getTitleForDisplay(TitleCollectible title, Account account, Player player) {
        String titleName = title.getName();

        switch (titleName) {
            case "Ping":
                int ping = getPing(player);
                String pingColor = ping < 50 ? "§a" : ping < 100 ? "§e" : "§c";
                return "§ePing: " + pingColor + ping + "ms";

            case "Clan":
                if (account.hasClan()) {
                    return "§c§lClan: §r§eMembro de " + account.getClanTag().trim();
                }
                return "§c§lClan: §r§7Sem clan";

            case "Sua medalha":
                return account.getMedal().getColoredSymbol();

            case "Nível Bed Wars":
                // Obter nível do Bed Wars
                com.minecraft.core.member.list.bedwars.BedMember bedMember = 
                    account.getMember(com.minecraft.core.member.list.bedwars.BedMember.class);
                if (bedMember != null) {
                    return "§cNível Bed Wars: §e" + bedMember.getLevel();
                }
                return "§cNível Bed Wars: §e0";

            case "Vitórias Bed Wars":
                // Obter vitórias do Bed Wars
                bedMember = account.getMember(com.minecraft.core.member.list.bedwars.BedMember.class);
                if (bedMember != null) {
                    return "§cVitórias Bed Wars: §e" + bedMember.getTotalWins();
                }
                return "§cVitórias Bed Wars: §e0";

            case "Desde":
                // Usar ano atual como placeholder (pode ser ajustado depois)
                return "§cDesde §b2026";

            case "Tempo jogado":
                // Placeholder - pode ser implementado depois com sistema de playtime
                return "§cTempo jogado: §b0 horas";

            default:
                // Usar o método getTitle() do TitleCollectible
                return title.getTitle();
        }
    }

    private int getPing(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            return (int) handle.getClass().getField("ping").get(handle);
        } catch (Exception e) {
            return 0;
        }
    }

    public void removeAboveName(Player player) {
        ArmorStand hologram = playerHolograms.remove(player.getUniqueId());
        if (hologram != null && !hologram.isDead()) {
            hologram.remove();
        }
        playerTitles.remove(player.getUniqueId());
        clearTitleAnimationState(player.getUniqueId());
    }

    public void removeAll() {
        for (ArmorStand hologram : playerHolograms.values()) {
            if (hologram != null && !hologram.isDead()) {
                hologram.remove();
            }
        }
        playerHolograms.clear();
        playerTitles.clear();
        titleAnimationFrameIndex.clear();
        titleAnimationCooldown.clear();
        titleAnimationLastCollectibleId.clear();
    }
}
