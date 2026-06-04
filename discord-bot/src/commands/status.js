import { EmbedBuilder } from 'discord.js';
import { fetchServerStatus } from '../api/client.js';

export const data = {
  name: 'status',
  description: 'Status da rede SavageMC.',
};

export async function execute(interaction) {
  await interaction.deferReply();
  const status = await fetchServerStatus();
  const online = status.onlinePlayers;

  const embed = new EmbedBuilder()
    .setTitle('SavageMC — Status')
    .setColor(0x55ff55)
    .setDescription(
      online != null
        ? `**${online}** jogador${online !== 1 ? 'es' : ''} online (contas marcadas online no banco).`
        : 'API indisponível ou banco inacessível.',
    )
    .addFields({ name: 'Site', value: 'https://savagemc.net' });

  await interaction.editReply({ embeds: [embed] });
}
