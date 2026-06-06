import { config } from '../config.js';

const PRIORITY = [
  { key: 'CHAMPION', match: (acc) => hasTag(acc, 'TAG_CHAMPION', 'CHAMPION') },
  { key: 'CREATOR_PLUS', match: (acc) => hasTag(acc, 'CREATOR_PLUS') },
  { key: 'CREATOR', match: (acc) => hasTag(acc, 'CREATOR') },
  {
    key: 'PRECIOUS',
    match: (acc) => hasRank(acc, 'PRECIOUS') || hasTag(acc, 'TAG_PRECIOUS'),
  },
  { key: 'BETA', match: (acc) => hasRank(acc, 'BETA') || hasTag(acc, 'TAG_BETA') },
  { key: 'SAVAGE', match: (acc) => hasRank(acc, 'SAVAGE') },
  { key: 'EMBER', match: (acc) => hasRank(acc, 'EMBER', 'EMBER_PLUS') },
  { key: 'NATURE', match: (acc) => hasRank(acc, 'NATIVE') || hasTag(acc, 'NATIVE') },
];

function normalizeId(value) {
  return String(value || '').toUpperCase();
}

function hasTag(acc, ...tags) {
  const tagId = normalizeId(acc?.tag?.id || acc?.tag?.name);
  return tags.some((t) => tagId === normalizeId(t));
}

function rankActive(rank) {
  if (!rank?.type) return false;
  const expires = rank.expiresAt ?? -1;
  return expires <= -1 || expires > Date.now();
}

function hasRank(acc, ...ranks) {
  const primary = normalizeId(acc?.rank?.type);
  if (ranks.includes(primary) && rankActive(acc?.rank)) return true;

  for (const r of acc?.availableRanks || []) {
    if (!rankActive(r)) continue;
    if (ranks.includes(normalizeId(r.type))) return true;
  }

  return false;
}

export function resolveRankRoleKey(acc) {
  for (const entry of PRIORITY) {
    if (entry.match(acc || {})) return entry.key;
  }
  return null;
}

export function allManagedRoleIds() {
  const ids = Object.values(config.roles.ranks).filter(Boolean);
  if (config.roles.linked) ids.push(config.roles.linked);
  return [...new Set(ids)];
}

export async function syncMemberRoles(guild, member, acc, linked) {
  if (!guild || !member) return { added: [], removed: [] };

  const managed = allManagedRoleIds();
  const desired = new Set();

  if (linked && config.roles.linked) {
    desired.add(config.roles.linked);
  }

  const rankKey = resolveRankRoleKey(acc);
  if (rankKey && config.roles.ranks[rankKey]) {
    desired.add(config.roles.ranks[rankKey]);
  }

  const toAdd = [];
  const toRemove = [];

  for (const roleId of managed) {
    const has = member.roles.cache.has(roleId);
    const want = desired.has(roleId);
    if (want && !has) toAdd.push(roleId);
    if (!want && has) toRemove.push(roleId);
  }

  if (toAdd.length) await member.roles.add(toAdd, 'SavageMC: sync rank/vinculo');
  if (toRemove.length) await member.roles.remove(toRemove, 'SavageMC: sync rank/vinculo');

  return { added: toAdd, removed: toRemove, rankKey };
}

export async function clearManagedRoles(member) {
  if (!member) return;
  const managed = allManagedRoleIds();
  const toRemove = managed.filter((id) => member.roles.cache.has(id));
  if (toRemove.length) {
    await member.roles.remove(toRemove, 'SavageMC: desvinculo');
  }
}

export function rankDisplayName(key) {
  if (!key) return 'Nenhum';
  return config.roles.labels[key] || key;
}
