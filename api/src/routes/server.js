import { Router } from 'express';
import { countOnlinePlayers } from '../services/presenceService.js';

const router = Router();

router.get('/status', async (_req, res, next) => {
  try {
    const online = await countOnlinePlayers();
    res.json({
      success: true,
      error_code: -1,
      onlinePlayers: online,
      website: 'https://savagemc.net',
    });
  } catch (e) {
    next(e);
  }
});

export default router;
