package com.minecraft.core.member.list.bedwars.objects.metadata;

import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.share.ShareType;
import com.minecraft.core.member.list.bedwars.objects.menu.BedMenuMetadata;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.stats.BedStats;
import com.minecraft.core.member.list.bedwars.objects.mission.MissionProgress;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class BedMetadata {

    private ProfileType profile = ProfileType.NORMAL;
    private ShareType share = ShareType.DEFAULT;

    private boolean tntTimerEnabled = true, showRank = true, showBar = true;

    private final List<BedStats> statsList = new ArrayList<>();
    private List<BedMenuMetadata> menus = new ArrayList<>();

    private final Map<BedCollectibleType, String> collectibles = new HashMap<>();
    
    // Sistema de moedas e habilidades
    private int coins = 0; // Moedas do jogador
    private final Set<String> purchasedAbilities = new HashSet<>(); // Nomes das habilidades compradas
    private final Set<String> selectedAbilities = new HashSet<>(); // Habilidades selecionadas para usar
    
    // Sistema de missões
    private final List<MissionProgress> missionProgress = new ArrayList<>();
    
    // Sistema de hotbar personalizada
    private final Map<Integer, String> hotbarSlots = new HashMap<>(); // Slot -> Nome da categoria (BedShop)
    
    // Sistema de Ranked
    private int rankedPoints = 0; // Pontos de ranked do jogador
    
    public com.minecraft.core.member.list.bedwars.objects.enums.BedShop getHotbarSlot(int slot) {
        String shopName = hotbarSlots.get(slot);
        if (shopName == null) return null;
        
        try {
            return com.minecraft.core.member.list.bedwars.objects.enums.BedShop.valueOf(shopName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public void setHotbarSlot(int slot, com.minecraft.core.member.list.bedwars.objects.enums.BedShop shop) {
        if (shop == null) {
            hotbarSlots.remove(slot);
        } else {
            hotbarSlots.put(slot, shop.name());
        }
    }
}
