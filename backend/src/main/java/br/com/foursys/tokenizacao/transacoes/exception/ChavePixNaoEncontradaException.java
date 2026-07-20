package br.com.foursys.tokenizacao.transacoes.exception;

public class ChavePixNaoEncontradaException extends RegraNegocioException {

    public ChavePixNaoEncontradaException(String chave) {
        super("Nenhuma conta encontrada para a chave PIX informada: " + chave);
    }
}
