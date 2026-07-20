package br.com.foursys.tokenizacao.transacoes.exception;

import br.com.foursys.tokenizacao.transacoes.model.TipoTransacao;

public class ContaDestinoObrigatoriaException extends RegraNegocioException {

    public ContaDestinoObrigatoriaException(TipoTransacao tipoTransacao) {
        super("A conta de destino e obrigatoria para o tipo " + tipoTransacao + ".");
    }
}
