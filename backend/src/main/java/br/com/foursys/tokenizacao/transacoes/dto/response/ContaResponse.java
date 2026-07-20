package br.com.foursys.tokenizacao.transacoes.dto.response;

import br.com.foursys.tokenizacao.transacoes.model.StatusConta;
import br.com.foursys.tokenizacao.transacoes.model.TipoConta;
import java.math.BigDecimal;

public record ContaResponse(
        String numeroConta,
        String nomeTitular,
        TipoConta tipoConta,
        BigDecimal saldoConta,
        StatusConta statusConta
) {
}
