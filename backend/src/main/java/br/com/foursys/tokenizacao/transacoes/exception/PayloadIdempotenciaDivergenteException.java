package br.com.foursys.tokenizacao.transacoes.exception;

public class PayloadIdempotenciaDivergenteException extends RuntimeException {

    public PayloadIdempotenciaDivergenteException() {
        super("A Idempotency-Key ja foi usada com um payload diferente.");
    }
}
