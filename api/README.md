# SavageMC API (Node.js)

API HTTP **somente leitura** sobre o mesmo MySQL dos plugins (`accounts`, `punishments`, `player_reports`).

## Requisitos

- Node.js 18+
- MySQL acessível (mesmas credenciais do `game/config.yml`)

## Configuração

```bash
cd api
cp .env.example .env
# edite MYSQL_* com host, database, user e password
npm install
```

## Executar

```bash
npm run dev    # hot reload
npm start      # produção
```

Servidor padrão: `http://0.0.0.0:25575`

## Endpoints

| Rota | Descrição |
|------|-----------|
| `GET /health` | Saúde + ping MySQL |
| `GET /api/player/:nick` | Perfil formato Mush (bot/site) |
| `GET /api/v1/profile/:nick` | Igual ao player |
| `GET /api/v1/punishments` | Lista (`?player=`, `?category=`, `?active=`) |
| `GET /api/v1/punishments/:id` | Uma punição |
| `GET /api/v1/reports` | Reports abertos/fechados |
| `GET /api/v1/catalog` | Tags, ranks, medalhas |
| `GET /api/v1/server/status` | Contagem `context.online` |

## Integração

O **discord-bot** consome esta API. Os plugins Minecraft enviam webhooks para o bot (`POST :4040/punishment`), não para esta API.
