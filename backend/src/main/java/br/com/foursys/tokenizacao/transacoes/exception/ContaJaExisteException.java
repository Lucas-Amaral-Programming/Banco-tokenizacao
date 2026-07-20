package br.com.foursys.tokenizacao.transacoes.exception;

public class ContaJaExisteException extends RegraNegocioException {

    public ContaJaExisteException(String numeroConta) {
        super("Ja existe uma conta com o numero: " + numeroConta);
    }
}
