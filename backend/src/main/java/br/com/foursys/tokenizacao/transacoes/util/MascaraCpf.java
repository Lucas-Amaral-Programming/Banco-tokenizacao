package br.com.foursys.tokenizacao.transacoes.util;

public final class MascaraCpf {

    private MascaraCpf() {
    }

    public static String mascarar(String cpf) {
        String digitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            return "";
        }
        return digitos.substring(0, 3) + ".***.***-" + digitos.substring(9, 11);
    }
}
