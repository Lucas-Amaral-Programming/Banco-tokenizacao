package br.com.foursys.tokenizacao.transacoes.exception;

public class TelefoneObrigatorioException extends RegraNegocioException {

    public TelefoneObrigatorioException() {
        super("Informe o celular.", "telefone");
    }
}
