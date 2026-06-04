import { Router } from 'express';
import { getFullProfile } from '../services/profileService.js';
import { mushNotFound, toMushPlayer } from '../formatters/mushPlayer.js';

const router = Router();

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

export default router;
