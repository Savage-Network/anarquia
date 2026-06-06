import { EmbedBuilder } from 'discord.js';
import { loadMenuProfile } from '../services/profileService.js';
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
    const profile = await loadMenuProfile(name);
    if (!profile) {
      await interaction.editReply({ content: '❌ Jogador nao encontrado.' });
      return;
    }

    const acc = profile.account;
    const ban = profile.punishments?.active?.some((x) => x.category === 'BAN');
    const mute = profile.punishments?.active?.some((x) => x.category === 'MUTE');

    const embed = new EmbedBuilder()
      .setTitle('Conta — SavageMC')
      .setThumbnail(config.minotar(acc.displayName || acc.name))
      .setColor(0x1a7a1a)
      .setDescription(
        `**${acc.displayName || acc.name}**\n\n` +
          `Rank: ${acc.rank?.name ?? '—'}\n` +
          `Tag: ${acc.tag?.name ?? '—'}\n` +
          `Medalha: ${acc.medal?.name ?? 'Nenhuma'}\n` +
          `Conta: ${acc.premium ? 'Original' : 'Pirata'}\n` +
          `Online: ${acc.online ? 'Sim' : 'Nao'}\n` +
          `Cla: ${acc.clan?.name ?? acc.clan?.tag ?? 'Nenhum'}\n\n` +
          `Primeiro login: ${formatDate(acc.createdAt)}\n` +
          `Ultimo login: ${formatDate(acc.lastLogin)}\n\n` +
          `Banido: ${ban ? 'Sim' : 'Nao'}\n` +
          `Silenciado: ${mute ? 'Sim' : 'Nao'}`,
      );

    await interaction.editReply({ embeds: [embed] });
  } catch (e) {
    await interaction.editReply({
      content: `❌ ${e.message || 'Nao foi possivel encontrar a conta.'}`,
    });
  }
}
