package br.com.foursys.tokenizacao.transacoes.dto.response;

import br.com.foursys.tokenizacao.transacoes.model.TipoChavePix;

public record DestinatarioPixResponse(
        String nomeTitular,
        String cpfMascarado,
        TipoChavePix tipoChavePix
) {
}
