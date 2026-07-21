package br.com.foursys.tokenizacao.transacoes.exception;

public class ChavePixInvalidaException extends RegraNegocioException {

    public ChavePixInvalidaException() {
        super("Chave PIX invalida para o tipo informado.");
    }
}
