package br.com.foursys.tokenizacao.transacoes.security;

import br.com.foursys.tokenizacao.transacoes.exception.ErroResposta;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.http.MediaType;

final class RespostaSegurancaJson {

    private RespostaSegurancaJson() {
    }

    static void escrever(HttpServletResponse response, ObjectMapper objectMapper,
                         int codigoHttp, String mensagem) throws IOException {
        response.setStatus(codigoHttp);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErroResposta erro = new ErroResposta(mensagem, null, codigoHttp, LocalDateTime.now());
        objectMapper.writeValue(response.getWriter(), erro);
    }
}
