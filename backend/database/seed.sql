USE foursys_banco_tokenizacao;

-- senha_conta abaixo corresponde a "123456" (hash BCrypt), usada apenas para contas de teste
INSERT INTO conta
    (numero_conta, nome_titular, cpf, email, tipo_conta, saldo_conta, status_conta, senha_conta, data_abertura)
VALUES
    ('00011', 'Maria Souza',    '11111111111', 'maria.souza@teste.com',    'CORRENTE', 5000.00, 'ATIVA',     '$2b$10$euP/4m2kX4eFdx6OMptwbe1ivrqyuLGu30F0ZdFPmNpltNjri0LFa', NOW()),
    ('00022', 'Joao Lima',      '22222222222', 'joao.lima@teste.com',      'POUPANCA', 5000.00, 'ATIVA',     '$2b$10$euP/4m2kX4eFdx6OMptwbe1ivrqyuLGu30F0ZdFPmNpltNjri0LFa', NOW()),
    ('00033', 'Carlos Pereira', '33333333333', 'carlos.pereira@teste.com', 'CORRENTE', 5000.00, 'BLOQUEADA', '$2b$10$euP/4m2kX4eFdx6OMptwbe1ivrqyuLGu30F0ZdFPmNpltNjri0LFa', NOW());

