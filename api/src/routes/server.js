import { Router } from 'express';
import { query } from '../db/pool.js';

const router = Router();

router.get('/status', async (_req, res, next) => {
  try {
    const { rows } = await query(
      `SELECT COUNT(*) AS online FROM accounts
       WHERE JSON_EXTRACT(data, '$.context.online') = true`,
    );
    const online = Number(rows[0]?.online ?? 0);
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
