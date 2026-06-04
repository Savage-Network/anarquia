import {
  ActionRowBuilder,
  ButtonBuilder,
  ButtonStyle,
  ChannelType,
  EmbedBuilder,
  AttachmentBuilder,
  PermissionFlagsBits,
} from 'discord.js';
import { config } from '../config.js';

const TICKET_TYPES = {
  SUPPORT: { displayName: 'Dúvidas Gerais', emoji: '❓' },
  BUG_REPORT: { displayName: 'Reportar Bug', emoji: '⚠️' },
  APPEAL: { displayName: 'Revisão de Punição', emoji: '🔨' },
  OTHER: { displayName: 'Outro', emoji: '⚫' },
};

const tickets = new Map();
const channelToTicket = new Map();

export function getTicketTypes() {
  return TICKET_TYPES;
}

export function getTicket(id) {
  return tickets.get(id);
}

export function getTicketByChannel(channelId) {
  const id = channelToTicket.get(channelId);
  return id ? tickets.get(id) : null;
}

function hasStaffRole(member) {
  if (!member || !config.ticket.staffRoleId) return false;
  return member.roles.cache.has(config.ticket.staffRoleId);
}

export function isStaff(member) {
  return hasStaffRole(member);
}

export async function createTicket(guild, creator, typeKey) {
  const type = TICKET_TYPES[typeKey];
  if (!type) return null;

  const category = guild.channels.cache.get(config.ticket.categoryId);
  if (!category) {
    console.warn('[ticket] Categoria não encontrada');
    return null;
  }

  const ticketId = `ticket-${Date.now()}`;
  const channelName = `${type.emoji}-${creator.user.username}`.toLowerCase().slice(0, 32);

  const channel = await guild.channels.create({
    name: channelName,
    type: ChannelType.GuildText,
    parent: category.id,
    permissionOverwrites: [
      { id: guild.id, deny: [PermissionFlagsBits.ViewChannel] },
      { id: creator.id, allow: [PermissionFlagsBits.ViewChannel, PermissionFlagsBits.SendMessages] },
      ...(config.ticket.staffRoleId
        ? [{
            id: config.ticket.staffRoleId,
            allow: [PermissionFlagsBits.ViewChannel, PermissionFlagsBits.SendMessages],
          }]
        : []),
    ],
  });

  const ticket = {
    id: ticketId,
    channelId: channel.id,
    creatorId: creator.id,
    type,
    typeKey,
    status: 'OPEN',
    claimedBy: null,
    createdAt: new Date(),
    closedAt: null,
    messages: [],
  };

  tickets.set(ticketId, ticket);
  channelToTicket.set(channel.id, ticketId);

  const welcome = new EmbedBuilder()
    .setTitle('🎫 Ticket Criado')
    .setDescription(`Olá ${creator}! Seu ticket foi criado com sucesso.`)
    .addFields(
      { name: 'Tipo', value: `${type.emoji} ${type.displayName}`, inline: false },
      { name: 'Status', value: 'Aguardando atendimento', inline: false },
    )
    .setColor(0x00aa00)
    .setTimestamp();

  const controls = new EmbedBuilder()
    .setTitle('🎫 Controles do Ticket')
    .setDescription('Use os botões abaixo para gerenciar este ticket.')
    .setColor(0x0099ff)
    .setTimestamp();

  const row = new ActionRowBuilder().addComponents(
    new ButtonBuilder()
      .setCustomId(`claim_${ticketId}`)
      .setLabel('Assumir Ticket')
      .setStyle(ButtonStyle.Primary),
    new ButtonBuilder()
      .setCustomId(`close_${ticketId}`)
      .setLabel('Fechar Ticket')
      .setStyle(ButtonStyle.Danger),
  );

  await channel.send({ embeds: [welcome] });
  await channel.send({ embeds: [controls], components: [row] });

  return ticket;
}

export async function claimTicket(ticketId, member, client) {
  const ticket = tickets.get(ticketId);
  if (!ticket || ticket.claimedBy) return false;

  ticket.claimedBy = member.id;
  ticket.status = 'CLAIMED';

  const channel = await client.channels.fetch(ticket.channelId).catch(() => null);
  if (channel?.isTextBased()) {
    await channel.send({
      embeds: [
        new EmbedBuilder()
          .setTitle('✅ Ticket Assumido')
          .setDescription(`O ticket foi assumido por ${member}`)
          .setColor(0xffaa00)
          .setTimestamp(),
      ],
    });
  }
  return true;
}

async function sendTicketLogs(ticket, client) {
  const logsChannel = await client.channels.fetch(config.ticket.logsChannelId).catch(() => null);
  if (!logsChannel?.isTextBased()) return;

  const creator = await client.users.fetch(ticket.creatorId).catch(() => null);
  const claimer = ticket.claimedBy
    ? await client.users.fetch(ticket.claimedBy).catch(() => null)
    : null;

  const fmt = (d) => d.toLocaleString('pt-BR');
  let log = '═══════════════════════════════════════\n';
  log += `LOGS DO TICKET #${ticket.id}\n`;
  log += '═══════════════════════════════════════\n\n';
  log += `Criador: ${creator?.tag ?? ticket.creatorId}\n`;
  log += `Tipo: ${ticket.type.emoji} ${ticket.type.displayName}\n`;
  log += `Assumido por: ${claimer?.tag ?? 'Ninguém'}\n`;
  log += `Criado em: ${fmt(ticket.createdAt)}\n`;
  log += `Fechado em: ${ticket.closedAt ? fmt(ticket.closedAt) : 'N/A'}\n\n`;
  log += 'MENSAGENS:\n\n';

  if (!ticket.messages.length) {
    log += 'Nenhuma mensagem registrada.\n';
  } else {
    for (const m of ticket.messages) {
      log += `[${fmt(m.at)}] ${m.authorTag}: ${m.content}\n`;
    }
  }

  const file = new AttachmentBuilder(Buffer.from(log, 'utf-8'), {
    name: `ticket-${ticket.id}.txt`,
  });

  const embed = new EmbedBuilder()
    .setTitle(`📋 Logs do Ticket #${ticket.id}`)
    .addFields(
      { name: 'Criador', value: creator ? `<@${creator.id}>` : '—', inline: true },
      { name: 'Tipo', value: `${ticket.type.emoji} ${ticket.type.displayName}`, inline: true },
      { name: 'Assumido por', value: claimer ? `<@${claimer.id}>` : 'Ninguém', inline: true },
      { name: 'Mensagens', value: String(ticket.messages.length), inline: true },
    )
    .setColor(0xaa00aa)
    .setTimestamp();

  await logsChannel.send({ embeds: [embed], files: [file] });
}

export async function closeTicket(ticketId, closer, client) {
  const ticket = tickets.get(ticketId);
  if (!ticket) return;

  ticket.status = 'CLOSED';
  ticket.closedAt = new Date();

  const channel = await client.channels.fetch(ticket.channelId).catch(() => null);
  if (channel?.isTextBased()) {
    await channel.send({
      embeds: [
        new EmbedBuilder()
          .setTitle('🔒 Ticket Fechado')
          .setDescription(`Este ticket foi fechado por ${closer}`)
          .setColor(0xff0000)
          .setTimestamp(),
      ],
    });
  }

  await sendTicketLogs(ticket, client);

  setTimeout(async () => {
    if (channel) await channel.delete().catch(() => {});
  }, 5000);

  tickets.delete(ticketId);
  channelToTicket.delete(ticket.channelId);
}

export function recordMessage(ticket, message) {
  if (!message.content?.trim()) return;
  ticket.messages.push({
    authorId: message.author.id,
    authorTag: message.author.tag,
    content: message.content,
    at: new Date(),
  });
}

export function canSendInTicket(ticket, member) {
  if (ticket.status === 'CLOSED') return false;
  if (!ticket.claimedBy) return true;
  if (ticket.creatorId === member.id) return true;
  if (ticket.claimedBy === member.id) return true;
  return hasStaffRole(member);
}

export function canCloseTicket(ticket, member) {
  if (ticket.creatorId === member.id) return true;
  if (!hasStaffRole(member)) return false;
  if (!ticket.claimedBy) return true;
  return ticket.claimedBy === member.id;
}
