package br.com.foursys.tokenizacao.transacoes.exception;

public class TelefoneJaCadastradoException extends RegraNegocioException {

    public TelefoneJaCadastradoException() {
        super("Ja existe uma conta para o telefone informado.");
    }
}
