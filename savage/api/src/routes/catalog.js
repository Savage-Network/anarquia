import { Router } from 'express';
import {
  BEDWARS_MODES,
  MEDALS,
  MINIGAMES,
  PUNISHMENT_CATEGORIES,
  RANKS,
  TAGS,
} from '../data/catalog.js';
import { config } from '../config.js';

const router = Router();

router.get('/', (_req, res) => {
  res.json({
    server: config.branding,
    endpoints: {
      profile: '/api/v1/profile/:nick',
      tags: '/api/v1/catalog/tags',
      medals: '/api/v1/catalog/medals',
      ranks: '/api/v1/catalog/ranks',
      punishments: '/api/v1/catalog/punishments',
      minigames: '/api/v1/catalog/minigames',
      bedwarsModes: '/api/v1/catalog/bedwars/modes',
    },
  });
});

router.get('/tags', (_req, res) => {
  res.json({ total: TAGS.length, tags: TAGS });
});

router.get('/medals', (_req, res) => {
  res.json({ total: MEDALS.length, medals: MEDALS });
});

router.get('/ranks', (_req, res) => {
  res.json({ total: RANKS.length, ranks: RANKS });
});

router.get('/punishments', (_req, res) => {
  res.json({ categories: PUNISHMENT_CATEGORIES });
});

router.get('/minigames', (_req, res) => {
  res.json({ minigames: MINIGAMES });
});

router.get('/bedwars/modes', (_req, res) => {
  res.json({ modes: BEDWARS_MODES });
});

export default router;
