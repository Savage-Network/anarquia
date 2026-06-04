import { EmbedBuilder } from 'discord.js';
import { fetchPlayer } from '../api/client.js';
import { config } from '../config.js';
import { formatDate } from '../utils/dates.js';

export const data = {
  name: 'conta',
  description: 'Colete informações dos jogadores.',
  options: [
    {
      name: 'nome',
      description: 'Nick do jogador.',
      type: 3,
      required: true,
    },
  ],
};

export async function execute(interaction) {
  const name = interaction.options.getString('nome', true);
  await interaction.deferReply();

  try {
    const p = await fetchPlayer(name);
    const acc = p.account;

    const ban = p.punishments?.active?.some((x) => x.category === 'BAN');
    const mute = p.punishments?.active?.some((x) => x.category === 'MUTE');

    const embed = new EmbedBuilder()
      .setTitle('Informações da Conta')
      .setThumbnail(config.minotar(acc.username || name))
      .setColor(0x1a7a1a)
      .setDescription(
        `**Menu:** ${acc.username}\n\n` +
          `➠ **Rank:** ${p.rank_tag?.name ?? acc.rank?.name}\n` +
          `➠ **Tag de Perfil:** ${p.profile_tag?.name ?? acc.tag?.name}\n` +
          `➠ **Medalha:** ${acc.medal?.name ?? 'Nenhuma'}\n` +
          `➠ **Conta:** ${acc.type === 'premium' ? 'Original' : 'Pirata'}\n` +
          `➠ **Online:** ${p.connected ? 'Online' : 'Offline'}\n` +
          `➠ **Clan:** ${p.clan?.name ?? 'Nenhum'}\n` +
          `➠ **Skin:** [Clique aqui](https://namemc.com/profile/${acc.username})\n\n` +
          `➠ **Primeiro login:** ${formatDate(p.first_login)}\n` +
          `➠ **Último login:** ${formatDate(p.last_login)}\n\n` +
          `➠ **Banido:** ${ban ? 'Sim' : 'Não'}\n` +
          `➠ **Silenciado:** ${mute ? 'Sim' : 'Não'}`,
      );

    await interaction.editReply({ embeds: [embed] });
  } catch (e) {
    await interaction.editReply({
      content: `❌ ${e.message || 'Não foi possível encontrar a conta.'}`,
    });
  }
}
