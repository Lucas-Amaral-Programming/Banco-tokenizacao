package br.com.foursys.tokenizacao.transacoes.exception;

public class TipoChavePixObrigatorioException extends RegraNegocioException {

    public TipoChavePixObrigatorioException() {
        super("Tipo da chave PIX e obrigatorio.");
    }
}
