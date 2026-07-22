package br.com.foursys.tokenizacao.transacoes.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MascaraCpfTest {

    @Test
    void mascaraMostraTresPrimeirosEDoisUltimos() {
        assertThat(MascaraCpf.mascarar("11111111111")).isEqualTo("111.***.***-11");
    }

    @Test
    void mascaraAceitaCpfComPontuacao() {
        assertThat(MascaraCpf.mascarar("529.982.247-25")).isEqualTo("529.***.***-25");
    }

    @Test
    void mascaraRetornaVazioParaEntradaInvalida() {
        assertThat(MascaraCpf.mascarar(null)).isEmpty();
        assertThat(MascaraCpf.mascarar("123")).isEmpty();
    }
}
