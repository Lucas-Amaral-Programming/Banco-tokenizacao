package br.com.foursys.tokenizacao.transacoes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transacao")
    private Long idTransacao;

    @Column(name = "token_transacao", nullable = false, unique = true, length = 80)
    private String tokenTransacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transacao", nullable = false, length = 20)
    private TipoTransacao tipoTransacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta_origem", nullable = true)
    private Conta contaOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta_destino", nullable = true)
    private Conta contaDestino;

    @Column(name = "valor_transacao", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTransacao;

    @Column(name = "descricao_transacao", length = 255)
    private String descricaoTransacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_transacao", nullable = false, length = 20)
    private StatusTransacao statusTransacao;

    @Column(name = "data_hora_transacao", nullable = false)
    private LocalDateTime dataHoraTransacao;
}
