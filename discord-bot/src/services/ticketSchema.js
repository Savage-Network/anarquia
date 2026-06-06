import { query } from '../db/pool.js';

export async function ensureTicketSchema() {
  await query(`
    CREATE TABLE IF NOT EXISTS discord_tickets (
      id            INT AUTO_INCREMENT PRIMARY KEY,
      channel_id    VARCHAR(32)  NOT NULL,
      guild_id      VARCHAR(32)  NOT NULL,
      owner_id      VARCHAR(32)  NOT NULL,
      category      VARCHAR(16)  NOT NULL,
      subject       VARCHAR(200) NOT NULL,
      description   TEXT         NULL,
      extra         TEXT         NULL,
      status        VARCHAR(16)  NOT NULL DEFAULT 'OPEN',
      claimed_by    VARCHAR(32)  NULL,
      claimed_at    BIGINT       NULL,
      created_at    BIGINT       NOT NULL,
      closed_at     BIGINT       NULL,
      closed_by     VARCHAR(32)  NULL,
      close_reason  TEXT         NULL,
      player_id     VARCHAR(36)  NULL,
      player_name   VARCHAR(16)  NULL,
      UNIQUE KEY uk_discord_tickets_channel (channel_id),
      INDEX idx_discord_tickets_owner (owner_id),
      INDEX idx_discord_tickets_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`);
}
