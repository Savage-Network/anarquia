# Prompt — Sistema de Cosméticos Savage (referência completa)

Documento de especificação para recriar **todo o sistema de cosméticos do Savage** dentro do **plugin único `game.jar`** (Paper 1.21+, servidor semi-anarquia). **Não** é um plugin separado — pacotes `api/collectible/` + `bukkit/menu/collectible/` + `bukkit/display/aura/` no mesmo JAR descrito em `PROMPT-SEMIANARQUIA-1.21-COMPLETO.md`.

No **lobby**, cosméticos equivalentes ficam no plugin **`lobbyhost.jar`** (`name: lobbyhost`).

Inclui categorias, catálogo, menus, persistência, conflitos, **auras por imagem (Imgur)** e **títulos animados com letra de música**.

---

## 1. Objetivo

Construir o subsistema de cosméticos **dentro do plugin `game`** (1 JAR, `name: game`):

- **21 categorias** agrupadas em 5 hubs de menu
- **1 cosmético ativo por categoria** (salvo exceções documentadas)
- **Operators** que aplicam/removem efeitos em runtime
- **Persistência por jogador** (desbloqueios + ativos + URL de aura custom)
- **Menus GUI** paginados com filtros e estatísticas de desbloqueio
- **Títulos** exibidos acima do nick, incluindo **animação frame-a-frame** com trechos de letras
- **Auras de imagem**: PNG do servidor ou link **Imgur** renderizado em partículas coloridas acima da cabeça

Idioma da UI: **português brasileiro**.

---

## 2. Stack sugerida (1.21)

| Item | Escolha |
|------|---------|
| Servidor | Paper 1.21+ |
| Java | 21 |
| Partículas | `Particle.DUST` + `DustOptions` (sem NMS) |
| Companheiros | Display entities / ArmorStand |
| Persistência | SQLite (MySQL opcional) |
| Config | YAML por categoria + `config.yml` |
| Permissões | LuckPerms / Vault |

---

## 3. Modelo de dados

### Cosmético (`Cosmetic`)

```yaml
id: "title:mythical:777-666"          # categoria:rarity:nome
category: TITLE
display_name: "777-666"
rarity: MYTHICAL                        # COMUM | RARE | EPIC | MYTHICAL
permission: "cosmetics.title.777_666"
required_ranks: [OWNER]                 # ou vazio = desbloqueio manual
icon: PAPER
enabled: true
data: {}                                # específico da categoria (ver seções)
```

### Perfil do jogador (`PlayerCosmeticProfile`)

```yaml
uuid: ...
unlocked: ["pet:comum:cachorro", ...]
active:
  TITLE: "title:mythical:777-666"
  AURA: "aura:epic:personalizada"
  PET: null
custom_aura_url: "https://i.imgur.com/abc.png"
custom_aura_name: "Personalizada"
favorites: []
```

### Identifier

Formato: `{categoria}:{raridade}:{nome}` em minúsculas (ex.: `trail:epic:chamas`).

---

## 4. Arquitetura — Operator Pattern

Cada categoria implementa:

```java
interface CosmeticOperator {
  void handle(Player player, Cosmetic cosmetic);
  void cancel(Player player);
}
```

Fluxo ao equipar:

1. Validar desbloqueio + permissão
2. `unloadConflicts(categoria)` — remove categorias conflitantes
3. `unload(categoria)` — cancela operator anterior
4. Criar novo operator + salvar em `active`
5. Reaplicar no `PlayerJoinEvent` e troca de mundo (se `requiresRespawnOnWorldChange`)

---

## 5. Grupos do menu principal

| Grupo | Categorias |
|-------|------------|
| **Aparência** | HAT, EMOTION, BANNER, CAPES, CLOTHING, GADGET |
| **Efeitos** | PARTICLE, AURA, TRAIL, MUTATION, KILL_EFFECT, FOOTPRINT, JUMP_EFFECT, DEATH_EFFECT |
| **Companheiros** | BALLOON, PET, COMPANION, WINGS |
| **Lobby** | ARTIFACT |
| **Social** | TITLE, JOIN_MESSAGE |

Comando: `/cosmetics` ou `/cosmeticos` → hub com 5 ícones → `CollectibleGroupMenu` → seletor paginado.

---

## 6. Conflitos entre categorias

```yaml
conflicts:
  BALLOON: [PET, WINGS, COMPANION]
  PET: [BALLOON, WINGS, COMPANION]
  WINGS: [BALLOON, PET, COMPANION]
  COMPANION: [BALLOON, PET, WINGS]
  HAT: [CLOTHING]          # se clothing incluir capacete
  CLOTHING: [HAT]
  GADGET: [ARTIFACT]
  ARTIFACT: [GADGET]
```

---

## 7. Raridades

| Enum | Nome PT | Cor chat |
|------|---------|----------|
| COMUM | Comum | §a |
| RARE | Raro | §e |
| EPIC | Épico | §b |
| MYTHICAL | Mítico | §c |

---

## 8. Catálogo completo por categoria

### 8.1 Aparência

#### HAT — Chapéus (140 itens, skull no slot capacete)

Carregados via enum `HatList`. Temas: animais, super-heróis, anime (Naruto, JoJo, One Piece, Death Note), Pokémon, Minecraft, natal, TMNT, etc.

<details>
<summary>Lista completa (140)</summary>

Cachorro, Jacaré, Zebra, Peixe, Urso, Macaco, Elefante, Pássaro, Leão, Cobra, Tartaruga, Tubarão, Carangueijo, Rottweiller, Bulldog, Coruja, Hipopótamo, Abelha, Porco, Javali, Pinto, Homem Aranha, Batman, Coringa, Arlequina, Arqueiro Verde, Mulher Maravilha, Zatanna, The Flash, Lanterna Verde, Hulk, Thor, Pantera Negra, Homem Formiga, Doutor Estranho, Watchman, Homem de Ferro, Capitão América, Thanos, Ultron, Loki, O Coisa, Naruto, Hinata, Kakashi, Majin Boo, Jotaro Kujo, Jonathan Joester, Dio Brando, Giorno Giovanna, Luffy, Roronoa Zoro, Deidara, Gamabunta, Sasori, Light Yagami, Misa Amane, Death Note, Kirito, Zetzu, Magikarp, Sableye, Charizard, Bruxa, Barba Ruiva, Jovem Pirata, Vendedor do Porto, Orc, Orc do Submundo, Rei Orc, Soldado Medieval, Cavaleiro de Lava, Guerreiro, Cavaleiro Dourado, Rei, Rainha, Sacerdote, Canibal, Carrasco, Guerreiro Viking, Viking, Troll, Múmia, Elfo, Minhoca, Fantasma, O Rei Zumbi, Pinguim, Wither, Guardião, Smurf, Megaman, Luigi, Mario, Robocop, Pato Donald, Link, Astronauta, Urso Polar, Abóbora, Tigre, Alto-Falante, Pokebola, Cubo Mágico, Bola de Futebol, Televisão, Galinha, Boneco de Neve, Planeta Terra, Bola de Basquete, Câmera, Esfera do Dragão, Ovelha, Boneco de Neve Natalino, Papai Noel, Mamãe Noel, Rudolf a Rena, Aldeão Árabe, Herobrine Ninja, Biscoito, Casa de Doces, Presente Vermelho/Laranja/Roxo/Rosa/Verde/Azul/Amarelo/Cinza, Elfa, Princesa Elfa, Sorvete, Venom, Michelangelo, Raphael, Leonardo, Donatello, Ender Dragon, Herobrine, Orquídea.

</details>

Submenu: `HatTypeSelectorMenu` → Chapéus | Banners | Emoções.

#### EMOTION — Emoções (15, cabeça animada)

| Nome | Raridade |
|------|----------|
| Apaixonado | Comum |
| Alegre | Raro |
| Triste | Épico |
| Brabo | Épico |
| Dando Língua | Épico |
| Legal | Épico |
| Sedutor | Épico |
| Abóbora Medonha | Comum |
| Abóbora Iluminada | Épico |
| Fantasma Apaixonado | Épico |
| RIP | Raro |
| Piscadela | Mítico |
| Relaxado | Mítico |
| Espantado | Mítico |
| TDFW | Mítico |

#### BANNER — Bandeiras (7, banner como chapéu)

Sky Block, Pug, Mojang, Coração, Halloween, Derp, Brasil.

#### CAPES — Capas (4, partículas de imagem nas costas)

| Nome | Recurso PNG | ratio |
|------|-------------|-------|
| Hope Wing | hope_wing | 0.10 |
| Harmony Wing | harmony_wing | 0.10 |
| Infernal Dark Wing | infernal_dark_wing | 0.10 |
| Rainbow Wing | rainbow_wing | 0.10 |

#### CLOTHING — Trajes (8, armadura de couro tingida)

Traje Rubi, Oceano, Floresta, Ouro, Savage, Roxo, Noturno, Rosa.

#### GADGET — Acessórios (7, slot hotbar 8)

Varinha Mágica, Rosa Dourada, Relógio de Bolso, Estrela Savage, Osso do Pet, Bússola VIP, Maçã Dourada.

---

### 8.2 Efeitos

#### PARTICLE — Partículas (4 tipos + sub-modos)

| Nome | Raridade | Efeito |
|------|----------|--------|
| Corações | Raro | HEART |
| Chamas | Épico | MOBSPAWNER_FLAMES |
| Fumaça | Comum | SMOKE |
| Magia | Épico | WITCH_MAGIC |

**Sub-modos** (`ParticleTypeSelectorMenu`): Simples, Espiral, Espiral Duplo, Circular. Apenas **1 partícula** ativa por vez.

#### AURA — Duas subfamílias

**A) Aura clássica** — anel de partículas no chão ao redor do jogador:

| Nome | Raridade | Efeito |
|------|----------|--------|
| Aura Corações | Raro | HEART |
| Aura Musical | Épico | NOTE |
| Aura Fada | Mítico | FLYING_GLYPH |
| Aura Estrelas | Épico | CRIT |
| Aura Nevasca | Raro | SNOWBALL_BREAK |

**B) Aura de imagem** — pixels coloridos ~2,35 blocos acima da cabeça, rotação com yaw:

| Nome | Tipo | Detalhe |
|------|------|---------|
| Yin-Yang | Preset | `resources/auras/yin_yang.png`, ratio 0.055 |
| Estrela | Preset | `resources/auras/estrela.png`, ratio 0.05 |
| Personalizada | Imgur | URL salva na conta; `/aura link <url>` |

Comandos aura:

```
/aura
/aura link <url>
/aura usar <nome>
/aura remover
```

Menu: `ImageAuraSelectorMenu` (presets + clássicas + link).

#### TRAIL — Rastros (5, ao caminhar)

| Nome | Raridade |
|------|----------|
| Lã Colorida | Raro |
| Chamas | Épico |
| Neve | Comum |
| Água | Raro |
| Notas Musicais | Épico |

#### MUTATION — Mutações (7, partículas constantes no corpo)

Brilho Místico, Chamas, Fumaça Verde, Energia do Portal, Energia Elétrica, Arco-íris, Pó de Fada.

#### KILL_EFFECT — Efeito de abate (6, ao matar no lobby)

Explosão, Raio, Sangue, Chamas, Fada, Abate Savage.

#### FOOTPRINT — Pegadas (6, partículas nos pés)

Pegadas Felizes, de Fogo, Musicais, do Portal, de Nuvem, Savage.

#### JUMP_EFFECT — Efeito de pulo (6, ao sair do chão)

Pulo das Nuvens, Explosivo, Apaixonado, Dimensional, Crítico, Savage.

#### DEATH_EFFECT — Efeito de morte (5)

Morte Sombria, Explosiva, Romântica, Elétrica, da Fada.

---

### 8.3 Companheiros

#### BALLOON — Balões (17)

WinRAR, Ondas, Rubi, Arco-Íris, Coelhinho, MC Donalds, Cubo Mágico, Lava, Gosmento, Oi!, Discoteca, Manopla de Ferro, Nuvens, Caixa Registradora, Cubo Colorido, Bolinhas, Alienígena.

#### PET — Pets (7, skull seguindo jogador)

Cachorro, Gato, Panda, Dragãozinho, Coelho, Coruja, Pet Savage.

#### WINGS — Asas (4)

Asas de Anjo, Asas Sombrias, Asas de Fogo, Asas de Gelo.

#### COMPANION — Companheiros (24 entidades)

Tartaruga, Pug, R2D2, Urso Polar, Perry o Ornitorrinco, Pinguim, Panda, Dragão Mítico, Seu filho, Dragão Mágico, Leão, Coala, Dragão de Gelo, Girafa, Gorila, Raposa, Dragão de Fogo, Pato, Dragão, Diglett, Chimpanzé, Caixa de Som, Abelha, BB-8.

Submenu companheiros: `PetTypeSelectorMenu` → Pets | Balões | Companheiros | Asas.

---

### 8.4 Lobby

#### ARTIFACT — Artefatos (8, item interativo slot 5)

Trampolim, Mob Gun, Fonte Dourada, Fantasmas, Foguete, Dracula, Fortuna de Cookies, Buraco Negro.

---

### 8.5 Social

#### JOIN_MESSAGE — Mensagens de entrada (22)

Formato no chat: `{tag}{nick} §6{mensagem}`.

| ID | Nome | Mensagem | Raridade msg |
|----|------|----------|--------------|
| default | Padrão | entrou no lobby! | Comum |
| connected | Conectado | se conectou! | Comum |
| arrived | Chegou | chegou chegando! | Comum |
| online | Online | está online! | Comum |
| joined | Entrou | entrou no servidor! | Comum |
| appeared | Apareceu | apareceu do nada! | Rara |
| spawned | Spawnou | spawnou no lobby! | Rara |
| teleported | Teleportou | se teleportou! | Rara |
| materialized | Materializou | se materializou! | Rara |
| descended | Desceu | desceu dos céus! | Épica |
| invaded | Invadiu | invadiu o servidor! | Épica |
| awakened | Despertou | despertou! | Épica |
| emerged | Emergiu | emergiu das sombras! | Épica |
| legendary | Lendário | fez uma entrada lendária! | Lendária |
| epic | Épico | fez uma entrada épica! | Lendária |
| boss | Boss | entrou como um boss! | Lendária |
| divine | Divino | desceu divinamente! | Lendária |
| mythical | Mítico | apareceu miticamente! | Mítica |
| godlike | Divindade | chegou como uma divindade! | Mítica |
| supreme | Supremo | fez uma entrada suprema! | Mítica |
| celestial | Celestial | desceu celestialmente! | Mítica |

---

## 9. Títulos — especificação completa

### 9.1 Comportamento geral

- Exibidos **acima do nick** (holograma / Display Text / TAB)
- **1 título ativo** por jogador
- Menu: `TitleCollectibleSelectorMenu` com **filtros por subgrupo**
- Raridade padrão dos títulos especiais: **Mítico**
- Rank exigido varia (OWNER, ADMIN, EMBER_PLUS, MEMBER, BETA)

### 9.2 Subgrupos de título (`CollectibleTitleGroup`)

| Grupo | Label menu | Descrição |
|-------|------------|-----------|
| PROFILE_STATS | Perfil & estatísticas | Dinâmicos (ping, tempo, medalha…) |
| BED_WARS | Bed Wars | Stats BedWars |
| SPECIAL | Especiais | Clan, beta, staff |
| AURA | Aura | Animações visuais (cores, meme) |
| MUSICAS | Músicas | **Letra animada frame-a-frame** |

Filtro extra em músicas: `CollectibleMusicArtist` (artista/intérprete).

### 9.3 Títulos animados — padrão técnico (OBRIGATÓRIO para músicas)

Cada título de música deve implementar:

```java
class ExampleMusicTitle extends TitleCollectible {
  private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

  private static final List<String> ANIMATION_FRAMES = List.of(
    "§5§l✦ §c§l777-666 §5§l✦",           // frame 0 = título
    "§c§l✦ §5§lTrecho da letra... §c§l✦", // frames 1..N
    // ...
    "§5§l✦ §c§l777-666 §5§l✦"            // loop: volta ao título
  );

  getTitleGroup() → MUSICAS
  getMusicArtist() → MATUE (ou artista correto)
  getTitleAnimationFrames() → ANIMATION_FRAMES
  getTitleAnimationStepsPerFrame() → 3   // mais alto = mais lento
  getTitle() → ANIMATION_FRAMES.get(0)
}
```

**Regras visuais das músicas:**

1. **10–20 frames** por música (trechos curtos da letra)
2. **Duas cores alternadas** nos bordos (ex.: `§5✦` / `§c✦`)
3. Texto central em **negrito** (`§l`)
4. Frame 0 = **nome da faixa**; último frame = nome novamente (loop suave)
5. Velocidade: `stepsPerFrame = 3` (~30 ticks por frame com tick de 10)
6. Exibir no holograma acima do nick; ciclar frames em task periódica

**Exemplo real — 777-666 (Matuê):**

```
§5§l✦ §c§l777-666 §5§l✦
§c§l✦ §5§lQuando ela bater §c§l✦
§5§l✦ §c§lCura ela vai te levar §5§l✦
... (18 frames total)
```

**Exemplo — Vida chique (Veigh):** bordos `§3◇` / `§b◇`, tema azul/ciano.

### 9.4 Catálogo de títulos — MÚSICAS (22 títulos animados)

Todos: raridade **Mítico**, grupo **MUSICAS**, rank **OWNER** (salvo nota).

| Título | Artista | Classe referência |
|--------|---------|-------------------|
| 777-666 | Matuê | TripleSevenTripleSixTitle |
| Crack com Mussilon | Matuê | CrackComMussilonTitle |
| Maquina do Tempo | Matuê | MaquinaDoTempoTitle |
| Anos Luz | Matuê | AnosLuzTitle |
| Vida chique | Veigh | VidaChiqueTitle |
| Artista Generico | Veigh | ArtistaGenericoTitle |
| Não mente pra mentiroso | Oruam | NaoMentePraMentirosoTitle |
| Complexo do Lins | Oruam | ComplexoDoLinsTitle |
| AK de Bipé | Oruam | AkDeBipeTitle |
| Lei ANTI ORUAM | Oruam | LeiAntiOruamTitle |
| 22 meu vulgo | Oruam | VinteDoisMeuVulgoTitle |
| Mitsubishi | Orochi | MitsubishiTitle |
| Van Cleef | Orochi | VanCleefTitle |
| MinhaCura | Cabelinho | MinhaCuraTittle |
| X1 | Cabelinho | X1Title |
| Me Postou no daily - MC GP | MC GP | MePostouNoDailyMcGpTitle |
| Meninos do Gueto - MC GP | MC GP | MeninosDoGuetoMcGpTitle |
| Me Postou no daily - MC Lele JP | MC Lele JP | MePostouNoDailyMcLeleJpTitle |
| Meninos do Gueto - Meno K | Meno K | MeninosDoGuetoMenoKTitle |
| Posso Até Não Te Dar Flores | Meno K | PossoAteNaoTeDarFloresTitle |
| Penélope Charmosa | Boladin 211 | PenelopeCharmosaTitle |
| Terminei Recentemente | Vinicin | TermineiRecentementeTitle |

**Artistas no filtro** (`CollectibleMusicArtist`): Boladin 211, Cabelinho, MC GP, MC Lele JP, Meno K, Oruam, Matuê, Orochi, Vinicin, Veigh, Wiu, Teto, Hungria, Filipe Ret, L7nnon, BK, Mc IG, MC Ryan SP, Justin Bieber, Travis Scott, Harry Styles, Sabrina Carpenter, The Kid LAROI, OneRepublic, Nicki Minaj, gnash, KayBlack, Cartel MCs, Kendrick Lamar, Sean Kingston, CJota, Sid, UNKNOWN.

### 9.5 Títulos — AURA (animação visual, sem artista)

| Título | Animação |
|--------|----------|
| Protagonista | Cicla §0–§f (16 cores) em "PROTAGONISTA" |
| Savage | Frames custom Savage |
| Cafajeste Chucro | Letra animada meme |
| Vivi na mão dos DRAKE | Letra meme (cores §1–§a) |

Grupo: `AURA`.

### 9.6 Títulos — BED WARS

| Título | Rank |
|--------|------|
| Campeão | ADMIN |
| Vitórias Bed Wars | EMBER_PLUS (dinâmico) |
| Nível Bed Wars | EMBER_PLUS (dinâmico) |

### 9.7 Títulos — ESPECIAIS

| Título | Rank |
|--------|------|
| Desde o Início | BETA, PRECIOUS |
| Destaque | ADMIN |
| Clan | MEMBER (dinâmico: nome do clan) |

### 9.8 Títulos — PERFIL & ESTATÍSTICAS (dinâmicos, sem animação)

| Título | Rank | Comportamento |
|--------|------|---------------|
| Desde | EMBER_PLUS | Mostra data de criação da conta |
| Tempo jogado | EMBER_PLUS | Playtime formatado |
| Ping | EMBER_PLUS | Ping atual |
| Sua medalha | EMBER_PLUS | Medalha equipada |
| Não Perturbe | MEMBER | Título estático |

### 9.9 YAML exemplo — título de música

```yaml
titles:
  777-666:
    category: TITLE
    title_group: MUSICAS
    music_artist: MATUE
    rarity: MYTHICAL
    permission: cosmetics.title.777_666
    required_ranks: [OWNER]
    animation:
      steps_per_frame: 3
      frames:
        - "§5§l✦ §c§l777-666 §5§l✦"
        - "§c§l✦ §5§lQuando ela bater §c§l✦"
        - "§5§l✦ §c§lCura ela vai te levar §5§l✦"
        # ... demais trechos
        - "§5§l✦ §c§l777-666 §5§l✦"
```

---

## 10. Aura de imagem — especificação técnica

```yaml
image_aura:
  max_size_px: 96
  height_offset: 2.35
  tick_interval: 2
  scan_quality: 1
  ratio: 0.055
  particle: DUST
  cache_urls: true
  imgur_normalize: true
```

Algoritmo:

1. Download async (Imgur) ou load PNG do jar
2. Redimensionar mantendo alpha
3. Por pixel opaco: spawn `Particle.DUST(rgb)` rotacionado pelo yaw
4. Enviar só para jogadores que `canSee(dono)`
5. Cache por URL; salvar URL no perfil para relog

Presets PNG: `plugins/.../auras/yin_yang.png`, `estrela.png`.

---

## 11. Menus GUI

### Hub (`CollectibleMenu`)

- Slot central: resumo (ativos, desbloqueados)
- Slots 20–24: Aparência, Efeitos, Companheiros, Lobby, Social

### Grupo (`CollectibleGroupMenu`)

- Até 14 categorias por página
- Lore: selecionado, X/Y desbloqueados, clique

### Seletor (`CollectibleSelectorMenu`)

- Paginação 6 linhas, slot inicial 10
- Filtros: A-Z, Z-A, raridade ↑↓, "meus primeiro"
- Botão remover (barrier) se categoria ativa
- Casos especiais: PARTICLE → tipo; AURA → ImageAuraSelectorMenu; HAT → HatTypeSelectorMenu

---

## 12. Comandos

| Comando | Descrição |
|---------|-----------|
| `/cosmetics` | Menu principal |
| `/aura link <url>` | Aura Imgur personalizada |
| `/aura usar <nome>` | Equipar preset de aura |
| `/aura remover` | Desequipar aura |
| `/customtitle <texto>` | Título custom (Creator+) |
| `/cosmeticsadmin give/remove/reload/list/clear` | Admin |

---

## 13. Permissões

```
cosmetics.use
cosmetics.admin
cosmetics.bypass.lock
cosmetics.category.*
cosmetics.<category>.<id>
cosmetics.aura.custom
cosmetics.aura.link
cosmetics.title.*
```

---

## 14. Persistência SQL

```sql
CREATE TABLE player_cosmetics (
  uuid VARCHAR(36) PRIMARY KEY,
  unlocked TEXT,
  active TEXT,
  custom_aura_url TEXT,
  custom_aura_name TEXT,
  favorites TEXT,
  updated_at TIMESTAMP
);
```

---

## 15. Eventos Bukkit

| Evento | Uso |
|--------|-----|
| PlayerJoinEvent | Load profile + re-equip |
| PlayerQuitEvent | Cancel operators + save |
| PlayerChangedWorldEvent | Re-spawn companions |
| PlayerMoveEvent | Trail, Footprint (throttle) |
| EntityDeathEvent | Kill effect |
| PlayerDeathEvent | Death effect |
| PlayerMoveEvent (ground) | Jump effect |

---

## 16. API pública

```java
interface CosmeticsAPI {
  void equip(UUID player, String cosmeticId);
  void unequip(UUID player, CosmeticCategory category);
  boolean hasUnlocked(UUID player, String cosmeticId);
  void unlock(UUID player, String cosmeticId);
  Optional<String> getActive(UUID player, CosmeticCategory category);
}
```

Eventos: `PlayerCosmeticEquipEvent`, `PlayerCosmeticUnequipEvent`, `PlayerCosmeticUnlockEvent`.

---

## 17. Fases de implementação

1. Core + registry + SQLite + operators base  
2. Menus hub + seletor + filtros  
3. Partículas, trails, footprints, jump/death/kill  
4. **ImageAuraEngine** + `/aura`  
5. Companheiros (balloon/pet/wings/companion)  
6. Hats, emotions, banners, capes, clothing, gadgets  
7. **Títulos** — estáticos + dinâmicos + **músicas animadas**  
8. Join messages + artifacts  
9. Admin + PlaceholderAPI + polish  

---

## 18. Critérios de aceite

- [ ] Todas as **21 categorias** funcionais  
- [ ] Catálogo com itens listados neste documento  
- [ ] **22 títulos de música** com animação frame-a-frame igual ao Savage  
- [ ] Filtro de artista no menu de títulos  
- [ ] Aura Imgur renderiza acima da cabeça e persiste no relog  
- [ ] 1 ativo por categoria + conflitos de companheiros  
- [ ] Menus em PT-BR com contador de desbloqueio  
- [ ] Paper 1.21+, sem NMS legado  

---

## 19. Referência no código Savage

| Área | Caminho |
|------|---------|
| Categorias | `core/.../CollectibleCategory.java` |
| Operators | `core/.../collectible/operator/` |
| Títulos | `core/.../collectible/type/title/list/` |
| Músicas / artistas | `CollectibleMusicArtist.java`, `CollectibleTitleGroup.java` |
| Aura imagem | `core-bukkit/.../ImageAuraEngine.java` |
| Menus | `core-bukkit/.../menu/server/collectible/` |
| Chapéus | `core/.../type/hat/HatList.java` (140 itens) |

---

*Documento gerado a partir do codebase Savage. Use como prompt único para IA ou briefing de desenvolvimento.*
