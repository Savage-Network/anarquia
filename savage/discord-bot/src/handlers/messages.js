import {
  canSendInTicket,
  getTicketByChannel,
  recordMessage,
} from '../tickets/manager.js';

export async function handleMessage(message, client) {
  if (message.author.bot || !message.guild) return;

  const ticket = getTicketByChannel(message.channel.id);
  if (!ticket) return;

  if (ticket.status === 'CLOSED') {
    await message.delete().catch(() => {});
    return;
  }

  const member = message.member;
  if (!member) return;

  if (!canSendInTicket(ticket, member)) {
    await message.delete().catch(() => {});
    await message.channel
      .send('❌ Apenas quem assumiu este ticket, o criador ou staff podem enviar mensagens aqui!')
      .then((m) => setTimeout(() => m.delete().catch(() => {}), 8000))
      .catch(() => {});
    return;
  }

  recordMessage(ticket, message);
}
