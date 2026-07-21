package br.com.foursys.tokenizacao.transacoes.concorrencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.foursys.tokenizacao.transacoes.dto.request.TransacaoRequest;
import br.com.foursys.tokenizacao.transacoes.exception.PayloadIdempotenciaDivergenteException;
import br.com.foursys.tokenizacao.transacoes.exception.SaldoInsuficienteException;
import br.com.foursys.tokenizacao.transacoes.model.Conta;
import br.com.foursys.tokenizacao.transacoes.model.StatusConta;
import br.com.foursys.tokenizacao.transacoes.model.TipoConta;
import br.com.foursys.tokenizacao.transacoes.model.TipoChavePix;
import br.com.foursys.tokenizacao.transacoes.model.TipoTransacao;
import br.com.foursys.tokenizacao.transacoes.repository.ContaRepository;
import br.com.foursys.tokenizacao.transacoes.repository.TransacaoRepository;
import br.com.foursys.tokenizacao.transacoes.service.ResultadoTransacao;
import br.com.foursys.tokenizacao.transacoes.service.TransacaoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class ConcorrenciaIdempotenciaIntegrationTest extends AbstractMySqlTestcontainers {

    private static final String CPF_A = "11111111111";
    private static final String CPF_B = "22222222222";

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void preparar() {
        transacaoRepository.deleteAll();
        contaRepository.deleteAll();
    }

    @Test
    void saquesConcorrentesNuncaDeixamSaldoNegativo() throws InterruptedException {
        criarConta("00011", CPF_A, "a@teste.com", new BigDecimal("100.00"), StatusConta.ATIVA);

        AtomicInteger aprovados = new AtomicInteger();
        AtomicInteger recusados = new AtomicInteger();

        List<Throwable> erros = executarConcorrente(20, indice -> {
            try {
                transacaoService.processar(
                        new TransacaoRequest(TipoTransacao.SAQUE, null, null, new BigDecimal("10.00"), "saque"),
                        "00011",
                        UUID.randomUUID().toString());
                aprovados.incrementAndGet();
            } catch (SaldoInsuficienteException e) {
                recusados.incrementAndGet();
            }
        });

        assertThat(erros).isEmpty();
        assertThat(aprovados.get()).isEqualTo(10);
        assertThat(recusados.get()).isEqualTo(10);
        assertThat(saldo("00011")).isEqualByComparingTo("0.00");
    }

    @Test
    void pixSimultaneosPreservamSomaDosSaldos() throws InterruptedException {
        criarConta("00011", CPF_A, "a@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);
        criarConta("00022", CPF_B, "b@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);

        List<Throwable> erros = executarConcorrente(20, indice ->
                transacaoService.processar(
                        new TransacaoRequest(TipoTransacao.PIX, CPF_B, TipoChavePix.CPF, new BigDecimal("10.00"), "pix"),
                        "00011",
                        UUID.randomUUID().toString()));

        assertThat(erros).isEmpty();
        assertThat(saldo("00011").add(saldo("00022"))).isEqualByComparingTo("2000.00");
        assertThat(saldo("00011")).isEqualByComparingTo("800.00");
        assertThat(saldo("00022")).isEqualByComparingTo("1200.00");
    }

    @Test
    void pixOpostosNaoGeramInconsistencia() throws InterruptedException {
        criarConta("00011", CPF_A, "a@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);
        criarConta("00022", CPF_B, "b@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);

        List<Throwable> erros = executarConcorrente(20, indice -> {
            boolean deAparaB = indice % 2 == 0;
            String origem = deAparaB ? "00011" : "00022";
            String chaveDestino = deAparaB ? CPF_B : CPF_A;
            transacaoService.processar(
                    new TransacaoRequest(TipoTransacao.PIX, chaveDestino, TipoChavePix.CPF, new BigDecimal("10.00"), "pix"),
                    origem,
                    UUID.randomUUID().toString());
        });

        assertThat(erros).isEmpty();
        assertThat(saldo("00011").add(saldo("00022"))).isEqualByComparingTo("2000.00");
        assertThat(saldo("00011")).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(saldo("00022")).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    void mesmaChaveConcorrenteCriaUmaUnicaTransacaoEUmUnicoDebito() throws InterruptedException {
        criarConta("00011", CPF_A, "a@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);
        criarConta("00022", CPF_B, "b@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);

        String chave = UUID.randomUUID().toString();
        List<String> tokens = Collections.synchronizedList(new ArrayList<>());

        List<Throwable> erros = executarConcorrente(10, indice -> {
            ResultadoTransacao resultado = transacaoService.processar(
                    new TransacaoRequest(TipoTransacao.PIX, CPF_B, TipoChavePix.CPF, new BigDecimal("100.00"), "pix"),
                    "00011",
                    chave);
            tokens.add(resultado.response().tokenTransacao());
        });

        assertThat(erros).isEmpty();
        assertThat(tokens).hasSize(10);
        assertThat(tokens.stream().distinct().toList()).hasSize(1);
        assertThat(transacaoRepository.findAll()).hasSize(1);
        assertThat(saldo("00011")).isEqualByComparingTo("900.00");
        assertThat(saldo("00022")).isEqualByComparingTo("1100.00");
    }

    @Test
    void retrySequencialComMesmaChaveRetornaOMesmoToken() {
        criarConta("00011", CPF_A, "a@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);
        criarConta("00022", CPF_B, "b@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);

        String chave = UUID.randomUUID().toString();
        TransacaoRequest request =
                new TransacaoRequest(TipoTransacao.PIX, CPF_B, TipoChavePix.CPF, new BigDecimal("100.00"), "pix");

        ResultadoTransacao primeira = transacaoService.processar(request, "00011", chave);
        ResultadoTransacao segunda = transacaoService.processar(request, "00011", chave);

        assertThat(primeira.replay()).isFalse();
        assertThat(segunda.replay()).isTrue();
        assertThat(segunda.response().tokenTransacao()).isEqualTo(primeira.response().tokenTransacao());
        assertThat(saldo("00011")).isEqualByComparingTo("900.00");
        assertThat(transacaoRepository.findAll()).hasSize(1);
    }

    @Test
    void mesmaChaveComPayloadDiferenteRetorna422() {
        criarConta("00011", CPF_A, "a@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);
        criarConta("00022", CPF_B, "b@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);

        String chave = UUID.randomUUID().toString();
        transacaoService.processar(
                new TransacaoRequest(TipoTransacao.PIX, CPF_B, TipoChavePix.CPF, new BigDecimal("100.00"), "pix"),
                "00011",
                chave);

        assertThatThrownBy(() -> transacaoService.processar(
                new TransacaoRequest(TipoTransacao.PIX, CPF_B, TipoChavePix.CPF, new BigDecimal("200.00"), "pix"),
                "00011",
                chave))
                .isInstanceOf(PayloadIdempotenciaDivergenteException.class);

        assertThat(saldo("00011")).isEqualByComparingTo("900.00");
    }

    @Test
    void mesmaChaveComTipoDeChavePixDiferenteRetorna422() {
        criarConta("00011", CPF_A, "a@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);
        criarConta("00022", CPF_B, "b@teste.com", new BigDecimal("1000.00"), StatusConta.ATIVA);

        String chave = UUID.randomUUID().toString();
        transacaoService.processar(
                new TransacaoRequest(TipoTransacao.PIX, CPF_B, TipoChavePix.CPF, new BigDecimal("100.00"), "pix"),
                "00011",
                chave);

        assertThatThrownBy(() -> transacaoService.processar(
                new TransacaoRequest(TipoTransacao.PIX, CPF_B, TipoChavePix.CELULAR, new BigDecimal("100.00"), "pix"),
                "00011",
                chave))
                .isInstanceOf(PayloadIdempotenciaDivergenteException.class);

        assertThat(saldo("00011")).isEqualByComparingTo("900.00");
    }

    private List<Throwable> executarConcorrente(int quantidadeThreads, IntConsumer tarefa)
            throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(quantidadeThreads);
        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch chegada = new CountDownLatch(quantidadeThreads);
        List<Throwable> erros = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < quantidadeThreads; i++) {
            final int indice = i;
            pool.submit(() -> {
                try {
                    largada.await();
                    tarefa.accept(indice);
                } catch (Throwable t) {
                    erros.add(t);
                } finally {
                    chegada.countDown();
                }
            });
        }

        largada.countDown();
        chegada.await(60, TimeUnit.SECONDS);
        pool.shutdownNow();
        return erros;
    }

    private BigDecimal saldo(String numeroConta) {
        return contaRepository.findByNumeroConta(numeroConta).orElseThrow().getSaldoConta();
    }

    private void criarConta(String numero, String cpf, String email, BigDecimal saldo, StatusConta status) {
        contaRepository.save(Conta.builder()
                .numeroConta(numero)
                .nomeTitular("Titular " + numero)
                .cpf(cpf)
                .telefone("119" + String.format("%08d", Long.parseLong(numero)))
                .email(email)
                .tipoConta(TipoConta.CORRENTE)
                .saldoConta(saldo)
                .statusConta(status)
                .senhaConta(passwordEncoder.encode("123456"))
                .dataAbertura(LocalDateTime.now())
                .build());
    }
}
