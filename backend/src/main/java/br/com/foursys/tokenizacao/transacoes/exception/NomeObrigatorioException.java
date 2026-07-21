package br.com.foursys.tokenizacao.transacoes.exception;

public class NomeObrigatorioException extends RegraNegocioException {

    public NomeObrigatorioException() {
        super("Informe o nome do titular.", "nomeTitular");
    }
}
