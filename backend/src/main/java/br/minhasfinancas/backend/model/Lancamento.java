package br.minhasfinancas.backend.model;

import br.minhasfinancas.backend.enums.StatusLancamento;
import br.minhasfinancas.backend.enums.TipoLancamento;
import br.minhasfinancas.backend.enums.TipoRecorrencia;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lancamentos")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Lancamento {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLancamento tipo;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "conta_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_lancamentos_conta_id")
    )
    private Conta conta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "conta_destino_id",
            foreignKey = @ForeignKey(name = "fk_lancamentos_conta_destino_id")
    )
    private Conta contaDestino; // Apenas para transferências

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "subcategoria_id",
            foreignKey = @ForeignKey(name = "fk_lancamentos_subcategoria_id")
    )
    private Subcategoria subcategoria;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_efetivacao")
    private LocalDate dataEfetivacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLancamento status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recorrencia", nullable = false)
    private TipoRecorrencia tipoRecorrencia = TipoRecorrencia.NENHUMA;

    @Column(name = "parcela_atual")
    private Integer parcelaAtual;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "lancamento_parcelado_id",
            foreignKey = @ForeignKey(name = "fk_lancamentos_parcelados_id")
    )
    private LancamentoParcelado lancamentoParcelado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "fatura_id",
            foreignKey = @ForeignKey(name = "fk_lancamentos_fatura_id")
    )
    private Fatura fatura;
}
