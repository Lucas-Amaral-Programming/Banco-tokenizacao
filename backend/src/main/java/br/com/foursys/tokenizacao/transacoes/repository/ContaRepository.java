package br.com.foursys.tokenizacao.transacoes.repository;

import br.com.foursys.tokenizacao.transacoes.model.Conta;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByNumeroConta(String numeroConta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conta c where c.idConta in :ids order by c.idConta asc")
    List<Conta> bloquearPorIds(@Param("ids") List<Long> idsCrescente);

    Optional<Conta> findByCpf(String cpf);

    Optional<Conta> findByEmail(String email);

    boolean existsByNumeroConta(String numeroConta);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);
}
