package br.com.foursys.tokenizacao.transacoes.controller;

import br.com.foursys.tokenizacao.transacoes.dto.request.LoginContaRequest;
import br.com.foursys.tokenizacao.transacoes.dto.response.ContaResponse;
import br.com.foursys.tokenizacao.transacoes.security.ContaPrincipal;
import br.com.foursys.tokenizacao.transacoes.service.ContaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final ContaService contaService;

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository,
                          ContaService contaService) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.contaService = contaService;
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<ContaResponse> login(@RequestBody LoginContaRequest request,
                                               HttpServletRequest servletRequest,
                                               HttpServletResponse servletResponse) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.cpf(), request.senha()));

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, servletRequest, servletResponse);

        ContaPrincipal principal = (ContaPrincipal) autenticacao.getPrincipal();
        return ResponseEntity.ok(contaService.buscarPorNumero(principal.getNumeroConta()));
    }

    @GetMapping("/me")
    public ResponseEntity<ContaResponse> me(Authentication autenticacao) {
        ContaPrincipal principal = (ContaPrincipal) autenticacao.getPrincipal();
        return ResponseEntity.ok(contaService.buscarPorNumero(principal.getNumeroConta()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
        HttpSession sessao = servletRequest.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
