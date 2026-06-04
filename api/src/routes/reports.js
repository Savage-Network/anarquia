import { Router } from 'express';
import { listReports } from '../services/reportService.js';

const router = Router();

router.get('/', async (req, res, next) => {
  try {
    const result = await listReports({
      status: req.query.status,
      target: req.query.target,
      limit: Number(req.query.limit) || 50,
      offset: Number(req.query.offset) || 0,
    });
    res.json(result);
  } catch (e) {
    if (e.code === 'ER_NO_SUCH_TABLE') {
      return res.json({ total: 0, limit: 50, offset: 0, items: [] });
    }
    next(e);
  }
});

export default router;
