package br.com.foursys.tokenizacao.transacoes.repository;

import br.com.foursys.tokenizacao.transacoes.model.ChavePix;
import br.com.foursys.tokenizacao.transacoes.model.TipoChavePix;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChavePixRepository extends JpaRepository<ChavePix, Long> {

    @EntityGraph(attributePaths = "conta")
    Optional<ChavePix> findByTipoChaveAndValorNormalizadoAndAtivaTrue(
            TipoChavePix tipoChave,
            String valorNormalizado);
}
