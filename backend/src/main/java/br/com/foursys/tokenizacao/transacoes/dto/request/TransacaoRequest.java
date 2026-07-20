package br.com.foursys.tokenizacao.transacoes.dto.request;

import br.com.foursys.tokenizacao.transacoes.model.TipoTransacao;
import java.math.BigDecimal;

public record TransacaoRequest(
        TipoTransacao tipoTransacao,
        String numeroContaDestino,
        BigDecimal valorTransacao,
        String descricaoTransacao
) {
}
