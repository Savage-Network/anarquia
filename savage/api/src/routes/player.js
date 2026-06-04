import { Router } from 'express';
import { getFullProfile } from '../services/profileService.js';
import { mushNotFound, toMushPlayer } from '../formatters/mushPlayer.js';

const router = Router();

/**
 * Formato Mush — perfil completo num endpoint:
 * GET /api/player/zrwfalegal
 */
router.get('/:identifier', async (req, res, next) => {
  try {
    const identifier = req.params.identifier;
    const profile = await getFullProfile(identifier);

    if (!profile) {
      return res.status(404).json(mushNotFound(identifier));
    }

    res.json(toMushPlayer(profile));
  } catch (e) {
    next(e);
  }
});

export default router;
