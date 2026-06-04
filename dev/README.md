# Testes locais — Paper 1.21.1

Ambiente rápido para validar plugins **antes** de subir na rede de produção.

## Pré-requisitos

- **Java 21** (`java -version`)
- Windows: PowerShell 5+ (scripts `.ps1`)
- ~500 MB de download na primeira execução (Paper + Velocity)

## Modos de teste

| Modo | Script | O que testa |
|------|--------|-------------|
| **Rápido** | `start-quick.ps1` | Só **lobby** Paper 1.21.1 + `lobbyhost.jar` (NPCs, hotbar, tags) |
| **Rede** | `start-network.ps1` | Velocity + auth + lobby + game (fluxo completo) |

## Passo a passo (primeira vez) — **use os .bat**

```batch
cd dev
install-all.bat
```

Ou na **raiz do projeto**:

- `INSTALAR-DEV.bat` — instalação completa
- `TESTAR-LOBBY.bat` — instala (se preciso) + inicia lobby
- `TESTAR-REDE.bat` — instala (se preciso) + inicia rede Velocity

Depois:

| Ação | Arquivo |
|------|---------|
| Só lobby (NPCs) | `dev\start-quick.bat` |
| Rede completa | `dev\start-network.bat` |
| Rebuild plugins | `dev\sync-plugins.bat` |
| Redownload Paper | `dev\setup-local.bat` |

Scripts PowerShell (`.ps1`) continuam disponíveis como alternativa.

```powershell
cd dev
.\install-all.ps1   # se criar; ou setup + sync manual
.\start-quick.ps1
```

Conecte no Minecraft **1.21.1** em `localhost` (modo rápido) ou `localhost:25577` (rede via Velocity).

## Estrutura gerada

```
dev/runtime/
├── shared/
│   └── anarquia-dev.db      # SQLite compartilhado (copiado nos plugins)
├── quick-lobby/             # Paper 1.21.1 — teste rápido
│   ├── plugins/lobbyhost.jar
│   └── plugins/lobbyhost/   # configs de dev
├── velocity/
├── auth/
├── lobby/
└── game/
```

## Configurações de dev

Templates em `dev/templates/`:

- `online-mode=false`, `allow-flight=true`
- SQLite único em `shared/anarquia-dev.db`
- `proxyhost`: `dev.enabled: true` (pula auth no proxy)
- `auth`: `dev.bypass-auth: true` (login automático no servidor auth)
- `lobbyhost`: `npcs.yml` + `architect.json` com spawn em Y=100

Após editar templates, rode `.\sync-plugins.ps1` (copia configs de novo).

## Comandos úteis no jogo (lobby)

| Comando | Uso |
|---------|-----|
| `/npc list` | Lista NPCs definidos/ativos |
| `/npc reload` | Recarrega `npcs.yml` + respawn |
| `/npc spawn npc_bw` | Spawna NPC na sua posição (teste) |
| `/lobbyprocess npcReload` | Mesmo que reload de NPCs |
| `/tag`, `/medal` | Fase 2 identidade |

## Build manual

Na raiz do monorepo:

```powershell
cd ..
.\gradlew.bat buildAll
.\dev\sync-plugins.ps1
```

## Versão Minecraft

Scripts fixam **Paper build 1.21.1** (API dos plugins: `1.21.1-R0.1-SNAPSHOT`).

Cliente Minecraft deve ser **1.21.1** (mesma versão do servidor).

## Problemas comuns

| Problema | Solução |
|----------|---------|
| NPCs não aparecem | Confirme spawn em `architect.json`; `/npc list` deve mostrar `[ativo]` |
| "Invalid session" | Use `online-mode=false` (já no template) |
| Proxy não conecta | Servidores Paper precisam `velocity-support` + secret igual em `velocity.toml` |
| Porta em uso | Edite portas em `dev/local.env.ps1` |
