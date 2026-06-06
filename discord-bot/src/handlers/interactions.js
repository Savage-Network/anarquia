import { commandMap } from '../commands/index.js';
import { handleTicketInteraction } from './ticketHandler.js';

export async function handleInteraction(interaction) {
  if (
    interaction.isStringSelectMenu()
    || interaction.isModalSubmit()
    || interaction.isButton()
  ) {
    const handled = await handleTicketInteraction(interaction);
    if (handled) return;
  }

  if (!interaction.isChatInputCommand()) return;

  const cmd = commandMap.get(interaction.commandName);
  if (cmd) await cmd.execute(interaction);
}
