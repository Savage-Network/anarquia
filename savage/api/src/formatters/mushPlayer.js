import { findCatalogItem, MEDALS, RANKS, TAGS } from '../data/catalog.js';

const COLOR_HEX = {
  GRAY: '#aaaaaa',
  GOLD: '#ffaa00',
  YELLOW: '#ffff55',
  RED: '#ff5555',
  DARK_GREEN: '#00aa00',
  BLUE: '#5555ff',
  DARK_PURPLE: '#aa00aa',
  AQUA: '#55ffff',
  DARK_AQUA: '#00aaaa',
  DARK_RED: '#aa0000',
  WHITE: '#ffffff',
  LIGHT_PURPLE: '#ff55ff',
  GREEN: '#55ff55',
  DARK_GRAY: '#555555',
  BLACK: '#000000',
};

const BEDWARS_PREFIX = {
  BEDWARS_SOLO: 'solo',
  BEDWARS_DUO: 'doubles',
  BEDWARS_TRIO: '3v3',
  BEDWARS_QUARTET: '4v4',
  BEDWARS_VERSUS_SOLO: '1v1',
  BEDWARS_VERSUS_DUO: '2v2',
  BEDWARS_VERSUS_TRIO: '3v3v3v3',
  BEDWARS_VERSUS_QUARTET: '4v4v4v4',
  BEDWARS_INFECTION: 'infection_solo',
};

const DUELS_PREFIX = {
  DUELS_SIMULATOR: 'simulator',
  DUELS_UHC: 'uhc',
  DUELS_FIREBALL: 'fireball_fight',
  DUELS_THE_BRIDGE: 'bridge',
  DUELS_GLADIATOR: 'gladiator',
  DUELS_NO_DEBUFF: 'no_debuff',
};

const SKYWARS_PREFIX = {
  SKYWARS_SOLO: 'solo',
  SKYWARS_DUO: '2v2',
  SKYWARS_TRIO: '3v3',
  SKYWARS_QUARTET: '4v4',
  SKYWARS_VERSUS_SOLO: '1v1',
  SKYWARS_VERSUS_DUO: 'duels',
  SKYWARS_VERSUS_TRIO: '3v3_duels',
  SKYWARS_VERSUS_QUARTET: '4v4_duels',
};

const EGGWARS_PREFIX = {
  EGGWARS_SOLO: 'solo',
  EGGWARS_DUO: 'doubles',
  EGGWARS_TRIO: '3v3',
  EGGWARS_QUARTET: '4v4',
};

const THEBRIDGE_PREFIX = {
  THE_BRIDGE_SOLO: 'solo',
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

function fkdr(finalKills, deaths) {
  if (!deaths) return finalKills > 0 ? finalKills * 100 : 0;
  return Math.round((finalKills / deaths) * 100);
}

function applyModeStats(target, prefix, s) {
  target[`${prefix}_wins`] = s.wins ?? 0;
  target[`${prefix}_losses`] = s.defeats ?? 0;
  target[`${prefix}_games_played`] = s.matches ?? 0;
  target[`${prefix}_kills`] = s.kills ?? 0;
  target[`${prefix}_deaths`] = s.deaths ?? 0;
  target[`${prefix}_assists`] = s.assists ?? 0;
  target[`${prefix}_final_kills`] = s.finalKills ?? 0;
  target[`${prefix}_beds_broken`] = s.bedDestruction ?? 0;
  target[`${prefix}_beds_lost`] = Math.max(0, (s.matches ?? 0) - (s.wins ?? 0));
  target[`${prefix}_winstreak`] = s.winStreak ?? 0;
  target[`${prefix}_max_winstreak`] = s.highWinStreak ?? 0;
  target[`${prefix}_fkdr`] = fkdr(s.finalKills ?? 0, s.deaths ?? 0);
  target[`${prefix}_kills_monthly`] = s.month?.kills ?? 0;
  target[`${prefix}_wins_monthly`] = s.month?.wins ?? 0;
}

function flattenBedwars(data) {
  const out = {};
  if (!data) return out;

  out.level = data.level ?? 0;
  out.xp = data.xp ?? 0;
  out.ranked_points = data.rankedPoints ?? 0;
  out.ranked_tier = data.rankedTier ?? 'Rookie';
  out.coins = data.coins ?? 0;

  const modes = data.modes || [];
  const totals = {
    wins: 0,
    losses: 0,
    games_played: 0,
    kills: 0,
    deaths: 0,
    assists: 0,
    final_kills: 0,
    beds_broken: 0,
    max_winstreak: 0,
    winstreak: 0,
  };

  for (const s of modes) {
    const arcade = String(s.arcade).toUpperCase();
    const prefix = BEDWARS_PREFIX[arcade];
    if (!prefix) continue;

    applyModeStats(out, prefix, s);

    totals.wins += s.wins ?? 0;
    totals.losses += s.defeats ?? 0;
    totals.games_played += s.matches ?? 0;
    totals.kills += s.kills ?? 0;
    totals.deaths += s.deaths ?? 0;
    totals.assists += s.assists ?? 0;
    totals.final_kills += s.finalKills ?? 0;
    totals.beds_broken += s.bedDestruction ?? 0;
    totals.max_winstreak = Math.max(totals.max_winstreak, s.highWinStreak ?? 0);
    totals.winstreak = Math.max(totals.winstreak, s.winStreak ?? 0);
  }

  Object.assign(out, totals);
  out.fkdr = fkdr(out.final_kills, out.deaths);
  out.beds_lost = Math.max(0, out.games_played - out.wins);

  return out;
}

function flattenArcadeModes(data, prefixMap, extra = {}) {
  const out = { ...extra };
  if (!data?.modes) return out;

  out.level = data.level ?? 0;
  out.xp = data.xp ?? 0;

  for (const s of data.modes) {
    const arcade = String(s.arcade).toUpperCase();
    const prefix = prefixMap[arcade] || arcade.toLowerCase().replace(/^skywars_/, '').replace(/^eggwars_/, '');
    applyModeStats(out, prefix, s);
  }

  return out;
}

function flattenPvp(data) {
  const out = {};
  if (!data?.modes) return out;

  for (const s of data.modes) {
    const prefix = s.mode || 'arena';
    applyModeStats(out, prefix, s);
    out[`${prefix}_max_killstreak`] = s.highKillStreak ?? 0;
    out[`${prefix}_killstreak`] = s.killStreak ?? 0;
  }

  return out;
}

function flattenDuels(data) {
  const out = { xp: data?.xp ?? 0, level: data?.level ?? 0 };
  if (!data?.modes) return out;

  for (const s of data.modes) {
    const arcade = String(s.arcade).toUpperCase();
    const prefix = DUELS_PREFIX[arcade] || arcade.replace(/^DUELS_/, '').toLowerCase();
    applyModeStats(out, prefix, s);
    out[`${prefix}_played`] = s.matches ?? 0;
    out[`${prefix}_xp`] = data.xp ?? 0;
  }

  return out;
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

/**
 * Converte o perfil interno para o formato Mush ({ success, error_code, response }).
 */
export function toMushPlayer(profile) {
  const acc = profile.account;
  const ctx = profile;
  const tagId = acc.tag?.id || 'MEMBER';
  const rankType = acc.rank?.type || 'MEMBER';
  const tagBlock = formatTagBlock(tagId, rankType);
  const rankBlock = formatTagBlock(rankType, rankType);

  const activeMutes = (profile.punishments?.active || []).filter(
    (p) => p.category === 'MUTE',
  ).length;

  const minigames = profile.minigames || {};

  const response = {
    account: {
      profile_id: acc.id,
      unique_id: acc.id,
      username: acc.name,
      type: acc.premium ? 'premium' : 'offline',
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
    stats: {
      bedwars: flattenBedwars(minigames.bedwars),
      skywars: flattenArcadeModes(minigames.skywars, SKYWARS_PREFIX),
      eggwars: flattenArcadeModes(minigames.eggwars, EGGWARS_PREFIX),
      pvp: flattenPvp(minigames.pvp),
      duels: flattenDuels(minigames.duels),
      thebridge: flattenArcadeModes(minigames.thebridge, THEBRIDGE_PREFIX),
    },
    tags: buildTagsList(acc),
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

export function mushError(code, message) {
  return {
    success: false,
    error_code: code,
    error_message: message,
    response: null,
  };
}
