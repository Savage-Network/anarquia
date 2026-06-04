package com.minecraft.core.api.collectible;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

@Getter
@AllArgsConstructor
public enum CollectibleCategory {

    /* Aparência */
    HAT("Chapéus", "Aparência", asList("§7Selecione um chapéu para", "§7andar com estilo.")),
    EMOTION("Emoções", "Aparência", asList("§7Selecione uma emoção para", "§7exibir diversas reações.")),
    BANNER("Bandeiras", "Aparência", asList("§7Selecione uma bandeira para", "§7mostrar suas diversidades.")),
    CAPES("Capas", "Aparência", asList("§7Selecione uma capa para", "§7mostrar suas exclusividades.")),
    CLOTHING("Trajes", "Aparência", asList("§7Vista trajes coloridos nos", "§7lobbies sem perder seu visual.")),
    GADGET("Acessórios", "Aparência", asList("§7Item decorativo no inventário", "§7apenas visual no lobby.")),

    /* Efeitos */
    PARTICLE("Partículas", "Efeitos", asList("§7Selecione uma partícula", "§7para se destacar.")),
    AURA("Auras", "Efeitos", asList("§7Imagens em partículas acima da cabeça", "§7(Imgur) ou auras clássicas.")),
    TRAIL("Rastros", "Efeitos", asList("§7Selecione um rastro para", "§7deixar sua marca por onde passar.")),
    MUTATION("Mutações", "Efeitos", asList("§7Efeitos especiais no corpo:", "§7brilho, fogo, fumaça e mais.")),
    KILL_EFFECT("Efeitos de Abate", "Efeitos", asList("§7Partículas ao eliminar", "§7jogadores no lobby.")),
    FOOTPRINT("Pegadas", "Efeitos", asList("§7Partículas deixadas nos pés", "§7ao caminhar pelo lobby.")),
    JUMP_EFFECT("Efeitos de Pulo", "Efeitos", asList("§7Explosão de partículas", "§7sempre que você pular.")),
    DEATH_EFFECT("Efeitos de Morte", "Efeitos", asList("§7Partículas quando você morre", "§7no lobby.")),

    /* Companheiros */
    BALLOON("Balões", "Companheiros", asList("§7Selecione um balão para", "§7mostrar seus exclusivos.")),
    PET("Pets", "Companheiros", asList("§7Bichinhos que seguem você", "§7pelos lobbies.")),
    COMPANION("Companheiros", "Companheiros", asList("§7Selecione um companheiro", "§7para andar com você.")),
    WINGS("Asas", "Companheiros", asList("§7Asas decorativas nas costas", "§7(como balões especiais).")),

    /* Lobby */
    ARTIFACT("Artefatos", "Lobby", asList("§7Selecione um artefato", "§7para se divertir nos lobbies.")),

    /* Social */
    TITLE("Títulos", "Social", asList("§7Selecione um título", "§7para esbanjar exclusividade.")),
    JOIN_MESSAGE("Mensagens de Entrada", "Social", asList("§7Selecione uma mensagem de entrada", "§7para personalizar sua chegada."));

    private final String name;
    private final String group;
    private final List<String> lore;

    public static CollectibleCategory of(String name) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(name) || category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static List<CollectibleCategory> byGroup(String group) {
        List<CollectibleCategory> list = new ArrayList<>();
        for (CollectibleCategory c : values()) {
            if (c.group.equalsIgnoreCase(group))
                list.add(c);
        }
        return list;
    }

    public static List<String> groups() {
        return Arrays.asList("Aparência", "Efeitos", "Companheiros", "Lobby", "Social");
    }

    public boolean isRespawnRequiredInWorldChange() {
        switch (this) {
            case BALLOON:
            case COMPANION:
            case PET:
            case WINGS:
            case ARTIFACT:
                return true;
            default:
                return false;
        }
    }
}
