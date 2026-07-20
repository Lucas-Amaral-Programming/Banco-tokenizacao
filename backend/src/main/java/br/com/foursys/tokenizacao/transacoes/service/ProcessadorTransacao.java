package br.com.foursys.tokenizacao.transacoes.service;

import br.com.foursys.tokenizacao.transacoes.dto.request.TransacaoRequest;
import br.com.foursys.tokenizacao.transacoes.dto.response.TransacaoResponse;
import br.com.foursys.tokenizacao.transacoes.exception.ChavePixNaoEncontradaException;
import br.com.foursys.tokenizacao.transacoes.exception.ContaDestinoObrigatoriaException;
import br.com.foursys.tokenizacao.transacoes.exception.ContaInativaException;
import br.com.foursys.tokenizacao.transacoes.exception.ContaNaoEncontradaException;
import br.com.foursys.tokenizacao.transacoes.exception.ContaOrigemObrigatoriaException;
import br.com.foursys.tokenizacao.transacoes.exception.ContasIguaisException;
import br.com.foursys.tokenizacao.transacoes.exception.PayloadIdempotenciaDivergenteException;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessadorTransacao {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final TokenTransacaoService tokenTransacaoService;

    public ProcessadorTransacao(ContaRepository contaRepository,
                                TransacaoRepository transacaoRepository,
                                TokenTransacaoService tokenTransacaoService) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.tokenTransacaoService = tokenTransacaoService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultadoTransacao processar(TransacaoRequest request,
                                        String numeroContaSolicitante,
                                        String idempotencyKey) {
        String fingerprint = FingerprintRequisicao.calcular(request);
        Conta solicitante = buscarConta(numeroContaSolicitante);

        Optional<Transacao> existente = transacaoRepository
                .findByContaSolicitante_IdContaAndIdempotencyKey(solicitante.getIdConta(), idempotencyKey);
        if (existente.isPresent()) {
            Transacao original = existente.get();
            if (!original.getFingerprintRequisicao().equals(fingerprint)) {
                throw new PayloadIdempotenciaDivergenteException();
            }
            return new ResultadoTransacao(TransacaoResponse.de(original), true);
        }

        validarEntrada(request);

        Conta origemRef = exigeContaOrigem(request.tipoTransacao()) ? solicitante : null;
        if (exigeContaOrigem(request.tipoTransacao()) && origemRef == null) {
            throw new ContaOrigemObrigatoriaException(request.tipoTransacao());
        }
        Conta destinoRef = resolverContaDestino(request);
        validarContasDiferentes(origemRef, destinoRef);

        List<Long> idsParaBloquear = Stream.of(origemRef, destinoRef)
                .filter(Objects::nonNull)
                .map(Conta::getIdConta)
                .distinct()
                .sorted()
                .toList();

        Map<Long, Conta> bloqueadas = contaRepository.bloquearPorIds(idsParaBloquear).stream()
                .collect(Collectors.toMap(Conta::getIdConta, conta -> conta));

        Conta origem = origemRef == null ? null : bloqueadas.get(origemRef.getIdConta());
        Conta destino = destinoRef == null ? null : bloqueadas.get(destinoRef.getIdConta());

        if (origem != null) {
            validarContaAtiva(origem, "origem");
        }
        if (destino != null) {
            validarContaAtiva(destino, "destino");
        }
        validarContasDiferentes(origem, destino);

        if (origem != null) {
            origem.debitar(request.valorTransacao());
        }
        if (destino != null) {
            destino.creditar(request.valorTransacao());
        }

        Transacao transacao = Transacao.builder()
                .tokenTransacao(tokenTransacaoService.gerarToken(
                        numeroDaConta(origem),
                        numeroDaConta(destino),
                        request.valorTransacao()))
                .tipoTransacao(request.tipoTransacao())
                .contaOrigem(origem)
                .contaDestino(destino)
                .contaSolicitante(solicitante)
                .idempotencyKey(idempotencyKey)
                .fingerprintRequisicao(fingerprint)
                .valorTransacao(request.valorTransacao())
                .descricaoTransacao(request.descricaoTransacao())
                .statusTransacao(StatusTransacao.APROVADA)
                .dataHoraTransacao(LocalDateTime.now())
                .build();

        return new ResultadoTransacao(TransacaoResponse.de(transacaoRepository.save(transacao)), false);
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

    private Conta resolverContaDestino(TransacaoRequest request) {
        if (!exigeContaDestino(request.tipoTransacao())) {
            return null;
        }
        if (ehVazio(request.numeroContaDestino())) {
            throw new ContaDestinoObrigatoriaException(request.tipoTransacao());
        }
        return request.tipoTransacao() == TipoTransacao.PIX
                ? buscarContaPorChavePix(request.numeroContaDestino())
                : buscarConta(request.numeroContaDestino());
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

    private String numeroDaConta(Conta conta) {
        return conta != null ? conta.getNumeroConta() : null;
    }

    private boolean ehVazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
