package br.com.foursys.tokenizacao.transacoes.controller;

import br.com.foursys.tokenizacao.transacoes.dto.request.CadastroContaRequest;
import br.com.foursys.tokenizacao.transacoes.dto.response.ContaResponse;
import br.com.foursys.tokenizacao.transacoes.security.ContaPrincipal;
import br.com.foursys.tokenizacao.transacoes.service.ContaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping
    public ResponseEntity<ContaResponse> cadastrar(@RequestBody CadastroContaRequest request) {
        ContaResponse response = contaService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ContaResponse> me(Authentication autenticacao) {
        ContaPrincipal principal = (ContaPrincipal) autenticacao.getPrincipal();
        return ResponseEntity.ok(contaService.buscarPorNumero(principal.getNumeroConta()));
    }
}
