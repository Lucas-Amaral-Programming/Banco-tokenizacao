package br.com.foursys.tokenizacao.transacoes.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class AcessoNegadoHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public AcessoNegadoHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        RespostaSegurancaJson.escrever(response, objectMapper,
                HttpStatus.FORBIDDEN.value(), "Acesso negado.");
    }
}
