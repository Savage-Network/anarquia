import { EmbedBuilder } from 'discord.js';
import { fetchServerStatusFromDb } from '../services/presenceService.js';

export const data = {
  name: 'status',
  description: 'Status da rede SavageMC.',
};

export async function execute(interaction) {
  await interaction.deferReply();
  const status = await fetchServerStatusFromDb();
  const online = status.onlinePlayers;

  const embed = new EmbedBuilder()
    .setTitle('SavageMC — Status')
    .setColor(online != null && online > 0 ? 0x55ff55 : 0xffaa00)
    .setDescription(
      online != null
        ? `**${online}** jogador${online !== 1 ? 'es' : ''} online na rede.`
        : 'Nao foi possivel consultar o banco de dados.',
    )
    .addFields({ name: 'Site', value: 'https://savagemc.net' });

  await interaction.editReply({ embeds: [embed] });
}
