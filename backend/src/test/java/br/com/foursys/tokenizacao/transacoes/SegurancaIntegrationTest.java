package br.com.foursys.tokenizacao.transacoes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.foursys.tokenizacao.transacoes.dto.request.CadastroContaRequest;
import br.com.foursys.tokenizacao.transacoes.dto.request.LoginContaRequest;
import br.com.foursys.tokenizacao.transacoes.dto.request.TransacaoRequest;
import br.com.foursys.tokenizacao.transacoes.model.Conta;
import br.com.foursys.tokenizacao.transacoes.model.StatusConta;
import br.com.foursys.tokenizacao.transacoes.model.TipoChavePix;
import br.com.foursys.tokenizacao.transacoes.model.TipoConta;
import br.com.foursys.tokenizacao.transacoes.model.TipoTransacao;
import br.com.foursys.tokenizacao.transacoes.repository.ContaRepository;
import br.com.foursys.tokenizacao.transacoes.repository.ChavePixRepository;
import br.com.foursys.tokenizacao.transacoes.repository.TransacaoRepository;
import br.com.foursys.tokenizacao.transacoes.service.ChavePixService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SegurancaIntegrationTest {

    private static final String CPF_MARIA = "52998224725";
    private static final String CPF_JOAO = "12345678909";
    private static final String CPF_CARLOS = "11144477735";
    private static final String SENHA = "123456";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ChavePixRepository chavePixRepository;

    @Autowired
    private ChavePixService chavePixService;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void preparar() {
        transacaoRepository.deleteAll();
        chavePixRepository.deleteAll();
        contaRepository.deleteAll();
        criarConta("00011", CPF_MARIA, "maria@teste.com", StatusConta.ATIVA);
        criarConta("00022", CPF_JOAO, "joao@teste.com", StatusConta.ATIVA);
        criarConta("00033", CPF_CARLOS, "carlos@teste.com", StatusConta.BLOQUEADA);
    }

    @Test
    void loginValidoAutenticaEDevolveConta() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(CPF_MARIA, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroConta").value("00011"))
                .andExpect(jsonPath("$.nomeTitular").value("Titular 00011"));
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(CPF_MARIA, "999999")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginDeContaBloqueadaRetorna403() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(CPF_CARLOS, SENHA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointPrivadoSemSessaoRetorna401() throws Exception {
        mvc.perform(get("/api/transacoes"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/contas/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postSemCsrfRetorna403() throws Exception {
        MockHttpSession sessao = login(CPF_MARIA, SENHA);
        mvc.perform(post("/api/transacoes").session(sessao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transacaoJson(TipoTransacao.PIX, CPF_JOAO, "100.00")))
                .andExpect(status().isForbidden());
    }

    @Test
    void resolverChavePixExigeSessaoECsrf() throws Exception {
        mvc.perform(post("/api/chaves-pix/resolver")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoChavePix\":\"CPF\",\"chave\":\"" + CPF_JOAO + "\"}"))
                .andExpect(status().isUnauthorized());

        MockHttpSession sessao = login(CPF_MARIA, SENHA);
        mvc.perform(post("/api/chaves-pix/resolver")
                        .session(sessao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoChavePix\":\"CPF\",\"chave\":\"" + CPF_JOAO + "\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/chaves-pix/resolver")
                        .session(sessao)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoChavePix\":\"CPF\",\"chave\":\"" + CPF_JOAO + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeTitular").value("Titular 00022"))
                .andExpect(jsonPath("$.tipoChavePix").value("CPF"))
                .andExpect(jsonPath("$.numeroConta").doesNotExist());
    }

    @Test
    void usuarioNaoAcessaExtratoDeOutraConta() throws Exception {
        MockHttpSession sessaoMaria = login(CPF_MARIA, SENHA);
        mvc.perform(post("/api/transacoes").session(sessaoMaria).with(csrf())
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transacaoJson(TipoTransacao.PIX, CPF_JOAO, "100.00")))
                .andExpect(status().isCreated());

        String cpfTerceiro = "16899535009";
        cadastrar("Novo Cliente", cpfTerceiro, "11977770001", "novo@teste.com");
        MockHttpSession sessaoTerceiro = login(cpfTerceiro, SENHA);

        MvcResult resultado = mvc.perform(get("/api/transacoes").session(sessaoTerceiro))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode extrato = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(extrato).isEmpty();
    }

    @Test
    void consultaPublicaPorNumeroDeContaNaoExisteMais() throws Exception {
        MockHttpSession sessao = login(CPF_MARIA, SENHA);
        mvc.perform(get("/api/contas/00022").session(sessao))
                .andExpect(status().isNotFound());
    }

    @Test
    void logoutInvalidaSessao() throws Exception {
        MockHttpSession sessao = login(CPF_MARIA, SENHA);
        mvc.perform(get("/api/auth/me").session(sessao))
                .andExpect(status().isOk());

        mvc.perform(post("/api/auth/logout").session(sessao).with(csrf()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/auth/me").session(sessao))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fluxoCompletoCadastroLoginTransacaoExtratoLogout() throws Exception {
        String cpf = "39053344705";
        String numeroConta = cadastrar("Cliente Teste", cpf, "11977770002", "e2e@teste.com");

        MockHttpSession sessao = login(cpf, SENHA);

        mvc.perform(post("/api/transacoes").session(sessao).with(csrf())
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transacaoJson(TipoTransacao.DEPOSITO, numeroConta, "200.00")))
                .andExpect(status().isCreated());

        MvcResult extrato = mvc.perform(get("/api/transacoes").session(sessao))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode lista = objectMapper.readTree(extrato.getResponse().getContentAsString());
        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).get("tipoTransacao").asText()).isEqualTo("DEPOSITO");

        mvc.perform(post("/api/auth/logout").session(sessao).with(csrf()))
                .andExpect(status().isNoContent());
    }

    private MockHttpSession login(String cpf, String senha) throws Exception {
        MvcResult resultado = mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(cpf, senha)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    private String cadastrar(String nome, String cpf, String telefone, String email) throws Exception {
        CadastroContaRequest request =
                new CadastroContaRequest(nome, cpf, telefone, email, TipoConta.CORRENTE, SENHA);
        MvcResult resultado = mvc.perform(post("/api/contas").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .get("numeroConta").asText();
    }

    private String loginJson(String cpf, String senha) throws Exception {
        return objectMapper.writeValueAsString(new LoginContaRequest(cpf, senha));
    }

    private String transacaoJson(TipoTransacao tipo, String destino, String valor) throws Exception {
        TipoChavePix tipoChavePix = tipo == TipoTransacao.PIX ? TipoChavePix.CPF : null;
        return objectMapper.writeValueAsString(
                new TransacaoRequest(tipo, destino, tipoChavePix, new BigDecimal(valor), "teste"));
    }

    private Conta criarConta(String numero, String cpf, String email, StatusConta status) {
        Conta conta = contaRepository.save(Conta.builder()
                .numeroConta(numero)
                .nomeTitular("Titular " + numero)
                .cpf(cpf)
                .telefone("119" + String.format("%08d", Long.parseLong(numero)))
                .email(email)
                .tipoConta(TipoConta.CORRENTE)
                .saldoConta(new BigDecimal("5000.00"))
                .statusConta(status)
                .senhaConta(passwordEncoder.encode(SENHA))
                .dataAbertura(LocalDateTime.now())
                .build());
        chavePixService.registrarChavesIniciais(conta);
        return conta;
    }
}
