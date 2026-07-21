package br.com.foursys.tokenizacao.transacoes.util;

import java.util.regex.Pattern;

public final class ValidadorEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private ValidadorEmail() {
    }

    public static boolean ehValido(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }
}
