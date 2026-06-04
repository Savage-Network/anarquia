# Prompt — Servidor Semi-Anarquia Paper 1.21 (plugin único — estilo Savage)

Documento de especificação para construir **um único plugin Paper** para servidor **semi-anarquia** em **Paper 1.21+** (Java 21).

**Regra principal:** tudo em **1 JAR / 1 `plugin.yml`** — tags, medalhas, cosméticos, eventos, anticheat, economia, clãs, punições, survival, Display entities. **Não** usar vários plugins separados no servidor.

**Nomenclatura de plugins (nome = papel do servidor, sem prefixo "Savage"):**

| Servidor | `plugin.yml` name | JAR | Pacote base (exemplo) |
|----------|-------------------|-----|------------------------|
| **Lobby** | `lobbyhost` | `lobbyhost.jar` | `com.minecraft.lobbyhost` |
| **Semi-anarquia / survival** | `game` | `game.jar` | `com.minecraft.game` |
| **Proxy** (opcional) | `proxyhost` | `proxyhost.jar` | `com.minecraft.proxyhost` |

Este documento descreve o plugin **`game`** (servidor de jogo semi-anarquia). O lobby usa **`lobbyhost`**.

Idioma da UI: **português brasileiro**.

---

## 1. Visão geral

### 1.1 O que é “semi-anarquia” neste projeto

| Permitido (default) | Restrito / configurável |
|---------------------|-------------------------|
| PvP em todo o mundo | Spawn protegido (raio configurável) |
| Grief / raiding | Anti-cheat (fly, reach, etc.) |
| Roubar baús | Spam / flood de chat |
| Traps, TNT, crystal (se habilitado) | Insultos graves (mute) |
| Sem proteção de claims (ou claims pagas opcionais) | Dupe conhecido (ban se config ativar) |

**Filosofia:** liberdade máxima no mundo + punição só para trapaça e abuso extremo.

### 1.2 Um plugin só (como Savage, mas unificado)

No Savage hoje existem **vários JARs** (`core`, `core-bukkit`, `core-bungee`, `bedwars`, `Eventos`…). Neste projeto:

| Savage (referência) | Este plugin (1.21) |
|---------------------|---------------------|
| `core` + `core-bukkit` | **Pacotes internos** no mesmo JAR |
| `core-bungee` | **Opcional:** segundo JAR proxy (Velocity) — só se usar rede; **gameplay fica no plugin Paper** |
| `lobby`, `bedwars`, `pvp`… | **Não existem** — substituídos por survival semi-anarquia |
| `Eventos` | Pacote `event/` **dentro do mesmo plugin** |
| Vários plugins no `/plugins` | **Apenas `game.jar`** |

```
Servidor Paper 1.21/plugins/   (servidor semi-anarquia)
└── game.jar                     ← plugin `game`
    (opcional proxy: proxyhost.jar)
```

**Arquitetura:** espelhar Savage (`core` + `core-bukkit`), **1 JAR por servidor**, pacotes internos. Sem arcades BedWars/EggWars etc.

### 1.2.1 Plugin `lobbyhost` (servidor lobby — outro JAR)

| Item | Valor |
|------|--------|
| `plugin.yml` name | **`lobbyhost`** |
| JAR | **`lobbyhost.jar`** |
| Pacote | `com.minecraft.lobbyhost` |
| Main | `com.minecraft.lobbyhost.LobbyHostPlugin` |
| Permissões | **`lobbyhost.*`** |
| Placeholders | **`%lobbyhost_*%`** |

Substitui **`SavageLobby`** / módulo `lobby`: spawn, hologramas, `/play`, NPCs, visibilidade, missões, cosméticos hub, tags, `/prefs`, `/skin`. **Sem** survival, KOTH, anticheat pesado.

```
Servidor Lobby Paper/plugins/
└── lobbyhost.jar
```

### 1.3 Espelhamento Savage → pacotes do plugin `game`

| Savage | Pacote no plugin único | Função |
|--------|------------------------|--------|
| `Core.java` | `com.minecraft.game.Core` | Bootstrap estático, controllers, Gson, logger |
| `Account` + `AccountContext` | `account.Account` | Perfil JSON (tags, medal, golds, cosméticos…) |
| `AccountController` | `controller.AccountController` | Cache online |
| `AccountData` + PostgreSQL | `backend.data.AccountData` | Save/load async |
| `RedisDatabase` + channels | `backend.redis.*` | Sync punições, profile (se proxy) |
| `Tag` enum + `TagManager` | `account.context.tag.*` + `bukkit.manager.TagManager` | Tags, tab, chat |
| `Medal` enum | `account.context.medal.Medal` | Símbolo no chat |
| `Collectible*` + `CollectibleOperator` | `api.collectible.*` | Cosméticos 21 categorias |
| `CollectibleController` | `controller.CollectibleController` | Registry reflection + enums |
| `CollectibleMenu` / GUIs | `bukkit.menu.collectible.*` | Menus paginados |
| `ImageAuraEngine` + `/aura` | `bukkit.display.aura.*` | Aura Imgur (DUST 1.21) |
| `Punishment` + `/cban` | `api.punishment.*` + commands | Ban/mute/kick |
| `ClanCommand` + `ClanData` | `api.clan.*` | Clãs |
| `BukkitCommandHandler` + `@Command` | `bukkit.command.*` | Comandos anotados |
| `ListenerHandler` | `bukkit.listener.handler.ListenerHandler` | Scan pacote listeners |
| `TagCommand`, `/prefs` | `bukkit.command.single.*` | Comandos jogador |
| `Eventos` (Sumo, Skit…) | `event.game.*` | KOTH, Envoy, Sumo… |
| `PlaceholderAPI` | `bukkit.placeholder.*` | `%game_tag%` etc. |
| **Novo** Display 1.21 | `bukkit.display.*` | TextDisplay, ItemDisplay… |
| **Novo** Anticheat | `anticheat.*` | Checks + VL (Savage só tem `/cban` manual) |

### 1.4 Bootstrap (igual Savage `BukkitCore.onEnable`)

Ordem de inicialização no **único** `JavaPlugin`:

```java
onLoad()  → Core.load(platform, logger)
onEnable():
  1. Database (PostgreSQL/MySQL) + Redis
  2. Controllers: Account, Collectible, Clan, Punishment, Event
  3. CollectibleController.handle() — reflection + enums (PetList, WingsList…)
  4. DisplayManager + ImageAuraEngine
  5. AnticheatEngine.registerChecks()
  6. EventScheduler.start()
  7. BukkitCommandHandler.scan("...game.bukkit.command")
  8. ListenerHandler.scan("...game.bukkit.listener")
  9. TaskManager (tags animadas, eventos, AC, display follow)
  10. PlaceholderAPI hook
onDisable() → save all, cancel tasks, remove displays
```

### 1.5 Catálogo completo — todos os sistemas Savage (1 plugin)

Legenda: **✅** = incluir no `game.jar` (mesmo padrão Savage) · **🔄** = adaptar para semi-anarquia · **❌** = não incluir (só arcade/rede) · **➕** = novo (Savage não tem, mas o plugin precisa)

| # | Sistema Savage | Módulo origem | Status | Pacote no plugin único |
|---|----------------|---------------|--------|------------------------|
| 1 | `Core` + bootstrap | core | ✅ | `Core.java`, `Constant.java` |
| 2 | `Account` + `AccountContext` | core | ✅ | `account/` — **espelhar todos os campos** (§1.6) |
| 3 | `AccountController` + `AccountData` | core | ✅ | `controller/` + `backend/data/` |
| 4 | PostgreSQL JSON + Redis | core | ✅ | `backend/database/`, `backend/redis/` |
| 5 | `Rank` + `RankInfo` + `RankType` | core | ✅ | `account/context/rank/` |
| 6 | `level` + `golds` no context | core | 🔄 | XP survival + moeda (`golds` = `$`) |
| 7 | `Tag` enum + animação Owner | core | ✅ | `account/context/tag/` + `TagManager` |
| 8 | `Medal` enum + `/medal` | core | ✅ | `account/context/medal/` + `MedalManager` |
| 9 | `Toggle` + `/prefs` | core-bukkit | ✅ | `account/context/toggle/` + `PreferenceMenu` |
| 10 | `TagPrefix` (VIP) | core | ✅ | Dentro de `Toggle` + `PreferenceMenu` |
| 11 | Cosméticos 21 cat. + operators | core + core-bukkit | ✅ | `api/collectible/` — ver doc cosméticos |
| 12 | `CollectibleController` reflection | core | ✅ | Scan enums `*List.java` |
| 13 | `CollectibleMenu` hub 5 grupos | core-bukkit | ✅ | `bukkit/menu/collectible/` |
| 14 | `ImageAuraEngine` + `/aura` | core-bukkit | ✅ | `bukkit/display/aura/` (DUST 1.21) |
| 15 | `CustomTitle` + `/customtitle` | core-bukkit | ✅ | `account/context/title/` |
| 16 | `JoinMessage` cosmético | core | ✅ | `JoinMessageController` + broadcast join |
| 17 | `SkinController` + `/skin` + biblioteca | core-bukkit | ✅ | `api/skin/` + `SkinLibraryCategoryMenu` |
| 18 | `AuthMetadata` + login/registro | auth + core-bungee | ✅ | `auth/` — `/login`, `/register`, captcha GUI |
| 19 | `Fake` + `/fake` | core-bungee | 🔄 | Opcional semi-anarquia (staff/VIP, config off default) |
| 20 | `FriendMetadata` + `/friend` | core-bungee | ✅ | `social/friend/` |
| 21 | Party + `/party` | core-bungee | 🔄 | Grupo RTP/warzone (não fila minigame) |
| 22 | `ClanMetadata` + `/clan` | core-bungee | ✅ | `api/clan/` + tag no chat |
| 23 | `Block` lista + `/block` | core | ✅ | `account/context/block/` |
| 24 | `/report` | core-bungee | ✅ | `social/report/` + webhook staff |
| 25 | `PunishmentHistory` + `/ban`… | core + bungee | ✅ | `api/punishment/` |
| 26 | `/cban` anticheat | core-bungee | ✅ | `anticheat/` + comando manual |
| 27 | `RedeemCode` + `/redeem` + `/code` | core-bukkit | ✅ | `economy/code/` |
| 28 | `Restrict` + permissões extras | core | ✅ | Dentro de `AccountContext` |
| 29 | `RouteContext` (servidor destino) | core | 🔄 | Single-server: só spawn/world; com proxy: `/server` |
| 30 | `ValidationManager` (premium) | core | ✅ | Mojang API + flag `premium` |
| 31 | `BukkitCommandHandler` + `@Command` | core-bukkit | ✅ | Scan automático |
| 32 | `ListenerHandler` | core-bukkit | ✅ | Scan automático |
| 33 | `TaskManager` | core-bukkit | ✅ | Tags, aura, displays, eventos |
| 34 | `Menu` + `MenuManager` | core-bukkit | ✅ | Base GUI paginada |
| 35 | `HologramManager` | core-bukkit | 🔄 | **TextDisplay** — spawn LB, crates, Discord link |
| 36 | `NpcManager` | core-bukkit | 🔄 | **Interaction** + skin — shop, evento, `/cosmetics` |
| 37 | `SidebarManager` | core-bukkit | 🔄 | Scoreboard survival (K/D, golds, combat tag) |
| 38 | `AboveNameManager` | core-bukkit | 🔄 | **TextDisplay** nametag (tag + título cosmético) |
| 39 | `CooldownManager` | core-bukkit | ✅ | Cooldowns globais + por chave no Account |
| 40 | `PermissionManager` | core-bukkit | ✅ | LuckPerms bridge |
| 41 | Eventos: Sumo, Skit, Team, Variable | Eventos | ✅ | `event/game/` + comandos originais |
| 42 | KOTH, Envoy, Boss… | — | ➕ | `event/game/` — eventos semi-anarquia |
| 43 | Anticheat automático | — | ➕ | `anticheat/` (Savage só `/cban` manual) |
| 44 | Survival: home, rtp, kit, combat | — | ➕ | `survival/` |
| 45 | AH + shop + bounty | — | ➕ | `economy/` |
| 46 | `Member` + stats arcade | core | 🔄 | **`GameStats`** — K/D, kills, playtime (sem BedWars stats) |
| 47 | `BotController` + Discord | core-bungee + discord-bot | 🔄 | Webhook HTTP (punishments, report, AC) |
| 48 | API REST `api/` | api | 🔄 | Opcional: `/api/player/:nick` para Discord bot |
| 49 | PlaceholderAPI expansion | core-bukkit | ✅ | `%game_*%` |
| 50 | BedWars / EggWars / Duels / SkyWars / Bridge / PvP | arcades | ❌ | Não implementar |
| 51 | `lobby` architects + `/play` | lobby | ❌ | Vai no plugin **`lobbyhost`**, não no `game` |
| 52 | `ArcadeManager` + instâncias Slime | core-bukkit | ❌ | Não instanciar arenas |
| 53 | Missões lobby BedWars | lobby | 🔄 | **`DailyQuest`** survival (opcional fase 10) |
| 54 | Parkour record (`parkourBestTime`) | AccountContext | 🔄 | Parkour spawn opcional com LB Display |

**Resumo:** o plugin único contém **tudo que o Savage tem em `core` + `core-bukkit` + `auth` + `Eventos`**, adaptado para mundo aberto, **mais** survival, anticheat e eventos globais. Só ficam de fora os **arcades** e o **lobby multi-servidor**.

### 1.6 `AccountContext` — espelhar Savage (obrigatório)

O JSON em `accounts.data` deve suportar **os mesmos campos** do Savage (Gson compatível):

| Campo Savage | Tipo | Uso no semi-anarquia |
|--------------|------|------------------------|
| `route` | RouteContext | Destino ao conectar (spawn/world) |
| `rankInfo` | RankInfo | Rank comprado + ranks disponíveis |
| `fake` | Fake | Nick/skin falsa (se habilitado) |
| `skin` | SkinMetadata | Skin custom + biblioteca |
| `friend` | FriendMetadata | Amigos + pedidos + toggle |
| `toggle` | Toggle | `/prefs` completo |
| `auth` | AuthMetadata | Login/registro/sessão |
| `clan` | ClanMetadata | Clã atual + convites |
| `joinMessage` | JoinMessageMetadata | Cosmético join message ativo |
| `imageAura` | ImageAuraMetadata | URL Imgur + preset aura |
| `history` | PunishmentHistory | Cache punições locais |
| `tag` | Tag | Tag ativa |
| `medal` | Medal | Medalha ativa |
| `customTitle` | CustomTitle | Título staff/custom |
| `emberPlusColor` | String | Cor do + EMBER+ |
| `partyIdentifier` | String | Party atual |
| `level` | int | Nível conta (XP survival) |
| `golds` | int | Economia principal |
| `collectibles` | List\<CosmeticEntry\> | Desbloqueios |
| `activeCollectibles` | List\<String\> | 1 por categoria |
| `favoriteCollectibles` | List\<String\> | Favoritos menu |
| `favoriteMaps` | Map | Ignorar ou adaptar warzone |
| `permissions` | List\<Permission\> | Permissões temporárias |
| `restricts` | List\<Restrict\> | Restrições staff |
| `medals` | List\<Medal\> | Medalhas desbloqueadas |
| `blocks` | List\<Block\> | Jogadores bloqueados |
| `cooldown` | Map\<String, Long\> | Cooldowns por chave |
| `parkourBestTime` | long | Record parkour spawn |
| `premium`, `online`, `ipAddress`, `lastLogin` | — | Metadados sessão |

Classe `Account.java` com métodos: `send()`, `sound()`, `hasCooldown()`, `setCooldown()`, `isStaffer()`, `isVIP()`, `getFormattedCooldown()` — **igual Savage**.

---

| Item | Escolha |
|------|---------|
| Servidor | **Paper 1.21.4+** |
| Java | **21** |
| Build | Gradle Kotlin DSL |
| Texto | **Adventure API** + MiniMessage |
| Comandos | Brigadier nativo Paper ou anotações |
| GUI | TriumphGUI ou inventários nativos |
| DB | SQLite dev / **MySQL prod** |
| Cache/sync | **Redis** (opcional v1, obrigatório com proxy) |
| Permissões | LuckPerms |
| Placeholders | PlaceholderAPI |
| Partículas | `Particle.DUST`, `DustOptions` |
| Entidades decorativas | **TextDisplay**, **ItemDisplay**, **BlockDisplay**, **Interaction** |
| Packets | Só se necessário (ProtocolLib opcional) |
| **Proibido** | NMS/reflection para versões antigas |

---

## 3. Estrutura de arquivos — **1 projeto Gradle, 1 plugin**

### 3.1 Repositório (simples — sem multi-módulo obrigatório)

```
game/                                    # rootProject.name = "game"
├── build.gradle.kts                     # shadowJar → game.jar
├── settings.gradle.kts                  # include apenas este projeto (ou :plugin)
├── gradle.properties
├── README.md
└── src/main/
    ├── java/com/minecraft/game/         # pacote base plugin `game`
    └── resources/
        └── plugin.yml                   # name: game, main: game.bukkit.GamePlugin
```

**build.gradle.kts (resumo):**

```kotlin
plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.0")
    implementation("com.google.code.gson:gson:2.11.0")
}
tasks.shadowJar {
    archiveFileName.set("game.jar")
}
```

### 3.2 Árvore Java completa (Savage-like, tudo no mesmo JAR)

Espelha `core/` + `core-bukkit/` + `Eventos/` do Savage, **sem** `bedwars`, `pvp`, `lobby`.

```
src/main/java/com/minecraft/game/
│
├── GamePlugin.java                      # extends JavaPlugin (= BukkitCore)
├── Core.java                            # static: getAccountController(), getCollectibleController()…
├── Constant.java                        # SOURCE_DIR, cooldown keys, templates ban
│
├── account/                             # === SAVAGE core/account ===
│   ├── Account.java
│   ├── AccountController.java
│   └── context/
│       ├── AccountContext.java          # JSON serializado (1 objeto por jogador)
│       └── objects/
│           ├── tag/Tag.java             # enum ou registry YAML
│           ├── tag/prefix/TagPrefix.java
│           ├── tag/role/TagRole.java
│           ├── medal/Medal.java
│           ├── rank/RankType.java
│           ├── clan/ClanMetadata.java
│           ├── cosmetic/CosmeticEntry.java
│           ├── joinmessage/JoinMessageMetadata.java
│           ├── aura/ImageAuraMetadata.java
│           ├── restrict/Restrict.java
│           └── toggle/Toggle.java
│
├── auth/                                # === módulo auth Savage ===
│   ├── AuthService.java
│   ├── menu/CaptchaMenu.java
│   └── listener/AuthListener.java
│
├── social/                              # === core-bungee social (no Paper) ===
│   ├── friend/FriendService.java
│   ├── party/PartyService.java
│   ├── report/ReportService.java
│   └── block/BlockService.java
│
├── backend/                             # === SAVAGE backend ===
│   ├── database/
│   │   ├── postgresql/PostgresDatabase.java
│   │   └── redis/
│   │       ├── RedisDatabase.java
│   │       └── message/                 # AccountUpdate, PunishmentMessage…
│   └── data/
│       ├── list/user/AccountData.java
│       ├── list/ClanData.java
│       └── list/PunishmentData.java
│
├── controller/                          # === SAVAGE controller ===
│   ├── Controller.java
│   └── list/
│       ├── AccountController.java
│       ├── CollectibleController.java
│       └── ClanController.java
│
├── api/                                 # === Domínio (sem Bukkit) ===
│   ├── collectible/
│   │   ├── Collectible.java
│   │   ├── CollectibleCategory.java   # 21 categorias + grupos menu
│   │   ├── CollectibleRarity.java
│   │   ├── operator/CollectibleOperator.java
│   │   ├── operator/list/             # HatOperator, ImageAuraOperator, PetOperator…
│   │   └── type/                      # hat/, pet/, aura/image/, trail/, title/…
│   ├── punishment/
│   │   ├── Punishment.java
│   │   └── objects/enums/PunishmentReason.java
│   ├── clan/
│   ├── joinmessage/
│   └── item/Item.java                   # wrapper item (cor, skull, lore)
│
├── bukkit/                              # === SAVAGE core-bukkit ===
│   ├── GamePlatform.java                # implementa Core Platform
│   ├── manager/
│   │   ├── Manager.java
│   │   ├── TagManager.java              # animação Owner, update tab
│   │   ├── MedalManager.java
│   │   ├── HologramManager.java         # TextDisplay LB
│   │   ├── NpcManager.java              # Interaction NPCs
│   │   ├── SidebarManager.java
│   │   ├── CooldownManager.java
│   │   └── PermissionManager.java
│   ├── command/
│   │   ├── structure/
│   │   │   ├── BukkitCommandHandler.java
│   │   │   ├── BukkitCommandContext.java
│   │   │   └── annotation/Command.java
│   │   ├── single/                      # TagCommand, AuraCommand, ClanCommand…
│   │   ├── NormalCommands.java
│   │   ├── ModeratorCommands.java
│   │   └── DevCommands.java
│   ├── menu/
│   │   ├── api/Menu.java                # paginação, som, back button
│   │   ├── collectible/
│   │   │   ├── CollectibleMenu.java     # hub 5 grupos
│   │   │   ├── CollectibleGroupMenu.java
│   │   │   └── list/CollectibleSelectorMenu.java
│   │   ├── account/preference/PreferenceMenu.java
│   │   ├── account/skin/library/SkinLibraryCategoryMenu.java
│   │   └── server/event/EventMenu.java
│   ├── listener/
│   │   ├── handler/ListenerHandler.java # scan pacote listener
│   │   ├── AccountListener.java         # join/quit load save
│   │   ├── ChatFormatListener.java
│   │   ├── TrailListener.java
│   │   ├── FootprintListener.java
│   │   ├── KillEffectListener.java
│   │   └── PunishmentListener.java
│   ├── display/                         # === 1.21 NOVO (substitui NMS balloon) ===
│   │   ├── DisplayManager.java
│   │   ├── TextDisplayHandle.java       # títulos, hologramas, nametag
│   │   ├── ItemDisplayHandle.java       # pet, balão, asa, gadget
│   │   ├── BlockDisplayHandle.java      # KOTH pillar, decoração
│   │   ├── InteractionHandle.java       # NPC/menu clicável
│   │   ├── DisplayFollowTask.java
│   │   └── aura/
│   │       ├── ImageAuraEngine.java     # Imgur → Particle.DUST
│   │       ├── ImageShapeBuilder.java
│   │       └── ImgurUtil.java
│   ├── service/
│   │   ├── task/TaskManager.java
│   │   └── discord/PunishmentNotifier.java  # HTTP webhook (como Savage)
│   └── placeholder/GameExpansion.java
│
├── event/                               # === SAVAGE Eventos/ adaptado ===
│   ├── EventPluginBridge.java           # registra games no Core
│   ├── EventController.java             # FSM global
│   ├── EventScheduler.java
│   ├── game/
│   │   ├── Game.java
│   │   ├── state/GameState.java
│   │   ├── sumo/SumoGame.java           # port Eventos/
│   │   ├── skit/Skit.java
│   │   ├── team/Team.java
│   │   ├── variable/Variable.java
│   │   ├── koth/KothGame.java
│   │   ├── envoy/EnvoyGame.java
│   │   └── boss/BossGame.java
│   ├── command/                         # StartCommand, SumoCommand…
│   ├── listener/
│   └── menu/
│
├── anticheat/                           # === NOVO (Savage só tem /cban) ===
│   ├── AnticheatEngine.java
│   ├── ViolationManager.java
│   ├── check/                           # Fly, Reach, Scaffold…
│   └── listener/MovementCombatListener.java
│
├── survival/                            # semi-anarquia (substitui arcades)
│   ├── home/HomeService.java
│   ├── combat/CombatTagService.java
│   ├── kit/KitService.java
│   ├── rtp/RtpService.java
│   ├── bounty/BountyService.java
│   └── command/
│
├── economy/
│   ├── EconomyService.java                # golds = AccountContext.golds
│   ├── code/RedeemCodeService.java        # /redeem, /code
│   ├── shop/ShopMenu.java
│   └── auction/AuctionHouse.java
│
├── stats/
│   └── GameStats.java                     # substitui Member arcade stats
│
└── util/
    ├── list/TimeUtil.java
    ├── list/JsonUtil.java
    └── list/loader/ClassLoader.java     # scan cosméticos (CollectibleController)
```

### 3.3 Recursos (`src/main/resources/`) — tudo no mesmo JAR

```
src/main/resources/
├── plugin.yml
├── config.yml
├── messages.yml
├── tags.yml                             # opcional: override enum Tag
├── medals.yml
├── collectibles/                        # YAML por categoria (ou só reflection Java)
├── events/
│   ├── koth.yml
│   ├── envoy.yml
│   └── schedule.yml
├── cosmetics/                           # ver PROMPT-COSMETICOS-SAVAGE-COMPLETO.md
├── anticheat/checks.yml
├── economy/shop.yml
├── survival/kits.yml
├── auras/
│   ├── yin_yang.png
│   └── LEIA-ME.txt
└── display/
    └── spawn_holograms.yml
```

### 3.4 `plugin.yml` (único)

```yaml
name: game
version: '${version}'
main: com.minecraft.game.bukkit.GamePlugin
api-version: '1.21'
authors: [SavageLabs]
softdepend: [PlaceholderAPI, LuckPerms]
commands:
  tag:
  medal:
  cosmetics:
  aura:
  prefs:
  skin:
  login:
  register:
  friend:
  party:
  clan:
  block:
  report:
  fake:
  redeem:
  code:
  customtitle:
  evento:
  sumo:
  skit:
  cban:
  ac:
  home:
  rtp:
  kit:
  pay:
```

### 3.5 Proxy (opcional — segundo JAR, **não** obrigatório)

Se usar Velocity, **apenas** punições + anúncios globais + `/send` — **sem** duplicar cosméticos/eventos:

```
proxyhost.jar   # plugin `proxyhost` — cban, ban, mute, glist — sync Redis com Paper
```

O servidor semi-anarquia roda **somente** `game.jar` com todas as features de jogo.

### 3.6 Scan automático (padrão Savage)

Igual `BukkitCore`:

```java
new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".game.bukkit.command");
new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".game.bukkit.listener");
CollectibleController.handle(this, Constant.SOURCE_DIR + ".game.api.collectible.type");
TaskManager.handle(this, Constant.SOURCE_DIR + ".game.bukkit.service.task.list");
```

Novos comandos/listeners = criar classe no pacote → **registro automático**, sem editar `plugin.yml` (exceto alias raiz).

---

## 4. Sistema de Tags (igual Savage)

### 4.1 Modelo — preferir enum `Tag` como no Savage

Manter **`Tag.java` enum** com prefix, suffix, priority, `TagRole`, frames animados (Owner rainbow), tags sazonais (NATAL, HALLOWEEN). Opcional: `tags.yml` só para **override** sem recompilar.

Exemplo (espelho Savage):

```java
TAG_OWNER("Owner", "0", "§4§lCEO§f✦§4§o", RankType.OWNER, TagRole.STAFF,
    Arrays.asList("§4§lCEO§f✦§4§o", "§4§lCEO§e✦§4§o", ...)),
TAG_SAVAGE("Savage", "H", "§2§lSAVAGE §2", RankType.SAVAGE, TagRole.VIP, ...),
TAG_NATAL("Natal", "J", "§c§lNATAL §c", TagRole.SEASONAL, ...),
```

`TagManager.startAnimationTask()` — igual Savage, cicla frames no tab/nametag **TextDisplay**.

### 4.2 YAML opcional (extensão)

```yaml
# tags.yml — override sem recompilar (enum Tag continua sendo fonte principal)
tags:
  savage:
    display: "Savage"
    prefix: "§2§lSAVAGE §2"
    priority: 100
    role: VIP
    permission: "game.tag.savage"

  owner:
    display: "Owner"
    prefix: "§4§lCEO§f✦§4§o"
    priority: 0
    role: STAFF
    permission: "game.tag.owner"
    animated: true
    animation_frames:
      - "§4§lCEO§f✦§4§o"
      - "§4§lCEO§e✦§4§o"
      - "§4§lCEO§d✦§4§o"
    animation_interval_ticks: 10

  natal_2026:
    display: "Natal 2026"
    prefix: "§c§lNATAL §c"
    role: SEASONAL
    permission: "game.tag.natal2026"
    available_until: "2026-01-06T00:00:00"
```

### 4.3 Roles de tag

| Role | Uso |
|------|-----|
| `MEMBER` | Default |
| `COLORED` | Tags de cor (Azul, Roxo...) |
| `VIP` | Ranks pagos |
| `SEASONAL` | Eventos (Natal, Halloween) |
| `SPECIAL` | Beta, Champion, conquistas |
| `STAFF` | Helper → Owner |

### 4.4 Formato de chat

```
{medal}{tag_prefix}{nick}{tag_suffix}§7: {message}
```

Exemplo: `§e⚝ §2§lSAVAGE §2Player§7: olá`

### 4.5 Tablist e nametag (1.21)

- **Tablist:** Adventure `Component` via Paper team API ou plugin tab
- **Nametag acima da cabeça:**
  - Opção A: scoreboard team prefix/suffix (compatível)
  - Opção B: **TextDisplay** acima do jogador (moderno, suporta animação frame-a-frame)
- **Prioridade:** tag staff > VIP > seasonal > member
- Cooldown troca tag: 5s (bypass staff)

### 4.6 Comandos

```
/tag                    — menu GUI ou lista
/tag <id>               — equipar tag desbloqueada
/tag list               — tags disponíveis
/tag admin give <p> <id>
/tag admin animated <id> — testar animação
```

### 4.7 Medalhas (símbolo antes da tag)

```yaml
# medals.yml
medals:
  estrelinha:
    symbol: "§e⚝ "
    permission: "game.medal.estrelinha"
  champion:
    symbol: "§6❂ "
    permission: "game.medal.champion"
```

Comando `/medal` — mesmo fluxo de `/tag`.

### 4.8 PlaceholderAPI

```
%game_tag_prefix%
%game_tag_name%
%game_tag_suffix%
%game_medal%
%game_tag_animated_frame%
%game_clan_tag%
```

---

## 5. Sistema de Eventos globais

Eventos **no mundo semi-anarquia** — não minigames instanciados. Correm no mapa principal ou em regiões marcadas.

### 5.1 Máquina de estados

```
IDLE → ANNOUNCING (60s) → RUNNING → ENDING (10s) → IDLE
```

- BossBar + broadcast + som
- Recompensas automáticas
- Anti-grief de **estruturas do evento** (não do mapa todo)
- Log em `event_history` table

### 5.2 Tipos de evento

**Do módulo Savage `Eventos/` (portar integral):**

| Evento Savage | Comandos | Descrição |
|---------------|----------|-----------|
| **Sumo** | `/sumo`, `/start`, `/leave`, `/winner` | Arena knockout, `SumoMapGenerator` |
| **Skit** | `/skit` | Kits temporários em evento staff |
| **Team** | `/team` | Times coloridos em evento |
| **Variable** | `/variable` | Flags globais do evento (`VariableListener`) |

**Novos para semi-anarquia (mesmo pacote `event/`):**

| Evento | Descrição | Display 1.21 |
|--------|-----------|----------------|
| **KOTH** | King of the Hill | BlockDisplay + TextDisplay timer |
| **Envoy** | Baús em coords aleatórias | ItemDisplay + partículas |
| **Boss Raid** | Boss HP alto | BossBar + TextDisplay |
| **Drop Party** | Chuva de itens no spawn | Item entities |
| **Supply Drop** | Baú raro global | BlockDisplay + Interaction |
| **Envoy PvP** | Envoy na warzone | — |
| **2x Rewards** | Multiplicador 30min | Flags only |
| **Mob Arena** | Horda de mobs | — |
| **Pinata** | Entidade batível | BlockDisplay composto |

### 5.3 Agendamento (`events/schedule.yml`)

```yaml
schedule:
  - cron: "0 18 * * *"              # todo dia 18h
    event: KOTH
    arena: warzone_koth
  - cron: "0 */3 * * *"
    event: ENVOY
    count: 8
    world: world
  - cron: "0 20 * * 6"              # sábado 20h
    event: BOSS
    boss: wither_king
```

### 5.4 Recompensas

```yaml
rewards:
  koth_winner:
    commands:
      - "game givekey %player% legendary 1"
      - "game money add %player% 50000"
    items:
      - "NETHERITE_INGOT:3"
    tag_unlock: "champion"           # opcional
    broadcast: "§6%player% §evenceu o KOTH!"
```

### 5.5 Comandos

```
/evento                     — próximo evento + tempo
/evento join                — teleporte se evento permitir
/evento top                 — estatísticas
/evento admin start <tipo>
/evento admin stop
/evento admin setregion koth pos1/pos2
```

### 5.6 Integração Display (exemplo KOTH)

```java
// Pseudocódigo — zona KOTH visual
BlockDisplay pillar = world.spawn(location, BlockDisplay.class, d -> {
    d.setBlock(Material.GOLD_BLOCK.createBlockData());
    d.setBillboard(Display.Billboard.CENTER);
    d.setBrightness(new Display.Brightness(15, 15));
});
TextDisplay label = world.spawn(location.add(0, 2, 0), TextDisplay.class, d -> {
    d.text(Component.text("KOTH", NamedTextColor.GOLD));
    d.setBillboard(Display.Billboard.CENTER);
    d.setSeeThrough(true);
});
// Atualizar label: "Capturando: Nick (45s)"
```

---

## 6. Cosméticos (resumo — doc dedicado Savage)

Usar **`docs/PROMPT-COSMETICOS-SAVAGE-COMPLETO.md`** integralmente:

- 21 categorias, `CollectibleOperator`, `CollectibleMenu` hub 5 grupos
- Aura Imgur → `ImageAuraEngine` + `Particle.DUST`
- **1.21:** pets/balões/asas = **`ItemDisplayHandle`** (substitui `BalloonOperator` NMS)
- Títulos animados = **`TextDisplay`** ou frames no `TitleCollectible`
- Tudo no pacote `api/collectible/` + `bukkit/menu/collectible/` **do mesmo JAR**

---

## 7. Anticheat (semi-anarquia)

Checks prioritários (mundo survival PvP):

1. Fly / Glide / Elytra abuse  
2. Speed / Timer  
3. Reach / Hitbox  
4. KillAura / AutoClick  
5. Scaffold / FastPlace  
6. Velocity / AntiKB  
7. NoFall / Jesus  
8. Inventory (item impossível)  
9. X-Ray behavior (opcional: alert only, não ban — semi-anarquia tolera menos false positives)

**Violation ladder + alertas Discord + integração ban.**

Exceções: veículos, knockback, RTP em progresso, eventos (Sumo), staff vanish.

---

## 8. Survival semi-anarquia (features populares)

| Feature | Comandos | Notas |
|---------|----------|-------|
| Homes | `/sethome`, `/home`, `/delhome` | Limite por rank |
| RTP | `/rtp` | Cooldown, blacklist biomes |
| TPA | `/tpa`, `/tpaccept`, `/tpdeny` | Combat tag block |
| Combat tag | automático | 15s, logout = kill |
| Kits | `/kit`, `/kits` | Cooldown YAML |
| Bounty | `/bounty <nick> <valor>` | Placa de recompensa |
| Back | `/back` | Última morte |
| Worth | `/worth`, `/sell` | Vender itens |
| Leaderboard | holograma Display | kills, balance, playtime |
| Crates | `/crate`, keys | integração economy |
| Player shop | `/ps create` | placa ou GUI |
| Spawn | `/spawn` | proteção cuboid |
| Warzone | região PvP always | no build ou build permitido |
| Stats | `/stats` | K/D, streak |

**Não incluir:** BedWars, EggWars, instâncias de arena arcade.

---

## 9. Economia

- Moeda: `$` ou `⛃` (configurável)
- `/money`, `/pay`, `/balancetop`
- **Leilão global** (`/ah sell`, `/ah`)
- **Shop GUI** (blocos, comida, gear básico — não pay2win extremo)
- Logs de transação (anti-dupe econômico)
- Placeholder: `%game_balance%`

---

## 10. Social e conta (estilo Savage)

### 10.1 Amigos — `/friend`

Espelhar `FriendCommand` + `FriendMetadata`: add, remove, list, accept, deny, toggle pedidos (`Toggle.allowFriendRequests`).

### 10.2 Party — `/party`

Espelhar `PartyCommand`: create, invite, accept, leave, kick, transfer. **Adaptação:** party teleporta junto no `/rtp` e warzone — **não** fila de minigame.

### 10.3 Clãs — `/clan`

Espelhar `ClanCommand` + `ClanData`: create, invite, kick, promote, tag `[CLAN]`, aliança (opcional).

### 10.4 Bloqueio — `/block`

Espelhar `BlockCommand`: bloquear jogador (tell, party, clan invite off).

### 10.5 Report — `/report`

Espelhar `ReportCommand`: motivo + webhook Discord staff.

### 10.6 Auth — `/login`, `/register`

Port do módulo `auth/`: captcha GUI (`CaptchaMenu`), `AuthMetadata`, estados `AuthState`, freeze até autenticar (contas offline).

### 10.7 Skin — `/skin`

Espelhar `NormalCommands.skin` + `SkinLibraryCategoryMenu` + `SkinController`: VIP muda skin, reset premium, cooldown 3min, Mojang API.

### 10.8 Fake — `/fake` (opcional)

Espelhar `FakeCommand`: nick/skin temporária — **desligado por default** em semi-anarquia (config `fake.enabled: false`).

### 10.9 Preferências — `/prefs`

Espelhar `PreferenceMenu` + `Toggle`:

- Mensagens privadas, pedidos amigo/party/clã, ver stats, tag de clã no chat
- Join message cosmético on/off
- `TagPrefix` (DEFAULT / NONE / etc.)
- Submenu staff: logs, build, auto-vanish, staff chat

### 10.10 Rank, level e golds

- `RankType` enum (MEMBER, VIP, SAVAGE, STAFF…) — LuckPerms sync
- `level` — XP por kills, playtime, eventos
- `golds` — moeda (`/pay`, shop, AH) — **mesmo campo Savage**

### 10.11 Códigos — `/redeem`, `/code`

Espelhar `RedeemCommand` + `CodeCommand` + `RedeemCode`: desbloqueia cosmético, golds, rank temporário.

### 10.12 Título custom — `/customtitle`

Espelhar `CustomTitleCommand` — staff define título acima do nick (TextDisplay).

### 10.13 Mensagens de entrada

Cosmético `JOIN_MESSAGE` + toggle `allowJoinMessage` — broadcast ao entrar no mundo.

### 10.14 Mail e ignore

- `/ignore` — silenciar jogador
- `/mail send` — mensagem offline (opcional)

---

## 11. Staff, punições e infra Savage

### 11.1 Moderação (espelhar `PunishmentCommands` + `ModeratorCommands`)

- `/ban`, `/mute`, `/kick`, `/warn`, `/history`, `/unban`, `/unmute`
- `/vanish`, `/invsee`, `/tp`, `/freeze`, `/stafflist`
- `/whitelist` — modo manutenção
- `/cban` — ban anticheat (manual + auto)
- `/rollback` — CoreProtect (soft depend)
- Discord webhook via `PunishmentNotifier` (como `BotController`)

### 11.2 Handlers e managers (obrigatório no mesmo JAR)

| Savage | Função 1.21 |
|--------|-------------|
| `BukkitCommandHandler` | Registro `@Command` por reflection |
| `ListenerHandler` | Registro listeners por pacote |
| `TaskManager` | Tasks periódicas (tag anim, aura, eventos) |
| `MenuManager` | Menus abertos por UUID |
| `CooldownManager` | Cooldowns globais |
| `TagManager` | Animação tab/nametag |
| `MedalManager` | Símbolo chat |
| `HologramManager` | Leaderboards **TextDisplay** |
| `NpcManager` | NPCs **Interaction** + ItemDisplay head |
| `SidebarManager` | Scoreboard survival |
| `AboveNameManager` | Nametag tag + título cosmético |
| `DisplayManager` | Pets, balões, asas, hologramas |
| `PermissionManager` | Bridge LuckPerms |

### 11.3 Redis + API (opcional rede)

Espelhar `RedisDatabase` + mensagens: sync punições, profile update, party cross-server.

API REST (`api/`): `GET /api/player/:nick` para bot Discord — **projeto separado**, plugin envia webhooks.

### 11.4 Comandos dev/staff Savage a portar

```
/reloadaccount, /reloadmember, /debug, /editor, /playsound
/account (info perfil), /stats, /reconnect
/evento admin…, /sumo, /skit, /team, /variable (Eventos/)
/setspawn, /setdie, /clearmobs (setup evento)
```

---

## 12. Entidades Display — guia de uso no projeto

### 12.1 Quando usar cada tipo

| Entidade | Uso no servidor |
|----------|-----------------|
| **TextDisplay** | Hologramas spawn, leaderboard, título cosmético, label KOTH, nametag animado |
| **ItemDisplay** | Pets, balões, asas, ícones flutuantes, envoy visual |
| **BlockDisplay** | Pilares KOTH, pinata, decoração spawn, “orbe” de evento |
| **Interaction** | NPC clicável abre menu, baú de supply drop clicável |

### 12.2 Regras

- Toda display com `ownerUUID` — limpar no quit
- `setPersistent(false)` — não salvar no chunk
- `setBillboard(CENTER)` — sempre face ao jogador quando aplicável
- Task 1 tick ou 2 tick para seguir jogador: `location = player.getLocation().add(offset.rotate(yaw))`
- Nunca spawnar displays em chunks sem jogadores

### 12.3 API interna

```java
public interface DisplayManager {
  TextDisplayHandle spawnText(UUID owner, Location loc, Component text);
  ItemDisplayHandle spawnItem(UUID owner, Location loc, ItemStack stack);
  void removeAll(UUID owner);
  void tickFollowers(); // chamado 1/tick
}
```

---

## 13. Banco de dados (schema Savage — tabela `accounts`)

```sql
-- Igual Savage AccountData (PostgreSQL JSONB ou MySQL JSON)
CREATE TABLE accounts (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(16) NOT NULL,
  data JSON NOT NULL,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE INDEX idx_accounts_name ON accounts(LOWER(name));

CREATE TABLE punishments (
  id VARCHAR(36) PRIMARY KEY,
  player_uuid VARCHAR(36),
  author_uuid VARCHAR(36),
  category VARCHAR(16),
  reason VARCHAR(64),
  expires_at BIGINT,
  created_at TIMESTAMP
);

CREATE TABLE clans (...);

CREATE TABLE event_history (
  id INT AUTO_INCREMENT PRIMARY KEY,
  event_type VARCHAR(32),
  winner_uuid VARCHAR(36),
  started_at TIMESTAMP,
  ended_at TIMESTAMP
);

CREATE TABLE anticheat_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  player_uuid VARCHAR(36),
  check_type VARCHAR(32),
  vl INT,
  detail TEXT,
  created_at TIMESTAMP
);
```

Perfil JSON (`data`) espelha Savage `AccountContext`: tags, medalhas, cosméticos ativos, settings.

---

## 14. Permissões (prefixo `game.`)

```text
game.tag.*
game.medal.*
game.cosmetics.*
game.aura.custom
game.event.join
game.event.admin
game.ac.alerts
game.ac.bypass
game.home.<n>
game.kit.<name>
game.vip
game.staff.*
```

Documento completo em `docs/PERMISSIONS.md`.

---

## 15. Config global (`config.yml`)

```yaml
server:
  plugin-name: game                       # deve bater com plugin.yml name
  mode: GAME                              # semi-anarquia
  spawn-protection-radius: 50
  combat-tag-seconds: 15
  allow-end: true
  allow-nether: true

display:
  use-text-display-nametags: true
  nametag-offset-y: 0.35
  max-displays-per-player: 12

tags:
  change-cooldown-seconds: 5
  animate-staff-tags: true

events:
  enabled: true
  auto-schedule: true
  announce-minutes-before: 5

anticheat:
  enabled: true
  alert-staff: true
  discord-webhook: ""

cosmetics:
  enabled: true
  max-image-size: 96

economy:
  starting-balance: 1000
  currency-symbol: "$"

discord:
  punishments-webhook: ""
  events-webhook: ""
  anticheat-webhook: ""
```

---

## 16. Fases de implementação (1 plugin, ordem Savage)

Cada fase entrega **`game.jar` funcional** — não JARs separados.

| Fase | Entrega no mesmo plugin |
|------|-------------------------|
| **1** | `GamePlugin` + `Core` + `AccountContext` completo + DB |
| **2** | Auth + Tag + Medal + chat/tab + `/prefs` |
| **3** | Skin + Fake + handlers scan + Menu base |
| **4** | `CollectibleController` + 21 cat. + menus + aura |
| **5** | DisplayManager + ItemDisplay pets + nametags |
| **6** | Eventos Savage (Sumo/Skit/Team) + KOTH/Envoy |
| **7** | Friend + Party + Clan + Block + Report |
| **8** | Punições + `/cban` + anticheat + Discord |
| **9** | Survival + economy + codes + GameStats |
| **10** | Holograms NPC + PAPI + polish + stress test |

---

## 17. Critérios de aceite

- [ ] **Um único JAR:** `game.jar` (`name: game`) com todos os sistemas §1.5
- [ ] `AccountContext` com **todos os campos** §1.6
- [ ] `/login`, `/prefs`, `/skin`, `/friend`, `/party`, `/clan`, `/redeem`
- [ ] `/tag`, `/medal`, animação Owner, cooldown 5s
- [ ] `/cosmetics` hub 5 grupos + **21 categorias** + operators
- [ ] `/aura link` Imgur + presets PNG
- [ ] Eventos **Sumo + Skit + Team** (Eventos Savage) + KOTH/Envoy
- [ ] Anticheat VL + `/cban` + webhook Discord
- [ ] Survival: `/home`, `/rtp`, `/kit`, `/pay`, combat tag
- [ ] `ListenerHandler` + `@Command` scan automático
- [ ] Hologramas/NPCs via Display entities

---

## 18. Entregáveis

1. **Projeto Gradle único** → `game.jar`  
2. `plugin.yml` com **`name: game`** + todos YAML em `src/main/resources/`  
3. README PT-BR (instalação: copiar **`game.jar`**, configurar DB/Redis)  
4. Proxy **`proxyhost.jar`** opcional (segundo JAR mínimo — só punições)  
5. Referência cruzada: `PROMPT-COSMETICOS-SAVAGE-COMPLETO.md` para detalhe de cosméticos  

**Não entregar:** pasta com 10 plugins separados.

---

## 19. Notas para o implementador (IA ou dev)

- **1 plugin por servidor** — nome = papel: `game`, `lobbyhost`, `proxyhost`  
- Copiar **padrões** Savage: `Account`, `CollectibleOperator`, `CollectibleMenu`, `TagManager`, `ListenerHandler`, `@Command`  
- **Não** copiar NMS 1.8 (`BalloonEntity`, `v1_8_R3`) — usar Display 1.21  
- **Não** BedWars, EggWars, Duels, lobby arcade, `ServerFlag.NAME_TAG_CHANGE` por minigame  
- Cosméticos funcionam **no mundo semi-anarquia** (não só hub) — config `disabled-worlds` se necessário  
- `AccountContext.golds` = economia (Savage já tem campo `golds`)  
- `PunishmentNotifier` HTTP → Discord (como `BotController` / `discord-bot`)  
- Performance: DB async, displays com limite por jogador, AC throttle se TPS < 18  

---

## 20. Mapa Savage → plugin único (completo)

| Savage | Plugin 1.21 |
|--------|-------------|
| `core/Account.java` | `account/Account.java` |
| `core/AccountContext.java` | `account/context/AccountContext.java` |
| `core/Tag.java` | `account/context/tag/Tag.java` |
| `core/Medal.java` | `account/context/medal/Medal.java` |
| `core/Toggle.java` | `account/context/toggle/Toggle.java` |
| `core/CollectibleCategory.java` | `api/collectible/CollectibleCategory.java` |
| `core/CollectibleOperator.java` | `api/collectible/operator/*` |
| `core-bukkit/CollectibleMenu.java` | `bukkit/menu/collectible/*` |
| `core-bukkit/ImageAuraEngine.java` | `bukkit/display/aura/*` |
| `core-bukkit/TagManager.java` | `bukkit/manager/TagManager.java` |
| `core-bukkit/PreferenceMenu.java` | `bukkit/menu/account/preference/*` |
| `core-bukkit/SkinLibrary*.java` | `bukkit/menu/account/skin/*` |
| `core-bukkit/BukkitCommandHandler.java` | `bukkit/command/structure/*` |
| `core-bukkit/ListenerHandler.java` | `bukkit/listener/handler/*` |
| `core-bukkit/HologramManager.java` | `bukkit/manager/HologramManager` (TextDisplay) |
| `core-bukkit/NpcManager.java` | `bukkit/manager/NpcManager` (Interaction) |
| `auth/Auth.java` | `auth/*` |
| `core-bungee/FriendCommand.java` | `social/friend/*` |
| `core-bungee/PartyCommand.java` | `social/party/*` |
| `core-bungee/ClanCommand.java` | `api/clan/*` |
| `core-bungee/ReportCommand.java` | `social/report/*` |
| `core-bungee/CheatBanCommand.java` | `anticheat/` + `ModeratorCommands` |
| `Eventos/Sumo*.java` | `event/game/sumo/*` |
| `Eventos/Skit*.java` | `event/game/skit/*` |
| `Eventos/Team*.java` | `event/game/team/*` |
| `Eventos/Variable*.java` | `event/game/variable/*` |
| `core/Member.java` (arcade) | `stats/GameStats.java` (survival) |
| `lobby/*` (arcade) | ❌ não portar |
| `bedwars`, `duels`, etc. | ❌ não portar |

Documentos irmãos:
- **`docs/PROMPT-COSMETICOS-SAVAGE-COMPLETO.md`** — detalhe 21 categorias + títulos música
- **`docs/PROMPT-SEMIANARQUIA-1.21-COMPLETO.md`** — este arquivo (visão geral 1 plugin)

---

*Prompt: Paper 1.21 — plugin **`game`** (semi-anarquia) — arquitetura estilo Savage Network. Lobby = plugin **`lobbyhost`**.*
