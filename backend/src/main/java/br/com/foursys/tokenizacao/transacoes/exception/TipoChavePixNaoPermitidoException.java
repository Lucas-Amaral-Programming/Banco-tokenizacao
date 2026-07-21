package br.com.foursys.tokenizacao.transacoes.exception;

public class TipoChavePixNaoPermitidoException extends RegraNegocioException {

    public TipoChavePixNaoPermitidoException() {
        super("Tipo da chave PIX so e permitido em transacoes PIX.");
    }
}
