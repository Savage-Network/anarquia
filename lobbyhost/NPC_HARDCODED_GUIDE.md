# Guia de NPCs Hardcoded (Código Java)

## 📋 Visão Geral

NPCs criados diretamente no código Java, **sem dependência de arquivos YAML ou JSON**.

## 🎯 NPC Semi Anarquia

### Especificações
- **Localização**: -5, -60, 3 (mundo "world")
- **Skin**: desenharWASD
- **Tamanho**: 1.5x (50% maior que jogador normal)
- **Holograma**:
  - `&4&lSEMI ANARQUIA`
  - `&c&lSobreviva se puder!`
  - `&7Clique para entrar`
- **Ação**: Conecta ao servidor BungeeCord "semi-anarquia"

## 📝 Arquivo Principal

**`HardcodedNpcs.java`**

Este arquivo contém todos os NPCs criados programaticamente.

### Método Principal

```java
public static void spawnAll() {
    // Spawna todos os NPCs hardcoded
}
```

### NPC Semi Anarquia

Para adicionar um novo NPC com item customizado, crie um método:

```java
private static void spawnSemiAnarchyNpc(World world) {
    // Coordenadas onde o NPC vai aparecer
    Location location = new Location(world, -5.5, -60, 3.5, 0, 0);

    // Definição do NPC com PICARETA
    NpcDefinition definition = NpcDefinition.builder()
            .id("npc_semi_anarquia")
            .locationKey("hardcoded_semi_anarquia")
            .displayName("Semi Anarquia")
            .hologramLines(List.of(
                    "&4&lSEMI ANARQUIA",
                    "&c&lSobreviva se puder!",
                    "&7Clique para entrar"
            ))
            .customItem("DIAMOND_PICKAXE")  // ← PICARETA!
            .actionType(NpcActionType.BUNGEE)
            .actionValue("semi-anarquia")
            .scale(1.5f)
            .build();

    // Spawn do NPC
    try {
        LobbyNpc npc = new LobbyNpc(definition, location);
        spawnedNpcs.add(npc);
    } catch (Exception e) {
        Core.getLogger().warning("Erro ao spawnar NPC: " + e.getMessage());
    }
}
```

### Adicionando Novos NPCs

Para adicionar um novo NPC, crie um método privado:

```java
private static void spawnMeuNpc(World world) {
    // Coordenadas onde o NPC vai aparecer
    Location location = new Location(world, x, y, z, yaw, pitch);

    // Definição do NPC
    NpcDefinition definition = NpcDefinition.builder()
            .id("npc_meu_npc")
            .locationKey("hardcoded_meu_npc")
            .displayName("Meu NPC")
            .hologramLines(List.of(
                    "&e&lTÍTULO",
                    "&7Subtítulo"
            ))
            .skinPlayer("NomeDoJogador")  // Ou use .skinTexture() para base64
            .actionType(NpcActionType.BUNGEE)  // MESSAGE, COMMAND, BUNGEE, NONE
            .actionValue("nome-servidor")
            .scale(1.5f)  // Tamanho: 1.0 = normal, 1.5 = 50% maior
            .build();

    // Spawn do NPC
    try {
        LobbyNpc npc = new LobbyNpc(definition, location, 1.5f);
        spawnedNpcs.add(npc);
    } catch (Exception e) {
        Core.getLogger().warning("Erro ao spawnar NPC: " + e.getMessage());
    }
}
```

Depois adicione a chamada em `spawnAll()`:

```java
public static void spawnAll() {
    removeAll();
    
    World world = Bukkit.getWorld("world");
    if (world == null) return;

    spawnSemiAnarchyNpc(world);
    spawnMeuNpc(world);  // <-- Adicione aqui
    
    // ...
}
```

## 🔄 Ciclo de Vida

### Carregamento
Os NPCs são spawnados automaticamente quando o plugin inicia:

```
LobbyHostPlugin.onEnable()
  └─> HardcodedNpcs.spawnAll()
      └─> spawnSemiAnarchyNpc(world)
          └─> new LobbyNpc(definition, location, scale)
```

### Descarregamento
Os NPCs são removidos quando o plugin desliga:

```
LobbyHostPlugin.onDisable()
  └─> HardcodedNpcs.removeAll()
      └─> npc.remove() (para cada NPC)
```

## ⚙️ Configuração

### Tipos de Ação

#### MESSAGE
Envia mensagem para o jogador:
```java
.actionType(NpcActionType.MESSAGE)
.actionValue("&aOlá! Bem-vindo ao servidor!")
```

#### COMMAND
Executa comando como o jogador:
```java
.actionType(NpcActionType.COMMAND)
.actionValue("menu")  // Executa /menu
```

#### BUNGEE
Conecta a outro servidor:
```java
.actionType(NpcActionType.BUNGEE)
.actionValue("semi-anarquia")  // Nome do servidor no BungeeCord
```

#### NONE
Apenas visual, sem ação:
```java
.actionType(NpcActionType.NONE)
```

### Coordenadas

As coordenadas devem incluir `.5` para centralizar:
```java
Location location = new Location(world, -5.5, -60, 3.5, 0, 0);
//                                   ↑    ↑    ↑   ↑  ↑
//                                   x    y    z  yaw pitch
```

### Escala (Tamanho)

- `0.5f` - Metade do tamanho
- `1.0f` - Tamanho normal
- `1.5f` - **Padrão** - 50% maior
- `2.0f` - Dobro do tamanho
- `2.5f` - 150% maior

### Skins

#### Skin de Jogador (Mojang)
```java
.skinPlayer("desenharWASD")
```

#### Skin Customizada (Base64)
```java
.skinTexture("ewogICJ0aW1lc3RhbXA...")
.skinSignature("opcional")
```

#### Skins Especiais do Minecraft
```java
.skinPlayer("MHF_Question")     // Ponto de interrogação
.skinPlayer("MHF_Exclamation")  // Ponto de exclamação
.skinPlayer("MHF_ArrowUp")      // Seta para cima
.skinPlayer("MHF_Chest")        // Baú
.skinPlayer("Herobrine")        // Herobrine
```

## 🎨 Holograma

Configuração de múltiplas linhas acima do NPC:

```java
.hologramLines(List.of(
        "&4&lLINHA 1",      // Topo
        "&c&lLINHA 2",
        "&7LINHA 3"         // Mais próxima da cabeça
))
```

Códigos de cor:
- `&0` - Preto
- `&1` - Azul escuro
- `&2` - Verde escuro
- `&3` - Ciano escuro
- `&4` - Vermelho escuro
- `&5` - Roxo
- `&6` - Laranja
- `&7` - Cinza
- `&8` - Cinza escuro
- `&9` - Azul
- `&a` - Verde
- `&b` - Ciano
- `&c` - Vermelho
- `&d` - Rosa
- `&e` - Amarelo
- `&f` - Branco
- `&l` - Negrito
- `&o` - Itálico
- `&n` - Sublinhado

## 🔧 Comandos Úteis

### Spawn Manual (Admin)
Se você quiser spawnar um NPC manualmente em uma posição:

```java
// No código:
NpcManager.spawnAtPlayer(player, "npc_semi_anarquia");
```

### Remover NPCs
```java
HardcodedNpcs.removeAll();
```

### Re-spawnar
```java
HardcodedNpcs.removeAll();
HardcodedNpcs.spawnAll();
```

## 🐛 Troubleshooting

### NPC não aparece
1. Verifique se o mundo "world" existe
2. Confirme que as coordenadas são válidas
3. Veja os logs: `[HardcodedNpcs] ...`

### Skin não carrega
1. Verifique se o nome do jogador existe (Mojang)
2. Para skins customizadas, valide o base64
3. Teste com `skinPlayer("Steve")` primeiro

### Clique não funciona
1. Verifique se `NpcInteractionListener` está registrado
2. Confirme que o servidor BungeeCord existe
3. Debug no console

## 📚 Vantagens vs YAML

### Hardcoded (Código Java) ✅
- **Controle total** sobre posição e configuração
- **Tipagem forte** - erros em compile-time
- **Versionamento** com Git
- **Fácil refatoração** com IDE
- **Sem parsing** de arquivos

### YAML ❌
- Erros só aparecem em runtime
- Sem autocomplete
- Precisa recarregar arquivo
- Typos difíceis de detectar

## 🚀 Exemplo Completo

```java
private static void spawnEventoNpc(World world) {
    Location location = new Location(world, 0.5, 100, 0.5, 180, 0);

    NpcDefinition definition = NpcDefinition.builder()
            .id("npc_evento_especial")
            .locationKey("hardcoded_evento")
            .displayName("Evento Especial")
            .hologramLines(List.of(
                    "&6&l★ EVENTO ESPECIAL ★",
                    "&e&lDOBRO DE XP",
                    "&aDisponível agora!",
                    "&7Clique para participar"
            ))
            .skinPlayer("Herobrine")
            .actionType(NpcActionType.COMMAND)
            .actionValue("evento participar")
            .scale(2.0f)  // NPC gigante!
            .build();

    try {
        LobbyNpc npc = new LobbyNpc(definition, location, 2.0f);
        spawnedNpcs.add(npc);
        Core.getLogger().info("[HardcodedNpcs] Evento NPC spawnado!");
    } catch (Exception e) {
        Core.getLogger().warning("Erro: " + e.getMessage());
    }
}
```

## 📌 Notas Importantes

1. **Mundo carregado**: O mundo precisa estar carregado antes de spawnar NPCs
2. **Persistência**: NPCs não são salvos no mundo, respawnam ao iniciar
3. **Cooldown**: Sistema tem cooldown de 500ms entre cliques
4. **BungeeCord**: Certifique-se que o canal está registrado
5. **Escala**: Ajusta automaticamente hitbox, cabeça e holograma

## 🔗 Arquivos Relacionados

- `HardcodedNpcs.java` - NPCs programáticos
- `LobbyNpc.java` - Entidade NPC
- `NpcDefinition.java` - Definição/configuração
- `NpcManager.java` - Gerenciador global
- `NpcInteractionListener.java` - Sistema de clique
- `LobbyHostPlugin.java` - Integração com plugin
