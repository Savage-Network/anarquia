import { EmbedBuilder } from 'discord.js';
import { config } from '../config.js';
import { formatExpiry } from '../utils/dates.js';

async function postWebhook(url, payload) {
  if (!url) return;
  await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  }).catch((e) => console.warn('[webhook]', e.message));
}

function embedToJson(embed) {
  return embed.toJSON();
}

/** Payload vindo do plugin Java (PunishmentService webhook). */
export function normalizePunishment(body) {
  const p = body.punishment || body;
  return {
    id: p.id,
    category: p.category,
    reason: p.reason,
    cause: p.cause,
    server: p.server,
    arcade: p.arcade,
    playerName: p.playerName || p.player?.name,
    playerUuid: p.playerUuid || p.player?.uuid || p.player,
    authorName: p.authorName || p.author?.name,
    expiresAt: p.expiresAt ?? -1,
    revokerName: p.revokerName || p.revocation?.authorName,
    revokeReason: p.revokeReason || p.revocation?.reason,
  };
}

export async function sendStaffPunishment(p) {
  const embed = new EmbedBuilder()
    .setTitle('Nova punição')
    .setColor(0x8b0000)
    .addFields(
      { name: 'ID', value: p.id || '—', inline: true },
      { name: 'Autor', value: p.authorName || 'CONSOLE', inline: true },
      { name: 'Jogador', value: p.playerName || '—', inline: true },
      { name: 'Categoria', value: p.category || '—', inline: true },
      { name: 'Motivo', value: p.reason || '—', inline: true },
      { name: 'Servidor', value: p.server || '—', inline: true },
    )
    .addFields({ name: 'Causa', value: `\`\`\`${p.cause || '—'}\`\`\`` })
    .setFooter({ text: 'SavageMC — Punições' });

  if (p.arcade && p.arcade !== 'NONE') {
    embed.addFields({ name: 'Modo', value: p.arcade, inline: true });
  }
  if (p.expiresAt > -1) {
    embed.addFields({
      name: 'Expira em',
      value: formatExpiry(p.expiresAt),
      inline: true,
    });
  }

  await postWebhook(config.webhooks.staff, { embeds: [embedToJson(embed)] });
}

export async function sendRevocation(p) {
  const embed = new EmbedBuilder()
    .setTitle('Punição revogada')
    .setColor(0xff69b4)
    .addFields(
      { name: 'ID', value: p.id || '—', inline: true },
      { name: 'Jogador', value: p.playerName || '—', inline: true },
      { name: 'Categoria', value: p.category || '—', inline: true },
      { name: 'Revogado por', value: p.revokerName || 'CONSOLE', inline: true },
      { name: 'Motivo', value: `\`\`\`${p.revokeReason || '—'}\`\`\`` },
    );

  await postWebhook(config.webhooks.staff, { embeds: [embedToJson(embed)] });
}

export async function sendPublicBan(p) {
  if (p.category !== 'BAN') return;
  const uuid = p.playerUuid || '';
  const head = uuid
    ? `https://crafatar.com/avatars/${uuid}?size=256&overlay`
    : null;

  const embed = new EmbedBuilder()
    .setTitle('BANIDO')
    .setDescription(`**${p.playerName}**`)
    .addFields({ name: 'Motivo', value: p.cause || '—' })
    .setColor(0xff0000);

  if (head) embed.setThumbnail(head);

  await postWebhook(config.webhooks.public, { embeds: [embedToJson(embed)] });
}

export async function sendPublicMute(p) {
  if (p.category !== 'MUTE') return;
  const uuid = p.playerUuid || '';
  const head = uuid
    ? `https://crafatar.com/avatars/${uuid}?size=256&overlay`
    : null;

  const embed = new EmbedBuilder()
    .setTitle('SILENCIADO')
    .setDescription(`**${p.playerName}**`)
    .addFields(
      { name: 'Motivo', value: p.cause || '—' },
      { name: 'Expira em', value: formatExpiry(p.expiresAt) },
    )
    .setColor(0xffaa00);

  if (head) embed.setThumbnail(head);

  await postWebhook(config.webhooks.public, { embeds: [embedToJson(embed)] });
}
