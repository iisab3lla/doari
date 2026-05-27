CREATE DATABASE IF NOT EXISTS usuarios_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS doacoes_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS notificacoes_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'doari'@'%' IDENTIFIED BY 'doari';
GRANT ALL PRIVILEGES ON usuarios_db.* TO 'doari'@'%';
GRANT ALL PRIVILEGES ON doacoes_db.* TO 'doari'@'%';
GRANT ALL PRIVILEGES ON notificacoes_db.* TO 'doari'@'%';
FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS usuarios_db.usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    telefone VARCHAR(255),
    documento VARCHAR(255),
    tipo ENUM('DOADOR', 'ONG') NOT NULL,
    criado_em DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS doacoes_db.doacao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    ong_id BIGINT,
    pedido_id BIGINT,
    item VARCHAR(255) NOT NULL,
    quantidade VARCHAR(255),
    descricao VARCHAR(255),
    status ENUM('DISPONIVEL', 'SOLICITADA', 'CONFIRMADA', 'ENTREGUE', 'RECUSADA', 'REMOVIDO') NOT NULL,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_doacao_usuario_status (usuario_id, status),
    KEY idx_doacao_item_status (item, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS doacoes_db.pedido (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ong_id BIGINT NOT NULL,
    item VARCHAR(255) NOT NULL,
    quantidade VARCHAR(255),
    descricao VARCHAR(255),
    status ENUM('DISPONIVEL', 'SOLICITADA', 'CONFIRMADA', 'ENTREGUE', 'RECUSADA', 'REMOVIDO') NOT NULL,
    criado_em DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_pedido_ong_status (ong_id, status),
    KEY idx_pedido_item_status (item, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notificacoes_db.notificacao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tipo VARCHAR(255) NOT NULL,
    destinatario_tipo ENUM('DOADOR', 'ONG') NOT NULL,
    destinatario_id BIGINT NOT NULL,
    doacao_id BIGINT,
    pedido_id BIGINT,
    mensagem VARCHAR(1000) NOT NULL,
    lida BOOLEAN NOT NULL,
    criada_em DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_notificacao_destinatario (destinatario_tipo, destinatario_id, criada_em)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
