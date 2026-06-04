import { EmbedBuilder } from 'discord.js';
import { config } from '../config.js';

async function postWebhook(url, payload) {
  if (!url) return;
  await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  }).catch((e) => console.warn('[webhook report]', e.message));
}

export async function sendStaffReport(report) {
  const embed = new EmbedBuilder()
    .setTitle('Novo report')
    .setColor(0xff6600)
    .addFields(
      { name: 'ID', value: String(report.id ?? '—'), inline: true },
      { name: 'Reporter', value: report.reporterName || '—', inline: true },
      { name: 'Alvo', value: report.targetName || '—', inline: true },
      { name: 'Motivo', value: report.reason || '—', inline: true },
      { name: 'Servidor', value: report.serverId || '—', inline: true },
    );

  if (report.details) {
    embed.addFields({
      name: 'Detalhes',
      value: `\`\`\`${String(report.details).slice(0, 900)}\`\`\``,
    });
  }

  embed.setFooter({ text: 'SavageMC — Reports' });

  await postWebhook(config.webhooks.report, { embeds: [embed.toJSON()] });
}
