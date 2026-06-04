# SavageLabs Discord Bot

Bot Discord **separado** do Bungee, usando a [API pública](../api/) do SavageMC.

## Funcionalidades

| Comando | Descrição |
|---------|-----------|
| `/conta nome:` | Perfil do jogador (via `GET /api/player/:nick`) |
| `/stats nick: modo:` | Estatísticas BedWars |
| `/ticket` | Painel de tickets (botões) |

- **Tickets:** criar, assumir, fechar, logs em arquivo
- **Punições:** o Bungee envia `POST /punishment` para webhooks no Discord
- **Status:** atividade com jogadores online (`/api/v1/server/status`)

## Instalação

```bash
cd discord-bot
cp .env.example .env
# DISCORD_TOKEN, SAVAGE_API_URL, webhooks...
npm install
npm run deploy-commands
npm start
```

## Variáveis (.env)

| Variável | Descrição |
|----------|-----------|
| `DISCORD_TOKEN` | Token do bot |
| `DISCORD_GUILD_ID` | ID do servidor Discord |
| `SAVAGE_API_URL` | URL da API (ex. `http://127.0.0.1:3000`) |
| `WEBHOOK_PUNISHMENT_STAFF` | Webhook staff (punições) |
| `WEBHOOK_PUNISHMENT_PUBLIC` | Webhook público (ban/mute) |
| `BOT_HTTP_PORT` | Porta HTTP para o Bungee (padrão `4040`) |
| `BOT_HTTP_SECRET` | Segredo opcional (`X-Bot-Secret`) |

## Bungee (proxy)

O plugin **não** inicia mais o Javacord. Configure no ambiente do proxy:

```bash
SAVAGE_BOT_URL=http://127.0.0.1:4040
SAVAGE_BOT_SECRET=seu_segredo_opcional
```

## Ordem de subida

1. PostgreSQL  
2. `cd api && npm start`  
3. `cd discord-bot && npm start`  
4. Bungee / servidores Minecraft  

## Autores

SavageLabs — zrfadev, desenhar, henriaum
