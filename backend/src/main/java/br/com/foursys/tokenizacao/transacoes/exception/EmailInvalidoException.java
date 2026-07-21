package br.com.foursys.tokenizacao.transacoes.exception;

public class EmailInvalidoException extends RegraNegocioException {

    public EmailInvalidoException() {
        super("E-mail invalido.");
    }
}
