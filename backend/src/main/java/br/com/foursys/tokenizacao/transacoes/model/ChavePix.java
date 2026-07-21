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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "chave_pix",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chave_pix_tipo_valor", columnNames = {"tipo_chave", "valor_normalizado"}),
                @UniqueConstraint(name = "uk_chave_pix_conta_tipo", columnNames = {"id_conta", "tipo_chave"})
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChavePix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chave_pix")
    private Long idChavePix;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conta", nullable = false)
    private Conta conta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_chave", nullable = false, length = 20)
    private TipoChavePix tipoChave;

    @Column(name = "valor_normalizado", nullable = false, length = 120)
    private String valorNormalizado;

    @Column(name = "ativa", nullable = false)
    private boolean ativa;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;
}
