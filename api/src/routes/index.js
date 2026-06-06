import { Router } from 'express';
import catalogRoutes from './catalog.js';
import discordRoutes from './discord.js';
import profileRoutes from './profile.js';
import punishmentsRoutes from './punishments.js';
import reportsRoutes from './reports.js';
import serverRoutes from './server.js';
import statsRoutes from './stats.js';
import staffRoutes from './staff.js';
import backupsRoutes from './backups.js';
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
      statsTop: '/api/v1/stats/top/:type',
      statsFeed: '/api/v1/stats/feed',
      staffDashboard: '/api/v1/staff/dashboard',
      backups: '/api/v1/backups',
      discordLink: '/api/v1/discord/link',
      discordMenu: '/api/v1/discord/menu/:nick',
    },
  });
});

router.use('/discord', discordRoutes);
router.use('/profile', profileRoutes);
router.use('/catalog', catalogRoutes);
router.use('/punishments', punishmentsRoutes);
router.use('/reports', reportsRoutes);
router.use('/server', serverRoutes);
router.use('/stats', statsRoutes);
router.use('/staff', staffRoutes);
router.use('/backups', backupsRoutes);

export default router;
