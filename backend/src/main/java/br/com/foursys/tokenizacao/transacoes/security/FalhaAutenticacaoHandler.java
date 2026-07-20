package br.com.foursys.tokenizacao.transacoes.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class FalhaAutenticacaoHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public FalhaAutenticacaoHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        RespostaSegurancaJson.escrever(response, objectMapper,
                HttpStatus.UNAUTHORIZED.value(), "Autenticacao necessaria.");
    }
}
