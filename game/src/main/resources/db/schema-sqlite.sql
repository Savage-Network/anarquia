-- Savage Anarquia — schema SQLite do modulo game (dev local)
-- Banco padrao: shared/anarquia-dev.db (config.yml database.sqlite-file)

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS accounts (
    id          TEXT NOT NULL PRIMARY KEY,
    name        TEXT NOT NULL,
    data        TEXT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_accounts_name ON accounts(name);

CREATE TABLE IF NOT EXISTS account_identity (
    player_id           TEXT NOT NULL PRIMARY KEY,
    primary_rank        TEXT NOT NULL DEFAULT 'MEMBER',
    primary_given_at    INTEGER NOT NULL DEFAULT 0,
    primary_expires_at  INTEGER NOT NULL DEFAULT 0,
    available_ranks     TEXT NOT NULL,
    tag                 TEXT NOT NULL DEFAULT 'MEMBER',
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS clans (
    id          TEXT NOT NULL PRIMARY KEY,
    name        TEXT NOT NULL,
    tag         TEXT NOT NULL,
    data        TEXT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_clans_name ON clans(name);
CREATE INDEX IF NOT EXISTS idx_clans_tag ON clans(tag);

CREATE TABLE IF NOT EXISTS network_stats (
    stat_key          TEXT NOT NULL PRIMARY KEY,
    category          TEXT NOT NULL,
    display_name      TEXT NOT NULL,
    kills             INTEGER NOT NULL DEFAULT 0,
    deaths            INTEGER NOT NULL DEFAULT 0,
    golds             INTEGER NOT NULL DEFAULT 0,
    clan_power        INTEGER NOT NULL DEFAULT 0,
    clan_members      INTEGER NOT NULL DEFAULT 0,
    best_kill_streak  INTEGER NOT NULL DEFAULT 0,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_network_stats_category ON network_stats(category);

CREATE TABLE IF NOT EXISTS player_reports (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    reporter_uuid   TEXT NOT NULL,
    reporter_name   TEXT NOT NULL,
    target_uuid     TEXT NOT NULL,
    target_name     TEXT NOT NULL,
    reason          TEXT NOT NULL,
    details         TEXT,
    server_id       TEXT NOT NULL,
    status          TEXT NOT NULL DEFAULT 'OPEN',
    staff_uuid      TEXT,
    staff_note      TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reports_status_created ON player_reports(status, created_at);
CREATE INDEX IF NOT EXISTS idx_reports_target_created ON player_reports(target_uuid, created_at);

CREATE TABLE IF NOT EXISTS punishments (
    id          TEXT NOT NULL PRIMARY KEY,
    player      TEXT NOT NULL,
    author      TEXT NOT NULL,
    data        TEXT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_punishments_player ON punishments(player);

CREATE TABLE IF NOT EXISTS sign_shops (
    id            TEXT NOT NULL PRIMARY KEY,
    world         TEXT NOT NULL,
    x             INTEGER NOT NULL,
    y             INTEGER NOT NULL,
    z             INTEGER NOT NULL,
    owner_id      TEXT NOT NULL,
    type          TEXT NOT NULL,
    material      TEXT NOT NULL,
    price         INTEGER NOT NULL,
    server_shop   INTEGER NOT NULL DEFAULT 0,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sign_shops_loc ON sign_shops(world, x, y, z);
CREATE INDEX IF NOT EXISTS idx_sign_shops_owner ON sign_shops(owner_id);

CREATE TABLE IF NOT EXISTS auction_listings (
    id            TEXT NOT NULL PRIMARY KEY,
    seller_id     TEXT NOT NULL,
    seller_name   TEXT NOT NULL,
    material      TEXT NOT NULL,
    amount        INTEGER NOT NULL,
    price         INTEGER NOT NULL,
    created_at    INTEGER NOT NULL,
    expires_at    INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_auction_seller ON auction_listings(seller_id);
CREATE INDEX IF NOT EXISTS idx_auction_expires ON auction_listings(expires_at);
