import { Router } from 'express';
import { requireApiSecret } from '../middleware/apiSecret.js';
import { pool } from '../db/pool.js';
import { countOnlinePlayers } from '../services/presenceService.js';

const router = Router();

router.use(requireApiSecret);

router.get('/dashboard', async (_req, res, next) => {
  try {
    const [[{ openReports }]] = await pool.query(
      "SELECT COUNT(*) AS openReports FROM player_reports WHERE status = 'OPEN'",
    );
    const [[{ activeBans }]] = await pool.query(
      "SELECT COUNT(*) AS activeBans FROM punishments WHERE data LIKE '%\"active\":true%'",
    );
    const online = await countOnlinePlayers();
    const [recentReports] = await pool.query(
      `SELECT id, reporter_name, target_name, reason, server_id, created_at
       FROM player_reports WHERE status = 'OPEN' ORDER BY id DESC LIMIT 10`,
    );
    const [feed] = await pool.query(
      `SELECT type, actor_name, message, server_id, created_at
       FROM activity_feed ORDER BY id DESC LIMIT 15`,
    );

    res.json({
      ok: true,
      online,
      openReports,
      activeBans,
      recentReports,
      feed,
    });
  } catch (err) {
    next(err);
  }
});

export default router;
