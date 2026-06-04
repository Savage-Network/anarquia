import 'dotenv/config';

const parseOrigins = (value) => {
  if (!value || value === '*') return '*';
  return value.split(',').map((s) => s.trim()).filter(Boolean);
};

export const config = {
  host: process.env.HOST || '0.0.0.0',
  port: Number(process.env.PORT || 3000),
  corsOrigin: parseOrigins(process.env.CORS_ORIGIN || '*'),
  rateLimitMax: Number(process.env.RATE_LIMIT_MAX || 300),
  pg: {
    host: process.env.PGHOST || '127.0.0.1',
    port: Number(process.env.PGPORT || 5432),
    database: process.env.PGDATABASE || 'savagemc',
    user: process.env.PGUSER || 'savagemc',
    password: process.env.PGPASSWORD || '',
    max: Number(process.env.PG_POOL_MAX || 10),
    idleTimeoutMillis: 30_000,
  },
  branding: {
    name: 'SavageLabs',
    website: 'https://savagemc.net',
    authors: ['zrfadev', 'desenhar', 'henriaum'],
  },
};
