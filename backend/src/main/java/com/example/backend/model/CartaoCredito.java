package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cartoes_credito")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartaoCredito {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal limiteTotal;

    @Column(nullable = false)
    private Integer diaFechamento;

    @Column(nullable = false)
    private Integer diaVencimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "conta_padrao_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cartoes_credito_conta_padrao_id")
    )
    private Conta contaPadrao;

    @Column(length = 50)
    private String bandeira;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean principal = false;
}
