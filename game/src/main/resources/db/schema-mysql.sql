-- Savage Anarquia — schema MySQL do modulo game
-- Execute no banco configurado em config.yml (database.mysql.database)
-- Idempotente: pode rodar varias vezes (CREATE TABLE IF NOT EXISTS)

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- Contas da rede (JSON com AccountContext: rank, tag, medal, golds, homes, stats, clan, etc.)
CREATE TABLE IF NOT EXISTS accounts (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    name        VARCHAR(16)  NOT NULL,
    data        JSON         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IF NOT EXISTS idx_accounts_name ON accounts(name);

-- Rank e tag autoritativos (nao sobrescritos por saves de perfil de auth/lobby)
CREATE TABLE IF NOT EXISTS account_identity (
    player_id           VARCHAR(36)  NOT NULL PRIMARY KEY,
    primary_rank        VARCHAR(32)  NOT NULL DEFAULT 'MEMBER',
    primary_given_at    BIGINT       NOT NULL DEFAULT 0,
    primary_expires_at  BIGINT       NOT NULL DEFAULT 0,
    available_ranks     TEXT         NOT NULL,
    tag                 VARCHAR(64)  NOT NULL DEFAULT 'MEMBER',
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Clas (JSON com membros, power, aliados, inimigos, etc.)
CREATE TABLE IF NOT EXISTS clans (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    name        VARCHAR(32)  NOT NULL,
    tag         VARCHAR(16)  NOT NULL,
    data        JSON         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IF NOT EXISTS idx_clans_name ON clans(name);
CREATE INDEX IF NOT EXISTS idx_clans_tag ON clans(tag);

-- Leaderboards agregados (jogadores e clas)
CREATE TABLE IF NOT EXISTS network_stats (
    stat_key          VARCHAR(80)  NOT NULL PRIMARY KEY,
    category          VARCHAR(16)  NOT NULL,
    display_name      VARCHAR(32)  NOT NULL,
    kills             INT          NOT NULL DEFAULT 0,
    deaths            INT          NOT NULL DEFAULT 0,
    golds             INT          NOT NULL DEFAULT 0,
    clan_power        INT          NOT NULL DEFAULT 0,
    clan_members      INT          NOT NULL DEFAULT 0,
    best_kill_streak  INT          NOT NULL DEFAULT 0,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IF NOT EXISTS idx_network_stats_category ON network_stats(category);

-- Presenca online (proxy atualiza; bot/API consultam)
CREATE TABLE IF NOT EXISTS network_presence (
    player_id     VARCHAR(36)  NOT NULL PRIMARY KEY,
    player_name   VARCHAR(16)  NOT NULL,
    server_id     VARCHAR(32)  NOT NULL DEFAULT 'proxy',
    connected_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IF NOT EXISTS idx_network_presence_seen ON network_presence(last_seen_at);

-- Reports de jogadores (/report)
CREATE TABLE IF NOT EXISTS player_reports (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    reporter_uuid   VARCHAR(36)  NOT NULL,
    reporter_name   VARCHAR(16)  NOT NULL,
    target_uuid     VARCHAR(36)  NOT NULL,
    target_name     VARCHAR(16)  NOT NULL,
    reason          VARCHAR(24)  NOT NULL,
    details         TEXT,
    server_id       VARCHAR(32)  NOT NULL,
    status          VARCHAR(16)  NOT NULL DEFAULT 'OPEN',
    staff_uuid      VARCHAR(36),
    staff_note      TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status_created (status, created_at),
    INDEX idx_target_created (target_uuid, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Punicoes ativas e historico (JSON Punishment)
CREATE TABLE IF NOT EXISTS punishments (
    id          VARCHAR(32)  NOT NULL PRIMARY KEY,
    player      VARCHAR(36)  NOT NULL,
    author      VARCHAR(36)  NOT NULL,
    data        TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IF NOT EXISTS idx_punishments_player ON punishments(player);

-- Lojas de placa (sign shops)
CREATE TABLE IF NOT EXISTS sign_shops (
    id            VARCHAR(80)  NOT NULL PRIMARY KEY,
    world         VARCHAR(64)  NOT NULL,
    x             INT          NOT NULL,
    y             INT          NOT NULL,
    z             INT          NOT NULL,
    owner_id      VARCHAR(36)  NOT NULL,
    type          VARCHAR(8)   NOT NULL,
    material      VARCHAR(64)  NOT NULL,
    price         INT          NOT NULL,
    server_shop   TINYINT(1)   NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IF NOT EXISTS idx_sign_shops_loc ON sign_shops(world, x, y, z);
CREATE INDEX IF NOT EXISTS idx_sign_shops_owner ON sign_shops(owner_id);

-- Leilao (/ah)
CREATE TABLE IF NOT EXISTS auction_listings (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    seller_id     VARCHAR(36)  NOT NULL,
    seller_name   VARCHAR(16)  NOT NULL,
    material      VARCHAR(64)  NOT NULL,
    amount        INT          NOT NULL,
    price         INT          NOT NULL,
    created_at    BIGINT       NOT NULL,
    expires_at    BIGINT       NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IF NOT EXISTS idx_auction_seller ON auction_listings(seller_id);
CREATE INDEX IF NOT EXISTS idx_auction_expires ON auction_listings(expires_at);
