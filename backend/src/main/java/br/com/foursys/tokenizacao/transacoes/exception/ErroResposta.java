package br.com.foursys.tokenizacao.transacoes.exception;

import java.time.LocalDateTime;

public record ErroResposta(
        String mensagem,
        int codigoHttp,
        LocalDateTime dataHora
) {
}
