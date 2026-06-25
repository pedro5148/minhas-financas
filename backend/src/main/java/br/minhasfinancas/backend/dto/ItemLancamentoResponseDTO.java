package br.minhasfinancas.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemLancamentoResponseDTO {
    private Long id;
    private ProdutoResponseDTO produto;
    private BigDecimal quantidade;
    private BigDecimal valorUnitarioBruto;
    private BigDecimal valorUnitarioLiquido;
    private BigDecimal valorTotalBruto;
    private BigDecimal valorTotalLiquido;
}
