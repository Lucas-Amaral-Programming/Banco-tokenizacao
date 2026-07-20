package br.com.foursys.tokenizacao.transacoes.exception;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraNegocio(RegraNegocioException excecao) {
        return montar(HttpStatus.BAD_REQUEST, excecao.getMessage());
    }

    @ExceptionHandler(PayloadIdempotenciaDivergenteException.class)
    public ResponseEntity<ErroResposta> tratarPayloadDivergente(PayloadIdempotenciaDivergenteException excecao) {
        return montar(HttpStatus.UNPROCESSABLE_ENTITY, excecao.getMessage());
    }

    @ExceptionHandler(ConflitoConcorrenciaException.class)
    public ResponseEntity<ErroResposta> tratarConflitoConcorrencia(ConflitoConcorrenciaException excecao) {
        return montar(HttpStatus.CONFLICT, excecao.getMessage());
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ErroResposta> tratarContaBloqueada(AccountStatusException excecao) {
        return montar(HttpStatus.FORBIDDEN, "Conta indisponivel para login.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResposta> tratarCredenciaisInvalidas(AuthenticationException excecao) {
        return montar(HttpStatus.UNAUTHORIZED, "CPF ou senha invalidos.");
    }

    private ResponseEntity<ErroResposta> montar(HttpStatus status, String mensagem) {
        ErroResposta erro = new ErroResposta(mensagem, status.value(), LocalDateTime.now());
        return ResponseEntity.status(status).body(erro);
    }
}
