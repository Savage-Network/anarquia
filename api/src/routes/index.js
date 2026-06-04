import { Router } from 'express';
import catalogRoutes from './catalog.js';
import profileRoutes from './profile.js';
import punishmentsRoutes from './punishments.js';
import reportsRoutes from './reports.js';
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
    name: 'SavageMC Public API',
    version: '1.0.0',
    website: config.branding.website,
    status: 'online',
    database,
    endpoints: {
      player: '/api/player/:nick',
      profile: '/api/v1/profile/:nick',
      punishments: '/api/v1/punishments',
      reports: '/api/v1/reports',
      catalog: '/api/v1/catalog',
      serverStatus: '/api/v1/server/status',
    },
  });
});

router.use('/profile', profileRoutes);
router.use('/catalog', catalogRoutes);
router.use('/punishments', punishmentsRoutes);
router.use('/reports', reportsRoutes);
router.use('/server', serverRoutes);

export default router;
