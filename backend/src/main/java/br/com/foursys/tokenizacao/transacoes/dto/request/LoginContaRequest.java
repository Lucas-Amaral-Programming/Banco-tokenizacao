package br.com.foursys.tokenizacao.transacoes.dto.request;

public record LoginContaRequest(
        String cpf,
        String senha
) {
}
