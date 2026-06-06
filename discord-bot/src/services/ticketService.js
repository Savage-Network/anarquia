import {
  ActionRowBuilder,
  ButtonBuilder,
  ButtonStyle,
  ChannelType,
  EmbedBuilder,
  ModalBuilder,
  PermissionFlagsBits,
  StringSelectMenuBuilder,
  TextInputBuilder,
  TextInputStyle,
} from 'discord.js';
import { config } from '../config.js';
import { categoryOptions, getCategory, TICKET_STATUS } from '../data/ticketCategories.js';
import { query } from '../db/pool.js';
import { findLinkByDiscord } from './linkService.js';

const PANEL_SELECT_ID = 'ticket:panel:select';
const MODAL_PREFIX = 'ticket:modal:';
const CLOSE_ID = 'ticket:close';
const CLOSE_CONFIRM_ID = 'ticket:close:confirm';
const CLOSE_CANCEL_ID = 'ticket:close:cancel';
const CLAIM_ID = 'ticket:claim';
const TRANSCRIPT_ID = 'ticket:transcript';

function readOptionalField(interaction, fieldId) {
  try {
    const value = interaction.fields.getTextInputValue(fieldId);
    return value?.trim() || null;
  } catch {
    return null;
  }
}

export function panelSelectCustomId() {
  return PANEL_SELECT_ID;
}

export function modalCustomId(categoryId) {
  return `${MODAL_PREFIX}${categoryId}`;
}

export function isTicketModal(id) {
  return id?.startsWith(MODAL_PREFIX);
}

export function parseModalCategory(id) {
  return id?.slice(MODAL_PREFIX.length)?.toUpperCase() || null;
}

export function buildPanelEmbed() {
  return new EmbedBuilder()
    .setColor(0xff8c42)
    .setTitle('🎫 Central de Atendimento · SavageMC')
    .setDescription(
      'Selecione a **categoria** abaixo para abrir um ticket privado com a equipe.\n\n' +
        '🛟 **Suporte** — dúvidas e ajuda geral\n' +
        '🚨 **Denúncia** — reportar jogadores ou regras\n' +
        '🐛 **Bug** — problemas técnicos no servidor\n' +
        '🛒 **Compras** — ranks, pacotes e cash\n\n' +
        '⏱ Tempo médio de resposta: o mais breve possível.\n' +
        '🌐 Loja vitrine: **loja.savagemc.net**',
    )
    .setFooter({ text: 'SavageMC · savagemc.net' })
    .setTimestamp();
}

export function buildPanelComponents() {
  const select = new StringSelectMenuBuilder()
    .setCustomId(PANEL_SELECT_ID)
    .setPlaceholder('Escolha a categoria do ticket')
    .addOptions(categoryOptions());

  return [new ActionRowBuilder().addComponents(select)];
}

export function buildOpenModal(categoryId) {
  const cat = getCategory(categoryId);
  if (!cat) throw new Error('Categoria invalida.');

  const modal = new ModalBuilder()
    .setCustomId(modalCustomId(cat.id))
    .setTitle(`${cat.emoji} Ticket · ${cat.label}`);

  const subject = new TextInputBuilder()
    .setCustomId('subject')
    .setLabel('Assunto')
    .setPlaceholder('Resumo curto do seu pedido')
    .setStyle(TextInputStyle.Short)
    .setRequired(true)
    .setMaxLength(100);

  const description = new TextInputBuilder()
    .setCustomId('description')
    .setLabel('Descricao detalhada')
    .setPlaceholder('Explique com o maximo de detalhes possivel')
    .setStyle(TextInputStyle.Paragraph)
    .setRequired(true)
    .setMaxLength(1500);

  modal.addComponents(
    new ActionRowBuilder().addComponents(subject),
    new ActionRowBuilder().addComponents(description),
  );

  if (cat.id === 'COMPRAS') {
    const product = new TextInputBuilder()
      .setCustomId('product')
      .setLabel('Produto desejado')
      .setPlaceholder('Ex: Savage, VIP 30 Dias, Pacote Economia')
      .setStyle(TextInputStyle.Short)
      .setRequired(true)
      .setMaxLength(120);

    const nick = new TextInputBuilder()
      .setCustomId('minecraft_nick')
      .setLabel('Nick Minecraft')
      .setPlaceholder('Seu nick exato no servidor')
      .setStyle(TextInputStyle.Short)
      .setRequired(true)
      .setMaxLength(16);

    modal.addComponents(
      new ActionRowBuilder().addComponents(product),
      new ActionRowBuilder().addComponents(nick),
    );
  } else if (cat.id === 'DENUNCIA') {
    const target = new TextInputBuilder()
      .setCustomId('target')
      .setLabel('Jogador denunciado')
      .setPlaceholder('Nick exato ou evidencias')
      .setStyle(TextInputStyle.Short)
      .setRequired(true)
      .setMaxLength(100);

    modal.addComponents(new ActionRowBuilder().addComponents(target));
  } else if (cat.id === 'BUG') {
    const repro = new TextInputBuilder()
      .setCustomId('repro')
      .setLabel('Como reproduzir')
      .setPlaceholder('Passo a passo para reproduzir o bug')
      .setStyle(TextInputStyle.Short)
      .setRequired(false)
      .setMaxLength(500);

    modal.addComponents(new ActionRowBuilder().addComponents(repro));
  } else {
    const nickOptional = new TextInputBuilder()
      .setCustomId('minecraft_nick')
      .setLabel('Nick Minecraft (opcional)')
      .setPlaceholder('Facilita o atendimento se informar')
      .setStyle(TextInputStyle.Short)
      .setRequired(false)
      .setMaxLength(16);

    modal.addComponents(new ActionRowBuilder().addComponents(nickOptional));
  }

  return modal;
}

function sanitizeChannelName(value) {
  return String(value || 'user')
    .toLowerCase()
    .replace(/[^a-z0-9-]/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
    .slice(0, 20) || 'user';
}

async function countOpenTickets(guildId, ownerId) {
  const { rows } = await query(
    `SELECT COUNT(*) AS total FROM discord_tickets
     WHERE guild_id = ? AND owner_id = ? AND status != 'CLOSED'`,
    [guildId, ownerId],
  );
  return Number(rows[0]?.total || 0);
}

async function findExistingChannel(guild, prefix, username) {
  const base = `${prefix}-${sanitizeChannelName(username)}`;
  let name = base;
  let attempt = 0;
  while (guild.channels.cache.some((c) => c.name === name)) {
    attempt += 1;
    name = `${base}-${attempt}`;
    if (attempt > 20) break;
  }
  return name;
}

export async function createTicketFromModal(interaction, categoryId) {
  if (!config.tickets.enabled) {
    return { ok: false, message: 'Sistema de tickets desativado.' };
  }

  const cat = getCategory(categoryId);
  if (!cat) return { ok: false, message: 'Categoria invalida.' };

  const guild = interaction.guild;
  if (!guild) return { ok: false, message: 'Use este painel dentro do servidor Discord.' };

  const openCount = await countOpenTickets(guild.id, interaction.user.id);
  if (openCount >= config.tickets.maxOpenPerUser) {
    return {
      ok: false,
      message: `Voce ja possui ${openCount} ticket(s) aberto(s). Feche um antes de abrir outro.`,
    };
  }

  const subject = interaction.fields.getTextInputValue('subject').trim();
  const description = interaction.fields.getTextInputValue('description').trim();
  const product = readOptionalField(interaction, 'product');
  const minecraftNick = readOptionalField(interaction, 'minecraft_nick');
  const target = readOptionalField(interaction, 'target');
  const repro = readOptionalField(interaction, 'repro');

  const extra = {
    product,
    minecraftNick,
    target,
    repro,
  };

  const link = await findLinkByDiscord(interaction.user.id);
  const channelName = await findExistingChannel(
    guild,
    cat.channelPrefix,
    interaction.user.username,
  );

  const overwrites = [
    {
      id: guild.roles.everyone.id,
      deny: [PermissionFlagsBits.ViewChannel],
    },
    {
      id: interaction.user.id,
      allow: [
        PermissionFlagsBits.ViewChannel,
        PermissionFlagsBits.SendMessages,
        PermissionFlagsBits.AttachFiles,
        PermissionFlagsBits.ReadMessageHistory,
        PermissionFlagsBits.EmbedLinks,
      ],
    },
  ];

  if (config.tickets.staffRoleId) {
    overwrites.push({
      id: config.tickets.staffRoleId,
      allow: [
        PermissionFlagsBits.ViewChannel,
        PermissionFlagsBits.SendMessages,
        PermissionFlagsBits.ManageMessages,
        PermissionFlagsBits.ReadMessageHistory,
        PermissionFlagsBits.AttachFiles,
        PermissionFlagsBits.EmbedLinks,
      ],
    });
  }

  const parentId = config.tickets.categoryId || null;

  const channel = await guild.channels.create({
    name: channelName,
    type: ChannelType.GuildText,
    parent: parentId,
    topic: `Ticket ${cat.label} · ${interaction.user.tag} · ${subject.slice(0, 80)}`,
    permissionOverwrites: overwrites,
    reason: `Ticket ${cat.id} aberto por ${interaction.user.tag}`,
  });

  const now = Date.now();
  await query(
    `INSERT INTO discord_tickets
      (channel_id, guild_id, owner_id, category, subject, description, extra,
       status, created_at, player_id, player_name)
     VALUES (?, ?, ?, ?, ?, ?, ?, 'OPEN', ?, ?, ?)`,
    [
      channel.id,
      guild.id,
      interaction.user.id,
      cat.id,
      subject,
      description,
      JSON.stringify(extra),
      now,
      link?.playerId || null,
      link?.playerName || minecraftNick || null,
    ],
  );

  const welcome = buildTicketEmbed({
    cat,
    subject,
    description,
    extra,
    owner: interaction.user,
    link,
    status: TICKET_STATUS.OPEN,
  });

  await channel.send({
    content: `${interaction.user} ${config.tickets.staffRoleId ? `<@&${config.tickets.staffRoleId}>` : ''}`.trim(),
    embeds: [welcome],
    components: buildTicketControls(false),
  });

  await logTicketEvent(guild, {
    title: '📥 Ticket aberto',
    color: cat.color,
    fields: [
      { name: 'Categoria', value: `${cat.emoji} ${cat.label}`, inline: true },
      { name: 'Autor', value: `<@${interaction.user.id}>`, inline: true },
      { name: 'Canal', value: `<#${channel.id}>`, inline: true },
      { name: 'Assunto', value: subject },
    ],
  });

  return {
    ok: true,
    channel,
    message: `Ticket criado: ${channel}`,
  };
}

function buildTicketEmbed({ cat, subject, description, extra, owner, link, status, claimedBy }) {
  const embed = new EmbedBuilder()
    .setColor(cat.color)
    .setTitle(`${cat.emoji} ${cat.label} · ${subject}`)
    .setDescription(description)
    .addFields(
      { name: 'Autor', value: `<@${owner.id}>`, inline: true },
      { name: 'Status', value: formatStatus(status), inline: true },
      {
        name: 'Conta MC',
        value: link?.playerName
          ? `**${link.playerName}** (vinculada)`
          : extra?.minecraftNick
            ? `**${extra.minecraftNick}** (informado)`
            : 'Nao informada',
        inline: true,
      },
    )
    .setFooter({ text: `ID categoria: ${cat.id}` })
    .setTimestamp();

  if (extra?.product) {
    embed.addFields({ name: 'Produto', value: extra.product, inline: true });
  }
  if (extra?.target) {
    embed.addFields({ name: 'Denunciado', value: extra.target, inline: true });
  }
  if (extra?.repro) {
    embed.addFields({ name: 'Reproducao', value: extra.repro });
  }
  if (claimedBy) {
    embed.addFields({ name: 'Staff responsavel', value: `<@${claimedBy}>`, inline: true });
  }

  return embed;
}

function formatStatus(status) {
  switch (status) {
    case TICKET_STATUS.CLAIMED:
      return '🟡 Em atendimento';
    case TICKET_STATUS.CLOSED:
      return '🔴 Fechado';
    default:
      return '🟢 Aberto';
  }
}

export function buildTicketControls(closing = false) {
  if (closing) {
    return [
      new ActionRowBuilder().addComponents(
        new ButtonBuilder()
          .setCustomId(CLOSE_CONFIRM_ID)
          .setLabel('Confirmar fechamento')
          .setStyle(ButtonStyle.Danger)
          .setEmoji('✅'),
        new ButtonBuilder()
          .setCustomId(CLOSE_CANCEL_ID)
          .setLabel('Cancelar')
          .setStyle(ButtonStyle.Secondary),
      ),
    ];
  }

  return [
    new ActionRowBuilder().addComponents(
      new ButtonBuilder()
        .setCustomId(CLAIM_ID)
        .setLabel('Reivindicar')
        .setStyle(ButtonStyle.Primary)
        .setEmoji('🙋'),
      new ButtonBuilder()
        .setCustomId(CLOSE_ID)
        .setLabel('Fechar ticket')
        .setStyle(ButtonStyle.Danger)
        .setEmoji('🔒'),
      new ButtonBuilder()
        .setCustomId(TRANSCRIPT_ID)
        .setLabel('Transcript')
        .setStyle(ButtonStyle.Secondary)
        .setEmoji('📄'),
    ),
  ];
}

export async function findTicketByChannel(channelId) {
  const { rows } = await query(
    `SELECT * FROM discord_tickets WHERE channel_id = ? LIMIT 1`,
    [channelId],
  );
  return rows[0] || null;
}

function parseExtra(raw) {
  if (!raw) return {};
  try {
    return typeof raw === 'string' ? JSON.parse(raw) : raw;
  } catch {
    return {};
  }
}

export async function claimTicket(interaction) {
  const ticket = await findTicketByChannel(interaction.channelId);
  if (!ticket || ticket.status === TICKET_STATUS.CLOSED) {
    return { ok: false, message: 'Ticket nao encontrado ou ja fechado.' };
  }

  if (ticket.claimed_by && ticket.claimed_by !== interaction.user.id) {
    return {
      ok: false,
      message: `Ticket ja reivindicado por <@${ticket.claimed_by}>.`,
    };
  }

  const now = Date.now();
  await query(
    `UPDATE discord_tickets SET status = 'CLAIMED', claimed_by = ?, claimed_at = ? WHERE channel_id = ?`,
    [interaction.user.id, now, interaction.channelId],
  );

  await interaction.channel.send({
    embeds: [
      new EmbedBuilder()
        .setColor(0xfbbf24)
        .setDescription(`🙋 <@${interaction.user.id}> assumiu este ticket.`),
    ],
  });

  return { ok: true };
}

export async function requestCloseTicket(interaction) {
  const ticket = await findTicketByChannel(interaction.channelId);
  if (!ticket || ticket.status === TICKET_STATUS.CLOSED) {
    return { ok: false, message: 'Ticket nao encontrado ou ja fechado.' };
  }

  const isOwner = ticket.owner_id === interaction.user.id;
  const isStaff = isTicketStaff(interaction);

  if (!isOwner && !isStaff) {
    return { ok: false, message: 'Apenas o autor ou staff pode fechar este ticket.' };
  }

  await interaction.channel.send({
    content: isStaff ? null : `<@${ticket.owner_id}>`,
    embeds: [
      new EmbedBuilder()
        .setColor(0xff6b6b)
        .setTitle('Confirmar fechamento')
        .setDescription(
          'Deseja realmente fechar este ticket?\nUm transcript sera salvo nos logs.',
        ),
    ],
    components: buildTicketControls(true),
  });

  return { ok: true, ephemeral: true, message: 'Confirmacao enviada no canal.' };
}

function isTicketStaff(interaction) {
  if (interaction.memberPermissions?.has(PermissionFlagsBits.ManageChannels)) {
    return true;
  }
  if (!config.tickets.staffRoleId) return false;
  return interaction.member?.roles?.cache?.has(config.tickets.staffRoleId);
}

export async function closeTicket(interaction, reason = 'Fechado pelo painel') {
  const ticket = await findTicketByChannel(interaction.channelId);
  if (!ticket || ticket.status === TICKET_STATUS.CLOSED) {
    return { ok: false, message: 'Ticket nao encontrado ou ja fechado.' };
  }

  const isOwner = ticket.owner_id === interaction.user.id;
  const isStaff = isTicketStaff(interaction);
  if (!isOwner && !isStaff) {
    return { ok: false, message: 'Sem permissao para fechar.' };
  }

  const transcript = await buildTranscript(interaction.channel);
  const now = Date.now();

  await query(
    `UPDATE discord_tickets
     SET status = 'CLOSED', closed_at = ?, closed_by = ?, close_reason = ?
     WHERE channel_id = ?`,
    [now, interaction.user.id, reason, interaction.channelId],
  );

  const cat = getCategory(ticket.category);

  await logTicketEvent(interaction.guild, {
    title: '📤 Ticket fechado',
    color: 0xff6b6b,
    fields: [
      { name: 'Categoria', value: cat ? `${cat.emoji} ${cat.label}` : ticket.category, inline: true },
      { name: 'Autor', value: `<@${ticket.owner_id}>`, inline: true },
      { name: 'Fechado por', value: `<@${interaction.user.id}>`, inline: true },
      { name: 'Assunto', value: ticket.subject },
      { name: 'Motivo', value: reason },
    ],
    files: transcript ? [{ attachment: Buffer.from(transcript, 'utf8'), name: `ticket-${interaction.channelId}.txt` }] : [],
  });

  await interaction.channel.send({
    embeds: [
      new EmbedBuilder()
        .setColor(0xff6b6b)
        .setDescription('🔒 Ticket fechado. Este canal sera removido em **5 segundos**.'),
    ],
    components: [],
  });

  setTimeout(() => {
    interaction.channel.delete('Ticket fechado').catch(() => {});
  }, 5000);

  return { ok: true };
}

async function buildTranscript(channel) {
  try {
    const messages = await channel.messages.fetch({ limit: 100 });
    const sorted = [...messages.values()].sort((a, b) => a.createdTimestamp - b.createdTimestamp);
    const lines = sorted.map((msg) => {
      const time = new Date(msg.createdTimestamp).toISOString();
      const author = msg.author?.tag || 'unknown';
      const content = msg.content || '[embed/anexo]';
      return `[${time}] ${author}: ${content}`;
    });
    return lines.join('\n') || 'Sem mensagens.';
  } catch {
    return null;
  }
}

export async function sendTranscript(interaction) {
  const ticket = await findTicketByChannel(interaction.channelId);
  if (!ticket) return { ok: false, message: 'Ticket nao encontrado.' };

  const transcript = await buildTranscript(interaction.channel);
  if (!transcript) return { ok: false, message: 'Nao foi possivel gerar transcript.' };

  await interaction.reply({
    ephemeral: true,
    files: [{ attachment: Buffer.from(transcript, 'utf8'), name: `ticket-${interaction.channelId}.txt` }],
  });

  return { ok: true, handled: true };
}

async function logTicketEvent(guild, { title, color, fields, files = [] }) {
  if (!guild || !config.tickets.logsChannelId) return;
  const logs = guild.channels.cache.get(config.tickets.logsChannelId)
    || await guild.channels.fetch(config.tickets.logsChannelId).catch(() => null);
  if (!logs?.isTextBased()) return;

  const embed = new EmbedBuilder().setColor(color).setTitle(title).addFields(fields).setTimestamp();
  await logs.send({ embeds: [embed], files }).catch(() => {});
}

export async function addMemberToTicket(interaction, member) {
  const ticket = await findTicketByChannel(interaction.channelId);
  if (!ticket || ticket.status === TICKET_STATUS.CLOSED) {
    return { ok: false, message: 'Ticket invalido.' };
  }
  if (!isTicketStaff(interaction)) {
    return { ok: false, message: 'Apenas staff pode adicionar membros.' };
  }

  await interaction.channel.permissionOverwrites.edit(member.id, {
    ViewChannel: true,
    SendMessages: true,
    ReadMessageHistory: true,
    AttachFiles: true,
  });

  return { ok: true, message: `${member} adicionado ao ticket.` };
}

export async function removeMemberFromTicket(interaction, member) {
  const ticket = await findTicketByChannel(interaction.channelId);
  if (!ticket || ticket.status === TICKET_STATUS.CLOSED) {
    return { ok: false, message: 'Ticket invalido.' };
  }
  if (!isTicketStaff(interaction)) {
    return { ok: false, message: 'Apenas staff pode remover membros.' };
  }
  if (member.id === ticket.owner_id) {
    return { ok: false, message: 'Nao e possivel remover o autor do ticket.' };
  }

  await interaction.channel.permissionOverwrites.delete(member.id);
  return { ok: true, message: `${member} removido do ticket.` };
}

export {
  CLOSE_ID,
  CLOSE_CONFIRM_ID,
  CLOSE_CANCEL_ID,
  CLAIM_ID,
  TRANSCRIPT_ID,
  PANEL_SELECT_ID,
};
