-- Greenfield, TFS-shaped schema. See specs/05-schema.md.
-- Must apply identically on MySQL 8 and on H2 running with MODE=MySQL.

CREATE TABLE accounts (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(32)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_accounts_name UNIQUE (name)
);

CREATE TABLE towns (
    id    INT         NOT NULL PRIMARY KEY,
    name  VARCHAR(64) NOT NULL,
    pos_x INT         NOT NULL,
    pos_y INT         NOT NULL,
    pos_z INT         NOT NULL,
    CONSTRAINT uq_towns_name UNIQUE (name)
);

CREATE TABLE players (
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id  BIGINT      NOT NULL,
    name        VARCHAR(29) NOT NULL,
    -- lowercased normalised name; the application fills this on insert and the
    -- unique constraint is what actually enforces case-insensitive uniqueness.
    name_key    VARCHAR(29) NOT NULL,
    vocation    INT         NOT NULL,
    sex         INT         NOT NULL,
    town_id     INT         NOT NULL,
    level       INT         NOT NULL,
    experience  BIGINT      NOT NULL,
    health      INT         NOT NULL,
    health_max  INT         NOT NULL,
    mana        INT         NOT NULL,
    mana_max    INT         NOT NULL,
    capacity    INT         NOT NULL,
    look_type   INT         NOT NULL,
    look_head   INT         NOT NULL,
    look_body   INT         NOT NULL,
    look_legs   INT         NOT NULL,
    look_feet   INT         NOT NULL,
    pos_x       INT         NOT NULL,
    pos_y       INT         NOT NULL,
    pos_z       INT         NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_players_name_key UNIQUE (name_key),
    CONSTRAINT fk_players_account  FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT fk_players_town     FOREIGN KEY (town_id)    REFERENCES towns (id)
);

CREATE INDEX idx_players_account ON players (account_id);

INSERT INTO towns (id, name, pos_x, pos_y, pos_z) VALUES
    (1, 'Thais',        32369, 32241,  7),
    (2, 'Carlin',       32360, 31782,  7),
    (3, 'Venore',       32957, 32076,  7),
    (4, 'Ab''Dendriel', 32732, 31634,  7),
    (5, 'Kazordoon',    32649, 31925, 11),
    (6, 'Rookgaard',    32097, 32219,  7);
