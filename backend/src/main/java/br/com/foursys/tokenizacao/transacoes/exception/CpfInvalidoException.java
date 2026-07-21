package br.com.foursys.tokenizacao.transacoes.exception;

public class CpfInvalidoException extends RegraNegocioException {

    public CpfInvalidoException() {
        super("CPF invalido.", "cpf");
    }
}
