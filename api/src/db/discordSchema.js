import { pool } from './pool.js';

export async function ensureDiscordSchema() {
  await pool.execute(`
    CREATE TABLE IF NOT EXISTS discord_link_codes (
      code         VARCHAR(8)  PRIMARY KEY,
      player_id    VARCHAR(36) NOT NULL,
      player_name  VARCHAR(16) NOT NULL,
      created_at   BIGINT      NOT NULL,
      expires_at   BIGINT      NOT NULL,
      INDEX idx_discord_codes_player (player_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`);

  await pool.execute(`
    CREATE TABLE IF NOT EXISTS discord_links (
      player_id    VARCHAR(36) PRIMARY KEY,
      player_name  VARCHAR(16) NOT NULL,
      discord_id   VARCHAR(32) NOT NULL,
      discord_tag  VARCHAR(128) NULL,
      linked_at    BIGINT      NOT NULL,
      UNIQUE KEY uk_discord_links_discord (discord_id),
      INDEX idx_discord_links_discord (discord_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`);
}
