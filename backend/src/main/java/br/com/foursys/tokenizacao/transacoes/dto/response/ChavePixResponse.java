package br.com.foursys.tokenizacao.transacoes.dto.response;

import br.com.foursys.tokenizacao.transacoes.model.TipoChavePix;

public record ChavePixResponse(
        TipoChavePix tipoChavePix,
        String chave
) {
}
