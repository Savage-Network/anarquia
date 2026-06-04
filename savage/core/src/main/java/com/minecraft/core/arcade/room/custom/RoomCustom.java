package com.minecraft.core.arcade.room.custom;

import com.minecraft.core.arcade.room.map.Map;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RoomCustom {

    private final UUID author;

    private List<Map> maps, playedMaps;

    private int teamSize;
    private int forgeSpeed; // Velocidade da forja: 1 = normal, 2 = 2x, 3 = 3x, 4 = 4x, 5 = 5x
    private ShopConfig shopConfig; // Configurações da loja

    public RoomCustom(UUID author, List<Map> maps, int teamSize) {
        this.author = author;

        this.maps = maps;
        this.playedMaps = new ArrayList<>();

        this.teamSize = teamSize;
        this.forgeSpeed = 1; // Velocidade padrão normal
        this.shopConfig = new ShopConfig(); // Inicializar configurações da loja
    }

    public boolean isAuthor(UUID author) {
        return this.author.equals(author);
    }

    public boolean isMapAlreadyPlayed(Map map) {
        return playedMaps.contains(map);
    }

    public boolean isMapSelected(Map map) {
        return maps.contains(map);
    }
}
