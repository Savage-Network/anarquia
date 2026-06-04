import { Router } from 'express';
import catalogRoutes from './catalog.js';
import profileRoutes from './profile.js';
import playersRoutes from './players.js';
import punishmentsRoutes from './punishments.js';
import statsRoutes from './stats.js';
import serverRoutes from './server.js';
import { healthCheck } from '../db/pool.js';
import { config } from '../config.js';

const router = Router();

router.get('/', async (_req, res) => {
  let database = 'down';
  try {
    database = (await healthCheck()) ? 'up' : 'down';
  } catch {
    database = 'down';
  }

  res.json({
    name: 'SavageLabs Public API',
    version: '1.0.0',
    website: config.branding.website,
    status: 'online',
    database,
    docs: '/api/v1',
    player: '/api/player/:nick',
    profile: '/api/v1/profile/:nick',
  });
});

router.use('/profile', profileRoutes);
router.use('/catalog', catalogRoutes);
router.use('/players', playersRoutes);
router.use('/punishments', punishmentsRoutes);
router.use('/stats', statsRoutes);
router.use('/server', serverRoutes);

export default router;
