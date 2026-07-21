package br.com.foursys.tokenizacao.transacoes.exception;

public class RegraNegocioException extends RuntimeException {

    private final String campo;

    public RegraNegocioException(String mensagem) {
        this(mensagem, null);
    }

    public RegraNegocioException(String mensagem, String campo) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
