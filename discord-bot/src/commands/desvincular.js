import { findLinkByDiscord, unlinkByDiscord } from '../services/linkService.js';
import { clearManagedRoles } from '../services/roleSync.js';

export const data = {
  name: 'desvincular',
  description: 'Remove o vinculo entre sua conta Minecraft e o Discord.',
};

export async function execute(interaction) {
  await interaction.deferReply({ ephemeral: true });

  try {
    const link = await findLinkByDiscord(interaction.user.id);
    if (!link) {
      await interaction.editReply({ content: 'Voce nao possui conta vinculada.' });
      return;
    }

    const result = await unlinkByDiscord(interaction.user.id);
    if (!result.ok) {
      await interaction.editReply({
        content: `❌ ${result.message || 'Nao foi possivel desvincular.'}`,
      });
      return;
    }

    const guild = interaction.guild;
    if (guild) {
      const member = await guild.members.fetch(interaction.user.id);
      await clearManagedRoles(member);
    }

    await interaction.editReply({
      content: `Vinculo com **${link.playerName}** removido.`,
    });
  } catch (e) {
    await interaction.editReply({
      content: `❌ ${e.message || 'Nao foi possivel desvincular.'}`,
    });
  }
}
