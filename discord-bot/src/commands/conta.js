import { EmbedBuilder } from 'discord.js';
import { fetchPlayer } from '../api/client.js';
import { config } from '../config.js';
import { formatDate } from '../utils/dates.js';

export const data = {
  name: 'conta',
  description: 'Informações de uma conta SavageMC.',
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
      .setTitle('Conta — SavageMC')
      .setThumbnail(config.minotar(acc.username || name))
      .setColor(0x1a7a1a)
      .setDescription(
        `**${acc.username}**\n\n` +
          `Rank: ${p.rank_tag?.name ?? acc.rank?.name ?? '—'}\n` +
          `Tag: ${p.profile_tag?.name ?? acc.tag?.name ?? '—'}\n` +
          `Medalha: ${acc.medal?.name ?? p.medal ?? 'Nenhuma'}\n` +
          `Conta: ${acc.type === 'premium' ? 'Original' : 'Pirata'}\n` +
          `Online: ${p.connected ? 'Sim' : 'Não'}\n` +
          `Clã: ${p.clan?.name ?? 'Nenhum'}\n\n` +
          `Primeiro login: ${formatDate(p.first_login)}\n` +
          `Último login: ${formatDate(p.last_login)}\n\n` +
          `Banido: ${ban ? 'Sim' : 'Não'}\n` +
          `Silenciado: ${mute ? 'Sim' : 'Não'}`,
      );

    await interaction.editReply({ embeds: [embed] });
  } catch (e) {
    await interaction.editReply({
      content: `❌ ${e.message || 'Não foi possível encontrar a conta.'}`,
    });
  }
}
