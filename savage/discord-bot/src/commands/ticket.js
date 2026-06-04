import {
  ActionRowBuilder,
  ButtonBuilder,
  ButtonStyle,
  EmbedBuilder,
} from 'discord.js';
import { getTicketTypes } from '../tickets/manager.js';

export const data = {
  name: 'ticket',
  description: 'Criar um painel de tickets',
};

export async function execute(interaction) {
  const types = getTicketTypes();
  const embed = new EmbedBuilder()
    .setTitle('Central de Atendimento')
    .setDescription(
      '> Caso necessite de suporte, clique no botão abaixo para abrir um ticket e solicitar atendimento diretamente com o nosso time de suporte.\n\n' +
        '`Pedimos que utilize este recurso apenas para solicitações que realmente exijam atendimento individual.`',
    )
    .addFields({
      name: 'Horário de Atendimento',
      value:
        '🗓️ Segunda à Sexta: **10h00** às **21h**.\n🗓️ Finais de semana e feriado: **14h00** às **21h**.',
    })
    .setColor(0x808080);

  const row = new ActionRowBuilder();
  for (const [key, type] of Object.entries(types)) {
    row.addComponents(
      new ButtonBuilder()
        .setCustomId(`create_ticket_${key}`)
        .setLabel(type.displayName)
        .setStyle(ButtonStyle.Primary),
    );
  }

  await interaction.reply({ embeds: [embed], components: [row] });
}
