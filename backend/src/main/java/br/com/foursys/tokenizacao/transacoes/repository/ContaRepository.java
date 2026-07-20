package br.com.foursys.tokenizacao.transacoes.repository;

import br.com.foursys.tokenizacao.transacoes.model.Conta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByNumeroConta(String numeroConta);

    Optional<Conta> findByCpf(String cpf);

    Optional<Conta> findByEmail(String email);

    boolean existsByNumeroConta(String numeroConta);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);
}
