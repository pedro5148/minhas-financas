package com.example.backend.dto;

import com.example.backend.enums.StatusLancamento;
import com.example.backend.enums.TipoLancamento;
import com.example.backend.enums.TipoRecorrencia;
import com.example.backend.model.Lancamento;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class LancamentoDTO {
    private Long id;
    private TipoLancamento tipo;
    private String descricao;
    private BigDecimal valor;
    private ContaDTO conta;
    private ContaDTO contaDestino;
    private CategoriaDTO categoria;
    private SubcategoriaDTO subcategoria;
    private LocalDate dataLancamento;
    private LocalDate dataVencimento;
    private LocalDate dataEfetivacao;
    private StatusLancamento status;
    private String observacoes;
    private TipoRecorrencia tipoRecorrencia;
    private Integer parcelaAtual;
    private Integer totalParcelas;
    private CartaoCreditoDTO cartaoCredito;
    private FaturaDTO fatura;

    public LancamentoDTO(Lancamento lancamento) {
        if(lancamento != null) {
            this.id = lancamento.getId();
            this.tipo = lancamento.getTipo();
            this.descricao = lancamento.getDescricao();
            this.valor = lancamento.getValor();
            this.conta = new ContaDTO(lancamento.getConta());
            this.contaDestino = lancamento.getContaDestino() != null ? new ContaDTO(lancamento.getContaDestino()) : null;
            this.categoria = lancamento.getCategoria() != null ? new CategoriaDTO(lancamento.getCategoria()) : null;
            this.subcategoria = lancamento.getSubcategoria() != null ? new SubcategoriaDTO(lancamento.getSubcategoria()) : null;
            this.dataLancamento = lancamento.getDataLancamento();
            this.dataVencimento = lancamento.getDataVencimento();
            this.dataEfetivacao = lancamento.getDataEfetivacao();
            this.status = lancamento.getStatus();
            this.observacoes = lancamento.getObservacoes();
            this.tipoRecorrencia = lancamento.getTipoRecorrencia();
            this.parcelaAtual = lancamento.getParcelaAtual();
            this.totalParcelas = lancamento.getTotalParcelas();
            this.cartaoCredito = lancamento.getCartaoCredito() != null ? new CartaoCreditoDTO(lancamento.getCartaoCredito()) : null;
            // Avoiding circular dependency by mapping fatura lightly or ignoring it
            this.fatura = lancamento.getFatura() != null ? new FaturaDTO(lancamento.getFatura()) : null;
        }
    }
}
