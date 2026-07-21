package br.com.foursys.tokenizacao.transacoes.dto.request;

import br.com.foursys.tokenizacao.transacoes.model.TipoConta;

public record CadastroContaRequest(
        String nomeTitular,
        String cpf,
        String telefone,
        String email,
        TipoConta tipoConta,
        String senha
) {
}
