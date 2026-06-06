import {
  buildOpenModal,
  claimTicket,
  closeTicket,
  createTicketFromModal,
  isTicketModal,
  panelSelectCustomId,
  parseModalCategory,
  requestCloseTicket,
  sendTranscript,
  buildPanelComponents,
  buildPanelEmbed,
  CLOSE_CANCEL_ID,
  CLOSE_CONFIRM_ID,
  CLOSE_ID,
  CLAIM_ID,
  TRANSCRIPT_ID,
  addMemberToTicket,
  removeMemberFromTicket,
} from '../services/ticketService.js';

export async function handleTicketInteraction(interaction) {
  try {
    if (interaction.isStringSelectMenu() && interaction.customId === panelSelectCustomId()) {
      const categoryId = interaction.values[0];
      await interaction.showModal(buildOpenModal(categoryId));
      return true;
    }

    if (interaction.isModalSubmit() && isTicketModal(interaction.customId)) {
      await interaction.deferReply({ ephemeral: true });
      const categoryId = parseModalCategory(interaction.customId);
      const result = await createTicketFromModal(interaction, categoryId);
      await interaction.editReply({
        content: result.ok
          ? `✅ ${result.message}`
          : `❌ ${result.message}`,
      });
      return true;
    }

    if (interaction.isButton()) {
      switch (interaction.customId) {
        case CLAIM_ID: {
          await interaction.deferReply({ ephemeral: true });
          const result = await claimTicket(interaction);
          await interaction.editReply({
            content: result.ok ? '✅ Ticket reivindicado.' : `❌ ${result.message}`,
          });
          return true;
        }
        case CLOSE_ID: {
          await interaction.deferReply({ ephemeral: true });
          const result = await requestCloseTicket(interaction);
          await interaction.editReply({
            content: result.ok ? '✅ Confirmacao enviada no canal.' : `❌ ${result.message}`,
          });
          return true;
        }
        case CLOSE_CONFIRM_ID: {
          await interaction.deferReply({ ephemeral: true });
          const result = await closeTicket(interaction);
          await interaction.editReply({
            content: result.ok ? '✅ Ticket fechado.' : `❌ ${result.message}`,
          });
          return true;
        }
        case CLOSE_CANCEL_ID: {
          await interaction.update({ components: [] }).catch(async () => {
            await interaction.reply({ content: 'Fechamento cancelado.', ephemeral: true });
          });
          return true;
        }
        case TRANSCRIPT_ID: {
          const result = await sendTranscript(interaction);
          if (!result.handled && !result.ok) {
            await interaction.reply({ content: `❌ ${result.message}`, ephemeral: true });
          }
          return true;
        }
        default:
          return false;
      }
    }
  } catch (e) {
    const msg = e?.message || 'Erro ao processar ticket.';
    if (interaction.deferred || interaction.replied) {
      await interaction.editReply({ content: `❌ ${msg}` }).catch(() => {});
    } else {
      await interaction.reply({ content: `❌ ${msg}`, ephemeral: true }).catch(() => {});
    }
    return true;
  }

  return false;
}

export async function sendTicketPanel(channel) {
  await channel.send({
    embeds: [buildPanelEmbed()],
    components: buildPanelComponents(),
  });
}

export async function handleTicketCommand(interaction) {
  const sub = interaction.options.getSubcommand();

  if (sub === 'painel') {
    if (!interaction.memberPermissions?.has('ManageChannels')) {
      await interaction.reply({
        content: '❌ Voce precisa de **Gerenciar Canais** para publicar o painel.',
        ephemeral: true,
      });
      return;
    }
    await interaction.deferReply({ ephemeral: true });
    await sendTicketPanel(interaction.channel);
    await interaction.editReply({ content: '✅ Painel de tickets publicado neste canal.' });
    return;
  }

  if (sub === 'fechar') {
    await interaction.deferReply({ ephemeral: true });
    const motivo = interaction.options.getString('motivo') || 'Fechado via comando';
    const result = await closeTicket(interaction, motivo);
    await interaction.editReply({
      content: result.ok ? '✅ Ticket fechado.' : `❌ ${result.message}`,
    });
    return;
  }

  if (sub === 'adicionar') {
    const member = interaction.options.getUser('membro', true);
    await interaction.deferReply({ ephemeral: true });
    const result = await addMemberToTicket(interaction, member);
    await interaction.editReply({
      content: result.ok ? `✅ ${result.message}` : `❌ ${result.message}`,
    });
    return;
  }

  if (sub === 'remover') {
    const member = interaction.options.getUser('membro', true);
    await interaction.deferReply({ ephemeral: true });
    const result = await removeMemberFromTicket(interaction, member);
    await interaction.editReply({
      content: result.ok ? `✅ ${result.message}` : `❌ ${result.message}`,
    });
  }
}
