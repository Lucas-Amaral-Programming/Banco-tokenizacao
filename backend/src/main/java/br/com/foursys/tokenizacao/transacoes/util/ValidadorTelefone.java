package br.com.foursys.tokenizacao.transacoes.util;

public final class ValidadorTelefone {

    private ValidadorTelefone() {
    }

    public static boolean ehValido(String telefone) {
        if (telefone == null) {
            return false;
        }

        String digitos = telefone.replaceAll("\\D", "");
        return digitos.length() == 11 && digitos.charAt(2) == '9';
    }
}
