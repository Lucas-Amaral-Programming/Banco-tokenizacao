CREATE DATABASE IF NOT EXISTS foursys_banco_tokenizacao
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE foursys_banco_tokenizacao;

CREATE TABLE IF NOT EXISTS conta (
    id_conta           BIGINT        NOT NULL AUTO_INCREMENT,
    numero_conta       VARCHAR(20)   NOT NULL,
    nome_titular       VARCHAR(120)  NOT NULL,
    cpf                VARCHAR(14)   NOT NULL,
    tipo_conta         VARCHAR(20)   NOT NULL,
    saldo_conta        DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status_conta       VARCHAR(20)   NOT NULL,
    senha_conta        VARCHAR(100)  NOT NULL,
    data_abertura      DATETIME      NOT NULL,
    PRIMARY KEY (id_conta),
    CONSTRAINT uk_numero_conta UNIQUE (numero_conta),
    CONSTRAINT uk_cpf UNIQUE (cpf)
);

CREATE TABLE IF NOT EXISTS transacao (
    id_transacao         BIGINT        NOT NULL AUTO_INCREMENT,
    token_transacao      VARCHAR(80)   NOT NULL,
    tipo_transacao       VARCHAR(20)   NOT NULL,
    id_conta_origem      BIGINT,
    id_conta_destino     BIGINT,
    valor_transacao      DECIMAL(15,2) NOT NULL,
    descricao_transacao  VARCHAR(255),
    status_transacao     VARCHAR(20)   NOT NULL,
    data_hora_transacao  DATETIME      NOT NULL,
    PRIMARY KEY (id_transacao),
    CONSTRAINT uk_token_transacao UNIQUE (token_transacao),
    CONSTRAINT fk_transacao_conta_origem
        FOREIGN KEY (id_conta_origem) REFERENCES conta (id_conta),
    CONSTRAINT fk_transacao_conta_destino
        FOREIGN KEY (id_conta_destino) REFERENCES conta (id_conta)
);

