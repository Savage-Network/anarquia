# Sistema de NPCs - SavageMC

Sistema completo de NPCs para lobbies usando Display Entities do Minecraft 1.21+.

## Características

- **NPCs 1.5x maiores** que jogadores normais por padrão
- **Escala customizável** por NPC (1.0 = normal, 1.5 = 50% maior, 2.0 = dobro do tamanho)
- **Skins customizáveis** (player name ou texture base64)
- **Hologramas** com múltiplas linhas
- **Sistema de clique** com cooldown
- **Ações configuráveis** (mensagem, comando, servidor Bungee)

## Tecnologia

O sistema usa as novas Display Entities do Minecraft 1.21:
- **Interaction**: Área clicável do NPC
- **ItemDisplay**: Cabeça do NPC (skin)
- **TextDisplay**: Linhas do holograma

## Configuração

### npcs.yml

```yaml
enabled: true
click-cooldown-ms: 500

definitions:
  npc_exemplo:
    location: npc_exemplo        # Chave no architect.json
    display-name: Exemplo        # Nome de exibição
    scale: 1.5                   # Tamanho do NPC (padrão: 1.5x)
    hologram:
      - "&b&lMeu NPC"
      - "&7Clique aqui!"
    skin:
      player: Notch              # Nome do jogador (Mojang)
      # OU texture base64:
      # texture: "ewogICJ0aW1lc3RhbXA..."
      # signature: "..."
    action:
      type: BUNGEE               # MESSAGE, COMMAND, BUNGEE
      value: lobby-bedwars
```

### Tipos de Ação

- **MESSAGE**: Envia mensagem para o jogador
- **COMMAND**: Executa comando como o jogador
- **BUNGEE**: Conecta o jogador a outro servidor

### Escalas Sugeridas

- `1.0` - Tamanho normal de jogador
- `1.5` - **Padrão** - 50% maior, destaque visual
- `2.0` - Dobro do tamanho, muito chamativo
- `0.75` - 25% menor, NPC discreto

## Arquitetura

### Classes Principais

1. **LobbyNpc**: Entidade NPC com todos os componentes
   - Spawna Interaction (zona de clique)
   - Spawna ItemDisplay (cabeça escalada)
   - Spawna TextDisplays (holograma)

2. **NpcDefinition**: Configuração estática do NPC
   - Carregada de `npcs.yml`
   - Contém skin, holograma, ação, escala

3. **NpcManager**: Gerenciador global
   - Carrega definições do YAML
   - Spawna NPCs baseado no Architect ativo
   - Cleanup de entidades órfãs

4. **NpcActionType**: Enum de ações disponíveis

5. **SkullProfiles**: Utilitário para criar cabeças customizadas

### Fluxo de Spawn

```
LobbyHostPlugin.onEnable()
  └─> NpcManager.load(plugin)           # Carrega npcs.yml
  └─> ArchitectLoader.load()
      └─> Architect.join(player)
          └─> NpcManager.spawnForArchitect(architect)
              └─> new LobbyNpc(def, location, scale)
                  ├─> spawn Interaction (hitbox escalada)
                  ├─> spawn ItemDisplay (cabeça 1.5x)
                  └─> spawn TextDisplays (holograma)
```

## Sistema de Escala

### Cálculos de Escala

```java
// Altura da cabeça
double headHeight = 1.35 * scale;  // 1.35 → 2.025 para scale=1.5

// Hitbox de interação
float width = 0.9f * scale;        // 0.9 → 1.35 para scale=1.5
float height = 2.0f * scale;       // 2.0 → 3.0 para scale=1.5

// Transformação da cabeça
Vector3f newScale = new Vector3f(scale, scale, scale);
headDisplay.setTransformation(...);

// Posição do holograma
double startY = 2.05 * scale;      // 2.05 → 3.075 para scale=1.5
```

### Proporções

Todos os elementos são escalados proporcionalmente:
- Altura da cabeça
- Largura e altura da hitbox
- Posição do holograma
- Tamanho visual da cabeça

## Exemplo de Uso

### NPC Gigante (2x)

```yaml
npc_boss:
  location: spawn_boss
  display-name: Boss NPC
  scale: 2.0
  hologram:
    - "&4&lBOSS FIGHT"
    - "&c&lClique para desafiar!"
  skin:
    player: Herobrine
  action:
    type: COMMAND
    value: boss challenge
```

### NPC Pequeno (0.75x)

```yaml
npc_info:
  location: spawn_info
  display-name: Informações
  scale: 0.75
  hologram:
    - "&7Informações"
  skin:
    player: _jeb
  action:
    type: MESSAGE
    value: "&aBem-vindo ao servidor!"
```

## Eventos de Clique

O sistema detecta cliques através do `InteractionEvent`:

```java
@EventHandler
public void onNpcClick(PlayerInteractEntityEvent event) {
    if (!(event.getRightClicked() instanceof Interaction)) return;
    
    Interaction interaction = (Interaction) event.getRightClicked();
    String npcId = interaction.getPersistentDataContainer()
            .get(LobbyNpc.NPC_ID_KEY, PersistentDataType.STRING);
    
    // Executa ação do NPC
    NpcClickHandler.handle(event.getPlayer(), npcId);
}
```

## Limpeza e Persistência

- NPCs **não são persistentes** (não salvam no chunk)
- Respawnam automaticamente ao recarregar o Architect
- Cleanup automático de entidades órfãs ao desligar
- Remoção completa ao trocar de Architect

## Performance

- Cada NPC usa 3-5+ entidades (Interaction + ItemDisplay + TextDisplays)
- Sistema otimizado para até 50 NPCs simultâneos
- Cooldown de clique previne spam (padrão: 500ms)

## Troubleshooting

### NPC não aparece
1. Verifique se `location` existe no `architect.json`
2. Confirme que o mundo está carregado
3. Veja logs: `[NpcManager] Location 'xyz' ausente...`

### Cabeça sem skin
1. Verifique nome do jogador (precisa existir na Mojang)
2. Para texture base64, use formato correto
3. Teste com `player: Steve` primeiro

### Clique não funciona
1. Verifique cooldown em `npcs.yml`
2. Confirme que `NpcClickHandler` está registrado
3. Debug: `/minecraft:debug start`

## Referências

- [Display Entities (1.19.4+)](https://minecraft.wiki/w/Display)
- [Interaction Entity (1.19.4+)](https://minecraft.wiki/w/Interaction)
- [Player Heads](https://minecraft-heads.com/)
