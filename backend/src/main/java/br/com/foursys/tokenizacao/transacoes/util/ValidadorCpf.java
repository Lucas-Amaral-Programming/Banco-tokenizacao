package br.com.foursys.tokenizacao.transacoes.util;

public final class ValidadorCpf {

    private ValidadorCpf() {
    }

    public static boolean ehValido(String cpf) {
        if (cpf == null) {
            return false;
        }

        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            return false;
        }

        return verificarDigito(digitos, 9, 10) && verificarDigito(digitos, 10, 11);
    }

    private static boolean verificarDigito(String digitos, int posicao, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < posicao; i++) {
            soma += (digitos.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = 11 - (soma % 11);
        int digitoCalculado = resto >= 10 ? 0 : resto;
        return digitoCalculado == (digitos.charAt(posicao) - '0');
    }
}
