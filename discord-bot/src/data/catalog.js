export const RANKS = [
  { id: 'MEMBER', name: 'Membro' },
  { id: 'VIP', name: 'VIP' },
  { id: 'NATIVE', name: 'Native' },
  { id: 'EMBER', name: 'Ember' },
  { id: 'EMBER_PLUS', name: 'Ember+' },
  { id: 'SAVAGE', name: 'Savage' },
  { id: 'BETA', name: 'Beta' },
  { id: 'PRECIOUS', name: 'Precious' },
  { id: 'CREATOR', name: 'Creator' },
  { id: 'CREATOR_PLUS', name: 'Creator+' },
  { id: 'CHAMPION', name: 'Champion' },
  { id: 'BUILDER', name: 'Builder' },
  { id: 'HELPER', name: 'Helper' },
  { id: 'MOD', name: 'Mod' },
  { id: 'MODPLUS', name: 'Mod+' },
  { id: 'ADMIN', name: 'Admin' },
  { id: 'DIRETOR', name: 'Diretor' },
  { id: 'VERMELHO', name: 'Vermelho' },
  { id: 'OWNER', name: 'Owner' },
  { id: 'ZW', name: 'ZW' },
];

export const MEDALS = [
  { id: 'NONE', name: 'Nenhuma' },
  { id: 'TOWER', name: 'Torre' },
  { id: 'MESTRE', name: 'Mestre' },
  { id: 'RAFA', name: 'Rafa' },
];

export const TAGS = [
  { id: 'MEMBER', name: 'Membro' },
  { id: 'VIP', name: 'VIP' },
  { id: 'NATIVE', name: 'Native' },
  { id: 'EMBER', name: 'Ember' },
  { id: 'EMBER_PLUS', name: 'Ember+' },
  { id: 'SAVAGE', name: 'Savage' },
  { id: 'BETA', name: 'Beta' },
  { id: 'PRECIOUS', name: 'Precious' },
  { id: 'CREATOR', name: 'Creator' },
  { id: 'CREATOR_PLUS', name: 'Creator+' },
  { id: 'TAG_CHAMPION', name: 'Champion' },
  { id: 'CHAMPION', name: 'Champion' },
  { id: 'BUILDER', name: 'Builder' },
  { id: 'HELPER', name: 'Helper' },
  { id: 'MOD', name: 'Mod' },
  { id: 'MODPLUS', name: 'Mod+' },
  { id: 'ADMIN', name: 'Admin' },
  { id: 'DIRETOR', name: 'Diretor' },
  { id: 'VERMELHO', name: 'Vermelho' },
  { id: 'OWNER', name: 'Owner' },
  { id: 'TAG_ZW', name: 'ZW' },
  { id: 'ZW', name: 'ZW' },
];

export function findCatalogItem(list, id) {
  if (!id) return null;
  const key = String(id).toUpperCase();
  return list.find((x) => x.id === key) || null;
}
