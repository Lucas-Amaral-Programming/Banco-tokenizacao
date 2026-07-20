package br.com.foursys.tokenizacao.transacoes.service;

import br.com.foursys.tokenizacao.transacoes.dto.request.CadastroContaRequest;
import br.com.foursys.tokenizacao.transacoes.dto.request.LoginContaRequest;
import br.com.foursys.tokenizacao.transacoes.dto.response.ContaResponse;
import br.com.foursys.tokenizacao.transacoes.exception.ContaNaoEncontradaException;
import br.com.foursys.tokenizacao.transacoes.exception.CpfInvalidoException;
import br.com.foursys.tokenizacao.transacoes.exception.CpfJaCadastradoException;
import br.com.foursys.tokenizacao.transacoes.exception.CredenciaisInvalidasException;
import br.com.foursys.tokenizacao.transacoes.exception.EmailJaCadastradoException;
import br.com.foursys.tokenizacao.transacoes.model.Conta;
import br.com.foursys.tokenizacao.transacoes.util.ValidadorCpf;
import br.com.foursys.tokenizacao.transacoes.model.StatusConta;
import br.com.foursys.tokenizacao.transacoes.repository.ContaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaService(ContaRepository contaRepository, PasswordEncoder passwordEncoder) {
        this.contaRepository = contaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ContaResponse cadastrar(CadastroContaRequest request) {
        String cpf = apenasDigitos(request.cpf());
        if (!ValidadorCpf.ehValido(cpf)) {
            throw new CpfInvalidoException();
        }
        if (contaRepository.existsByCpf(cpf)) {
            throw new CpfJaCadastradoException();
        }
        if (contaRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException();
        }

        Conta conta = Conta.builder()
                .numeroConta(gerarNumeroConta())
                .nomeTitular(request.nomeTitular())
                .cpf(cpf)
                .email(request.email())
                .tipoConta(request.tipoConta())
                .saldoConta(BigDecimal.ZERO)
                .statusConta(StatusConta.ATIVA)
                .senhaConta(passwordEncoder.encode(request.senha()))
                .dataAbertura(LocalDateTime.now())
                .build();

        return montarResposta(contaRepository.save(conta));
    }

    @Transactional(readOnly = true)
    public ContaResponse autenticar(LoginContaRequest request) {
        Conta conta = contaRepository.findByCpf(apenasDigitos(request.cpf()))
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(request.senha(), conta.getSenhaConta())) {
            throw new CredenciaisInvalidasException();
        }

        return montarResposta(conta);
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorNumero(String numeroConta) {
        Conta conta = contaRepository.findByNumeroConta(numeroConta)
                .orElseThrow(() -> new ContaNaoEncontradaException(numeroConta));
        return montarResposta(conta);
    }

    private String apenasDigitos(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    private String gerarNumeroConta() {
        String numeroConta;
        do {
            numeroConta = String.format("%08d", ThreadLocalRandom.current().nextInt(0, 100_000_000));
        } while (contaRepository.existsByNumeroConta(numeroConta));
        return numeroConta;
    }

    private ContaResponse montarResposta(Conta conta) {
        return new ContaResponse(
                conta.getNumeroConta(),
                conta.getNomeTitular(),
                conta.getTipoConta(),
                conta.getSaldoConta(),
                conta.getStatusConta());
    }
}
