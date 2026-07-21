package br.com.foursys.tokenizacao.transacoes.controller;

import br.com.foursys.tokenizacao.transacoes.dto.request.ResolverChavePixRequest;
import br.com.foursys.tokenizacao.transacoes.dto.response.DestinatarioPixResponse;
import br.com.foursys.tokenizacao.transacoes.model.Conta;
import br.com.foursys.tokenizacao.transacoes.service.ChavePixService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chaves-pix")
public class ChavePixController {

    private final ChavePixService chavePixService;

    public ChavePixController(ChavePixService chavePixService) {
        this.chavePixService = chavePixService;
    }

    @PostMapping("/resolver")
    public ResponseEntity<DestinatarioPixResponse> resolver(@RequestBody ResolverChavePixRequest request) {
        Conta conta = chavePixService.resolver(request.tipoChavePix(), request.chave());
        return ResponseEntity.ok(new DestinatarioPixResponse(conta.getNomeTitular(), request.tipoChavePix()));
    }
}
