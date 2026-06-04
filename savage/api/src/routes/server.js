import { Router } from 'express';
import { query } from '../db/pool.js';

const router = Router();

router.get('/status', async (_req, res, next) => {
  try {
    const { rows } = await query(
      `SELECT COUNT(*)::int AS online
       FROM accounts
       WHERE COALESCE((data->'context'->>'online')::boolean, false) = true`,
    );
    res.json({
      success: true,
      error_code: -1,
      response: {
        onlinePlayers: rows[0]?.online ?? 0,
        website: 'https://savagemc.net',
      },
    });
  } catch (e) {
    next(e);
  }
});

export default router;
