import { EmbedBuilder } from 'discord.js';
import { findLinkByDiscord } from '../services/linkService.js';
import { loadMenuProfile } from '../services/profileService.js';
import { config } from '../config.js';
import { formatRelative } from '../utils/relativeTime.js';

function displayRank(acc) {
  const staffTags = ['HELPER', 'MOD', 'MODPLUS', 'ADMIN', 'DIRETOR', 'VERMELHO', 'OWNER', 'ZW'];
  const tagId = String(acc?.tag?.id || acc?.tag?.name || 'MEMBER').toUpperCase();
  if (staffTags.some((s) => tagId.includes(s))) {
    return acc?.tag?.name || tagId;
  }
  return acc?.rank?.name || acc?.tag?.name || 'Membro';
}

function displayTitle(customTitle) {
  if (!customTitle?.title) return 'Nenhum';
  return customTitle.title;
}

function skinUrl(username, value) {
  if (value) {
    return `https://visage.surgeplay.com/face/256/${encodeURIComponent(value)}`;
  }
  return config.minotar(username, 256);
}

function buildMenuEmbed(profile, viewerName, discordMention) {
  const acc = profile.account;
  const name = acc.displayName || acc.name;
  const ban = profile.punishments?.active?.some((p) => p.category === 'BAN');
  const mute = profile.punishments?.active?.some((p) => p.category === 'MUTE');
  const stats = profile.stats;
  const kills = stats?.kills ?? 0;
  const deaths = stats?.deaths ?? 0;

  const clanName =
    acc.clan?.name ||
    acc.clan?.tag ||
    profile.account?.clan?.identifier ||
    'Nenhum';

  const linkedText = discordMention || 'Nenhum';
  const skinLink = `[Clique aqui](${skinUrl(name, acc.skin?.value)})`;

  const lines = [
    `» **Rank:** ${displayRank(acc)}`,
    `» **Tag de Perfil:** ${acc.tag?.name || 'Membro'}`,
    `» **Titulo:** ${displayTitle(acc.customTitle)}`,
    `» **Medalha:** ${acc.medal?.name || 'Nenhuma'}`,
    `» **Conta:** ${acc.premium ? 'Original' : 'Pirata'}`,
    `» **Online:** ${acc.online ? 'Sim' : 'Nao'}`,
    `» **Clan:** ${clanName}`,
    `» **Discord Vinculado:** ${linkedText}`,
    `» **Skin:** ${skinLink}`,
    '',
    `» **Primeiro login:** ${formatRelative(acc.createdAt)}`,
    `» **Ultima vez visto:** ${formatRelative(acc.lastLogin)}`,
    `» **K/D:** ${kills}/${deaths}`,
    '',
    `» **Biografia:** Nenhuma`,
    `» **Banido:** ${ban ? 'Sim' : 'Nao'}`,
    `» **Silenciado:** ${mute ? 'Sim' : 'Nao'}`,
  ];

  return new EmbedBuilder()
    .setColor(0xb026ff)
    .setAuthor({
      name: `Voce esta visualizando o perfil de: ${name}`,
    })
    .setTitle(`⛏ • Perfil: ${name}`)
    .setDescription(lines.join('\n'))
    .setThumbnail(config.minotar(name, 128))
    .setFooter({
      text: `SavageMC • ${viewerName}`,
    })
    .setTimestamp();
}

export const data = {
  name: 'menu',
  description: 'Visualiza o perfil de um jogador SavageMC.',
  options: [
    {
      name: 'nick',
      description: 'Nick do jogador (vazio = sua conta vinculada).',
      type: 3,
      required: false,
    },
  ],
};

export async function execute(interaction) {
  await interaction.deferReply();

  try {
    let nick = interaction.options.getString('nick');
    let discordMention = null;

    if (!nick) {
      const link = await findLinkByDiscord(interaction.user.id);
      if (!link) {
        await interaction.editReply({
          content:
            'Voce nao esta vinculado. Use `/vincular` ou informe um nick: `/menu nick:Steve`.',
        });
        return;
      }
      nick = link.playerName;
      discordMention = `<@${link.discordId}>`;
    }

    const profile = await loadMenuProfile(nick);
    if (!profile) {
      await interaction.editReply({ content: '❌ Jogador nao encontrado.' });
      return;
    }

    if (!discordMention && profile.discord?.discordId) {
      discordMention = `<@${profile.discord.discordId}>`;
    }

    const embed = buildMenuEmbed(
      profile,
      interaction.user.username,
      discordMention,
    );

    await interaction.editReply({ embeds: [embed] });
  } catch (e) {
    await interaction.editReply({
      content: `❌ ${e.message || 'Nao foi possivel carregar o perfil.'}`,
    });
  }
}
