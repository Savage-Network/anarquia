import { query } from '../db/pool.js';
import { getMinigame, MINIGAMES } from '../data/catalog.js';
import { computeRatios, getRankedTier, pickInt } from '../utils/format.js';
import { findAccount } from './accountService.js';

export async function fetchMemberRow(table, accountId) {
  const { rows } = await query(`SELECT data FROM ${table} WHERE id = $1`, [accountId]);
  if (!rows.length) return null;
  return rows[0].data;
}

function normalizeStatsEntry(entry) {
  const arcade = entry.arcade || entry.arcade?.name || 'UNKNOWN';
  const type = entry.type || null;
  const base = {
    arcade,
    type,
    matches: pickInt(entry, 'matches'),
    wins: pickInt(entry, 'wins'),
    defeats: pickInt(entry, 'defeats'),
    kills: pickInt(entry, 'kills'),
    deaths: pickInt(entry, 'deaths'),
    assists: pickInt(entry, 'assists'),
    finalKills: pickInt(entry, 'finalKills'),
    bedDestruction: pickInt(entry, 'bedDestruction'),
    winStreak: pickInt(entry, 'winStreak'),
    highWinStreak: pickInt(entry, 'highWinStreak'),
    killStreak: pickInt(entry, 'killStreak'),
    highKillStreak: pickInt(entry, 'highKillStreak'),
    week: {
      wins: pickInt(entry, 'weekWins'),
      defeats: pickInt(entry, 'weekDefeats'),
      matches: pickInt(entry, 'weekMatches'),
      kills: pickInt(entry, 'weekKills'),
    },
    month: {
      wins: pickInt(entry, 'monthWins'),
      defeats: pickInt(entry, 'monthDefeats'),
      matches: pickInt(entry, 'monthMatches'),
      kills: pickInt(entry, 'monthKills'),
    },
  };
  return { ...base, ratios: computeRatios(base) };
}

function parseStatsList(data) {
  const list = data?.metadata?.statsList || [];
  return list.map(normalizeStatsEntry);
}

function parseContextStatsMap(data) {
  const statsMap = data?.context?.stats || {};
  if (Array.isArray(statsMap)) {
    return statsMap.map(normalizeStatsEntry);
  }
  return Object.entries(statsMap).map(([arcade, entry]) =>
    normalizeStatsEntry({ arcade, ...(typeof entry === 'object' ? entry : {}) }),
  );
}

function parsePvpStats(data) {
  const stats = data?.stats;
  if (!stats) return [];
  const modes = ['arena', 'fps', 'lava', 'mlg'];
  return modes
    .filter((mode) => stats[mode])
    .map((mode) => ({
      mode,
      ...normalizeStatsEntry(stats[mode]),
    }));
}

function combineBedwarsGeneral(statsList) {
  const bed = statsList.filter((s) => s.arcade?.startsWith?.('BEDWARS_') || String(s.arcade).startsWith('BEDWARS_'));
  if (!bed.length) return null;

  const combined = bed.reduce(
    (acc, s) => {
      acc.matches += s.matches;
      acc.wins += s.wins;
      acc.defeats += s.defeats;
      acc.kills += s.kills;
      acc.deaths += s.deaths;
      acc.finalKills += s.finalKills;
      acc.bedDestruction += s.bedDestruction;
      acc.winStreak = Math.max(acc.winStreak, s.winStreak);
      acc.highWinStreak = Math.max(acc.highWinStreak, s.highWinStreak);
      return acc;
    },
    {
      arcade: 'BEDWARS_GENERAL',
      type: null,
      matches: 0,
      wins: 0,
      defeats: 0,
      kills: 0,
      deaths: 0,
      assists: 0,
      finalKills: 0,
      bedDestruction: 0,
      winStreak: 0,
      highWinStreak: 0,
    },
  );

  return { ...combined, ratios: computeRatios(combined) };
}

export function parseMinigameStats(modeId, raw) {
  if (!raw) return null;

  let modes = [];
  if (modeId === 'pvp') {
    modes = parsePvpStats(raw);
  } else if (modeId === 'duels' || modeId === 'thebridge') {
    modes = parseContextStatsMap(raw);
  } else {
    modes = parseStatsList(raw);
  }

  const context = raw.context || {};
  const metadata = raw.metadata || {};

  const payload = {
    id: raw.id,
    name: raw.name,
    level: pickInt(context, 'level'),
    xp: pickInt(context, 'levelXp'),
    joinedAt: context.joinedAt,
    rankedPoints: pickInt(metadata, 'rankedPoints'),
    rankedTier: getRankedTier(pickInt(metadata, 'rankedPoints')),
    coins: pickInt(metadata, 'coins'),
    modes,
  };

  if (modeId === 'bedwars') {
    const general = combineBedwarsGeneral(modes);
    if (general) payload.general = general;
  }

  return payload;
}

export async function getMinigameStats(modeId, identifier, arcadeFilter) {
  const minigame = getMinigame(modeId);
  if (!minigame) return { error: 'INVALID_MODE' };

  const account = await findAccount(identifier);
  if (!account) return { error: 'PLAYER_NOT_FOUND' };

  const raw = await fetchMemberRow(minigame.table, account.id);
  if (!raw) return { error: 'NO_DATA', player: { id: account.id, name: account.name } };

  const parsed = parseMinigameStats(modeId, raw);
  if (arcadeFilter) {
    const upper = arcadeFilter.toUpperCase();
    if (upper === 'GENERAL' && modeId === 'bedwars' && parsed.general) {
      return { minigame: minigame.id, player: { id: account.id, name: account.name }, stats: parsed.general };
    }
    const match = parsed.modes.find(
      (m) => String(m.arcade).toUpperCase() === upper || String(m.mode).toUpperCase() === upper,
    );
    if (!match) return { error: 'MODE_NOT_FOUND', player: { id: account.id, name: account.name } };
    return { minigame: minigame.id, player: { id: account.id, name: account.name }, stats: match };
  }

  return { minigame: minigame.id, player: { id: account.id, name: account.name }, ...parsed };
}

export async function getAllMinigameStats(identifier) {
  const account = await findAccount(identifier);
  if (!account) return null;

  const result = {
    player: { id: account.id, name: account.name },
    minigames: {},
  };

  await Promise.all(
    MINIGAMES.map(async (game) => {
      const raw = await fetchMemberRow(game.table, account.id);
      result.minigames[game.id] = raw ? parseMinigameStats(game.id, raw) : null;
    }),
  );

  return result;
}
