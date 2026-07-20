package br.com.foursys.tokenizacao.transacoes.exception;

import br.com.foursys.tokenizacao.transacoes.model.TipoTransacao;

public class ContaOrigemObrigatoriaException extends RegraNegocioException {

    public ContaOrigemObrigatoriaException(TipoTransacao tipoTransacao) {
        super("A conta de origem e obrigatoria para o tipo " + tipoTransacao + ".");
    }
}
