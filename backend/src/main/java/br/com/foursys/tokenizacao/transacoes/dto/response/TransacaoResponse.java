package br.com.foursys.tokenizacao.transacoes.dto.response;

import br.com.foursys.tokenizacao.transacoes.model.StatusTransacao;
import br.com.foursys.tokenizacao.transacoes.model.TipoTransacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoResponse(
        String tokenTransacao,
        TipoTransacao tipoTransacao,
        String numeroContaOrigem,
        String numeroContaDestino,
        BigDecimal valorTransacao,
        String descricaoTransacao,
        StatusTransacao statusTransacao,
        LocalDateTime dataHoraTransacao
) {
}
