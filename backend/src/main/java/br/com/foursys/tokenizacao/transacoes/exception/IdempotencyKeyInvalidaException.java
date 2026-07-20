package br.com.foursys.tokenizacao.transacoes.exception;

public class IdempotencyKeyInvalidaException extends RegraNegocioException {

    public IdempotencyKeyInvalidaException(String mensagem) {
        super(mensagem);
    }
}
