import { commandMap } from '../commands/index.js';
import {
  canCloseTicket,
  claimTicket,
  closeTicket,
  createTicket,
  getTicket,
  isStaff,
} from '../tickets/manager.js';

export async function handleInteraction(interaction, client) {
  if (interaction.isChatInputCommand()) {
    const cmd = commandMap.get(interaction.commandName);
    if (cmd) await cmd.execute(interaction);
    return;
  }

  if (!interaction.isButton()) return;

  const id = interaction.customId;
  const member = interaction.member;

  if (id.startsWith('claim_')) {
    const ticketId = id.slice(6);
    const ticket = getTicket(ticketId);
    if (!ticket) {
      return interaction.reply({ content: '❌ Ticket não encontrado!', ephemeral: true });
    }
    if (ticket.claimedBy) {
      return interaction.reply({ content: '❌ Este ticket já foi assumido!', ephemeral: true });
    }
    if (!isStaff(member)) {
      return interaction.reply({
        content: '❌ Você não tem permissão para assumir tickets!',
        ephemeral: true,
      });
    }
    const ok = await claimTicket(ticketId, member, client);
    return interaction.reply({
      content: ok ? '✅ Ticket assumido com sucesso!' : '❌ Erro ao assumir o ticket!',
      ephemeral: true,
    });
  }

  if (id.startsWith('close_')) {
    const ticketId = id.slice(6);
    const ticket = getTicket(ticketId);
    if (!ticket) {
      return interaction.reply({ content: '❌ Ticket não encontrado!', ephemeral: true });
    }
    if (ticket.status === 'CLOSED') {
      return interaction.reply({ content: '❌ Este ticket já está fechado!', ephemeral: true });
    }

    if (!canCloseTicket(ticket, member)) {
      return interaction.reply({
        content: '❌ Você não tem permissão para fechar este ticket!',
        ephemeral: true,
      });
    }

    await closeTicket(ticketId, member, client);
    return interaction.reply({ content: '✅ Ticket fechado com sucesso!', ephemeral: true });
  }

  if (id.startsWith('create_ticket_')) {
    const typeKey = id.slice(14);
    const ticket = await createTicket(interaction.guild, member, typeKey);
    return interaction.reply({
      content: ticket
        ? '✅ Ticket criado com sucesso! Verifique o canal criado.'
        : '❌ Erro ao criar o ticket!',
      ephemeral: true,
    });
  }
}
