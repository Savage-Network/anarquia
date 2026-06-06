import { query } from '../db/pool.js';

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

async function resolvePlayerId(identifier) {
  const isUuid =
    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(
      identifier,
    );
  if (isUuid) return identifier;

  const { rows } = await query(
    'SELECT id FROM accounts WHERE LOWER(name) = LOWER(?) LIMIT 1',
    [identifier],
  );
  return rows[0]?.id || null;
}

/** Perfil minimo para sync de cargos Discord (rank + tag). */
export async function loadAccountForRoleSync(identifier) {
  const playerId = await resolvePlayerId(identifier);
  if (!playerId) return null;

  const { rows } = await query(
    `SELECT primary_rank, primary_expires_at, available_ranks, tag
     FROM account_identity WHERE player_id = ? LIMIT 1`,
    [playerId],
  );

  if (!rows.length) {
    return { account: { rank: { type: 'MEMBER', expiresAt: -1 }, tag: { id: 'MEMBER' }, availableRanks: [] } };
  }

  const row = rows[0];
  const rankType = parseRankType(row.primary_rank);
  const tagId = parseRankType(row.tag);

  return {
    account: {
      rank: {
        type: rankType,
        expiresAt: row.primary_expires_at ?? -1,
      },
      tag: { id: tagId, name: tagId },
      availableRanks: parseAvailableRanks(row.available_ranks).map((r) => ({
        type: parseRankType(r.type),
        expiresAt: r.expiresAt ?? -1,
      })),
    },
  };
}
