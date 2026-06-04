import { createBot, loginBot } from './bot.js';
import { startHttpServer } from './http/server.js';

console.log('[SavageMC Bot] Iniciando...');

startHttpServer();

const client = createBot();
await loginBot(client);
