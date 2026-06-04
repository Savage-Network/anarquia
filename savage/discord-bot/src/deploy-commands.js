import { REST, Routes } from 'discord.js';
import { config } from './config.js';
import { commands } from './commands/index.js';

if (!config.token || !config.guildId) {
  console.error('Configure DISCORD_TOKEN e DISCORD_GUILD_ID no .env');
  process.exit(1);
}

const rest = new REST({ version: '10' }).setToken(config.token);
const app = await rest.get(Routes.oauth2CurrentApplication());

await rest.put(Routes.applicationGuildCommands(app.id, config.guildId), {
  body: commands.map((c) => c.data),
});

console.log(`[deploy] ${commands.length} slash commands registrados.`);
