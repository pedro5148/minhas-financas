package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContaRequestDTO {
    @NotBlank(message = "O nome da conta é obrigatório")
    private String nome;
    private BigDecimal saldoInicial;
    private LocalDate dataCriacao;
    private boolean padrao;
}
