import { query } from '../db/pool.js';

const CODE_TTL_MS = 600_000;

function normalizeCode(code) {
  return String(code || '')
    .trim()
    .toUpperCase()
    .replace(/[^A-Z0-9]/g, '');
}

export async function findLinkByDiscord(discordId) {
  const { rows } = await query(
    `SELECT player_id, player_name, discord_id, discord_tag, linked_at
     FROM discord_links WHERE discord_id = ? LIMIT 1`,
    [String(discordId)],
  );
  return rows[0] || null;
}

export async function findLinkByPlayer(identifier) {
  const isUuid =
    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(
      identifier,
    );

  const sql = isUuid
    ? `SELECT player_id, player_name, discord_id, discord_tag, linked_at
       FROM discord_links WHERE player_id = ? LIMIT 1`
    : `SELECT player_id, player_name, discord_id, discord_tag, linked_at
       FROM discord_links WHERE LOWER(player_name) = LOWER(?) LIMIT 1`;

  const { rows } = await query(sql, [identifier]);
  return rows[0] || null;
}

export async function confirmLink(code, discordId, discordTag) {
  const normalized = normalizeCode(code);
  if (normalized.length < 4) {
    return { ok: false, error: 'INVALID_CODE', message: 'Codigo invalido.' };
  }

  const { rows: codeRows } = await query(
    `SELECT code, player_id, player_name, expires_at
     FROM discord_link_codes WHERE code = ? LIMIT 1`,
    [normalized],
  );

  if (!codeRows.length) {
    return { ok: false, error: 'CODE_NOT_FOUND', message: 'Codigo nao encontrado ou expirado.' };
  }

  const row = codeRows[0];
  if (row.expires_at <= Date.now()) {
    await query('DELETE FROM discord_link_codes WHERE code = ?', [normalized]);
    return { ok: false, error: 'CODE_EXPIRED', message: 'Codigo expirado. Gere outro no jogo com /vincular.' };
  }

  const existingDiscord = await findLinkByDiscord(discordId);
  if (existingDiscord) {
    return {
      ok: false,
      error: 'DISCORD_ALREADY_LINKED',
      message: `Este Discord ja esta vinculado a ${existingDiscord.player_name}.`,
      link: existingDiscord,
    };
  }

  const existingPlayer = await findLinkByPlayer(row.player_id);
  if (existingPlayer) {
    return {
      ok: false,
      error: 'PLAYER_ALREADY_LINKED',
      message: `A conta ${row.player_name} ja esta vinculada.`,
      link: existingPlayer,
    };
  }

  const now = Date.now();
  await query(
    `INSERT INTO discord_links (player_id, player_name, discord_id, discord_tag, linked_at)
     VALUES (?, ?, ?, ?, ?)`,
    [row.player_id, row.player_name, String(discordId), discordTag || null, now],
  );
  await query('DELETE FROM discord_link_codes WHERE code = ?', [normalized]);
  await query('DELETE FROM discord_link_codes WHERE player_id = ?', [row.player_id]);

  return {
    ok: true,
    link: {
      player_id: row.player_id,
      player_name: row.player_name,
      discord_id: String(discordId),
      discord_tag: discordTag || null,
      linked_at: now,
    },
  };
}

export async function unlinkByDiscord(discordId) {
  const existing = await findLinkByDiscord(discordId);
  if (!existing) {
    return { ok: false, error: 'NOT_LINKED', message: 'Nenhuma conta vinculada.' };
  }

  await query('DELETE FROM discord_links WHERE discord_id = ?', [String(discordId)]);
  return { ok: true, link: existing };
}

export async function createCodeForPlayer(playerId, playerName) {
  const chars = 'ABCDEFGHJKMNPQRSTUVWXYZ23456789';
  const now = Date.now();
  const expiresAt = now + CODE_TTL_MS;

  await query('DELETE FROM discord_link_codes WHERE player_id = ?', [playerId]);

  for (let attempt = 0; attempt < 12; attempt++) {
    let code = '';
    for (let i = 0; i < 6; i++) {
      code += chars[Math.floor(Math.random() * chars.length)];
    }

    try {
      await query(
        `INSERT INTO discord_link_codes (code, player_id, player_name, created_at, expires_at)
         VALUES (?, ?, ?, ?, ?)`,
        [code, playerId, playerName, now, expiresAt],
      );
      return { ok: true, code, expiresAt };
    } catch (e) {
      if (e.code !== 'ER_DUP_ENTRY') throw e;
    }
  }

  return { ok: false, error: 'CODE_GENERATION_FAILED' };
}

export function formatLink(row) {
  if (!row) return null;
  return {
    playerId: row.player_id,
    playerName: row.player_name,
    discordId: row.discord_id,
    discordTag: row.discord_tag,
    linkedAt: row.linked_at,
  };
}
