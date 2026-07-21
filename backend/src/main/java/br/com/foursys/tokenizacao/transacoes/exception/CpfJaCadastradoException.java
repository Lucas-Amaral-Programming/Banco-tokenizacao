package br.com.foursys.tokenizacao.transacoes.exception;

public class CpfJaCadastradoException extends RegraNegocioException {

    public CpfJaCadastradoException() {
        super("Ja existe uma conta para o CPF informado.", "cpf");
    }
}
