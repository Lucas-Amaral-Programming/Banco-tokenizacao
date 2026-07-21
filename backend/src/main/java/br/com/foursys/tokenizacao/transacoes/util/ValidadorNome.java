package br.com.foursys.tokenizacao.transacoes.util;

import java.util.regex.Pattern;

public final class ValidadorNome {

    private static final Pattern PARTE = Pattern.compile("^\\p{L}{2,}$");

    private ValidadorNome() {
    }

    public static boolean ehCompleto(String nome) {
        if (nome == null) {
            return false;
        }
        String[] partes = nome.trim().split("\\s+");
        if (partes.length < 2) {
            return false;
        }
        for (String parte : partes) {
            if (!PARTE.matcher(parte).matches()) {
                return false;
            }
        }
        return true;
    }
}
