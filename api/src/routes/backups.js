import { Router } from 'express';
import { requireApiSecret } from '../middleware/apiSecret.js';
import { pool } from '../db/pool.js';

const router = Router();

router.use(requireApiSecret);

router.get('/', async (req, res, next) => {
  try {
    const limit = Math.min(parseInt(req.query.limit, 10) || 50, 200);
    const player = req.query.player;
    let sql = `SELECT id, player_id, player_name, backup_type, created_at
               FROM account_backups`;
    const params = [];
    if (player) {
      sql += ' WHERE LOWER(player_name) = LOWER(?)';
      params.push(player);
    }
    sql += ' ORDER BY id DESC LIMIT ?';
    params.push(limit);
    const [rows] = await pool.query(sql, params);
    res.json({ ok: true, entries: rows });
  } catch (err) {
    next(err);
  }
});

export default router;
