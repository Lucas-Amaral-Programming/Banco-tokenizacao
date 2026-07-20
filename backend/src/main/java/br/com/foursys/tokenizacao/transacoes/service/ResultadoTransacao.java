package br.com.foursys.tokenizacao.transacoes.service;

import br.com.foursys.tokenizacao.transacoes.dto.response.TransacaoResponse;

public record ResultadoTransacao(TransacaoResponse response, boolean replay) {
}
