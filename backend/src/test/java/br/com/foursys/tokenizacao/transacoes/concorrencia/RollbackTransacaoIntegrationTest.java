package br.com.foursys.tokenizacao.transacoes.concorrencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import br.com.foursys.tokenizacao.transacoes.dto.request.TransacaoRequest;
import br.com.foursys.tokenizacao.transacoes.model.Conta;
import br.com.foursys.tokenizacao.transacoes.model.StatusConta;
import br.com.foursys.tokenizacao.transacoes.model.TipoConta;
import br.com.foursys.tokenizacao.transacoes.model.TipoTransacao;
import br.com.foursys.tokenizacao.transacoes.model.Transacao;
import br.com.foursys.tokenizacao.transacoes.repository.ContaRepository;
import br.com.foursys.tokenizacao.transacoes.repository.TransacaoRepository;
import br.com.foursys.tokenizacao.transacoes.service.TransacaoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class RollbackTransacaoIntegrationTest extends AbstractMySqlTestcontainers {

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private TransacaoRepository transacaoRepository;

    @BeforeEach
    void preparar() {
        contaRepository.deleteAll();
        when(transacaoRepository.findByContaSolicitante_IdContaAndIdempotencyKey(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(transacaoRepository.existsByTokenTransacao(anyString())).thenReturn(false);
        when(transacaoRepository.save(any(Transacao.class)))
                .thenThrow(new RuntimeException("falha simulada ao salvar a transacao"));
    }

    @Test
    void falhaAoSalvarTransacaoReverteTodosOsSaldos() {
        criarConta("00011", "11111111111", "a@teste.com", new BigDecimal("1000.00"));
        criarConta("00022", "22222222222", "b@teste.com", new BigDecimal("1000.00"));

        assertThatThrownBy(() -> transacaoService.processar(
                new TransacaoRequest(TipoTransacao.PIX, "22222222222", new BigDecimal("100.00"), "pix"),
                "00011",
                UUID.randomUUID().toString()))
                .isInstanceOf(RuntimeException.class);

        assertThat(saldo("00011")).isEqualByComparingTo("1000.00");
        assertThat(saldo("00022")).isEqualByComparingTo("1000.00");
    }

    private BigDecimal saldo(String numeroConta) {
        return contaRepository.findByNumeroConta(numeroConta).orElseThrow().getSaldoConta();
    }

    private void criarConta(String numero, String cpf, String email, BigDecimal saldo) {
        contaRepository.save(Conta.builder()
                .numeroConta(numero)
                .nomeTitular("Titular " + numero)
                .cpf(cpf)
                .email(email)
                .tipoConta(TipoConta.CORRENTE)
                .saldoConta(saldo)
                .statusConta(StatusConta.ATIVA)
                .senhaConta(passwordEncoder.encode("123456"))
                .dataAbertura(LocalDateTime.now())
                .build());
    }
}
