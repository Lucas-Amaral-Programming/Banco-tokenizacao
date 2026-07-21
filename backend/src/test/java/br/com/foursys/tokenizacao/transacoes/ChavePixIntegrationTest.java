package br.com.foursys.tokenizacao.transacoes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.foursys.tokenizacao.transacoes.dto.request.CadastroContaRequest;
import br.com.foursys.tokenizacao.transacoes.dto.request.TransacaoRequest;
import br.com.foursys.tokenizacao.transacoes.exception.ChavePixInvalidaException;
import br.com.foursys.tokenizacao.transacoes.exception.ChavePixNaoEncontradaException;
import br.com.foursys.tokenizacao.transacoes.model.ChavePix;
import br.com.foursys.tokenizacao.transacoes.model.Conta;
import br.com.foursys.tokenizacao.transacoes.model.StatusConta;
import br.com.foursys.tokenizacao.transacoes.model.TipoChavePix;
import br.com.foursys.tokenizacao.transacoes.model.TipoConta;
import br.com.foursys.tokenizacao.transacoes.model.TipoTransacao;
import br.com.foursys.tokenizacao.transacoes.repository.ChavePixRepository;
import br.com.foursys.tokenizacao.transacoes.repository.ContaRepository;
import br.com.foursys.tokenizacao.transacoes.repository.TransacaoRepository;
import br.com.foursys.tokenizacao.transacoes.service.ChavePixService;
import br.com.foursys.tokenizacao.transacoes.service.ContaService;
import br.com.foursys.tokenizacao.transacoes.service.TransacaoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ChavePixIntegrationTest {

    @Autowired
    private ChavePixService chavePixService;

    @Autowired
    private ContaService contaService;

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private ChavePixRepository chavePixRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void preparar() {
        transacaoRepository.deleteAll();
        chavePixRepository.deleteAll();
        contaRepository.deleteAll();
    }

    @Test
    void cadastroNormalizaERegistraAsTresChaves() {
        contaService.cadastrar(new CadastroContaRequest(
                "Maria", "529.982.247-25", "(11) 98765-4321",
                " MARIA@EMAIL.COM ", TipoConta.CORRENTE, "12345678"));

        Long idConta = contaRepository.findByCpf("52998224725").orElseThrow().getIdConta();
        assertThat(chavePixRepository.count()).isEqualTo(3);
        assertThat(chavePixService.resolver(TipoChavePix.CPF, "529.982.247-25").getIdConta())
                .isEqualTo(idConta);
        assertThat(chavePixService.resolver(TipoChavePix.EMAIL, "maria@email.com").getIdConta())
                .isEqualTo(idConta);
        assertThat(chavePixService.resolver(TipoChavePix.CELULAR, "11987654321").getIdConta())
                .isEqualTo(idConta);
    }

    @Test
    void mesmoValorPodePertencerATiposDiferentes() {
        Conta cpf = criarConta("00011", "52998224725", "11987654321", "a@teste.com", true);
        Conta celular = criarConta("00022", "12345678909", "52998224725", "b@teste.com", true);

        assertThat(chavePixService.resolver(TipoChavePix.CPF, "52998224725").getIdConta())
                .isEqualTo(cpf.getIdConta());
        assertThat(chavePixService.resolver(TipoChavePix.CELULAR, "52998224725").getIdConta())
                .isEqualTo(celular.getIdConta());
    }

    @Test
    void pixPorCelularCreditaAContaCorreta() {
        criarConta("00011", "52998224725", "11987654321", "a@teste.com", true);
        criarConta("00022", "12345678909", "21987654321", "b@teste.com", true);

        transacaoService.processar(
                new TransacaoRequest(
                        TipoTransacao.PIX, "21987654321", TipoChavePix.CELULAR,
                        new BigDecimal("100.00"), "pix celular"),
                "00011",
                UUID.randomUUID().toString());

        assertThat(contaRepository.findByNumeroConta("00011").orElseThrow().getSaldoConta())
                .isEqualByComparingTo("900.00");
        assertThat(contaRepository.findByNumeroConta("00022").orElseThrow().getSaldoConta())
                .isEqualByComparingTo("1100.00");
    }

    @Test
    void tipoIncorretoNaoResolveChave() {
        criarConta("00011", "52998224725", "11987654321", "a@teste.com", true);

        assertThatThrownBy(() -> chavePixService.resolver(TipoChavePix.CELULAR, "52998224725"))
                .isInstanceOf(ChavePixNaoEncontradaException.class);
    }

    @Test
    void formatoInvalidoEChaveInexistenteNaoResolvem() {
        assertThatThrownBy(() -> chavePixService.resolver(TipoChavePix.CPF, "123"))
                .isInstanceOf(ChavePixInvalidaException.class);
        assertThatThrownBy(() -> chavePixService.resolver(TipoChavePix.EMAIL, "inexistente@teste.com"))
                .isInstanceOf(ChavePixNaoEncontradaException.class);
    }

    @Test
    void chaveInativaNaoResolve() {
        Conta conta = criarConta("00011", "52998224725", "11987654321", "a@teste.com", false);
        chavePixRepository.save(ChavePix.builder()
                .conta(conta)
                .tipoChave(TipoChavePix.CPF)
                .valorNormalizado("52998224725")
                .ativa(false)
                .dataCadastro(LocalDateTime.now())
                .build());

        assertThatThrownBy(() -> chavePixService.resolver(TipoChavePix.CPF, "52998224725"))
                .isInstanceOf(ChavePixNaoEncontradaException.class);
    }

    @Test
    void colisaoNoRegistroDasChavesReverteCadastro() {
        Conta donaDaChave = criarConta("00011", "12345678909", "11987654321", "dona@teste.com", false);
        chavePixRepository.save(ChavePix.builder()
                .conta(donaDaChave)
                .tipoChave(TipoChavePix.EMAIL)
                .valorNormalizado("duplicado@teste.com")
                .ativa(true)
                .dataCadastro(LocalDateTime.now())
                .build());

        assertThatThrownBy(() -> contaService.cadastrar(new CadastroContaRequest(
                "Nova", "11144477735", "21987654321", "duplicado@teste.com",
                TipoConta.CORRENTE, "12345678")))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(contaRepository.findByCpf("11144477735")).isEmpty();
    }

    private Conta criarConta(String numero,
                             String cpf,
                             String telefone,
                             String email,
                             boolean registrarChaves) {
        Conta conta = contaRepository.save(Conta.builder()
                .numeroConta(numero)
                .nomeTitular("Titular " + numero)
                .cpf(cpf)
                .telefone(telefone)
                .email(email)
                .tipoConta(TipoConta.CORRENTE)
                .saldoConta(new BigDecimal("1000.00"))
                .statusConta(StatusConta.ATIVA)
                .senhaConta(passwordEncoder.encode("12345678"))
                .dataAbertura(LocalDateTime.now())
                .build());
        if (registrarChaves) {
            chavePixService.registrarChavesIniciais(conta);
        }
        return conta;
    }
}
