CREATE TABLE associacao_discord (
    id_associacao UUID PRIMARY KEY,
    nome_usuario VARCHAR(255) UNIQUE,
    discord_id VARCHAR(255),
    token VARCHAR(255) UNIQUE,
    associado BOOLEAN
);
