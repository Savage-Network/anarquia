import { Router } from 'express';
import { searchAccounts } from '../services/accountService.js';
import { getFullProfile } from '../services/profileService.js';
import { getPlayerPunishments } from '../services/punishmentService.js';
import { mushNotFound, toMushPlayer } from '../formatters/mushPlayer.js';

const router = Router();

router.get('/search', async (req, res, next) => {
  try {
    const q = String(req.query.q || '').trim();
    if (q.length < 2) {
      return res.status(400).json({
        success: false,
        error_code: 2,
        error_message: 'Use pelo menos 2 caracteres.',
        response: null,
      });
    }
    const accounts = await searchAccounts(q, Number(req.query.limit) || 20);
    res.json({
      success: true,
      error_code: -1,
      response: {
        query: q,
        total: accounts.length,
        players: accounts.map((a) => ({
          unique_id: a.id,
          username: a.name,
          connected: a.context?.online ?? false,
        })),
      },
    });
  } catch (e) {
    next(e);
  }
});

router.get('/:identifier', async (req, res, next) => {
  try {
    const profile = await getFullProfile(req.params.identifier);
    if (!profile) {
      return res.status(404).json(mushNotFound(req.params.identifier));
    }
    res.json(toMushPlayer(profile));
  } catch (e) {
    next(e);
  }
});

router.get('/:identifier/punishments', async (req, res, next) => {
  try {
    const data = await getPlayerPunishments(req.params.identifier, {
      category: req.query.category,
      active: req.query.active,
    });
    if (!data) {
      return res.status(404).json(mushNotFound(req.params.identifier));
    }
    res.json({
      success: true,
      error_code: -1,
      response: data,
    });
  } catch (e) {
    next(e);
  }
});

export default router;
