package br.com.foursys.tokenizacao.transacoes.dto.request;

import br.com.foursys.tokenizacao.transacoes.model.TipoChavePix;

public record ResolverChavePixRequest(
        TipoChavePix tipoChavePix,
        String chave
) {
}
