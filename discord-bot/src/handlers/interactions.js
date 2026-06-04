import { commandMap } from '../commands/index.js';

export async function handleInteraction(interaction) {
  if (!interaction.isChatInputCommand()) return;

  const cmd = commandMap.get(interaction.commandName);
  if (cmd) await cmd.execute(interaction);
}
