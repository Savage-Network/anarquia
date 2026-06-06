import { confirmLink, findLinkByDiscord } from '../services/linkService.js';
import { loadAccountForRoleSync } from '../services/accountIdentity.js';
import { syncMemberRoles } from '../services/roleSync.js';

export const data = {
  name: 'vincular',
  description: 'Vincula sua conta Minecraft ao Discord.',
  options: [
    {
      name: 'codigo',
      description: 'Codigo gerado no jogo com /vincular.',
      type: 3,
      required: true,
    },
  ],
};

export async function execute(interaction) {
  const code = interaction.options.getString('codigo', true);
  await interaction.deferReply({ ephemeral: true });

  try {
    const existing = await findLinkByDiscord(interaction.user.id);
    if (existing) {
      await interaction.editReply({
        content: `Voce ja esta vinculado a **${existing.playerName}**.`,
      });
      return;
    }

    const tag = interaction.user.discriminator === '0'
      ? interaction.user.username
      : `${interaction.user.username}#${interaction.user.discriminator}`;

    const result = await confirmLink(code, interaction.user.id, tag);
    if (!result.ok) {
      await interaction.editReply({
        content: `❌ ${result.message || 'Nao foi possivel vincular.'}`,
      });
      return;
    }

    const playerName = result.link?.playerName;
    const guild = interaction.guild;

    if (guild) {
      const member = await guild.members.fetch(interaction.user.id);
      const profile = await loadAccountForRoleSync(result.link.playerId || playerName);
      await syncMemberRoles(guild, member, profile?.account, true);
    }

    await interaction.editReply({
      content:
        `Conta **${playerName}** vinculada com sucesso!\n` +
        'Seus cargos de rank foram atualizados.',
    });
  } catch (e) {
    await interaction.editReply({
      content: `❌ ${e.message || 'Nao foi possivel vincular.'}`,
    });
  }
}
