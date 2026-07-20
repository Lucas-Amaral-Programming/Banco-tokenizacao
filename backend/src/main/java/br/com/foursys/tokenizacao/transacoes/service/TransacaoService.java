package br.com.foursys.tokenizacao.transacoes.service;

import br.com.foursys.tokenizacao.transacoes.dto.request.TransacaoRequest;
import br.com.foursys.tokenizacao.transacoes.dto.response.TransacaoResponse;
import br.com.foursys.tokenizacao.transacoes.exception.ConflitoConcorrenciaException;
import br.com.foursys.tokenizacao.transacoes.exception.IdempotencyKeyInvalidaException;
import br.com.foursys.tokenizacao.transacoes.repository.TransacaoRepository;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransacaoService {

    private static final int MAX_RETENTATIVAS = 3;

    private final ProcessadorTransacao processadorTransacao;
    private final TransacaoRepository transacaoRepository;

    public TransacaoService(ProcessadorTransacao processadorTransacao,
                            TransacaoRepository transacaoRepository) {
        this.processadorTransacao = processadorTransacao;
        this.transacaoRepository = transacaoRepository;
    }

    public ResultadoTransacao processar(TransacaoRequest request,
                                        String numeroContaSolicitante,
                                        String idempotencyKey) {
        validarIdempotencyKey(idempotencyKey);

        int retentativas = 0;
        while (true) {
            try {
                return processadorTransacao.processar(request, numeroContaSolicitante, idempotencyKey);
            } catch (RuntimeException excecao) {
                if (!ehConflitoTransitorio(excecao)) {
                    throw excecao;
                }
                if (retentativas >= MAX_RETENTATIVAS) {
                    throw new ConflitoConcorrenciaException(excecao);
                }
                retentativas++;
                aguardarBackoff(retentativas);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listarPorConta(String numeroConta) {
        return transacaoRepository.buscarPorConta(numeroConta).stream()
                .map(TransacaoResponse::de)
                .toList();
    }

    private void validarIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IdempotencyKeyInvalidaException("Header Idempotency-Key e obrigatorio.");
        }
        try {
            UUID.fromString(idempotencyKey.trim());
        } catch (IllegalArgumentException e) {
            throw new IdempotencyKeyInvalidaException("Header Idempotency-Key deve ser um UUID valido.");
        }
    }

    private boolean ehConflitoTransitorio(Throwable excecao) {
        return excecao instanceof CannotAcquireLockException
                || excecao instanceof PessimisticLockingFailureException
                || excecao instanceof ObjectOptimisticLockingFailureException
                || excecao instanceof QueryTimeoutException
                || excecao instanceof DataIntegrityViolationException;
    }

    private void aguardarBackoff(int retentativas) {
        long atraso = ThreadLocalRandom.current().nextLong(20L, 80L) * retentativas;
        try {
            Thread.sleep(atraso);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConflitoConcorrenciaException(e);
        }
    }
}
