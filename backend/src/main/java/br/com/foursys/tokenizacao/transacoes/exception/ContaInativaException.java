package br.com.foursys.tokenizacao.transacoes.exception;

public class ContaInativaException extends RegraNegocioException {

    public ContaInativaException(String papelDaConta) {
        super("A conta de " + papelDaConta + " nao esta ativa.");
    }
}
