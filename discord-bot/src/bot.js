import {
  ActivityType,
  Client,
  GatewayIntentBits,
  REST,
  Routes,
} from 'discord.js';
import { config } from './config.js';
import { fetchServerStatusFromDb } from './services/presenceService.js';
import { commands } from './commands/index.js';
import { handleInteraction } from './handlers/interactions.js';

export function createBot() {
  const client = new Client({
    // GuildMembers e privilegiado no portal Discord; nao e necessario aqui
    // (sync de cargos usa guild.members.fetch via REST nos comandos).
    intents: [GatewayIntentBits.Guilds],
  });

  client.once('ready', async () => {
    console.log(`[discord] Logado como ${client.user.tag}`);
    await registerCommands();
    updateActivity(client);
    setInterval(() => updateActivity(client), 30_000);
  });

  client.on('interactionCreate', (i) => handleInteraction(i));

  return client;
}

async function updateActivity(client) {
  const status = await fetchServerStatusFromDb();
  const online = status.onlinePlayers;
  const text =
    online != null
      ? `savagemc.net | ${online} online`
      : 'savagemc.net';

  client.user?.setActivity(text, { type: ActivityType.Watching });
}

async function registerCommands() {
  if (!config.guildId || !config.token) return;
  const rest = new REST({ version: '10' }).setToken(config.token);
  const app = await rest.get(Routes.oauth2CurrentApplication());
  await rest.put(Routes.applicationGuildCommands(app.id, config.guildId), {
    body: commands.map((c) => c.data),
  });
  console.log(`[discord] ${commands.length} comandos registrados.`);
}

export async function loginBot(client) {
  if (!config.token) {
    throw new Error('DISCORD_TOKEN não configurado no .env');
  }
  await client.login(config.token);
}
