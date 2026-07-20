package br.com.foursys.tokenizacao.transacoes.exception;

public class EmailJaCadastradoException extends RegraNegocioException {

    public EmailJaCadastradoException() {
        super("Ja existe uma conta para o e-mail informado.");
    }
}
