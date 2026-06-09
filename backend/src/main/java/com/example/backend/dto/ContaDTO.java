package com.example.backend.dto;

import com.example.backend.model.Conta;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ContaDTO {
    private Long id;
    private String nome;
    private BigDecimal saldoInicial;
    private LocalDate dataCriacao;
    private boolean padrao;

    public ContaDTO(Conta conta) {
        if (conta != null) {
            this.id = conta.getId();
            this.nome = conta.getNome();
            this.saldoInicial = conta.getSaldoInicial();
            this.dataCriacao = conta.getDataCriacao();
            this.padrao = conta.isPadrao();
        }
    }
}
