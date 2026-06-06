import { findCatalogItem, RANKS, TAGS } from '../data/catalog.js';

const COLOR_HEX = {
  GRAY: '#aaaaaa',
  GOLD: '#ffaa00',
  YELLOW: '#ffff55',
  RED: '#ff5555',
  DARK_GREEN: '#00aa00',
  BLUE: '#5555ff',
  DARK_PURPLE: '#aa00aa',
  AQUA: '#55ffff',
  DARK_RED: '#aa0000',
  WHITE: '#ffffff',
};

function colorHex(colorName) {
  return COLOR_HEX[colorName] || '#aaaaaa';
}

function formatTagBlock(tagId, rankType) {
  const tag = findCatalogItem(TAGS, tagId);
  const rank = findCatalogItem(RANKS, rankType || tag?.rankSource || tagId);
  return {
    name: tag?.name || tagId,
    color: colorHex(rank?.color),
    data: { plus: 'black' },
  };
}

function medalIdList(medals) {
  return (medals || []).map((m) => {
    const id = typeof m === 'string' ? m : m?.id;
    return id ? id.toLowerCase() : 'none';
  });
}

function buildTagsList(account) {
  const tags = new Set(['member']);
  const tagId = account.tag?.id || 'MEMBER';
  const rankId = account.rank?.type || 'MEMBER';
  tags.add(tagId.toLowerCase());
  tags.add(rankId.toLowerCase());
  for (const r of account.availableRanks || []) {
    if (r.type) tags.add(String(r.type).toLowerCase());
  }
  return [...tags];
}

export function toMushPlayer(profile) {
  const acc = profile.account;
  const tagId = acc.tag?.id || 'MEMBER';
  const rankType = acc.rank?.type || 'MEMBER';
  const tagBlock = formatTagBlock(tagId, rankType);
  const rankBlock = formatTagBlock(rankType, rankType);

  const activeMutes = (profile.punishments?.active || []).filter(
    (p) => p.category === 'MUTE',
  ).length;

  const response = {
    account: {
      profile_id: acc.id,
      unique_id: acc.id,
      username: acc.name,
      type: acc.premium ? 'premium' : 'offline',
      rank: acc.rank,
      tag: acc.tag,
      medal: acc.medal,
    },
    best_tag: tagBlock,
    profile_tag: tagBlock,
    rank_tag: rankBlock,
    clan: acc.clan
      ? {
          name: acc.clan.identifier,
          tag: acc.clan.identifier,
          tag_color: '#55ff55',
          role: acc.clan.role,
        }
      : null,
    connected: acc.online,
    first_login: acc.createdAt,
    last_login: acc.lastLogin,
    friends: {
      count: acc.friends?.count ?? 0,
      limit: acc.friends?.max ?? 12,
    },
    medal: (acc.medal?.id || 'NONE').toLowerCase(),
    medals: medalIdList(acc.medals),
    mute_blacklist_count: activeMutes,
    punishments: profile.punishments,
    skin: acc.skin
      ? {
          value: acc.skin.value,
          type: acc.skin.type,
          slim: false,
        }
      : null,
    level: acc.level,
    golds: acc.golds,
    party: acc.party,
    stats: profile.stats || {},
    tags: buildTagsList(acc),
    discord: profile.discord || null,
  };

  return {
    success: true,
    error_code: -1,
    response,
  };
}

export function mushNotFound(username) {
  return {
    success: false,
    error_code: 1,
    error_message: `Jogador "${username}" não encontrado.`,
    response: null,
  };
}
