# 📏 Sistema de Escala de NPCs - 1.5x Padrão

## ✅ Todos os NPCs têm 1.5x de tamanho

Todos os NPCs do servidor SavageMC são **1.5x maiores** que um jogador normal (50% maiores).

## 🎯 Configuração

### Padrão Global
O sistema está configurado para usar **1.5x** como escala padrão em todos os lugares:

1. **NpcDefinition.java**
   ```java
   @Builder.Default
   private final float scale = 1.5f;  // ← PADRÃO
   ```

2. **LobbyNpc.java**
   ```java
   private static final float DEFAULT_SCALE = 1.5f;  // ← PADRÃO
   
   public LobbyNpc(NpcDefinition definition, Location base) {
       this(definition, base, definition.getScale());  // ← USA ESCALA DA DEFINIÇÃO
   }
   ```

3. **npcs.yml**
   ```yaml
   scale: 1.5  # ← Todos os NPCs explicitamente
   ```

4. **HardcodedNpcs.java**
   ```java
   .scale(1.5f)  // ← NPCs hardcoded
   ```

## 📊 Comparação Visual

```
Jogador Normal:  [████]  1.0x (1.8 blocos de altura)
NPC Padrão:      [██████]  1.5x (2.7 blocos de altura)
```

## 🔧 O que é Escalado

Quando um NPC tem escala 1.5x, **TODOS** os componentes são escalados:

### 1. Cabeça (ItemDisplay)
- Tamanho visual: 1.5x
- Transformação aplicada via `Transformation`

### 2. Hitbox (Interaction)
- Largura: `0.9 * 1.5 = 1.35` blocos
- Altura: `2.0 * 1.5 = 3.0` blocos

### 3. Posição da Cabeça
- Altura: `1.35 * 1.5 = 2.025` blocos

### 4. Holograma
- Posição inicial: `2.05 * 1.5 = 3.075` blocos
- Espaçamento entre linhas: 0.28 blocos (constante)

## 📝 NPCs Configurados

### YAML (npcs.yml)
Todos os NPCs no arquivo têm explicitamente `scale: 1.5`:

- ✅ **npc_bw** (BedWars) - 1.5x
- ✅ **npc_duels** (Duels) - 1.5x
- ✅ **npc_ew** (EggWars) - 1.5x
- ✅ **npc_sw** (SkyWars) - 1.5x
- ✅ **npc_tb** (The Bridge) - 1.5x
- ✅ **npc_anarquia** (Semi Anarquia) - 1.5x

### Hardcoded (Java)
NPCs criados em código:

- ✅ **npc_semi_anarquia** - 1.5x
  - Localização: -5, -60, 3
  - Skin: desenharWASD
  - Ação: Conecta a "semi-anarquia"

## 🔄 Fluxo de Escala

```
1. Definição criada (NpcDefinition)
   └─> scale = 1.5f (padrão ou especificado)

2. NPC spawnado (LobbyNpc)
   └─> Usa definition.getScale()
   └─> Calcula todas as proporções

3. Componentes criados
   ├─> Interaction: hitbox escalada
   ├─> ItemDisplay: cabeça transformada
   └─> TextDisplay: holograma posicionado
```

## 💡 Como Funciona

### Construtor Simplificado
```java
// Automaticamente usa scale da definição (1.5x)
LobbyNpc npc = new LobbyNpc(definition, location);
```

### Construtor com Override
```java
// Override manual da escala (raramente necessário)
LobbyNpc npc = new LobbyNpc(definition, location, 2.0f);
```

### Builder Pattern
```java
NpcDefinition definition = NpcDefinition.builder()
    .id("npc_exemplo")
    .skinPlayer("Notch")
    .scale(1.5f)  // ← Se omitido, usa 1.5f por padrão
    .build();
```

## 🎨 Por que 1.5x?

### Vantagens
1. **Visibilidade** - NPCs se destacam claramente dos jogadores
2. **Imersão** - Mantém proporção razoável (não exagerado)
3. **Clareza** - Fácil identificar NPCs à distância
4. **Padrão da indústria** - Muitos servidores populares usam escala similar

### Alternativas
- `1.0x` - Tamanho normal (jogador comum)
- `1.2x` - Discretamente maior
- `1.8x` - Muito grande
- `2.0x` - Dobro do tamanho (muito chamativo)

## 📐 Cálculos Técnicos

### Jogador Normal (1.0x)
```
Altura: 1.8 blocos
Largura: 0.6 blocos
Área de clique: 0.9 x 2.0 blocos
```

### NPC 1.5x
```
Altura visual: 2.7 blocos (1.8 * 1.5)
Largura visual: 0.9 blocos (0.6 * 1.5)
Área de clique: 1.35 x 3.0 blocos (0.9 * 1.5, 2.0 * 1.5)
```

## ✨ Consistência

**TODO** o sistema usa 1.5x como padrão:
- ✅ NPCs do YAML
- ✅ NPCs hardcoded
- ✅ NPCs spawnados dinamicamente
- ✅ NPCs de teste (admin)

Não há **NENHUM** NPC com tamanho normal (1.0x) a menos que explicitamente configurado.

## 🛠️ Modificação

Para alterar a escala de um NPC específico:

### YAML
```yaml
npc_especial:
  scale: 2.0  # ← Mude aqui
```

### Hardcoded
```java
.scale(2.0f)  // ← Mude aqui
```

### Global (todos os NPCs)
```java
// NpcDefinition.java
@Builder.Default
private final float scale = 2.0f;  // ← Mude o padrão aqui
```

## 📊 Resumo

| Componente | Escala | Status |
|------------|--------|--------|
| Padrão do sistema | 1.5x | ✅ |
| NPCs YAML | 1.5x | ✅ |
| NPCs Hardcoded | 1.5x | ✅ |
| Semi Anarquia | 1.5x | ✅ |
| Todos os outros | 1.5x | ✅ |

**Resultado:** 100% dos NPCs têm 1.5x de tamanho! 🎉
