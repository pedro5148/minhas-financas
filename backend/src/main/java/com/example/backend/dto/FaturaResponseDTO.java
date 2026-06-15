package com.example.backend.dto;

import com.example.backend.enums.StatusFatura;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FaturaResponseDTO {
    private Long id;
    private CartaoCreditoResponseDTO cartao;
    private String mesAno;
    private BigDecimal valorTotal;
    private BigDecimal valorPago;
    private StatusFatura status;
    private LocalDate dataVencimento;
    private LocalDate dataFechamento;
}
