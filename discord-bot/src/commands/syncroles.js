import { findLinkByDiscord } from '../services/linkService.js';
import { loadAccountForRoleSync } from '../services/accountIdentity.js';
import { syncMemberRoles } from '../services/roleSync.js';

export const data = {
  name: 'syncroles',
  description: 'Atualiza seus cargos de rank conforme a conta vinculada.',
};

export async function execute(interaction) {
  await interaction.deferReply({ ephemeral: true });

  try {
    const link = await findLinkByDiscord(interaction.user.id);
    if (!link) {
      await interaction.editReply({
        content: 'Voce precisa vincular sua conta com `/vincular` primeiro.',
      });
      return;
    }

    const guild = interaction.guild;
    if (!guild) {
      await interaction.editReply({ content: 'Este comando so funciona em um servidor.' });
      return;
    }

    const profile = await loadAccountForRoleSync(link.playerId || link.playerName);
    const member = await guild.members.fetch(interaction.user.id);
    const result = await syncMemberRoles(guild, member, profile?.account, true);

    await interaction.editReply({
      content:
        `Cargos sincronizados para **${link.playerName}**` +
        (result.rankKey ? ` (rank: ${result.rankKey})` : '') +
        '.',
    });
  } catch (e) {
    await interaction.editReply({
      content: `❌ ${e.message || 'Nao foi possivel sincronizar cargos.'}`,
    });
  }
}
