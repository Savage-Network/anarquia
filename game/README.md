# game — plugin único (Paper 1.21, Java 21)

Servidor **semi-anarquia** estilo Savage Network, consolidado em **1 JAR** (`game.jar`).
**Game design completo (PvP, economia, eventos, temporadas):** [`docs/SEMI-ANARQUIA-DESIGN.md`](docs/SEMI-ANARQUIA-DESIGN.md)

## Status atual — Fase 1

A Fase 1 entrega a **fundação** sobre a qual todas as próximas fases serao construídas:

- Projeto Gradle Kotlin DSL standalone → `build/libs/game.jar` via Shadow.
- `plugin.yml` único com `name: game`, `api-version: 1.21`.
- Bootstrap espelhando `BukkitCore.onEnable` do Savage: `Core.load → DB → Redis → Controllers → Tasks → Scan`.
- `AccountContext` **com todos os campos do §1.6 da spec** (route, rankInfo, fake, skin, friend, toggle, auth, clan, joinMessage, imageAura, history, tag, medal, customTitle, golds, level, collectibles, favoritos, permissions, restricts, medals, blocks, cooldown, parkourBestTime, …).
- `Account` + `AccountController` (cache UUID + async save).
- `HikariProvider` com **MySQL e SQLite** (default SQLite para dev).
- `RedisProvider` lazy (config-flag `redis.enabled`).
- `BukkitCommandHandler` + `ListenerHandler` (scan reflection idêntico ao Savage).
- `@Command` / `@Completer` (anotações).
- `AccountListener` (load async no pre-login, save async no quit).
- `TaskManager` com auto-save periódico de contas dirty.
- Comando de validação `/account [jogador]`.
- `/gamereload` (admin).

## Como construir

Requer Gradle 8.10+ e JDK 21.

```bash
# Dentro de game/
gradle shadowJar
# JAR final em build/libs/game.jar
```

> Se preferir wrapper, copie `gradlew` / `gradlew.bat` / `gradle/wrapper/` de qualquer
> projeto Paper 1.21 atualizado e rode `./gradlew shadowJar`.

## Como instalar

1. Coloque `game.jar` em `<servidor-paper-1.21>/plugins/`.
2. Inicie o servidor uma vez para gerar `plugins/game/config.yml`.
3. (Opcional) Configure `database.type: MYSQL` e credenciais.
4. (Opcional) Configure `redis.enabled: true` se houver rede cross-server.
5. Reinicie. Entre e rode `/account` para validar.

## Estrutura do projeto

```
game/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── src/main/
    ├── java/com/minecraft/game/
    │   ├── GamePlugin.java          ← extends JavaPlugin (= BukkitCore Savage)
    │   ├── Core.java                ← singletons estáticos
    │   ├── Constant.java
    │   ├── account/
    │   │   ├── Account.java
    │   │   ├── AccountController.java
    │   │   └── context/
    │   │       ├── AccountContext.java    ← TODOS os campos §1.6
    │   │       └── objects/
    │   │           ├── aura/ImageAuraMetadata.java
    │   │           ├── auth/{AuthMetadata, AuthState}.java
    │   │           ├── block/BlockedPlayer.java
    │   │           ├── clan/ClanMetadata.java
    │   │           ├── cosmetic/CosmeticEntry.java
    │   │           ├── fake/Fake.java
    │   │           ├── friend/{Friend, FriendMetadata, FriendRequest}.java
    │   │           ├── joinmessage/JoinMessageMetadata.java
    │   │           ├── medal/Medal.java
    │   │           ├── permission/Permission.java
    │   │           ├── punishment/PunishmentHistory.java
    │   │           ├── rank/{Rank, RankInfo, RankType}.java
    │   │           ├── restrict/Restrict.java
    │   │           ├── route/RouteContext.java
    │   │           ├── skin/SkinMetadata.java
    │   │           ├── tag/{Tag, TagRole}.java
    │   │           ├── title/CustomTitle.java
    │   │           └── toggle/{Toggle, TagPrefix}.java
    │   ├── backend/
    │   │   ├── database/HikariProvider.java
    │   │   ├── data/AccountData.java
    │   │   └── redis/RedisProvider.java
    │   ├── bukkit/
    │   │   ├── command/
    │   │   │   ├── annotation/{Command, Completer}.java
    │   │   │   ├── structure/{BukkitCommandHandler, BukkitCommandContext, CommandInheritor}.java
    │   │   │   └── single/{AccountCommand, ReloadCommand}.java
    │   │   ├── listener/
    │   │   │   ├── AccountListener.java
    │   │   │   └── handler/ListenerHandler.java
    │   │   └── manager/TaskManager.java
    │   └── util/ClassScanner.java
    └── resources/
        ├── plugin.yml
        ├── config.yml
        └── messages.yml
```

## Roadmap — fases restantes (referência §16 da spec)

| Fase | Conteúdo |
|------|----------|
| **2** | Sistema de tags (catálogo completo + animação Owner), medalhas, chat/tab, `/prefs`, `Toggle` GUI |
| **3** | Auth (`/login`, `/register`, captcha), Skin (`/skin` + library), Fake, Menu base |
| **4** | 21 categorias de cosméticos, `CollectibleController` reflection, `ImageAuraEngine` (DUST 1.21) |
| **5** | `DisplayManager` + ItemDisplay pets/balões/asas + TextDisplay nametags |
| **6** | Eventos Savage (Sumo/Skit/Team/Variable) + KOTH/Envoy/Boss |
| **7** | Friend, Party, **Clan** ✅ (base), alianças/rivalidades/guerra, Block, Report |
| **8** | Punições, `/cban`, anticheat (`AnticheatEngine`), Discord webhook |
| **9** | Survival (`/home`, `/rtp`, `/kit`, `/pay`, combat tag), economy, codes, GameStats |
| **10** | Hologramas leaderboard, NPCs Interaction, PlaceholderAPI expansion, polish, stress test |

## Notas de design

- **Compatibilidade Gson** com o JSON do Savage — campos identicos, mesmo tipo lombok `@Getter @Setter`.
- **Sem NMS**: tudo via Bukkit/Adventure API públicas. Pets/balões na Fase 5 usam `ItemDisplay` (1.21).
- **Sem multi-módulo**: 1 projeto Gradle, 1 JAR. `lobbyhost.jar` e `proxyhost.jar` (se vierem) seriam projetos irmãos.
- **Sem arcades**: BedWars, EggWars, Duels, etc. ficam de fora por design (spec §1.5 categoria ❌).

## Configuração mínima do servidor

`paper-global.yml`:
```yaml
unsupported-settings:
  allow-permanent-block-break-exploits: false
```

`server.properties`:
```
spawn-protection=0   # plugin gerencia via spawn-protection-radius
online-mode=false    # ou true, conforme política do servidor
```

LuckPerms recomendado para administrar `game.*` permissions.
