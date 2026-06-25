package br.minhasfinancas.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_lancamento")
@Getter
@Setter
@NoArgsConstructor
public class ItemLancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lancamento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_lancamento"))
    private Lancamento lancamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_produto"))
    private Produto produto;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal valorUnitarioBruto;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotalBruto;
}