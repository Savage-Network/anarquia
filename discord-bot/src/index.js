import { createBot, loginBot } from './bot.js';
import { config } from './config.js';
import { healthCheck } from './db/pool.js';
import { ensurePresenceSchema } from './services/presenceService.js';
import { ensureTicketSchema } from './services/ticketSchema.js';
import { startHttpServer } from './http/server.js';

console.log('[SavageMC Bot] Iniciando...');

try {
  const dbOk = await healthCheck();
  if (dbOk) {
    console.log(`[SavageMC Bot] MySQL OK (${config.mysql.host}/${config.mysql.database})`);
    await ensurePresenceSchema();
    await ensureTicketSchema();
  } else {
    console.warn('[SavageMC Bot] MySQL respondeu, mas health check falhou.');
  }
} catch (e) {
  console.error(
    `[SavageMC Bot] ERRO MySQL: ${e.message}. ` +
      'Configure MYSQL_* no .env (mesmo banco do game/API).',
  );
}

startHttpServer();

const client = createBot();
await loginBot(client);
