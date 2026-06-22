package br.minhasfinancas.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContaResponseDTO {
    private Long id;
    private String nome;
    private BigDecimal saldoInicial;
    private LocalDate dataCriacao;
    private boolean padrao;
}
