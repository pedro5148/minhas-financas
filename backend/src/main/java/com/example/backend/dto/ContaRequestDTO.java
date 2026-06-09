package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContaRequestDTO {
    @NotBlank(message = "O nome da conta é obrigatório")
    private String nome;

    @NotNull(message = "O saldo inicial é obrigatório")
    private BigDecimal saldoInicial;

    private LocalDate dataCriacao;
    private boolean padrao;
}
