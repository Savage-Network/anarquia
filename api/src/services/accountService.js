import { query } from '../db/pool.js';
import { findCatalogItem, MEDALS, RANKS, TAGS } from '../data/catalog.js';
import { isUuid, parseJsonColumn, stripMcColors } from '../utils/format.js';

export async function findAccount(identifier) {
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

export async function searchAccounts(q, limit = 20) {
  const { rows } = await query(
    `SELECT id, name, data FROM accounts
     WHERE LOWER(name) LIKE LOWER(?)
     ORDER BY name ASC
     LIMIT ?`,
    [`%${q}%`, Math.min(limit, 50)],
  );
  return rows
    .map((r) => {
      const acc = parseJsonColumn(r.data);
      if (!acc) return null;
      if (!acc.id) acc.id = r.id;
      if (!acc.name) acc.name = r.name;
      return acc;
    })
    .filter(Boolean);
}

function mapEnum(list, id) {
  if (!id) return null;
  const item = findCatalogItem(list, id);
  return item
    ? { id: item.id, name: item.name, ...(item.role ? { role: item.role } : {}) }
    : { id, name: id };
}

export function formatAccountPublic(account) {
  const ctx = account.context || {};
  const rank = ctx.rankInfo?.rank || {};
  const tagId = typeof ctx.tag === 'string' ? ctx.tag : ctx.tag?.name || 'MEMBER';
  const medalId = typeof ctx.medal === 'string' ? ctx.medal : ctx.medal?.name || 'NONE';

  const medalsOwned = (ctx.medals || []).map((m) =>
    mapEnum(MEDALS, typeof m === 'string' ? m : m?.name),
  );

  const availableRanks = (ctx.rankInfo?.availableRanks || []).map((r) => ({
    type: r.type,
    name: mapEnum(RANKS, r.type)?.name || r.type,
    expiresAt: r.expiresAt ?? -1,
    permanent: (r.expiresAt ?? -1) <= -1,
    assignedAt: r.assignedAt,
  }));

  const history = ctx.history?.punishments || [];

  return {
    id: account.id,
    name: account.name,
    nickname: ctx.fake?.valid ? ctx.fake.nick : account.name,
    online: Boolean(ctx.online),
    premium: Boolean(ctx.premium),
    level: ctx.level ?? 1,
    golds: ctx.golds ?? 0,
    createdAt: ctx.createdAt,
    lastLogin: ctx.lastLogin,
    tag: mapEnum(TAGS, tagId),
    medal: mapEnum(MEDALS, medalId),
    medals: medalsOwned,
    rank: {
      type: rank.type || 'MEMBER',
      name: mapEnum(RANKS, rank.type)?.name || rank.type,
      expiresAt: rank.expiresAt ?? -1,
      permanent: (rank.expiresAt ?? -1) <= -1,
      assignedAt: rank.assignedAt,
      author: rank.author,
    },
    availableRanks,
    clan: ctx.clan?.tag && ctx.clan.tag !== '...'
      ? { identifier: ctx.clan.tag, tag: ctx.clan.tag, id: ctx.clan.id, role: ctx.clan.role }
      : ctx.clan?.identifier && ctx.clan.identifier !== '...'
        ? { identifier: ctx.clan.identifier, tag: ctx.clan.identifier, id: ctx.clan.id, role: ctx.clan.role }
        : null,
    punishmentHistoryCount: history.length,
    displayName: stripMcColors(account.name),
  };
}
