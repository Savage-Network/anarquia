import 'dotenv/config';

const parseOrigins = (value) => {
  if (!value || value === '*') return '*';
  return value.split(',').map((s) => s.trim()).filter(Boolean);
};

export const config = {
  host: process.env.HOST || '0.0.0.0',
  port: Number(process.env.PORT || 25575),
  corsOrigin: parseOrigins(process.env.CORS_ORIGIN || '*'),
  rateLimitMax: Number(process.env.RATE_LIMIT_MAX || 300),
  mysql: {
    host: process.env.MYSQL_HOST || '127.0.0.1',
    port: Number(process.env.MYSQL_PORT || 3306),
    database: process.env.MYSQL_DATABASE || 'anarquia',
    user: process.env.MYSQL_USER || 'root',
    password: process.env.MYSQL_PASSWORD || '',
    connectionLimit: Number(process.env.MYSQL_POOL_MAX || 10),
    waitForConnections: true,
    enableKeepAlive: true,
  },
  branding: {
    name: 'SavageMC',
    website: 'https://savagemc.net',
  },
};
