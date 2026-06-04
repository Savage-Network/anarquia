package com.minecraft.core.api.joinmessage;

import com.minecraft.core.account.context.objects.joinmessage.JoinMessageRarity;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import lombok.Getter;

@Getter
public enum JoinMessageType {
    
    DEFAULT("default", "Padrão", "entrou no lobby!", JoinMessageRarity.COMMON, RankType.MEMBER),
    CONNECTED("connected", "Conectado", "se conectou!", JoinMessageRarity.COMMON, RankType.MEMBER),
    ARRIVED("arrived", "Chegou", "chegou chegando!", JoinMessageRarity.COMMON, RankType.MEMBER),
    ONLINE("online", "Online", "está online!", JoinMessageRarity.COMMON, RankType.MEMBER),
    JOINED("joined", "Entrou", "entrou no servidor!", JoinMessageRarity.COMMON, RankType.MEMBER),
    
    // Mensagens Raras
    APPEARED("appeared", "Apareceu", "apareceu do nada!", JoinMessageRarity.RARE, RankType.VIP),
    SPAWNED("spawned", "Spawnou", "spawnou no lobby!", JoinMessageRarity.RARE, RankType.VIP),
    TELEPORTED("teleported", "Teleportou", "se teleportou!", JoinMessageRarity.RARE, RankType.VIP),
    MATERIALIZED("materialized", "Materializou", "se materializou!", JoinMessageRarity.RARE, RankType.VIP),
    
    // Mensagens Épicas
    DESCENDED("descended", "Desceu", "desceu dos céus!", JoinMessageRarity.EPIC, RankType.NATIVE),
    INVADED("invaded", "Invadiu", "invadiu o servidor!", JoinMessageRarity.EPIC, RankType.NATIVE),
    AWAKENED("awakened", "Despertou", "despertou!", JoinMessageRarity.EPIC, RankType.NATIVE),
    EMERGED("emerged", "Emergiu", "emergiu das sombras!", JoinMessageRarity.EPIC, RankType.NATIVE),
    
    // Mensagens Lendárias
    LEGENDARY_ARRIVAL("legendary", "Lendário", "fez uma entrada lendária!", JoinMessageRarity.LEGENDARY, RankType.EMBER),
    EPIC_ENTRANCE("epic", "Épico", "fez uma entrada épica!", JoinMessageRarity.LEGENDARY, RankType.EMBER),
    BOSS_ENTRY("boss", "Boss", "entrou como um boss!", JoinMessageRarity.LEGENDARY, RankType.EMBER),
    DIVINE("divine", "Divino", "desceu divinamente!", JoinMessageRarity.LEGENDARY, RankType.EMBER),
    
    // Mensagens Míticas
    MYTHICAL("mythical", "Mítico", "apareceu miticamente!", JoinMessageRarity.MYTHICAL, RankType.EMBER_PLUS),
    GODLIKE("godlike", "Divindade", "chegou como uma divindade!", JoinMessageRarity.MYTHICAL, RankType.EMBER_PLUS),
    SUPREME("supreme", "Supremo", "fez uma entrada suprema!", JoinMessageRarity.MYTHICAL, RankType.EMBER_PLUS),
    CELESTIAL("celestial", "Celestial", "desceu celestialmente!", JoinMessageRarity.MYTHICAL, RankType.EMBER_PLUS);
    
    private final String id;
    private final String name;
    private final String message;
    private final JoinMessageRarity rarity;
    private final RankType requiredRank;
    
    JoinMessageType(String id, String name, String message, JoinMessageRarity rarity, RankType requiredRank) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.rarity = rarity;
        this.requiredRank = requiredRank;
    }
    
    public JoinMessage toJoinMessage() {
        return new JoinMessage(id, name, message, rarity, requiredRank);
    }
    
    public static JoinMessageType byId(String id) {
        for (JoinMessageType type : values()) {
            if (type.getId().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}

