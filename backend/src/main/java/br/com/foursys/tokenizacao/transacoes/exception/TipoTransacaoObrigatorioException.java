package br.com.foursys.tokenizacao.transacoes.exception;

public class TipoTransacaoObrigatorioException extends RegraNegocioException {

    public TipoTransacaoObrigatorioException() {
        super("O tipo da transacao e obrigatorio.");
    }
}
