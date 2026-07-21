package br.com.foursys.tokenizacao.transacoes.exception;

public class NomeIncompletoException extends RegraNegocioException {

    public NomeIncompletoException() {
        super("Informe o nome completo (nome e sobrenome).", "nomeTitular");
    }
}
