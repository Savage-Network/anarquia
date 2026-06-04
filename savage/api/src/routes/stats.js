import { Router } from 'express';
import { MINIGAMES } from '../data/catalog.js';
import { getAllMinigameStats, getMinigameStats } from '../services/memberService.js';

const router = Router();
const MODES = new Set([...MINIGAMES.map((m) => m.id), 'all']);

router.get('/', (_req, res) => {
  res.json({
    modes: MINIGAMES.map((m) => m.id),
    examples: [
      '/api/v1/stats/all/Steve',
      '/api/v1/stats/bedwars/Steve',
      '/api/v1/stats/bedwars/Steve/BEDWARS_SOLO',
      '/api/v1/stats/bedwars/Steve/GENERAL',
    ],
  });
});

router.get('/all/:identifier', async (req, res, next) => {
  try {
    const data = await getAllMinigameStats(req.params.identifier);
    if (!data) {
      return res.status(404).json({ error: 'PLAYER_NOT_FOUND', message: 'Jogador não encontrado.' });
    }
    res.json(data);
  } catch (e) {
    next(e);
  }
});

router.get('/:mode/:identifier/:arcade?', async (req, res, next) => {
  try {
    const { mode, identifier, arcade } = req.params;

    if (!MODES.has(mode)) {
      return res.status(400).json({
        error: 'INVALID_MODE',
        message: `Modo inválido. Use: ${[...MODES].join(', ')}`,
      });
    }

    if (mode === 'all') {
      const data = await getAllMinigameStats(identifier);
      if (!data) {
        return res.status(404).json({ error: 'PLAYER_NOT_FOUND', message: 'Jogador não encontrado.' });
      }
      return res.json(data);
    }

    const data = await getMinigameStats(mode, identifier, arcade);
    if (data.error === 'PLAYER_NOT_FOUND') {
      return res.status(404).json({ error: data.error, message: 'Jogador não encontrado.' });
    }
    if (data.error === 'INVALID_MODE') {
      return res.status(400).json({ error: data.error, message: 'Minigame inválido.' });
    }
    if (data.error === 'NO_DATA') {
      return res.status(404).json({
        error: data.error,
        message: 'Jogador sem dados neste minigame.',
        player: data.player,
      });
    }
    if (data.error === 'MODE_NOT_FOUND') {
      return res.status(404).json({
        error: data.error,
        message: 'Modo/arcade não encontrado para este jogador.',
        player: data.player,
      });
    }

    res.json(data);
  } catch (e) {
    next(e);
  }
});

export default router;
