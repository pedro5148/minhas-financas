package com.example.backend.dto;

import com.example.backend.enums.StatusLancamento;
import com.example.backend.enums.TipoLancamento;
import com.example.backend.enums.TipoRecorrencia;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LancamentoResponseDTO {
    private Long id;
    private TipoLancamento tipo;
    private String descricao;
    private BigDecimal valor;
    private ContaResponseDTO conta;
    private ContaResponseDTO contaDestino;
    private CategoriaResponseDTO categoria;
    private SubcategoriaResponseDTO subcategoria;
    private LocalDate dataLancamento;
    private LocalDate dataVencimento;
    private LocalDate dataEfetivacao;
    private StatusLancamento status;
    private String observacoes;
    private TipoRecorrencia tipoRecorrencia;
    private Integer parcelaAtual;
    private Integer totalParcelas;
    private CartaoCreditoResponseDTO cartaoCredito;
    private FaturaResponseDTO fatura;
}
