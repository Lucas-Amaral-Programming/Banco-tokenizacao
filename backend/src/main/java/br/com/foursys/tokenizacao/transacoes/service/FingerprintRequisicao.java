package br.com.foursys.tokenizacao.transacoes.service;

import br.com.foursys.tokenizacao.transacoes.dto.request.TransacaoRequest;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class FingerprintRequisicao {

    private FingerprintRequisicao() {
    }

    public static String calcular(TransacaoRequest request) {
        String base = String.join("|",
                normalizarTipo(request),
                normalizarTexto(request.numeroContaDestino()),
                normalizarValor(request.valorTransacao()),
                normalizarTexto(request.descricaoTransacao()));
        return sha256(base);
    }

    private static String normalizarTipo(TransacaoRequest request) {
        return request.tipoTransacao() == null ? "" : request.tipoTransacao().name();
    }

    private static String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static String normalizarValor(BigDecimal valor) {
        return valor == null ? "" : valor.stripTrailingZeros().toPlainString();
    }

    private static String sha256(String valor) {
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
