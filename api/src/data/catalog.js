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
  { id: 'PRECIOUS', name: 'Precious', role: 'VIP', color: 'AQUA' },
  { id: 'CREATOR', name: 'Creator', role: 'SPECIAL', color: 'AQUA' },
  { id: 'CREATOR_PLUS', name: 'Creator+', role: 'SPECIAL', color: 'AQUA' },
  { id: 'CHAMPION', name: 'Champion', role: 'SPECIAL', color: 'GOLD' },
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
  { id: 'NATIVE', name: 'Native', order: 12, role: 'VIP', rankSource: 'NATIVE' },
  { id: 'EMBER', name: 'Ember', order: 13, role: 'VIP', rankSource: 'EMBER' },
  { id: 'EMBER_PLUS', name: 'Ember+', order: 14, role: 'VIP', rankSource: 'EMBER_PLUS' },
  { id: 'SAVAGE', name: 'Savage', order: 15, role: 'VIP', rankSource: 'SAVAGE' },
  { id: 'BETA', name: 'Beta', order: 16, role: 'VIP', rankSource: 'BETA' },
  { id: 'PRECIOUS', name: 'Precious', order: 17, role: 'VIP', rankSource: 'PRECIOUS' },
  { id: 'CREATOR', name: 'Creator', order: 80, role: 'SPECIAL', rankSource: 'MEMBER' },
  { id: 'CREATOR_PLUS', name: 'Creator+', order: 81, role: 'SPECIAL', rankSource: 'MEMBER' },
  { id: 'TAG_CHAMPION', name: 'Champion', order: 82, role: 'SPECIAL', rankSource: 'MEMBER' },
  { id: 'CHAMPION', name: 'Champion', order: 82, role: 'SPECIAL', rankSource: 'MEMBER' },
];

export function findCatalogItem(list, id) {
  if (!id) return null;
  const key = String(id).toUpperCase();
  return list.find((x) => x.id === key) || null;
}
