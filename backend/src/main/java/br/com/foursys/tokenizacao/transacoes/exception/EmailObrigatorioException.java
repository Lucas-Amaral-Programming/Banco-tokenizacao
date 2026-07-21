package br.com.foursys.tokenizacao.transacoes.exception;

public class EmailObrigatorioException extends RegraNegocioException {

    public EmailObrigatorioException() {
        super("Informe o e-mail.", "email");
    }
}
