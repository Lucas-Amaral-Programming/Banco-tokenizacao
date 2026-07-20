package br.com.foursys.tokenizacao.transacoes.exception;

public class ContaNaoEncontradaException extends RegraNegocioException {

    public ContaNaoEncontradaException(String numeroConta) {
        super("Conta nao encontrada: " + numeroConta);
    }
}
