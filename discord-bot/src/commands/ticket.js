export const data = {
  name: 'ticket',
  description: 'Sistema de tickets SavageMC.',
  options: [
    {
      name: 'painel',
      description: 'Publica o painel de abertura de tickets neste canal.',
      type: 1,
    },
    {
      name: 'fechar',
      description: 'Fecha o ticket do canal atual.',
      type: 1,
      options: [
        {
          name: 'motivo',
          description: 'Motivo do fechamento.',
          type: 3,
          required: false,
        },
      ],
    },
    {
      name: 'adicionar',
      description: 'Adiciona um membro ao ticket atual (staff).',
      type: 1,
      options: [
        {
          name: 'membro',
          description: 'Membro a adicionar.',
          type: 6,
          required: true,
        },
      ],
    },
    {
      name: 'remover',
      description: 'Remove um membro do ticket atual (staff).',
      type: 1,
      options: [
        {
          name: 'membro',
          description: 'Membro a remover.',
          type: 6,
          required: true,
        },
      ],
    },
  ],
};

export { handleTicketCommand as execute } from '../handlers/ticketHandler.js';
