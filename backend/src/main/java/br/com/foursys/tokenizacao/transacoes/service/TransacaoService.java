package br.com.foursys.tokenizacao.transacoes.service;

import br.com.foursys.tokenizacao.transacoes.dto.request.TransacaoRequest;
import br.com.foursys.tokenizacao.transacoes.dto.response.TransacaoResponse;
import br.com.foursys.tokenizacao.transacoes.exception.ChavePixNaoEncontradaException;
import br.com.foursys.tokenizacao.transacoes.exception.ContaDestinoObrigatoriaException;
import br.com.foursys.tokenizacao.transacoes.exception.ContaInativaException;
import br.com.foursys.tokenizacao.transacoes.exception.ContaNaoEncontradaException;
import br.com.foursys.tokenizacao.transacoes.exception.ContaOrigemObrigatoriaException;
import br.com.foursys.tokenizacao.transacoes.exception.ContasIguaisException;
import br.com.foursys.tokenizacao.transacoes.exception.SaldoInsuficienteException;
import br.com.foursys.tokenizacao.transacoes.exception.TipoTransacaoObrigatorioException;
import br.com.foursys.tokenizacao.transacoes.exception.ValorTransacaoInvalidoException;
import br.com.foursys.tokenizacao.transacoes.model.Conta;
import br.com.foursys.tokenizacao.transacoes.model.StatusConta;
import br.com.foursys.tokenizacao.transacoes.model.StatusTransacao;
import br.com.foursys.tokenizacao.transacoes.model.TipoTransacao;
import br.com.foursys.tokenizacao.transacoes.model.Transacao;
import br.com.foursys.tokenizacao.transacoes.repository.ContaRepository;
import br.com.foursys.tokenizacao.transacoes.repository.TransacaoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransacaoService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final TokenTransacaoService tokenTransacaoService;

    public TransacaoService(ContaRepository contaRepository,
                            TransacaoRepository transacaoRepository,
                            TokenTransacaoService tokenTransacaoService) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.tokenTransacaoService = tokenTransacaoService;
    }

    @Transactional
    public TransacaoResponse processar(TransacaoRequest request) {
        validarEntrada(request);

        Conta contaOrigem = resolverContaOrigem(request);
        Conta contaDestino = resolverContaDestino(request);
        validarContasDiferentes(contaOrigem, contaDestino);

        if (contaOrigem != null) {
            contaOrigem.setSaldoConta(contaOrigem.getSaldoConta().subtract(request.valorTransacao()));
        }
        if (contaDestino != null) {
            contaDestino.setSaldoConta(contaDestino.getSaldoConta().add(request.valorTransacao()));
        }

        Transacao transacao = Transacao.builder()
                .tokenTransacao(tokenTransacaoService.gerarToken(request))
                .tipoTransacao(request.tipoTransacao())
                .contaOrigem(contaOrigem)
                .contaDestino(contaDestino)
                .valorTransacao(request.valorTransacao())
                .descricaoTransacao(request.descricaoTransacao())
                .statusTransacao(StatusTransacao.APROVADA)
                .dataHoraTransacao(LocalDateTime.now())
                .build();

        return montarResposta(transacaoRepository.save(transacao));
    }

    public List<TransacaoResponse> listarPorConta(String numeroConta) {
        return transacaoRepository.buscarPorConta(numeroConta).stream()
                .map(this::montarResposta)
                .toList();
    }

    private void validarEntrada(TransacaoRequest request) {
        if (request.tipoTransacao() == null) {
            throw new TipoTransacaoObrigatorioException();
        }
        if (request.valorTransacao() == null
                || request.valorTransacao().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorTransacaoInvalidoException();
        }
    }

    private Conta resolverContaOrigem(TransacaoRequest request) {
        if (!exigeContaOrigem(request.tipoTransacao())) {
            return null;
        }
        if (ehVazio(request.numeroContaOrigem())) {
            throw new ContaOrigemObrigatoriaException(request.tipoTransacao());
        }
        Conta contaOrigem = buscarConta(request.numeroContaOrigem());
        validarContaAtiva(contaOrigem, "origem");
        if (contaOrigem.getSaldoConta().compareTo(request.valorTransacao()) < 0) {
            throw new SaldoInsuficienteException();
        }
        return contaOrigem;
    }

    private Conta resolverContaDestino(TransacaoRequest request) {
        if (!exigeContaDestino(request.tipoTransacao())) {
            return null;
        }
        if (ehVazio(request.numeroContaDestino())) {
            throw new ContaDestinoObrigatoriaException(request.tipoTransacao());
        }
        Conta contaDestino = request.tipoTransacao() == TipoTransacao.PIX
                ? buscarContaPorChavePix(request.numeroContaDestino())
                : buscarConta(request.numeroContaDestino());
        validarContaAtiva(contaDestino, "destino");
        return contaDestino;
    }

    private Conta buscarContaPorChavePix(String chave) {
        String chaveTratada = chave.trim();
        if (chaveTratada.contains("@")) {
            return contaRepository.findByEmail(chaveTratada)
                    .orElseThrow(() -> new ChavePixNaoEncontradaException(chaveTratada));
        }
        String cpfTratado = chaveTratada.replaceAll("\\D", "");
        return contaRepository.findByCpf(cpfTratado)
                .orElseThrow(() -> new ChavePixNaoEncontradaException(chaveTratada));
    }

    private void validarContasDiferentes(Conta contaOrigem, Conta contaDestino) {
        if (contaOrigem != null && contaDestino != null
                && contaOrigem.getNumeroConta().equals(contaDestino.getNumeroConta())) {
            throw new ContasIguaisException();
        }
    }

    private boolean exigeContaOrigem(TipoTransacao tipo) {
        return tipo == TipoTransacao.SAQUE || tipo == TipoTransacao.PIX;
    }

    private boolean exigeContaDestino(TipoTransacao tipo) {
        return tipo == TipoTransacao.DEPOSITO || tipo == TipoTransacao.PIX;
    }

    private Conta buscarConta(String numeroConta) {
        return contaRepository.findByNumeroConta(numeroConta)
                .orElseThrow(() -> new ContaNaoEncontradaException(numeroConta));
    }

    private void validarContaAtiva(Conta conta, String papel) {
        if (conta.getStatusConta() != StatusConta.ATIVA) {
            throw new ContaInativaException(papel);
        }
    }

    private TransacaoResponse montarResposta(Transacao transacao) {
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

    private String numeroDaConta(Conta conta) {
        return conta != null ? conta.getNumeroConta() : null;
    }

    private boolean ehVazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
