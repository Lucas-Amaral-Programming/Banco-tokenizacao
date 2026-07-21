package br.com.foursys.tokenizacao.transacoes.exception;

public class TelefoneInvalidoException extends RegraNegocioException {

    public TelefoneInvalidoException() {
        super("Telefone invalido.");
    }
}
