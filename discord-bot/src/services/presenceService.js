import { query } from '../db/pool.js';

const ONLINE_WINDOW_SECONDS = 90;

/** Jogadores com heartbeat recente na tabela network_presence. */
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

/**
 * Original = conta Mojang (sem senha local em player_auth).
 * Pirata = registro com senha na tabela player_auth.
 */
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

export async function fetchServerStatusFromDb() {
  try {
    const onlinePlayers = await countOnlinePlayers();
    return { onlinePlayers, source: 'mysql' };
  } catch {
    return { onlinePlayers: null, source: 'mysql' };
  }
}

/** Garante tabela de presenca (proxy tambem cria no boot). */
export async function ensurePresenceSchema() {
  await query(`
    CREATE TABLE IF NOT EXISTS network_presence (
      player_id     VARCHAR(36)  NOT NULL PRIMARY KEY,
      player_name   VARCHAR(16)  NOT NULL,
      server_id     VARCHAR(32)  NOT NULL DEFAULT 'proxy',
      connected_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
      last_seen_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  `);
  try {
    await query(
      'CREATE INDEX IF NOT EXISTS idx_network_presence_seen ON network_presence(last_seen_at)',
    );
  } catch {
    /* indice opcional */
  }
}
