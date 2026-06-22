package br.minhasfinancas.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartaoCreditoResponseDTO {
    private Long id;
    private String nome;
    private BigDecimal limiteTotal;
    private Integer diaFechamento;
    private Integer diaVencimento;
    private ContaResponseDTO contaPadrao;
    private String bandeira;
    private Boolean principal;
    private BigDecimal valorFatura;
}
