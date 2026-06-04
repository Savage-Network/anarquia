# SavageMC Discord Bot (Node.js)

Bot Discord separado da API. Consulta `../api` e expõe HTTP para os plugins publicarem punições e reports no Discord.

## Requisitos

- Node.js 18+
- API rodando (`SAVAGE_API_URL`)
- Bot criado no [Discord Developer Portal](https://discord.com/developers/applications)

## Configuração

```bash
cd discord-bot
cp .env.example .env
# DISCORD_TOKEN, DISCORD_GUILD_ID, webhooks, BOT_HTTP_SECRET
npm install
npm run deploy-commands
```

## Executar

```bash
npm run dev
npm start
```

- Discord: slash `/conta`, `/status`
- HTTP: `http://127.0.0.1:4040` (padrão)

## Webhooks (plugins → bot)

Header obrigatório se `BOT_HTTP_SECRET` estiver definido:

`x-bot-secret: <seu-segredo>`

### Punição

`POST /punishment`

```json
{
  "action": "create",
  "punishment": {
    "id": "abc123",
    "category": "BAN",
    "reason": "CHEATING",
    "cause": "Uso de cheat",
    "server": "game-1",
    "playerName": "Steve",
    "playerUuid": "uuid",
    "authorName": "Mod",
    "expiresAt": -1
  }
}
```

`action`: `create` | `revoke`

### Report

`POST /report`

```json
{
  "report": {
    "id": 1,
    "reporterName": "Alice",
    "targetName": "Bob",
    "reason": "CHEAT",
    "details": "fly",
    "serverId": "game-1"
  }
}
```

No `config.yml` do game/proxy (futuro), aponte para `http://127.0.0.1:4040` com o mesmo secret.
