import { Router } from 'express';
import { pool } from '../db/pool.js';

const router = Router();

router.get('/top/:type', async (req, res, next) => {
  try {
    const type = (req.params.type || 'kills').toLowerCase();
    const limit = Math.min(parseInt(req.query.limit, 10) || 10, 50);
    const order = ['kills', 'deaths', 'golds', 'kdr', 'best_kill_streak'].includes(type)
      ? type
      : 'kills';

    const sql = order === 'kdr'
      ? `SELECT display_name, kills, deaths,
           CASE WHEN deaths = 0 THEN kills ELSE ROUND(kills / deaths, 2) END AS kdr
         FROM network_stats WHERE category = 'player'
         ORDER BY kdr DESC LIMIT ?`
      : `SELECT display_name, kills, deaths, golds, best_kill_streak
         FROM network_stats WHERE category = 'player'
         ORDER BY ${order} DESC LIMIT ?`;

    const [rows] = await pool.query(sql, [limit]);
    res.json({ ok: true, type: order, limit, entries: rows });
  } catch (err) {
    next(err);
  }
});

router.get('/feed', async (req, res, next) => {
  try {
    const limit = Math.min(parseInt(req.query.limit, 10) || 20, 50);
    const [rows] = await pool.query(
      `SELECT id, type, actor_name, message, server_id, created_at
       FROM activity_feed ORDER BY id DESC LIMIT ?`,
      [limit],
    );
    res.json({ ok: true, entries: rows });
  } catch (err) {
    next(err);
  }
});

export default router;
