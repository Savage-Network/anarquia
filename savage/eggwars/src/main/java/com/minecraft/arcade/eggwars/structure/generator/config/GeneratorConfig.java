package com.minecraft.arcade.eggwars.structure.generator.config;

import com.minecraft.arcade.eggwars.structure.generator.objects.EggOre;
import com.minecraft.arcade.eggwars.structure.generator.objects.level.GeneratorLevel;
import lombok.Getter;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração centralizada para todos os geradores de minérios.
 * 
 * Para modificar os valores, edite os métodos correspondentes abaixo.
 */
@Getter
public class GeneratorConfig {

    private static final GeneratorConfig instance = new GeneratorConfig();

    public static GeneratorConfig getInstance() {
        return instance;
    }

    private GeneratorConfig() {
        initializeConfigs();
    }

    // ==================== CONFIGURAÇÕES POR TIPO DE GERADOR ====================

    /**
     * Retorna o tempo de geração (em segundos) para um nível específico de um gerador.
     * 
     * @param ore Tipo de minério (IRON, GOLD, DIAMOND)
     * @param level Nível do gerador (ONE, TWO, THREE)
     * @param isMiddleGenerator Se é gerador do meio (true) ou da ilha (false)
     * @return Tempo em segundos
     */
    public int getGenerationTime(EggOre ore, GeneratorLevel level, boolean isMiddleGenerator) {
        return switch (ore) {
            case IRON -> getIronGenerationTime(level);
            case GOLD -> getGoldGenerationTime(level, isMiddleGenerator);
            case DIAMOND -> getDiamondGenerationTime(level, isMiddleGenerator);
        };
    }

    /**
     * Retorna o custo de upgrade para o próximo nível.
     * 
     * @param ore Tipo de minério
     * @param nextLevel Próximo nível (ONE para reparar, TWO/THREE para evoluir)
     * @return Quantidade de minérios necessários
     */
    public int getUpgradeCost(EggOre ore, GeneratorLevel nextLevel) {
        return switch (ore) {
            case IRON -> getIronUpgradeCost(nextLevel);
            case GOLD -> getGoldUpgradeCost(nextLevel);
            case DIAMOND -> getDiamondUpgradeCost(nextLevel);
        };
    }

    /**
     * Retorna o material necessário para upgrade.
     */
    public Material getUpgradeMaterial(EggOre ore) {
        return switch (ore) {
            case IRON -> Material.IRON_INGOT;
            case GOLD -> Material.GOLD_INGOT;
            case DIAMOND -> Material.DIAMOND;
        };
    }

    /**
     * Retorna o nível máximo que um gerador pode alcançar.
     */
    public int getMaxLevel(EggOre ore) {
        return switch (ore) {
            case IRON -> 3; // Níveis 1, 2 e 3
            case GOLD -> 3; // Níveis 1, 2 e 3
            case DIAMOND -> 3; // Níveis 1, 2 e 3
        };
    }

    /**
     * Retorna o tamanho máximo de stack de itens no chão.
     * Modifique aqui para alterar a quantidade máxima de minérios que podem ficar no chão.
     */
    public int getMaxStackSize(EggOre ore) {
        return switch (ore) {
            case IRON -> 32;
            case GOLD -> 32;
            case DIAMOND -> 32;
        };
    }

    /**
     * Retorna se o gerador começa quebrado (apenas para ouro e diamante).
     */
    public boolean startsBroken(EggOre ore, boolean isMiddleGenerator) {
        return switch (ore) {
            case IRON -> false; // Ferro nunca começa quebrado
            case GOLD -> !isMiddleGenerator; // Ouro da ilha começa quebrado
            case DIAMOND -> isMiddleGenerator; // Diamante do meio começa quebrado
        };
    }

    // ==================== CONFIGURAÇÕES ESPECÍFICAS ====================

    /**
     * Tempos de geração do GERADOR DE FERRO (em segundos)
     * Modifique aqui para alterar os tempos do gerador de ferro.
     */
    private int getIronGenerationTime(GeneratorLevel level) {
        return switch (level) {
            case ONE -> 6;   // Nível 1: 6 segundos
            case TWO -> 4;   // Nível 2: 4 segundos
            case THREE -> 3; // Nível 3: 3 segundos
            default -> 6;
        };
    }

    /**
     * Custos de upgrade do GERADOR DE FERRO
     * Modifique aqui para alterar os custos do gerador de ferro.
     */
    private int getIronUpgradeCost(GeneratorLevel nextLevel) {
        return switch (nextLevel) {
            case TWO -> 20;   // Nível 1 -> 2: 20 ferros
            case THREE -> 48;  // Nível 2 -> 3: 48 ferros
            default -> 0;
        };
    }

    /**
     * Tempos de geração do GERADOR DE OURO (em segundos)
     * Modifique aqui para alterar os tempos do gerador de ouro.
     * 
     * @param isMiddleGenerator true = gerador do meio, false = gerador da ilha
     */
    private int getGoldGenerationTime(GeneratorLevel level, boolean isMiddleGenerator) {
        if (isMiddleGenerator) {
            // Gerador do MEIO
            return switch (level) {
                case ONE -> 8;   // Nível 1: 8 segundos
                case TWO -> 7;   // Nível 2: 7 segundos
                case THREE -> 6; // Nível 3: 6 segundos
                default -> 8;
            };
        } else {
            // Gerador da ILHA
            return switch (level) {
                case ONE -> 10;  // Nível 1: 10 segundos
                case TWO -> 7;   // Nível 2: 7 segundos
                case THREE -> 7; // Nível 3: 7 segundos
                default -> 10;
            };
        }
    }

    /**
     * Custos de upgrade do GERADOR DE OURO
     * Modifique aqui para alterar os custos do gerador de ouro.
     */
    private int getGoldUpgradeCost(GeneratorLevel nextLevel) {
        return switch (nextLevel) {
            case ONE -> 4;   // Reparar (NONE -> ONE): 4 diamantes
            case TWO -> 12;  // Nível 1 -> 2: 12 ouros
            case THREE -> 24; // Nível 2 -> 3: 24 ouros
            default -> 0;
        };
    }

    /**
     * Tempos de geração do GERADOR DE DIAMANTE (em segundos)
     * Modifique aqui para alterar os tempos do gerador de diamante.
     * 
     * @param isMiddleGenerator true = gerador do meio, false = gerador da ilha
     */
    private int getDiamondGenerationTime(GeneratorLevel level, boolean isMiddleGenerator) {
        if (isMiddleGenerator) {
            // Gerador do MEIO
            return switch (level) {
                case ONE -> 8;   // Nível 1: 8 segundos
                case TWO -> 7;   // Nível 2: 7 segundos
                case THREE -> 6; // Nível 3: 6 segundos
                default -> 8;
            };
        } else {
            // Gerador da ILHA
            return switch (level) {
                case ONE -> 10;  // Nível 1: 10 segundos
                case TWO -> 7;   // Nível 2: 7 segundos
                case THREE -> 7; // Nível 3: 7 segundos
                default -> 10;
            };
        }
    }

    /**
     * Custos de upgrade do GERADOR DE DIAMANTE
     * Modifique aqui para alterar os custos do gerador de diamante.
     */
    private int getDiamondUpgradeCost(GeneratorLevel nextLevel) {
        return switch (nextLevel) {
            case ONE -> 4;   // Reparar (NONE -> ONE): 4 diamantes
            case TWO -> 15;  // Nível 1 -> 2: 15 diamantes
            case THREE -> 30; // Nível 2 -> 3: 30 diamantes
            default -> 0;
        };
    }

    // ==================== INICIALIZAÇÃO ====================

    private void initializeConfigs() {
        // Configurações podem ser carregadas de arquivo aqui no futuro
    }
}

