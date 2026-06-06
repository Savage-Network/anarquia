import { query } from '../db/pool.js';

export async function findClanById(clanId) {
  if (!clanId || clanId === '00000000-0000-0000-0000-000000000000') return null;

  const { rows } = await query(
    'SELECT id, name, tag FROM clans WHERE id = ? LIMIT 1',
    [clanId],
  );
  return rows[0] || null;
}

export async function findClanByTag(tag) {
  if (!tag || tag === '...') return null;

  const { rows } = await query(
    'SELECT id, name, tag FROM clans WHERE UPPER(tag) = UPPER(?) LIMIT 1',
    [tag],
  );
  return rows[0] || null;
}

export async function resolveClanForAccount(ctx) {
  const meta = ctx?.clan;
  if (!meta) return null;

  const id = meta.id;
  const tag = meta.tag || meta.identifier;

  if (id && id !== '00000000-0000-0000-0000-000000000000') {
    const byId = await findClanById(id);
    if (byId) {
      return {
        id: byId.id,
        name: byId.name,
        tag: byId.tag,
        tagColorId: meta.tagColorId || 'padrao',
      };
    }
  }

  if (tag && tag !== '...' && tag.length > 0) {
    const byTag = await findClanByTag(tag);
    if (byTag) {
      return {
        id: byTag.id,
        name: byTag.name,
        tag: byTag.tag,
        tagColorId: meta.tagColorId || 'padrao',
      };
    }
    return {
      id: id || null,
      name: tag,
      tag,
      tagColorId: meta.tagColorId || 'padrao',
    };
  }

  return null;
}

export async function getPlayerStats(uuid) {
  if (!uuid) return null;
  const { rows } = await query(
    `SELECT kills, deaths, golds, best_kill_streak
     FROM network_stats WHERE stat_key = ? LIMIT 1`,
    [`player:${uuid}`],
  );
  return rows[0] || null;
}
