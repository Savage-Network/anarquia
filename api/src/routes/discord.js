import { Router } from 'express';
import { requireApiSecret } from '../middleware/apiSecret.js';
import {
  confirmLink,
  findLinkByDiscord,
  findLinkByPlayer,
  formatLink,
  unlinkByDiscord,
} from '../services/linkService.js';
import { getFullProfile } from '../services/profileService.js';

const router = Router();

router.get('/link/discord/:discordId', async (req, res, next) => {
  try {
    const link = await findLinkByDiscord(req.params.discordId);
    if (!link) {
      return res.status(404).json({ ok: false, error: 'NOT_FOUND' });
    }
    res.json({ ok: true, link: formatLink(link) });
  } catch (e) {
    next(e);
  }
});

router.get('/link/player/:identifier', async (req, res, next) => {
  try {
    const link = await findLinkByPlayer(req.params.identifier);
    if (!link) {
      return res.status(404).json({ ok: false, error: 'NOT_FOUND' });
    }
    res.json({ ok: true, link: formatLink(link) });
  } catch (e) {
    next(e);
  }
});

router.post('/link', requireApiSecret, async (req, res, next) => {
  try {
    const { code, discordId, discordTag } = req.body || {};
    if (!code || !discordId) {
      return res.status(400).json({
        ok: false,
        error: 'MISSING_FIELDS',
        message: 'Informe code e discordId.',
      });
    }

    const result = await confirmLink(code, discordId, discordTag);
    if (!result.ok) {
      const status =
        result.error === 'INVALID_CODE' || result.error === 'CODE_NOT_FOUND'
          ? 404
          : 409;
      return res.status(status).json(result);
    }

    res.json(result);
  } catch (e) {
    next(e);
  }
});

router.delete('/link', requireApiSecret, async (req, res, next) => {
  try {
    const discordId = req.body?.discordId || req.query?.discordId;
    if (!discordId) {
      return res.status(400).json({
        ok: false,
        error: 'MISSING_FIELDS',
        message: 'Informe discordId.',
      });
    }

    const result = await unlinkByDiscord(discordId);
    if (!result.ok) {
      return res.status(404).json(result);
    }

    res.json(result);
  } catch (e) {
    next(e);
  }
});

router.get('/menu/:identifier', async (req, res, next) => {
  try {
    const profile = await getFullProfile(req.params.identifier);
    if (!profile) {
      return res.status(404).json({
        ok: false,
        error: 'NOT_FOUND',
        message: 'Jogador nao encontrado.',
      });
    }

    res.json({ ok: true, profile });
  } catch (e) {
    next(e);
  }
});

export default router;
