-- Tabela para guardar os jogadores
CREATE TABLE player (
    pk_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    type VARCHAR(10) NOT NULL DEFAULT 'HUMAN',
    image_url VARCHAR(255) NULL
);

-- Tabela para guardar as roles (funções) dos usuários
CREATE TABLE role (
    pk_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Tabela para fazer a ligação entre jogadores e roles
CREATE TABLE player_role (
    pk_id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_player FOREIGN KEY (player_id) REFERENCES player(pk_id),
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES role(pk_id)
);

-- Insere as roles básicas no banco de dados
INSERT INTO Role (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- Tabela de objetivos do jogo
CREATE TABLE objective (
    pk_id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL
);

-- Tabela para guardar os territórios
CREATE TABLE territory (
    pk_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    continent VARCHAR(50) NOT NULL
);

-- Tabela para guardar as fronteiras entre territórios
CREATE TABLE territory_border (
    pk_id BIGSERIAL PRIMARY KEY,
    territory_a_id BIGINT NOT NULL,
    territory_b_id BIGINT NOT NULL,
    CONSTRAINT fk_territory_a FOREIGN KEY (territory_a_id) REFERENCES territory(pk_id),
    CONSTRAINT fk_territory_b FOREIGN KEY (territory_b_id) REFERENCES territory(pk_id)
);

-- Tabela para guardar as cartas
CREATE TABLE card (
    pk_id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    territory_id BIGINT NULL,
    image_name VARCHAR(100) NULL,
    CONSTRAINT fk_territory FOREIGN KEY (territory_id) REFERENCES territory(pk_id)
);

-- Tabela para guardar os jogos
CREATE TABLE game (
    pk_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    turn_player_id BIGINT NULL,
    winner_id BIGINT NULL,
    card_set_exchange_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tabela de relacionamento entre jogadores e jogos
CREATE TABLE player_game (
    pk_id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    color VARCHAR(20) NULL,
    turn_order INTEGER NULL,
    is_owner BOOLEAN NOT NULL DEFAULT FALSE,
    objective_id BIGINT NULL,
    conquered_territory_this_turn BOOLEAN NOT NULL DEFAULT FALSE,
    still_in_game BOOLEAN NOT NULL DEFAULT TRUE,
    unallocated_armies INTEGER NOT NULL DEFAULT 0,
    username VARCHAR(50) NOT NULL,
    image_url VARCHAR(255) NULL,
    CONSTRAINT fk_player FOREIGN KEY (player_id) REFERENCES player(pk_id),
    CONSTRAINT fk_game FOREIGN KEY (game_id) REFERENCES game(pk_id),
    CONSTRAINT fk_objective FOREIGN KEY (objective_id) REFERENCES objective(pk_id)
);

-- Chaves Estrangeiras para a tabela Game
ALTER TABLE Game
ADD CONSTRAINT fk_turn_player FOREIGN KEY (turn_player_id) REFERENCES player_game(pk_id),
ADD CONSTRAINT fk_winner FOREIGN KEY (winner_id) REFERENCES player_game(pk_id);

-- Tabela de relacionamento para as cartas que os jogadores possuem em um jogo
CREATE TABLE player_card (
    pk_id BIGSERIAL PRIMARY KEY,
    player_game_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    CONSTRAINT fk_player_game FOREIGN KEY (player_game_id) REFERENCES player_game(pk_id),
    CONSTRAINT fk_card FOREIGN KEY (card_id) REFERENCES card(pk_id)
);

-- Tabela de relacionamento para os territórios de um jogo
CREATE TABLE game_territory (
    pk_id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL,
    territory_id BIGINT NOT NULL,
    player_game_id BIGINT NULL,
    static_armies INTEGER NOT NULL DEFAULT 0,
    moved_in_armies INTEGER NOT NULL DEFAULT 0,
    unallocated_armies INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_game FOREIGN KEY (game_id) REFERENCES game(pk_id),
    CONSTRAINT fk_territory FOREIGN KEY (territory_id) REFERENCES territory(pk_id),
    CONSTRAINT fk_player_game FOREIGN KEY (player_game_id) REFERENCES player_game(pk_id)
);

-- Tabela para armazenar histórico de movimentação de tropas
CREATE TABLE troop_movement (
    pk_id BIGSERIAL PRIMARY KEY,
    source_territory_id BIGINT NOT NULL,
    target_territory_id BIGINT NOT NULL,
    number_of_troops INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    game_id BIGINT NOT NULL,
    player_game_id BIGINT NOT NULL,
    CONSTRAINT fk_source_territory FOREIGN KEY (source_territory_id) REFERENCES game_territory(pk_id),
    CONSTRAINT fk_target_territory FOREIGN KEY (target_territory_id) REFERENCES game_territory(pk_id),
    CONSTRAINT fk_game_troop FOREIGN KEY (game_id) REFERENCES game(pk_id),
    CONSTRAINT fk_player_game_troop FOREIGN KEY (player_game_id) REFERENCES player_game(pk_id)
);

--INICIALIZANDO OS BOTS

INSERT INTO player (username, email, password_hash, type) VALUES 
('GabrielBOT', 'gabriel.bot@war.com', '$2a$10$C8.q9A1X9pQ4z6n.v3A6W.F7d4vBf3eP2vV8e/jM.n2', 'AI_EASY'),
('LucasBOT', 'lucas.bot@war.com', '$2a$10$C8.q9A1X9pQ4z6n.v3A6W.F7d4vBf3eP2vV8e/jM.n2', 'AI_EASY'),
('PedroBOT', 'pedro.bot@war.com', '$2a$10$C8.q9A1X9pQ4z6n.v3A6W.F7d4vBf3eP2vV8e/jM.n2', 'AI_EASY');

INSERT INTO player (username, email, password_hash, type) VALUES 
('SofiaBOT', 'sofia.bot@war.com', '$2a$10$C8.q9A1X9pQ4z6n.v3A6W.F7d4vBf3eP2vV8e/jM.n2', 'AI_EASY'),
('JuliaBOT', 'julia.bot@war.com', '$2a$10$C8.q9A1X9pQ4z6n.v3A6W.F7d4vBf3eP2vV8e/jM.n2', 'AI_EASY'),
('LauraBOT', 'laura.bot@war.com', '$2a$10$C8.q9A1X9pQ4z6n.v3A6W.F7d4vBf3eP2vV8e/jM.n2', 'AI_EASY');

-- Insere todas as IAs na role 'ROLE_USER' (Assumindo que IAs devem ter a mesma role base)
INSERT INTO player_role (player_id, role_id)
SELECT pk_id, (SELECT pk_id FROM role WHERE name = 'ROLE_USER')
FROM player
WHERE username LIKE '%BOT';