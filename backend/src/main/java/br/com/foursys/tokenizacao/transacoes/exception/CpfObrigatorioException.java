package br.com.foursys.tokenizacao.transacoes.exception;

public class CpfObrigatorioException extends RegraNegocioException {

    public CpfObrigatorioException() {
        super("Informe o CPF.", "cpf");
    }
}
