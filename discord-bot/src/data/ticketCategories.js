/** Categorias de ticket — espelha o painel e o site-loja. */
export const TICKET_CATEGORIES = {
  SUPORTE: {
    id: 'SUPORTE',
    label: 'Suporte',
    emoji: '🛟',
    description: 'Dúvidas gerais, conta, vinculação e ajuda in-game.',
    color: 0x38bdf8,
    channelPrefix: 'suporte',
  },
  DENUNCIA: {
    id: 'DENUNCIA',
    label: 'Denúncia',
    emoji: '🚨',
    description: 'Reporte jogadores, cheats ou violações de regras.',
    color: 0xff6b6b,
    channelPrefix: 'denuncia',
  },
  BUG: {
    id: 'BUG',
    label: 'Bug',
    emoji: '🐛',
    description: 'Problemas técnicos, glitches ou falhas no servidor.',
    color: 0xfbbf24,
    channelPrefix: 'bug',
  },
  COMPRAS: {
    id: 'COMPRAS',
    label: 'Compras',
    emoji: '🛒',
    description: 'Adquira ranks, pacotes e cash com a equipe.',
    color: 0x3ddc97,
    channelPrefix: 'compras',
  },
};

export const TICKET_STATUS = {
  OPEN: 'OPEN',
  CLAIMED: 'CLAIMED',
  CLOSED: 'CLOSED',
};

export function getCategory(id) {
  if (!id) return null;
  return TICKET_CATEGORIES[String(id).toUpperCase()] || null;
}

export function categoryOptions() {
  return Object.values(TICKET_CATEGORIES).map((cat) => ({
    label: `${cat.emoji} ${cat.label}`,
    description: cat.description.slice(0, 100),
    value: cat.id,
  }));
}
