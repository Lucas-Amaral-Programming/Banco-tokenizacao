package br.com.foursys.tokenizacao.transacoes.exception;

public class CredenciaisInvalidasException extends RegraNegocioException {

    public CredenciaisInvalidasException() {
        super("CPF ou senha invalidos.");
    }
}
