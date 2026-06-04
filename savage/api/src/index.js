import cors from 'cors';
import express from 'express';
import rateLimit from 'express-rate-limit';
import helmet from 'helmet';
import { config } from './config.js';
import { errorHandler, notFoundHandler } from './middleware/errorHandler.js';
import apiRoutes from './routes/index.js';
import playerRoutes from './routes/player.js';

const app = express();

app.use(helmet());
app.use(express.json({ limit: '64kb' }));

app.use(
  cors({
    origin: config.corsOrigin === '*' ? true : config.corsOrigin,
    methods: ['GET', 'HEAD', 'OPTIONS'],
  }),
);

app.use(
  rateLimit({
    windowMs: 15 * 60 * 1000,
    max: config.rateLimitMax,
    standardHeaders: true,
    legacyHeaders: false,
    message: { error: 'RATE_LIMIT', message: 'Muitas requisições. Tente novamente em alguns minutos.' },
  }),
);

app.get('/health', async (_req, res) => {
  try {
    const { healthCheck } = await import('./db/pool.js');
    const ok = await healthCheck();
    res.status(ok ? 200 : 503).json({ ok });
  } catch {
    res.status(503).json({ ok: false });
  }
});

app.get('/api', (_req, res) => {
  res.json({
    success: true,
    error_code: -1,
    response: {
      player: '/api/player/:username',
      example: '/api/player/Steve',
      catalog: '/api/v1/catalog',
      legacy: '/api/v1',
    },
  });
});

app.use('/api/player', playerRoutes);
app.use('/api/v1', apiRoutes);
app.use(notFoundHandler);
app.use(errorHandler);

app.listen(config.port, config.host, () => {
  console.log(`[SavageLabs API] http://${config.host}:${config.port}`);
  console.log(`[SavageLabs API] ${config.branding.website}`);
});
