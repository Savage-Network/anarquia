import {
  ActivityType,
  Client,
  GatewayIntentBits,
  Partials,
  REST,
  Routes,
} from 'discord.js';
import { config } from './config.js';
import { fetchServerStatus } from './api/client.js';
import { commands } from './commands/index.js';
import { handleInteraction } from './handlers/interactions.js';
import { handleMessage } from './handlers/messages.js';

export function createBot() {
  const client = new Client({
    intents: [
      GatewayIntentBits.Guilds,
      GatewayIntentBits.GuildMessages,
      GatewayIntentBits.MessageContent,
      GatewayIntentBits.GuildMembers,
    ],
    partials: [Partials.Channel],
  });

  client.once('ready', async () => {
    console.log(`[discord] Logado como ${client.user.tag}`);
    await registerCommands();
    updateActivity(client);
    setInterval(() => updateActivity(client), 30_000);
  });

  client.on('interactionCreate', (i) => handleInteraction(i, client));
  client.on('messageCreate', (m) => handleMessage(m, client));

  return client;
}

async function updateActivity(client) {
  const status = await fetchServerStatus();
  const online = status.onlinePlayers;
  const text =
    online != null
      ? `🌟 savagemc.net | ${online} jogador${online !== 1 ? 'es' : ''} online`
      : '🌟 savagemc.net';

  client.user?.setActivity(text, { type: ActivityType.Watching });
}

async function registerCommands() {
  if (!config.guildId) return;
  const rest = new REST({ version: '10' }).setToken(config.token);
  const app = await rest.get(Routes.oauth2CurrentApplication());
  await rest.put(Routes.applicationGuildCommands(app.id, config.guildId), {
    body: commands.map((c) => c.data),
  });
  console.log(`[discord] ${commands.length} comandos slash registrados.`);
}

export async function loginBot(client) {
  if (!config.token) {
    throw new Error('DISCORD_TOKEN não configurado no .env');
  }
  await client.login(config.token);
}
