package br.com.foursys.tokenizacao.transacoes.dto.response;

import br.com.foursys.tokenizacao.transacoes.model.Conta;
import br.com.foursys.tokenizacao.transacoes.model.StatusTransacao;
import br.com.foursys.tokenizacao.transacoes.model.TipoTransacao;
import br.com.foursys.tokenizacao.transacoes.model.Transacao;
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

    public static TransacaoResponse de(Transacao transacao) {
        return new TransacaoResponse(
                transacao.getTokenTransacao(),
                transacao.getTipoTransacao(),
                numeroDaConta(transacao.getContaOrigem()),
                numeroDaConta(transacao.getContaDestino()),
                transacao.getValorTransacao(),
                transacao.getDescricaoTransacao(),
                transacao.getStatusTransacao(),
                transacao.getDataHoraTransacao());
    }

    private static String numeroDaConta(Conta conta) {
        return conta != null ? conta.getNumeroConta() : null;
    }
}
