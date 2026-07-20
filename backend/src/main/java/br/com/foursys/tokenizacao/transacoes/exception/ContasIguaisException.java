package br.com.foursys.tokenizacao.transacoes.exception;

public class ContasIguaisException extends RegraNegocioException {

    public ContasIguaisException() {
        super("A conta de origem e destino nao podem ser a mesma.");
    }
}
