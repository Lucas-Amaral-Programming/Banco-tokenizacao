USE foursys_banco_tokenizacao;

-- senha_conta abaixo corresponde a "123456" (hash BCrypt), usada apenas para contas de teste
INSERT INTO conta
    (numero_conta, nome_titular, cpf, telefone, email, tipo_conta, saldo_conta, status_conta, senha_conta, data_abertura)
VALUES
    ('00011', 'Maria Souza',    '52998224725', '11999990011', 'maria.souza@teste.com',    'CORRENTE', 5000.00, 'ATIVA',     '$2b$10$euP/4m2kX4eFdx6OMptwbe1ivrqyuLGu30F0ZdFPmNpltNjri0LFa', NOW()),
    ('00022', 'Joao Lima',      '12345678909', '11999990022', 'joao.lima@teste.com',      'POUPANCA', 5000.00, 'ATIVA',     '$2b$10$euP/4m2kX4eFdx6OMptwbe1ivrqyuLGu30F0ZdFPmNpltNjri0LFa', NOW()),
    ('00033', 'Carlos Pereira', '11144477735', '11999990033', 'carlos.pereira@teste.com', 'CORRENTE', 5000.00, 'BLOQUEADA', '$2b$10$euP/4m2kX4eFdx6OMptwbe1ivrqyuLGu30F0ZdFPmNpltNjri0LFa', NOW());

INSERT INTO chave_pix (id_conta, tipo_chave, valor_normalizado, ativa, data_cadastro)
SELECT id_conta, 'CPF', cpf, TRUE, NOW() FROM conta;

INSERT INTO chave_pix (id_conta, tipo_chave, valor_normalizado, ativa, data_cadastro)
SELECT id_conta, 'EMAIL', LOWER(TRIM(email)), TRUE, NOW() FROM conta;

INSERT INTO chave_pix (id_conta, tipo_chave, valor_normalizado, ativa, data_cadastro)
SELECT id_conta, 'CELULAR', telefone, TRUE, NOW() FROM conta;


