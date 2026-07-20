package br.com.foursys.tokenizacao.transacoes.service;

import br.com.foursys.tokenizacao.transacoes.dto.request.TransacaoRequest;
import br.com.foursys.tokenizacao.transacoes.repository.TransacaoRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TokenTransacaoService {

    private static final String PREFIXO = "TRX-";
    private static final int TAMANHO_HASH = 32;

    private final TransacaoRepository transacaoRepository;

    public TokenTransacaoService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    public String gerarToken(TransacaoRequest request) {
        String tokenGerado;
        do {
            tokenGerado = montarToken(request);
        } while (transacaoRepository.existsByTokenTransacao(tokenGerado));
        return tokenGerado;
    }

    private String montarToken(TransacaoRequest request) {
        String base = String.join("|",
                request.numeroContaOrigem(),
                request.numeroContaDestino(),
                String.valueOf(request.valorTransacao()),
                LocalDateTime.now().toString(),
                UUID.randomUUID().toString());

        return PREFIXO + gerarHashSha256(base).substring(0, TAMANHO_HASH);
    }

    private String gerarHashSha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexadecimal = new StringBuilder();
            for (byte b : bytes) {
                hexadecimal.append(String.format("%02x", b));
            }
            return hexadecimal.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponivel no ambiente", e);
        }
    }
}
