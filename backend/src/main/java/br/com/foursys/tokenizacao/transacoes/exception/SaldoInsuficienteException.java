package br.com.foursys.tokenizacao.transacoes.exception;

public class SaldoInsuficienteException extends RegraNegocioException {

    public SaldoInsuficienteException() {
        super("Saldo insuficiente na conta de origem.");
    }
}
