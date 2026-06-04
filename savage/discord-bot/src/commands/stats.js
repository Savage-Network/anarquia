import { EmbedBuilder } from 'discord.js';
import { fetchPlayer } from '../api/client.js';
import { config } from '../config.js';
import { buildStatsDescription, getModeLabel, pickBedwarsStats } from '../utils/bedwars.js';

const MODE_CHOICES = [
  { name: 'Geral', value: 'GENERAL' },
  { name: 'BedWars Solo', value: 'BEDWARS_SOLO' },
  { name: 'BedWars Duplas', value: 'BEDWARS_DUO' },
  { name: 'BedWars Trios', value: 'BEDWARS_TRIO' },
  { name: 'BedWars Quartetos', value: 'BEDWARS_QUARTET' },
  { name: 'BedWars 1v1', value: 'BEDWARS_VERSUS_SOLO' },
  { name: 'BedWars 2v2', value: 'BEDWARS_VERSUS_DUO' },
  { name: 'BedWars 3v3', value: 'BEDWARS_VERSUS_TRIO' },
  { name: 'BedWars 4v4', value: 'BEDWARS_VERSUS_QUARTET' },
  { name: 'BedWars Infection', value: 'BEDWARS_INFECTION' },
];

export const data = {
  name: 'stats',
  description: 'Veja as estatísticas de BedWars de um jogador.',
  options: [
    { name: 'nick', description: 'Nick do jogador.', type: 3, required: true },
    {
      name: 'modo',
      description: 'Modo do BedWars.',
      type: 3,
      required: true,
      choices: MODE_CHOICES,
    },
  ],
};

export async function execute(interaction) {
  const nick = interaction.options.getString('nick', true);
  const mode = interaction.options.getString('modo', true);

  await interaction.deferReply();

  try {
    const p = await fetchPlayer(nick);
    const bw = p.stats?.bedwars;
    const stats = pickBedwarsStats(bw, mode);

    if (!stats || (stats.matches === 0 && !stats.level)) {
      await interaction.editReply({
        content: `❌ **${p.account.username}** não possui dados de BedWars neste modo.`,
      });
      return;
    }

    const extra =
      mode === 'GENERAL'
        ? {
            level: stats.level,
            xp: stats.xp,
            rankedTier: stats.rankedTier,
          }
        : {};

    const { desc, title } = buildStatsDescription(stats, getModeLabel(mode), extra);

    const embed = new EmbedBuilder()
      .setTitle(`${p.account.username} - ${title}`)
      .setThumbnail(config.minotar(p.account.username))
      .setDescription(desc)
      .setColor(0x1a7a1a);

    await interaction.editReply({ embeds: [embed] });
  } catch (e) {
    await interaction.editReply({
      content: `❌ ${e.message || 'Jogador não encontrado.'}`,
    });
  }
}
