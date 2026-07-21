package br.com.foursys.tokenizacao.transacoes.service;

import br.com.foursys.tokenizacao.transacoes.exception.ChavePixInvalidaException;
import br.com.foursys.tokenizacao.transacoes.exception.ChavePixNaoEncontradaException;
import br.com.foursys.tokenizacao.transacoes.exception.TipoChavePixObrigatorioException;
import br.com.foursys.tokenizacao.transacoes.model.ChavePix;
import br.com.foursys.tokenizacao.transacoes.model.Conta;
import br.com.foursys.tokenizacao.transacoes.model.TipoChavePix;
import br.com.foursys.tokenizacao.transacoes.repository.ChavePixRepository;
import br.com.foursys.tokenizacao.transacoes.util.ValidadorCpf;
import br.com.foursys.tokenizacao.transacoes.util.ValidadorEmail;
import br.com.foursys.tokenizacao.transacoes.util.ValidadorTelefone;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ChavePixService {

    private final ChavePixRepository chavePixRepository;

    public ChavePixService(ChavePixRepository chavePixRepository) {
        this.chavePixRepository = chavePixRepository;
    }

    public void registrarChavesIniciais(Conta conta) {
        LocalDateTime agora = LocalDateTime.now();
        chavePixRepository.saveAll(List.of(
                nova(conta, TipoChavePix.CPF, normalizar(TipoChavePix.CPF, conta.getCpf()), agora),
                nova(conta, TipoChavePix.EMAIL, normalizar(TipoChavePix.EMAIL, conta.getEmail()), agora),
                nova(conta, TipoChavePix.CELULAR, normalizar(TipoChavePix.CELULAR, conta.getTelefone()), agora)));
    }

    public Conta resolver(TipoChavePix tipo, String chave) {
        String valorNormalizado = normalizar(tipo, chave);
        return chavePixRepository
                .findByTipoChaveAndValorNormalizadoAndAtivaTrue(tipo, valorNormalizado)
                .map(ChavePix::getConta)
                .orElseThrow(() -> new ChavePixNaoEncontradaException(valorNormalizado));
    }

    private ChavePix nova(Conta conta, TipoChavePix tipo, String valor, LocalDateTime dataCadastro) {
        return ChavePix.builder()
                .conta(conta)
                .tipoChave(tipo)
                .valorNormalizado(valor)
                .ativa(true)
                .dataCadastro(dataCadastro)
                .build();
    }

    private String normalizar(TipoChavePix tipo, String chave) {
        if (tipo == null) {
            throw new TipoChavePixObrigatorioException();
        }
        String valor = chave == null ? "" : chave.trim();
        return switch (tipo) {
            case CPF -> validarCpf(apenasDigitos(valor));
            case EMAIL -> validarEmail(valor.toLowerCase(Locale.ROOT));
            case CELULAR -> validarTelefone(apenasDigitos(valor));
        };
    }

    private String validarCpf(String valor) {
        if (!ValidadorCpf.ehValido(valor)) {
            throw new ChavePixInvalidaException();
        }
        return valor;
    }

    private String validarEmail(String valor) {
        if (!ValidadorEmail.ehValido(valor)) {
            throw new ChavePixInvalidaException();
        }
        return valor;
    }

    private String validarTelefone(String valor) {
        if (!ValidadorTelefone.ehValido(valor)) {
            throw new ChavePixInvalidaException();
        }
        return valor;
    }

    private String apenasDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
