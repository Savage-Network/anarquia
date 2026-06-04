export const PUNISHMENT_CATEGORIES = [
  { id: 'BAN', name: 'Banimento' },
  { id: 'MUTE', name: 'Mute' },
  { id: 'SKIN', name: 'Skin' },
  { id: 'REPORT', name: 'Report' },
];

export const RANKS = [
  { id: 'MEMBER', name: 'Membro', role: 'NONE', color: 'GRAY' },
  { id: 'VIP', name: 'VIP', role: 'VIP', color: 'GOLD' },
  { id: 'NATIVE', name: 'Native', role: 'VIP', color: 'YELLOW' },
  { id: 'EMBER', name: 'Ember', role: 'VIP', color: 'RED' },
  { id: 'EMBER_PLUS', name: 'Ember+', role: 'VIP', color: 'GOLD' },
  { id: 'SAVAGE', name: 'Savage', role: 'VIP', color: 'DARK_GREEN' },
  { id: 'BETA', name: 'Beta', role: 'VIP', color: 'BLUE' },
  { id: 'CREATOR', name: 'Creator', role: 'SPECIAL', color: 'AQUA' },
  { id: 'HELPER', name: 'Helper', role: 'STAFF', color: 'BLUE' },
  { id: 'MOD', name: 'Mod', role: 'STAFF', color: 'DARK_PURPLE' },
  { id: 'ADMIN', name: 'Admin', role: 'STAFF', color: 'DARK_RED' },
  { id: 'OWNER', name: 'Owner', role: 'STAFF', color: 'DARK_RED' },
];

export const MEDALS = [
  { id: 'NONE', name: 'Nenhuma', symbol: '' },
  { id: 'TOWER', name: 'Torre', symbol: 'T' },
  { id: 'MESTRE', name: 'Mestre', symbol: '✠' },
  { id: 'RAFA', name: 'Rafa', symbol: '❥' },
];

export const TAGS = [
  { id: 'MEMBER', name: 'Membro', order: 0, role: 'NONE', rankSource: 'MEMBER' },
  { id: 'VIP', name: 'VIP', order: 10, role: 'VIP', rankSource: 'VIP' },
  { id: 'HELPER', name: 'Helper', order: 90, role: 'STAFF', rankSource: 'HELPER' },
  { id: 'MOD', name: 'Mod', order: 95, role: 'STAFF', rankSource: 'MOD' },
  { id: 'ADMIN', name: 'Admin', order: 98, role: 'STAFF', rankSource: 'ADMIN' },
  { id: 'OWNER', name: 'Owner', order: 99, role: 'STAFF', rankSource: 'OWNER' },
];

export function findCatalogItem(list, id) {
  if (!id) return null;
  const key = String(id).toUpperCase();
  return list.find((x) => x.id === key) || null;
}
