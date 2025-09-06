-- Tabela para guardar os jogadores
CREATE TABLE player (
    pk_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
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

-- Tabela para guardar as cartas
CREATE TABLE card (
    pk_id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    territory_id BIGINT NULL,
    CONSTRAINT fk_territory FOREIGN KEY (territory_id) REFERENCES territory(pk_id)
);

-- Tabela para guardar os jogos
CREATE TABLE game (
    pk_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    turn_player_id BIGINT NULL,
    winner_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tabela de relacionamento entre jogadores e jogos
CREATE TABLE player_game (
    pk_id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    color VARCHAR(20) NULL,
    is_ready BOOLEAN NOT NULL DEFAULT FALSE,
    turn_order INTEGER NULL,
    is_owner BOOLEAN NOT NULL DEFAULT FALSE,
    objective_id BIGINT NULL,
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
    player_game_id BIGINT NOT NULL,  -- ⬅️ Nova chave estrangeira
    card_id BIGINT NOT NULL,
    CONSTRAINT fk_player_game FOREIGN KEY (player_game_id) REFERENCES player_game(pk_id),
    CONSTRAINT fk_card FOREIGN KEY (card_id) REFERENCES card(pk_id)
);

-- Tabela de relacionamento para os territórios de um jogo
CREATE TABLE game_territory (
    pk_id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL,
    territory_id BIGINT NOT NULL,
    player_game_id BIGINT NULL,  -- ⬅️ Nova chave estrangeira
    armies INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_game FOREIGN KEY (game_id) REFERENCES game(pk_id),
    CONSTRAINT fk_territory FOREIGN KEY (territory_id) REFERENCES territory(pk_id),
    CONSTRAINT fk_player_game FOREIGN KEY (player_game_id) REFERENCES player_game(pk_id)
);