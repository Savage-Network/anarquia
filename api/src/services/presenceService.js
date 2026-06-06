import { query } from '../db/pool.js';

const ONLINE_WINDOW_SECONDS = 90;

export async function countOnlinePlayers() {
  const { rows } = await query(
    `SELECT COUNT(*) AS online FROM network_presence
     WHERE last_seen_at >= DATE_SUB(NOW(), INTERVAL ? SECOND)`,
    [ONLINE_WINDOW_SECONDS],
  );
  return Number(rows[0]?.online ?? 0);
}

export async function isPlayerOnline(playerId) {
  if (!playerId) return false;
  const { rows } = await query(
    `SELECT 1 AS ok FROM network_presence
     WHERE player_id = ? AND last_seen_at >= DATE_SUB(NOW(), INTERVAL ? SECOND)
     LIMIT 1`,
    [playerId, ONLINE_WINDOW_SECONDS],
  );
  return rows.length > 0;
}

export async function resolveAccountPremium(playerId, ctx = {}) {
  const { rows } = await query(
    `SELECT password_hash FROM player_auth WHERE player_id = ? LIMIT 1`,
    [playerId],
  );

  const hash = rows[0]?.password_hash;
  const hasLocalAuth = typeof hash === 'string' && hash.length > 0;
  if (hasLocalAuth) {
    return { premium: false, type: 'offline' };
  }

  const premiumFlag = Boolean(ctx.premium);
  const authState = ctx.auth?.state;
  const premium =
    premiumFlag || authState === 'OK' || authState === 'LOGIN';

  return { premium, type: premium ? 'premium' : 'offline' };
}
