import { Router } from 'express';
import { getPunishmentById, listPunishments } from '../services/punishmentService.js';

const router = Router();

router.get('/', async (req, res, next) => {
  try {
    const limit = Number(req.query.limit) || 50;
    const offset = Number(req.query.offset) || 0;
    const result = await listPunishments({
      category: req.query.category,
      player: req.query.player,
      active: req.query.active,
      limit,
      offset,
    });
    res.json(result);
  } catch (e) {
    next(e);
  }
});

router.get('/:id', async (req, res, next) => {
  try {
    const punishment = await getPunishmentById(req.params.id);
    if (!punishment) {
      return res.status(404).json({ error: 'PUNISHMENT_NOT_FOUND', message: 'Punição não encontrada.' });
    }
    res.json({ punishment });
  } catch (e) {
    next(e);
  }
});

export default router;
