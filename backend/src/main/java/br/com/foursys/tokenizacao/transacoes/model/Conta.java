package br.com.foursys.tokenizacao.transacoes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conta")
    private Long idConta;

    @Column(name = "numero_conta", nullable = false, unique = true, length = 20)
    private String numeroConta;

    @Column(name = "nome_titular", nullable = false, length = 120)
    private String nomeTitular;

    @Column(name = "cpf", nullable = false, length = 14)
    private String cpf;

    @Column(name = "email", unique = true, length = 120)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false, length = 20)
    private TipoConta tipoConta;

    @Column(name = "saldo_conta", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoConta;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_conta", nullable = false, length = 20)
    private StatusConta statusConta;

    @Column(name = "senha_conta", nullable = false, length = 100)
    private String senhaConta;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;
}
