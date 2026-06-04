# Anarquia — Plugins Paper 1.21 + Velocity

Monorepo Gradle que porta a rede **Savage 1.8** para **Java 21 / Paper 1.21** e **Velocity 3.3**. Cada subprojeto gera **um único JAR** com o nome do papel do servidor (sem prefixo `Savage`).

```
anarquia/                          ← rootProject.name = "anarquia"
├── settings.gradle.kts            ← :game, :auth, :lobbyhost, :eventos, :proxyhost
├── build.gradle.kts               ← tasks buildAll / cleanAll
├── gradlew / gradlew.bat
├── savage/                        ← Savage 1.8 original (referência; monorepo próprio)
├── game/           → game.jar
├── auth/           → auth.jar
├── lobbyhost/      → lobbyhost.jar
├── eventos/        → eventos.jar
└── proxyhost/      → proxyhost.jar
```

| Plugin | JAR | Plataforma | Equivalente Savage 1.8 |
|--------|-----|------------|-------------------------|
| `game` | `game.jar` | Paper | `game` + gameplay semi-anarquia |
| `auth` | `auth.jar` | Paper | `auth` |
| `lobbyhost` | `lobbyhost.jar` | Paper | `lobby` |
| `eventos` | `eventos.jar` | Paper | `Eventos` |
| `proxyhost` | `proxyhost.jar` | Velocity | `core-bungee` |

**IntelliJ:** abra a pasta `anarquia/` → import Gradle → todos os módulos aparecem juntos (`:game`, `:auth`, …) como no Savage.

---

## Estrutura Gradle (vs Savage 1.8)

### Savage original (`savage/`)

```
savage/
├── settings.gradle.kts     → :core, :core-bukkit, :core-bungee, :lobby, :auth, :bedwars…
├── build.gradle.kts        → subprojects { java, lombok, group com.minecraft }
├── core/                   → biblioteca (domínio)
├── core-bukkit/            → bootstrap Paper
├── core-bungee/            → bootstrap Bungee
└── lobby/, auth/, bedwars/ → implementation(project(":core")) + :core-bukkit
```

Cada plugin Bukkit **embute** `:core` + `:core-bukkit` no fat JAR (`tasks.jar { from(runtimeClasspath) }`).

### Anarquia 1.21 (este repo)

| Aspecto | Savage 1.8 | Anarquia 1.21 |
|---------|------------|---------------|
| Módulo `:core` compartilhado | ✅ `implementation(project(":core"))` | ❌ (por enquanto) |
| Core no JAR final | classes de `:core` copiadas no jar | classes **copiadas** em `com.minecraft.<plugin>.*` |
| Bootstrap Bukkit | `:core-bukkit` | pacotes `bukkit.*` dentro de cada plugin |
| Proxy | `:core-bungee` | `:proxyhost` (Velocity API) |
| Empacotamento | fat `jar` | **Shadow 9.0** + relocate para `*.libs.*` |
| Java | 8 (PandaSpigot) | **21** |
| API servidor | Spigot 1.8 | Paper **1.21.4** |

**Por que ainda não há `:core`?** Cada JAR roda em servidores **diferentes** (classloaders isolados). A cópia por namespace evita refatorar o monorepo inteiro antes da Fase 1/2 estabilizar. O próximo passo mecânico é extrair `:savage-core` (ou `:core`) como `java-library` e trocar cópias por `implementation(project(":core"))` + Shadow — o IntelliJ continuaria mostrando tudo junto.

### Pirâmide de dependências (meta futura)

```
                    ┌──────────────┐
                    │    :core     │  (futuro — Account, Tag, Medal, DB, Redis)
                    └──────┬───────┘
           ┌─────────────────┼─────────────────┐
           ▼                 ▼                 ▼
   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
   │ :core-bukkit │  │:core-velocity│  │   (libs)     │
   └──────┬───────┘  └──────┬───────┘  └──────────────┘
          │                 │
          ▼                 ▼
   game, auth,         proxyhost
   lobbyhost, eventos
```

**Hoje:** cada plugin é autossuficiente (core “inline”).

---

## Stack comum

- **Paper 1.21.4** + **Java 21** (Gradle 9.1, toolchain)
- **Velocity API 3.3** (`proxyhost`)
- **Adventure** para mensagens
- **Gson** → `AccountContext` JSON
- **HikariCP** + **MySQL** / **SQLite**
- **Jedis** (Redis, opcional)
- **Shadow 9.0** — libs relocadas para `com.minecraft.<plugin>.libs.*`

Banco **compartilhado:** aponte todos os `config.yml` para o mesmo MySQL → tabela `accounts` com coluna `data` (JSON idêntico entre plugins). Enums (`Tag`, `Medal`, `RankType`, …) devem ser **iguais** em todos os JARs (Gson).

---

## Testes locais (Paper 1.21.1)

Ambiente em `dev/` para validar antes de subir na rede:

**Windows (.bat — recomendado):**

| Na raiz do projeto | Em `dev/` |
|--------------------|-----------|
| `INSTALAR-DEV.bat` | `install-all.bat` |
| `TESTAR-LOBBY.bat` | `start-quick.bat` |
| `TESTAR-REDE.bat` | `start-network.bat` |

```batch
REM Primeira vez
INSTALAR-DEV.bat

REM Testar lobby + NPCs
TESTAR-LOBBY.bat
```

PowerShell alternativo: `dev\setup-local.ps1`, `dev\sync-plugins.ps1`, `dev\start-quick.ps1`

Detalhes: [dev/README.md](dev/README.md). NPCs do hub: `lobbyhost` + `/npc list`.

---

## Build

### Monorepo (raiz)

```bash
# Na pasta anarquia/
./gradlew projects          # lista :game, :auth, :lobbyhost, :eventos, :proxyhost
./gradlew buildAll          # shadowJar de todos
./gradlew cleanAll

# Um módulo (+ dependências Gradle, se houver :core no futuro)
./gradlew :lobbyhost:shadowJar
./gradlew :proxyhost:shadowJar
```

### Saída dos JARs

| Módulo | Caminho |
|--------|---------|
| game | `game/build/libs/game.jar` |
| auth | `auth/build/libs/auth.jar` |
| lobbyhost | `lobbyhost/build/libs/lobbyhost.jar` |
| eventos | `eventos/build/libs/eventos.jar` |
| proxyhost | `proxyhost/build/libs/proxyhost.jar` |

Cada JAR ~20 MB (drivers + pool + Gson + Jedis embutidos).

### Build individual (ainda funciona)

```bash
cd game && ./gradlew shadowJar
```

Cada subpasta mantém seu próprio `gradlew` para builds isolados sem abrir o monorepo.

### Savage 1.8 (referência)

```bash
cd savage && ./gradlew build    # monorepo separado — não incluído em :anarquia
```

---

## Deploy em rede (Velocity)

```
Velocity
└── plugins/proxyhost.jar
    ├── auth          → auth.jar
    ├── lobby         → lobbyhost.jar   (architect.json: server=HUB)
    ├── lobby-bedwars → lobbyhost.jar   (architect.json: server=HUB_BEDWARS)
    ├── eventos       → eventos.jar
    └── game          → game.jar
```

`proxyhost` carrega `Account` no login, roteia jogador não-premium → `auth-server`, demais → `default-fallback` (lobby). Comandos: `/account`, `/tell`, `/r`, `/staffchat`.

Após `/login` ou `/register` no `auth`, redirect via canal **BungeeCord** (`Connect` → lobby).

---

## Fase 2 — Tags, medalhas, chat (game + lobbyhost + eventos)

| Recurso | game | lobbyhost | eventos | auth |
|---------|------|-----------|---------|------|
| Enums Tag/Medal/Rank completos | ✅ | ✅ | ✅ | enums only (Gson) |
| `TagManager` (tab, nametag, animação) | ✅ | ✅ | ✅ | — |
| `MedalManager`, `InfoListener` (chat) | ✅ | ✅ | ✅ | — |
| `AboveNameDisplay` (TextDisplay 1.21) | ✅ | ✅ | ✅ | — |
| `/tag`, `/medal`, `/prefixtype`, `/customtitle`, `/prefs` | ✅ | ✅ | ✅ | — |

---

## Plugin `auth`

| Comando | Permissão | Descrição |
|---------|-----------|-----------|
| `/login <senha>` | — | Login |
| `/register <senha> <confirmar>` | — | Registro |
| `/changepassword` | — | Troca senha |
| `/authreload` | `auth.admin` | Reload config |
| `/account` | `auth.staff` | Inspeção de conta |

Fluxo: pre-login carrega conta → join congela jogador → após auth redirect BungeeCord → `lobby`. Senha **SHA-256** em `AuthMetadata`.

---

## Plugin `lobbyhost`

`architect.json` + `ServerType` → `MainArchitect` (HUB). Hotbar fixa, sidebar, **DiscordHologram** (TextDisplay 1.21).

Architects `HUB_BEDWARS`, `HUB_DUELS`, etc. → **TODO**.

---

## Plugin `eventos`

FSM: `WAITING → STARTING → PLAYING → ENDING`. Comandos staff: `/start`, `/sumo`, `/skit`, `/team`, `/variable`, `/setspawn`, `/winner`, `/leave`, …

---

## Plugin `proxyhost`

| Comando | Permissão | Descrição |
|---------|-----------|-----------|
| `/account info\|setrank\|settag\|…` | `proxyhost.account` | CRUD staff (mesmo schema MySQL) |
| `/tell` (`/msg`, `/w`) | — | PM cross-server |
| `/r` | — | Resposta |
| `/staffchat` (`/sc`) | staff / `proxyhost.staffchat` | Chat staff global |

Redis (opcional): canais `proxy-tell-channel`, `proxy-staffchat-channel`, `account-update-channel`.

---

## Pacotes Java

| Módulo | Pacote raiz | `Constant.SOURCE_DIR` |
|--------|-------------|------------------------|
| game | `com.minecraft.game` | `com.minecraft.game` |
| auth | `com.minecraft.auth` | `com.minecraft.auth` |
| lobbyhost | `com.minecraft.lobbyhost` | `com.minecraft.lobbyhost` |
| eventos | `com.minecraft.eventos` | `com.minecraft.eventos` |
| proxyhost | `com.minecraft.proxyhost` | `com.minecraft.proxyhost` |

Scan reflection: `bukkit.command` + `bukkit.listener` (Paper); Velocity usa registro explícito no `ProxyHostPlugin`.

---

## Status / Roadmap

| Plugin | Entregue | Pendente |
|--------|----------|----------|
| game | Fase 1 + Fase 2 (tags/medals/chat/titles) | anticheat, clan, punishments, homes, RTP, GUIs |
| auth | login/register/freeze/redirect | captcha visual, validation HTTP completa |
| lobbyhost | MainArchitect, hotbar, Discord hologram, **NPCs Display 1.21**, Fase 2 | architects HUB_*, menus, leaderboards |
| eventos | Game FSM, sumo, skit, team, Fase 2 | TeamSelectorMenu, SumoMapGenerator |
| proxyhost | account load, routing, tell/sc | LuckPerms bridge, punições proxy, MOTD dinâmico |

| Build / DX | Entregue | Pendente |
|------------|----------|----------|
| Monorepo `:anarquia` | ✅ `settings.gradle.kts` + `buildAll` | módulo `:core` compartilhado |
| IntelliJ multi-módulo | ✅ import raiz | `copyJar` por env (deploy local) |

---

## Checklist — novo módulo no monorepo

1. Criar pasta `novomodulo/` com `src/main/java` + `plugin.yml` ou `@Plugin` (Velocity)
2. Adicionar `include("novomodulo")` em `settings.gradle.kts` (raiz)
3. Criar `novomodulo/build.gradle.kts` (Shadow + deps Paper ou Velocity)
4. Adicionar `:novomodulo:shadowJar` em `buildAll` no `build.gradle.kts` raiz
5. **Reload Gradle** no IntelliJ

---

## Problemas comuns

| Problema | Solução |
|----------|---------|
| Módulo não aparece no IDE | Abrir **raiz** `anarquia/`, não subpasta `game/` |
| Gson falha ao deserializar Tag | Sincronizar enums em todos os plugins (`_replicate_enums_only.ps1`) |
| Build JDK 25 | Wrapper Gradle 9.1 + Shadow 9.0 |
| JAR grande duplicado | Esperado — cada plugin embute o core até existir `:core` |

---

## Referência Savage 1.8

O documento de estrutura Gradle do Savage (`core` → `core-bukkit` / `core-bungee` → minigames) está em `savage/settings.gradle.kts`. Use `savage/` apenas como leitura ao portar comportamentos; **não** misture classpath Savage 1.8 com plugins 1.21.

---

*Monorepo Anarquia — 1 JAR por servidor — migração Savage → Paper 1.21 / Velocity.*
