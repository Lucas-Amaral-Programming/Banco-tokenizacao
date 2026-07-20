package br.com.foursys.tokenizacao.transacoes.exception;

public class ValorTransacaoInvalidoException extends RegraNegocioException {

    public ValorTransacaoInvalidoException() {
        super("O valor da transacao deve ser maior que zero.");
    }
}
