package br.com.foursys.tokenizacao.transacoes.security;

import br.com.foursys.tokenizacao.transacoes.model.Conta;
import br.com.foursys.tokenizacao.transacoes.repository.ContaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ContaUserDetailsService implements UserDetailsService {

    private final ContaRepository contaRepository;

    public ContaUserDetailsService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String cpf) throws UsernameNotFoundException {
        String cpfNormalizado = cpf == null ? "" : cpf.replaceAll("\\D", "");
        Conta conta = contaRepository.findByCpf(cpfNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais invalidas."));
        return new ContaPrincipal(conta);
    }
}
