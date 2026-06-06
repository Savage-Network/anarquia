import { findCatalogItem, MEDALS, RANKS, TAGS } from '../data/catalog.js';
import { query } from '../db/pool.js';
import { findLinkByPlayer } from './linkService.js';
import { isPlayerOnline, resolveAccountPremium } from './presenceService.js';
import { isUuid, parseJsonColumn, stripMcColors } from '../utils/format.js';

function parseRankType(raw) {
  if (!raw) return 'MEMBER';
  return String(raw)
    .trim()
    .toUpperCase()
    .replace('MOD+', 'MODPLUS')
    .replace('MOD_PLUS', 'MODPLUS');
}

function parseAvailableRanks(json) {
  if (!json) return [];
  try {
    const parsed = typeof json === 'string' ? JSON.parse(json) : json;
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function mapEnum(list, id) {
  if (!id) return null;
  const item = findCatalogItem(list, id);
  return item ? { id: item.id, name: item.name } : { id, name: id };
}

async function findAccount(identifier) {
  const sql = isUuid(identifier)
    ? 'SELECT id, name, data FROM accounts WHERE id = ?'
    : 'SELECT id, name, data FROM accounts WHERE LOWER(name) = LOWER(?) LIMIT 1';

  const { rows } = await query(sql, [identifier]);
  if (!rows.length) return null;

  const raw = parseJsonColumn(rows[0].data);
  if (!raw) return null;
  if (!raw.id) raw.id = rows[0].id;
  if (!raw.name) raw.name = rows[0].name;
  return raw;
}

async function loadAccountIdentity(playerId) {
  const { rows } = await query(
    `SELECT primary_rank, primary_expires_at, available_ranks, tag
     FROM account_identity WHERE player_id = ? LIMIT 1`,
    [playerId],
  );
  if (!rows.length) return null;

  const row = rows[0];
  const rankType = parseRankType(row.primary_rank);
  const tagId = parseRankType(row.tag);

  return {
    rank: {
      type: rankType,
      name: mapEnum(RANKS, rankType)?.name || rankType,
      expiresAt: row.primary_expires_at ?? -1,
      permanent: (row.primary_expires_at ?? -1) <= -1,
    },
    tag: mapEnum(TAGS, tagId) || { id: tagId, name: tagId },
    availableRanks: parseAvailableRanks(row.available_ranks).map((r) => ({
      type: parseRankType(r.type),
      name: mapEnum(RANKS, parseRankType(r.type))?.name || r.type,
      expiresAt: r.expiresAt ?? -1,
      permanent: (r.expiresAt ?? -1) <= -1,
    })),
  };
}

async function resolveClan(ctx) {
  const meta = ctx?.clan;
  if (!meta) return null;

  const id = meta.id;
  const tag = meta.tag || meta.identifier;

  if (id && id !== '00000000-0000-0000-0000-000000000000') {
    const { rows } = await query(
      'SELECT id, name, tag FROM clans WHERE id = ? LIMIT 1',
      [id],
    );
    if (rows[0]) {
      return { id: rows[0].id, name: rows[0].name, tag: rows[0].tag };
    }
  }

  if (tag && tag !== '...') {
    const { rows } = await query(
      'SELECT id, name, tag FROM clans WHERE UPPER(tag) = UPPER(?) LIMIT 1',
      [tag],
    );
    if (rows[0]) {
      return { id: rows[0].id, name: rows[0].name, tag: rows[0].tag };
    }
    return { id: id || null, name: tag, tag };
  }

  return null;
}

async function getPlayerStats(uuid) {
  const { rows } = await query(
    `SELECT kills, deaths, golds, best_kill_streak
     FROM network_stats WHERE stat_key = ? LIMIT 1`,
    [`player:${uuid}`],
  );
  return rows[0] || null;
}

function formatSkin(ctx) {
  const skin = ctx?.skin;
  if (!skin) return null;
  return {
    type: skin.type || skin.skinType,
    value: skin.value,
    lastUpdate: skin.lastUpdate,
  };
}

function isPunishmentActive(raw) {
  const revoked = raw.revocation?.valid === true || raw.valid === false;
  return (
    raw.valid === true &&
    !revoked &&
    !(raw.expiresAt > 0 && raw.expiresAt <= Date.now())
  );
}

function collectPunishments(ctx) {
  const history = ctx?.history?.punishments || [];
  const formatted = history.map((raw) => ({
    id: raw.id,
    category: raw.category,
    active: isPunishmentActive(raw),
  }));
  const active = formatted.filter((p) => p.active);
  return { active, history: formatted, total: formatted.length, activeCount: active.length };
}

function formatAccount(account, identity, clan, session) {
  const ctx = account.context || {};
  const tagId = identity?.tag?.id || (typeof ctx.tag === 'string' ? ctx.tag : ctx.tag?.name) || 'MEMBER';
  const medalId = typeof ctx.medal === 'string' ? ctx.medal : ctx.medal?.name || 'NONE';
  const rank = identity?.rank || ctx.rankInfo?.rank || {};

  return {
    id: account.id,
    name: account.name,
    displayName: stripMcColors(account.name),
    online: session.online,
    premium: session.premium,
    accountType: session.type,
    createdAt: ctx.createdAt,
    lastLogin: ctx.lastLogin,
    tag: identity?.tag || mapEnum(TAGS, tagId),
    medal: mapEnum(MEDALS, medalId),
    rank: {
      type: rank.type || 'MEMBER',
      name: rank.name || mapEnum(RANKS, rank.type)?.name || rank.type || 'Membro',
      expiresAt: rank.expiresAt ?? -1,
      permanent: (rank.expiresAt ?? -1) <= -1,
    },
    availableRanks: identity?.availableRanks || [],
    clan: clan || (ctx.clan?.tag && ctx.clan.tag !== '...'
      ? { identifier: ctx.clan.tag, tag: ctx.clan.tag, name: ctx.clan.tag }
      : null),
    skin: formatSkin(ctx),
    customTitle: ctx.customTitle || null,
  };
}

/** Perfil completo para /menu (MySQL direto, sem API). */
export async function loadMenuProfile(identifier) {
  const account = await findAccount(identifier);
  if (!account) return null;

  const [identity, clan, stats, discordLink, online, premiumInfo] = await Promise.all([
    loadAccountIdentity(account.id),
    resolveClan(account.context || {}),
    getPlayerStats(account.id),
    findLinkByPlayer(account.id),
    isPlayerOnline(account.id),
    resolveAccountPremium(account.id, account.context || {}),
  ]);

  const formatted = formatAccount(account, identity, clan, {
    online,
    premium: premiumInfo.premium,
    type: premiumInfo.type,
  });
  const punishments = collectPunishments(account.context || {});

  return {
    id: account.id,
    name: account.name,
    account: formatted,
    punishments,
    stats: stats
      ? {
          kills: stats.kills,
          deaths: stats.deaths,
          golds: stats.golds,
          bestKillStreak: stats.best_kill_streak,
        }
      : null,
    discord: discordLink,
    meta: { fetchedAt: Date.now() },
  };
}
