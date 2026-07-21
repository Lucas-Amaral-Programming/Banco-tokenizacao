CREATE DATABASE IF NOT EXISTS foursys_banco_tokenizacao
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE foursys_banco_tokenizacao;

CREATE TABLE IF NOT EXISTS conta (
    id_conta           BIGINT        NOT NULL AUTO_INCREMENT,
    numero_conta       VARCHAR(20)   NOT NULL,
    nome_titular       VARCHAR(120)  NOT NULL,
    cpf                VARCHAR(14)   NOT NULL,
    telefone           VARCHAR(11)   NOT NULL,
    email              VARCHAR(120),
    tipo_conta         VARCHAR(20)   NOT NULL,
    saldo_conta        DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status_conta       VARCHAR(20)   NOT NULL,
    senha_conta        VARCHAR(100)  NOT NULL,
    data_abertura      DATETIME      NOT NULL,
    versao_conta       BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id_conta),
    CONSTRAINT uk_numero_conta UNIQUE (numero_conta),
    CONSTRAINT uk_cpf UNIQUE (cpf),
    CONSTRAINT uk_telefone UNIQUE (telefone),
    CONSTRAINT uk_email UNIQUE (email),
    CONSTRAINT ck_saldo_nao_negativo CHECK (saldo_conta >= 0)
);

CREATE TABLE IF NOT EXISTS transacao (
    id_transacao          BIGINT        NOT NULL AUTO_INCREMENT,
    token_transacao       VARCHAR(80)   NOT NULL,
    tipo_transacao        VARCHAR(20)   NOT NULL,
    id_conta_origem       BIGINT,
    id_conta_destino      BIGINT,
    id_conta_solicitante  BIGINT        NOT NULL,
    idempotency_key       VARCHAR(36)   NOT NULL,
    fingerprint_requisicao VARCHAR(64)  NOT NULL,
    valor_transacao       DECIMAL(15,2) NOT NULL,
    descricao_transacao   VARCHAR(255),
    status_transacao      VARCHAR(20)   NOT NULL,
    data_hora_transacao   DATETIME      NOT NULL,
    PRIMARY KEY (id_transacao),
    CONSTRAINT uk_token_transacao UNIQUE (token_transacao),
    CONSTRAINT uk_idempotencia UNIQUE (id_conta_solicitante, idempotency_key),
    CONSTRAINT ck_valor_positivo CHECK (valor_transacao > 0),
    CONSTRAINT fk_transacao_conta_origem
        FOREIGN KEY (id_conta_origem) REFERENCES conta (id_conta),
    CONSTRAINT fk_transacao_conta_destino
        FOREIGN KEY (id_conta_destino) REFERENCES conta (id_conta),
    CONSTRAINT fk_transacao_conta_solicitante
        FOREIGN KEY (id_conta_solicitante) REFERENCES conta (id_conta)
);
