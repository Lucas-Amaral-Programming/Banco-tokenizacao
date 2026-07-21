package br.com.foursys.tokenizacao.transacoes.exception;

public class SenhaObrigatoriaException extends RegraNegocioException {

    public SenhaObrigatoriaException() {
        super("Informe a senha.", "senha");
    }
}
