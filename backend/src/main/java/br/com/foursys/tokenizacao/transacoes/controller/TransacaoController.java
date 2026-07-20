package br.com.foursys.tokenizacao.transacoes.controller;

import br.com.foursys.tokenizacao.transacoes.dto.request.TransacaoRequest;
import br.com.foursys.tokenizacao.transacoes.dto.response.TransacaoResponse;
import br.com.foursys.tokenizacao.transacoes.security.ContaPrincipal;
import br.com.foursys.tokenizacao.transacoes.service.ResultadoTransacao;
import br.com.foursys.tokenizacao.transacoes.service.TransacaoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping
    public ResponseEntity<TransacaoResponse> criar(
            @RequestBody TransacaoRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            Authentication autenticacao) {
        ResultadoTransacao resultado =
                transacaoService.processar(request, numeroConta(autenticacao), idempotencyKey);
        return ResponseEntity
                .status(resultado.replay() ? HttpStatus.OK : HttpStatus.CREATED)
                .header("Idempotency-Replayed", String.valueOf(resultado.replay()))
                .body(resultado.response());
    }

    @GetMapping
    public ResponseEntity<List<TransacaoResponse>> listar(Authentication autenticacao) {
        return ResponseEntity.ok(transacaoService.listarPorConta(numeroConta(autenticacao)));
    }

    private String numeroConta(Authentication autenticacao) {
        return ((ContaPrincipal) autenticacao.getPrincipal()).getNumeroConta();
    }
}
