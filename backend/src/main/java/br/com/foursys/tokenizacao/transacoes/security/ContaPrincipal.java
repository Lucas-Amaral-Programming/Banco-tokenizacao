package br.com.foursys.tokenizacao.transacoes.security;

import br.com.foursys.tokenizacao.transacoes.model.Conta;
import br.com.foursys.tokenizacao.transacoes.model.StatusConta;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class ContaPrincipal implements UserDetails {

    private final String cpf;
    private final String numeroConta;
    private final String senhaConta;
    private final StatusConta statusConta;

    public ContaPrincipal(Conta conta) {
        this.cpf = conta.getCpf();
        this.numeroConta = conta.getNumeroConta();
        this.senhaConta = conta.getSenhaConta();
        this.statusConta = conta.getStatusConta();
    }

    public String getNumeroConta() {
        return numeroConta;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    @Override
    public String getPassword() {
        return senhaConta;
    }

    @Override
    public String getUsername() {
        return cpf;
    }

    @Override
    public boolean isAccountNonLocked() {
        return statusConta != StatusConta.BLOQUEADA;
    }

    @Override
    public boolean isEnabled() {
        return statusConta == StatusConta.ATIVA;
    }
}
