USE foursys_banco_tokenizacao;

-- Adiciona telefone a bancos que ja possuem a tabela conta.
-- Passo 1: coluna temporariamente anulavel para permitir o backfill.
ALTER TABLE conta ADD COLUMN telefone VARCHAR(11) NULL AFTER cpf;

-- Passo 2: backfill deterministico e unico para as contas existentes
-- (prefixo 119 garante 3o digito 9; sufixo com id_conta garante unicidade).
UPDATE conta
   SET telefone = CONCAT('119', LPAD(id_conta, 8, '0'))
 WHERE telefone IS NULL;

-- Passo 3: aplica NOT NULL e unicidade.
ALTER TABLE conta MODIFY telefone VARCHAR(11) NOT NULL;
ALTER TABLE conta ADD CONSTRAINT uk_telefone UNIQUE (telefone);
