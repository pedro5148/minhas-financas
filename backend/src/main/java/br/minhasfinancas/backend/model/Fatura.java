package br.minhasfinancas.backend.model;

import br.minhasfinancas.backend.enums.StatusFatura;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "faturas")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Fatura {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cartao_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_faturas_cartao_id")
    )
    private CartaoCredito cartao;

    @Column(nullable = false, length = 7)
    private String mesAno; // "MM/YYYY"

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFatura status = StatusFatura.ABERTA;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Column(nullable = false)
    private LocalDate dataFechamento;
}
