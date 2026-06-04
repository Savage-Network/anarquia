# SavageLabs Public API

API pública no estilo [Mush](https://mush.com.br/api/player/zrwfalegal) para o **SavageMC** (`savagemc.net`).

## Instalação

```bash
cd api
cp .env.example .env
npm install
npm start
```

## Endpoint principal (formato Mush)

```
GET /api/player/:username
GET /api/player/:uuid
```

### Exemplo

```bash
curl http://localhost:3000/api/player/zrwfalegal
```

### Resposta

```json
{
  "success": true,
  "error_code": -1,
  "response": {
    "account": { "unique_id": "...", "username": "zrwfalegal", "type": "premium" },
    "best_tag": { "name": "VIP", "color": "#ffaa00", "data": { "plus": "black" } },
    "rank_tag": { ... },
    "clan": { "name": "...", "tag": "...", "tag_color": "#55ff55" },
    "connected": false,
    "medal": "wg",
    "medals": ["none", "toxic", ...],
    "stats": {
      "bedwars": { "solo_wins": 120, "games_played": 255, "level": 53, ... },
      "skywars": { ... },
      "duels": { ... },
      "pvp": { ... },
      "eggwars": { ... },
      "thebridge": { ... }
    },
    "tags": ["member", "vip", ...],
    "punishments": { "active": [], "history": [], ... }
  }
}
```

Jogador não encontrado:

```json
{
  "success": false,
  "error_code": 1,
  "error_message": "Jogador \"nick\" não encontrado.",
  "response": null
}
```

## Outras rotas

| Rota | Descrição |
|------|-----------|
| `GET /api` | Links da API |
| `GET /api/v1/catalog/tags` | Catálogo de tags |
| `GET /api/v1/catalog/medals` | Catálogo de medalhas |
| `GET /api/v1/catalog/ranks` | Catálogo de ranks |
| `GET /api/v1/players/search?q=` | Busca de jogadores |
| `GET /api/v1/stats/bedwars/:nick` | Stats de um modo |

Aliases do perfil Mush: `/api/v1/profile/:nick` e `/api/v1/players/:nick`.

## Deploy

Exemplo: `https://api.savagemc.net/api/player/zrwfalegal`

## Autores

SavageLabs — zrfadev, desenhar, henriaum
