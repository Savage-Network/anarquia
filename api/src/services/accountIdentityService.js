import { query } from '../db/pool.js';
import { findCatalogItem, RANKS, TAGS } from '../data/catalog.js';

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

export async function loadAccountIdentity(playerId) {
  const { rows } = await query(
    `SELECT primary_rank, primary_given_at, primary_expires_at, available_ranks, tag
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
      name: findCatalogItem(RANKS, rankType)?.name || rankType,
      expiresAt: row.primary_expires_at ?? -1,
      permanent: (row.primary_expires_at ?? -1) <= -1,
      assignedAt: row.primary_given_at,
    },
    tag: findCatalogItem(TAGS, tagId) || { id: tagId, name: tagId },
    availableRanks: parseAvailableRanks(row.available_ranks).map((r) => ({
      type: parseRankType(r.type),
      name: findCatalogItem(RANKS, parseRankType(r.type))?.name || r.type,
      expiresAt: r.expiresAt ?? -1,
      permanent: (r.expiresAt ?? -1) <= -1,
      assignedAt: r.assignedAt,
    })),
  };
}

export function applyIdentityToAccount(formattedAccount, identity) {
  if (!identity || !formattedAccount) return formattedAccount;

  formattedAccount.rank = identity.rank;
  formattedAccount.tag = identity.tag;
  formattedAccount.availableRanks = identity.availableRanks;
  return formattedAccount;
}
