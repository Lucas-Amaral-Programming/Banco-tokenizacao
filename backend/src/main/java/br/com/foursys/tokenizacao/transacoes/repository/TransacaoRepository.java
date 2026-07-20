package br.com.foursys.tokenizacao.transacoes.repository;

import br.com.foursys.tokenizacao.transacoes.model.Transacao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    Optional<Transacao> findByTokenTransacao(String tokenTransacao);

    boolean existsByTokenTransacao(String tokenTransacao);

    Optional<Transacao> findByContaSolicitante_IdContaAndIdempotencyKey(
            Long idContaSolicitante, String idempotencyKey);

    @Query("select t from Transacao t "
            + "left join t.contaOrigem o "
            + "left join t.contaDestino d "
            + "where o.numeroConta = :numero or d.numeroConta = :numero "
            + "order by t.dataHoraTransacao desc")
    List<Transacao> buscarPorConta(@Param("numero") String numeroConta);
}
