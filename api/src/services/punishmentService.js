import { query } from '../db/pool.js';
import { findAccount } from './accountService.js';
import { parseJsonColumn } from '../utils/format.js';

const nameCache = new Map();

async function resolveName(uuid) {
  if (!uuid) return 'CONSOLE';
  const key = String(uuid).toLowerCase();
  if (nameCache.has(key)) return nameCache.get(key);

  const account = await findAccount(key);
  const name =
    account?.name ||
    (key === '00000000-0000-0000-0000-000000000000' ? 'CONSOLE' : '...');
  nameCache.set(key, name);
  return name;
}

export function formatPunishment(raw, playerName, authorName) {
  const revoked = raw.revocation?.valid === true || raw.valid === false;
  const active =
    raw.valid === true &&
    !revoked &&
    !(raw.expiresAt > 0 && raw.expiresAt <= Date.now());

  return {
    id: raw.id,
    category: raw.category,
    reason: raw.reason,
    cause: raw.cause,
    server: raw.server,
    arcade: raw.arcade,
    player: { uuid: raw.player, name: playerName },
    author: { uuid: raw.author, name: authorName },
    ip: raw.ip,
    valid: raw.valid,
    active,
    createdAt: raw.createdAt,
    expiresAt: raw.expiresAt,
    temporary: raw.expiresAt > -1,
    expired: raw.expiresAt > 0 && raw.expiresAt <= Date.now(),
    revocation: raw.revocation
      ? {
          author: raw.revocation.author,
          reason: raw.revocation.reason,
          at: raw.revocation.data,
          valid: raw.revocation.valid,
        }
      : null,
  };
}

export async function listPunishments({ category, player, active, limit = 50, offset = 0 }) {
  let sql = 'SELECT id, data, player, author, created_at FROM punishments';
  const clauses = [];
  const params = [];

  if (category) {
    params.push(category.toUpperCase());
    clauses.push(`JSON_UNQUOTE(JSON_EXTRACT(data, '$.category')) = ?`);
  }

  if (player) {
    let playerUuid = player;
    if (!/^[0-9a-f-]{36}$/i.test(player)) {
      const account = await findAccount(player);
      if (account) playerUuid = account.id;
    }
    params.push(playerUuid);
    clauses.push('player = ?');
  }

  if (clauses.length) sql += ` WHERE ${clauses.join(' AND ')}`;
  sql += ' ORDER BY created_at DESC LIMIT 500';

  const { rows } = await query(sql, params);
  let items = [];

  for (const row of rows) {
    const raw = parseJsonColumn(row.data);
    if (!raw) continue;
    const playerName = await resolveName(raw.player);
    const authorName = await resolveName(raw.author);
    const formatted = formatPunishment(raw, playerName, authorName);
    if (active === 'true' && !formatted.active) continue;
    if (active === 'false' && formatted.active) continue;
    items.push(formatted);
  }

  const total = items.length;
  items = items.slice(offset, offset + Math.min(limit, 200));

  return { total, limit, offset, items };
}

export async function getPunishmentById(id) {
  const { rows } = await query(
    `SELECT data FROM punishments
     WHERE LOWER(JSON_UNQUOTE(JSON_EXTRACT(data, '$.id'))) = LOWER(?)
        OR id = ?
     LIMIT 1`,
    [id, id],
  );
  if (!rows.length) return null;

  const raw = parseJsonColumn(rows[0].data);
  if (!raw) return null;
  const playerName = await resolveName(raw.player);
  const authorName = await resolveName(raw.author);
  return formatPunishment(raw, playerName, authorName);
}

export async function getPlayerPunishments(identifier, { category, active } = {}) {
  const account = await findAccount(identifier);
  if (!account) return null;

  const fromHistory = account.context?.history?.punishments || [];
  const fromDb = await listPunishments({
    player: account.id,
    category,
    active,
    limit: 500,
    offset: 0,
  });

  const byId = new Map();
  for (const p of fromDb.items) byId.set(p.id, p);
  for (const raw of fromHistory) {
    const playerName = account.name;
    const authorName = await resolveName(raw.author);
    const formatted = formatPunishment(raw, playerName, authorName);
    if (category && formatted.category !== category.toUpperCase()) continue;
    if (active === 'true' && !formatted.active) continue;
    if (active === 'false' && formatted.active) continue;
    byId.set(formatted.id, formatted);
  }

  return {
    player: { id: account.id, name: account.name },
    punishments: [...byId.values()].sort((a, b) => b.createdAt - a.createdAt),
  };
}
